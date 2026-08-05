# Production Readiness To-Do

## 1. Security Hardening
- Remove hardcoded secrets from docker-compose and service configuration files.
- Use environment variables or a secret manager such as Vault, Azure Key Vault, or Docker secrets.
- Enable HTTPS/TLS for all public endpoints.
- Protect actuator endpoints and expose only health/readiness publicly.
- Add stricter authentication and authorization checks.
- Enforce input validation and CORS allowlists.
- Add dependency scanning and container image vulnerability scanning.

## 2. Environment Separation
- Create separate configuration files for development, staging, and production.
- Use different credentials and endpoints for database, Redis, Kafka, and JWT.
- Keep production secrets out of version control.

## 3. Reliability and Resilience
- Add health checks and readiness/liveness probes for each service.
- Configure retries, timeouts, and circuit breakers for inter-service communication.
- Make Kafka producers and consumers idempotent.
- Add dead-letter queues for failed events.
- Add graceful shutdown behavior and container resource limits.
- Set up database backups and MySQL restore procedures.

## 4. Observability
- Add structured JSON logging.
- Centralize logs using ELK, Loki, or a similar tool.
- Add metrics and dashboards with Prometheus and Grafana.
- Add distributed tracing with OpenTelemetry.
- Configure alerts for downtime, latency, and high error rates.

## 5. Testing and Quality Gates
- Add unit tests, integration tests, and contract tests.
- Add frontend tests for critical user flows.
- Add CI checks for build, lint, tests, and security scanning.
- Block deployments when tests or security checks fail.

## 6. CI/CD
- Add GitHub Actions, GitLab CI, or Azure DevOps pipelines.
- Build Docker images automatically in CI.
- Deploy to staging first, then production.
- Add rollback strategy and deployment approvals.

## 7. Deployment Readiness
- Deploy using Kubernetes, Azure Container Apps, or another production-grade orchestrator.
- Add autoscaling and managed services where possible.
- Put the frontend behind a reverse proxy and optimize static asset delivery.

## 8. Frontend Production Polish
- Add proper error boundaries and loading/error states.
- Handle expired tokens and failed API requests gracefully.
- Use environment-based API URLs.
- Optimize bundle size and enable caching.

## Highest Priority Next Steps
1. Remove hardcoded secrets from docker-compose and environment files.
2. Add health/readiness endpoints and secure them.
3. Add CI pipeline with build, tests, and lint.
4. Add centralized logging and monitoring.
5. Add production deployment configuration.
