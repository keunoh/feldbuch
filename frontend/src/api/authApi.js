import apiClient from "@/api/apiClient.js";

export async function login(request) {
  const response = await apiClient.post('/auth/login', request);
  return response.data;
}

export async function signup(request) {
  const response = await apiClient.post('/users/signup', request);
  return response.data;
}

export async function getMe() {
  const response = await apiClient.get('/auth/me');
  return response.data;
}
