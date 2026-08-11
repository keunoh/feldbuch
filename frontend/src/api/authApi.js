import axios from 'axios';

import apiClient from '@/api/common/apiClient.js';

const authClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export async function login(request) {
  const response =
    await apiClient.post(
      '/auth/login',
      request,
    );

  return response.data;
}

export async function signup(request) {
  const response =
    await apiClient.post(
      '/users/signup',
      request,
    );

  return response.data;
}

export async function getMe() {
  const response =
    await apiClient.get(
      '/auth/me',
    );

  return response.data;
}
