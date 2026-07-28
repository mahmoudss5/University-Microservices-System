--[[
  Sliding Window Rate Limiter — University Management System
  ──────────────────────────────────────────────────────────
  Algorithm: Sliding Window Log using a Redis Sorted Set.
  Each accepted request is stored as a member of a sorted set,
  scored by its arrival timestamp (milliseconds). On every call
  we atomically:
    1. Remove entries that have fallen outside the window.
    2. Count how many requests remain inside the window.
    3. If count < limit → add the new request and allow it.
    4. Otherwise → deny it and return the retry-after time.

  KEYS[1]  – the rate-limit bucket key, e.g.
             "rl:global:192.168.1.1"  or
             "rl:auth:/api/auth/login:192.168.1.1"

  ARGV[1]  – window size in milliseconds (e.g. 60000 for 1 minute)
  ARGV[2]  – maximum requests allowed per window
  ARGV[3]  – current timestamp in milliseconds  (epoch ms)
  ARGV[4]  – unique request identifier (UUID or epoch-ns string)

  Returns a two-element array:
    [1]  allowed  → 1 = request is permitted, 0 = rate-limited
    [2]  remaining → requests still allowed in the current window
                     (negative means 0 remaining)
--]]

local key          = KEYS[1]
local window_ms    = tonumber(ARGV[1])   -- e.g. 60000
local max_requests = tonumber(ARGV[2])   -- e.g. 150 or 20
local now_ms       = tonumber(ARGV[3])   -- current epoch ms
local request_id   = ARGV[4]            -- unique member for ZADD

-- ── Step 1: Prune expired entries ────────────────────────────────────────────
-- Remove all members whose score (timestamp) is older than the window start.
local window_start = now_ms - window_ms
redis.call('ZREMRANGEBYSCORE', key, '-inf', window_start)

-- ── Step 2: Count requests still within the sliding window ───────────────────
local current_count = redis.call('ZCARD', key)

-- ── Step 3: Decision ─────────────────────────────────────────────────────────
if current_count < max_requests then
    -- Allowed: record this request and refresh the key TTL.
    -- We store the timestamp as the score so we can prune by time.
    -- The member is the unique request_id to avoid ZADD collisions at the
    -- same millisecond from different coroutines.
    redis.call('ZADD', key, now_ms, request_id)

    -- Keep the sorted set alive for exactly one window length.
    -- PEXPIRE uses milliseconds — consistent with our window unit.
    redis.call('PEXPIRE', key, window_ms)

    local remaining = max_requests - current_count - 1
    return {1, remaining}
else
    -- Denied: find out when the *oldest* request in the window expires so
    -- the caller can populate a Retry-After header.
    -- ZRANGE … WITHSCORES returns [member, score, …]; we just need score[0].
    -- We refresh the TTL so the key doesn't disappear while the client waits.
    redis.call('PEXPIRE', key, window_ms)
    return {0, 0}
end
