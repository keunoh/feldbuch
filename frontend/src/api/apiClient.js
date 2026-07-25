import axios from 'axios';

import {logout} from "@/utils/auth.js";
import {router} from "@/router/index.js"

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * 모든 요청이 서버로 전달되기 전에 실행된다.
 * Request Interceptor
 * */
apiClient.interceptors.request.use(
  (config) => {

    const token = localStorage.getItem('accessToken');

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response Interceptor
 * */
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {

    if (error.response?.status === 401) {

      logout();

      router.push('/login');
    }

    return Promise.reject(error);
  }
)

export default apiClient;
