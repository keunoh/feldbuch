import axios from 'axios';

import {getAccessToken, getRefreshToken, logout, saveAccessToken,} from '@/utils/tokenStorage.js';

import {router} from '@/router/index.js';

const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Refresh 요청 전용 클라이언트.
 * apiClient interceptor를 타지 않는다.
 */
const refreshClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * 모든 요청이 서버로 전달되기 전에 실행된다.
 * Request Interceptor
 */
apiClient.interceptors.request.use(
  config => {
    const token =
      getAccessToken();

    if (token) {
      config.headers.Authorization =
        `Bearer ${token}`;
    }

    return config;
  },

  error => {
    return Promise.reject(
      error,
    );
  },
);

/**
 * Access Token 재발급
 */
async function refreshAccessToken() {
  const refreshToken =
    getRefreshToken();

  if (!refreshToken) {
    throw new Error(
      'Refresh Token이 없습니다.',
    );
  }

  const response =
    await refreshClient.post(
      '/auth/refresh',
      {
        refreshToken,
      },
    );

  const newAccessToken =
    response.data.data.accessToken;

  saveAccessToken(
    newAccessToken,
  );

  return newAccessToken;
}

/**
 * Response Interceptor
 */
apiClient.interceptors.response.use(
  response => {
    return response;
  },

  async error => {
    const originalRequest =
      error.config;

    if (
      error.response?.status === 401
      && !originalRequest._retry
    ) {
      originalRequest._retry = true;

      try {
        const newAccessToken =
          await refreshAccessToken();

        originalRequest.headers.Authorization =
          `Bearer ${newAccessToken}`;

        return apiClient(
          originalRequest,
        );
      } catch (refreshError) {
        logout();

        await router.push(
          '/login',
        );

        return Promise.reject(
          refreshError,
        );
      }
    }

    return Promise.reject(
      error,
    );
  },
);

export default apiClient;
