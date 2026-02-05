import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 30,
    duration: '30s',
};

export default function () {
    const res = http.get('http://localhost:8080/api/notices/all');

    check(res, {
        'is status 200': (r) => r.status === 200,
    });
}