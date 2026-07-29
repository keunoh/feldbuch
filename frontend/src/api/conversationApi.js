import apiClient from "@/api/apiClient.js";

/**
 * 특정 대화 상세 조회
 * */
export async function getConversation(conversationId) {
  const response = await apiClient.get(
    `/conversations/${conversationId}`
  );

  return response.data;
}

/**
 * 특정 대화 삭제
 * */
export async function deleteConversation(conversationId) {
  const response = await apiClient.delete(
    `/conversations/${conversationId}`
  );

  return response.data;
}

/**
 * 대화 목록 조회
 * */
export async function getConversations() {
  const response = await apiClient.get('/conversations');

  return response.data;
}

/**
 * 새 대화 생성
 * */
export async function createConversation(title = '새 대화') {
  const response = await apiClient.post(
    `/conversations`, {
      title
    }
  );

  return response.data;
}

/**
 * 특정 대화 메시지 조회
 * */
export async function getMessages(conversationId) {
  const response = await apiClient.get(
    `/conversations/${conversationId}/messages`
  );

  return response.data;
}

/**
 * 메시지 전송
 * */
export async function sendMessage(conversationId, message) {
  const response = await apiClient.post(
    `/conversations/${conversationId}/chat`, {
      message
    });

  return response.data;
}

/**
 * 특정 대화 제목 수정
 * */
export async function updateConversationTitle(conversationId, title) {
  const response = await apiClient.patch(
    `/conversations/${conversationId}`,
    {
      title
    }
  );

  return response.data;
}
