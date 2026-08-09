import apiClient from "@/api/common/apiClient.js";
import {logout} from "@/utils/tokenStorage.js";
import router from "@/router/index.js";

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


/**
 * 메시지 스트리밍 전송
 *
 * @param {number} conversationId
 * @param {string} message
 * @param {{
 *   onToken: function(string): void,
 *   onComplete: function(): void,
 *   onError: function(string): void
 * }} handlers
 * @param {AbortSignal} [signal]
 */
export async function streamMessage(
  conversationId,
  message,
  handlers,
  signal
) {
  const token = localStorage.getItem('accessToken');

  const response = await fetch(
    `http://localhost:8080/api/conversations/${conversationId}/chat/stream`,
    {
      method: "POST",
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        ...(token && {
          Authorization: `Bearer ${token}`
        })
      },
      body: JSON.stringify({
        message
      }),
      signal
    }
  );

  if (response.status === 401) {
    logout();
    await router.push('/login');
    throw new Error('인증이 만료되었습니다.');
  }

  if (!response.ok) {
    throw new Error(
      `스트리밍 요청에 실패했습니다. status=${response.status}`
    );
  }

  if (!response.body) {
    throw new Error('스트리밍 응답 본문이 존재하지 않습니다.');
  }

  await readSseStream(response.body, handlers);
}

async function readSseStream(stream, handlers) {
  const reader = stream.getReader();
  const decoder = new TextDecoder('utf-8');

  let buffer = '';

  try {
    while (true) {
      const {done, value} = await reader.read();

      if (done) {
        break;
      }

      buffer += decoder.decode(value, {
        stream: true
      });

      const events = buffer.split('\n\n');

      buffer = events.pop() ?? '';

      for (const event of events) {
        handleSseEvent(event, handlers);
      }
    }

    buffer += decoder.decode();

    if (buffer.trim()) {
      handleSseEvent(buffer, handlers);
    }
  } finally {
    reader.releaseLock();
  }
}

function handleSseEvent(rawEvent, handlers) {
  const data = rawEvent
    .split('\n')
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice(5).trimStart())
    .join('\n');

  if (!data) {
    return;
  }

  const event = JSON.parse(data);

  switch (event.type) {
    case 'TOKEN':
      if (event.content) {
        handlers.onToken(event.content);
      }
      break;

    case 'COMPLETE':
      handlers.onComplete();
      break;

    case 'ERROR':
      handlers.onError(
        event.content ?? 'AI 응답 생성 중 오류가 발생했습니다.'
      );
      break;
  }
}
