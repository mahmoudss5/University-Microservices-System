import http from 'k6/http';
import { check, sleep } from 'k6';

// Configure the load test parameters
export const options = {
    // Simulate 50 concurrent virtual users
    vus: 500,
    // Run the test for exactly 30 seconds
    duration: '60s',
};

export default function () {
    // Send a GET request to the target API endpoint
    const res = http.get('http://localhost:8080/api/courses/popular');

    // Verify the response from the server
    check(res, {
        // Did the server respond with success?
        'is status 200': (r) => r.status === 200,
        // Did the server respond in less than 500 milliseconds?
        'transaction time OK': (r) => r.timings.duration < 500,
    });

    // Pause for 1 second to simulate realistic user behavior before the next request
    sleep(1);
}