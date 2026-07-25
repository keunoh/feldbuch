import apiClient from "@/api/apiClient.js";

export async function login(request) {
  const response = await apiClient.post('/auth/login', request);
  return response.data;
}
