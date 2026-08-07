<script setup>
import {computed, nextTick, onMounted, reactive, ref} from 'vue'
import {useRouter} from "vue-router";

import {
  createConversation,
  deleteConversation,
  getConversation,
  getConversations,
  streamMessage as streamChatMessage,
  updateConversationTitle
} from "@/api/conversationApi.js";

import {logout} from "@/utils/auth.js";

import ChatInput from '@/components/chat/ChatInput.vue'
import MessageList from '@/components/chat/MessageList.vue'
import StudyInfoPanel from '@/components/chat/StudyInfoPanel.vue'
import WorkspaceSidebar from '@/components/sidebar/WorkspaceSidebar.vue'
import KnowledgeWorkspace from "@/components/knowledge/KnowledgeWorkspace.vue";
import {STORAGE_KEYS} from "@/constants/storageKeys.js";
import {getMe} from "@/api/authApi.js";
import UserProfilePanel from "@/components/sidebar/UserProfilePanel.vue";

const router = useRouter();

const conversation = ref(null);
const conversations = ref([]);
const selectedConversationId = ref(null);
const messages = ref([]);
const messageContainer = ref(null);

const creatingConversation = ref(false);
const deletingConversationId = ref(null);
const updatingConversationId = ref(null);
const sendingMessage = ref(false);

const sidebarMode = ref(loadSidebarMode())
const storedKnowledgeId = localStorage.getItem(STORAGE_KEYS.SELECTED_KNOWLEDGE_ID);
const storedKnowledgeNoteId = localStorage.getItem(STORAGE_KEYS.SELECTED_KNOWLEDGE_NOTE_ID);

const currentUser = ref(null)
const userLoading = ref(false)

async function loadCurrentUser() {
  userLoading.value = true

  try {
    const response =
      await getMe()

    currentUser.value =
      response.data
  } catch (error) {
    console.error(
      '현재 사용자 조회 실패',
      error,
    )

    logoutUser()
  } finally {
    userLoading.value = false
  }
}

const selectedKnowledgeId = ref(
  storedKnowledgeId && Number.isInteger(Number(storedKnowledgeId))
    ? Number(storedKnowledgeId)
    : null,
);

const selectedKnowledgeNoteId = ref(
  storedKnowledgeNoteId && Number.isInteger(Number(storedKnowledgeNoteId))
    ? Number(storedKnowledgeNoteId)
    : null,
);

const selectedKnowledgePath = ref(
  loadStoredKnowledgePath()
)

const selectedConversation = computed(() => {
  return conversations.value.find(
    conversation =>
      conversation.id === selectedConversationId.value
  );
})

async function refreshConversationTitle(
  conversationId,
  options = {},
) {
  const {
    maxAttempts = 5,
    interval = 700,
  } = options

  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    const response = await getConversation(conversationId);

    const updatedConversation = response.data

    updateConversationTitleInState(
      updatedConversation.id,
      updatedConversation.title,
    )

    if (updatedConversation.title !== '새 대화') {
      return
    }

    await new Promise(response =>
      setTimeout(response, interval)
    )
  }
}

function loadStoredKnowledgePath() {
  const storedPath =
    localStorage.getItem(STORAGE_KEYS.SELECTED_KNOWLEDGE_PATH)

  if (!storedPath) {
    return []
  }

  try {
    const parsedPath = JSON.parse(storedPath)

    return Array.isArray(parsedPath)
      ? parsedPath
      : []
  } catch (error) {
    localStorage.removeItem(STORAGE_KEYS.SELECTED_KNOWLEDGE_PATH)

    return []
  }
}

function selectKnowledgeNote(noteId) {
  selectedKnowledgeNoteId.value = noteId;

  if (noteId === null) {
    localStorage.removeItem(STORAGE_KEYS.SELECTED_KNOWLEDGE_NOTE_ID);
  }

  localStorage.setItem(STORAGE_KEYS.SELECTED_KNOWLEDGE_NOTE_ID, String(noteId));
}

function findInitialConversationId() {
  const storedId = localStorage.getItem(STORAGE_KEYS.SELECTED_CONVERSATION_ID);

  if (!storedId) {
    return conversations.value[0]?.id ?? null;
  }

  const parsedId = Number(storedId);

  if (!Number.isInteger(parsedId)) {
    localStorage.removeItem(STORAGE_KEYS.SELECTED_CONVERSATION_ID);

    return conversations.value[0]?.id ?? null
  }

  const exists =
    conversations.value.some(
      item => item.id === parsedId
    )

  if (exists) {
    return parsedId
  }

  localStorage.removeItem(STORAGE_KEYS.SELECTED_CONVERSATION_ID);

  return conversations.value[0]?.id ?? null
}


function loadSidebarMode() {

  const mode = localStorage.getItem(STORAGE_KEYS.SIDEBAR_MODE);

  if (mode === 'conversation' || mode === 'knowledge') {
    return mode
  }

  return 'conversation'
}

function changeSidebarMode(mode) {
  sidebarMode.value = mode

  localStorage.setItem(STORAGE_KEYS.SIDEBAR_MODE, mode)
}

function selectKnowledge({id, path}) {
  selectedKnowledgeId.value = id;
  selectedKnowledgePath.value = path;

  localStorage.setItem(STORAGE_KEYS.SELECTED_KNOWLEDGE_ID, String(id))
  localStorage.setItem(STORAGE_KEYS.SELECTED_KNOWLEDGE_PATH, JSON.stringify(path))

  selectKnowledgeNote(null);
}

async function scrollToBottom() {
  await nextTick();

  const container = messageContainer.value;

  if (!container) {
    return;
  }

  container.scrollTop = container.scrollHeight;
}

async function loadConversations() {
  const response = await getConversations();

  conversations.value = response.data;
}

async function loadConversation(conversationId) {
  const response = await getConversation(conversationId);

  conversation.value = response.data;
  messages.value = response.data.messages;


  updateConversationTitleInState(
    response.data.id,
    response.data.title
  );
}

async function selectConversation(conversationId) {
  try {
    await loadConversation(conversationId);

    selectedConversationId.value = conversationId;

    localStorage.setItem(STORAGE_KEYS.SELECTED_CONVERSATION_ID, String(conversationId));

    await scrollToBottom();
  } catch (error) {
    console.log(error);

    conversation.value = null;
    messages.value = [];
    selectedConversationId.value = null;

    localStorage.removeItem(STORAGE_KEYS.SELECTED_CONVERSATION_ID);
  }
}

async function createNewConversation() {
  if (creatingConversation.value) {
    return;
  }

  creatingConversation.value = true;

  try {
    const response = await createConversation('새 대화');

    const createdConversationId = response.data;

    await loadConversations();

    // 생성한 대화를 자동으로 선택한다.
    await selectConversation(createdConversationId);
  } catch (error) {
    console.error(error);
  } finally {
    creatingConversation.value = false;
  }
}

async function removeConversation(conversationId) {
  if (deletingConversationId.value !== null) {
    return;
  }

  const targetConversation = conversations.value.find(
    conversation => conversation.id === conversationId
  );

  const confirmed = window.confirm(
    `"${targetConversation?.title ?? '이 대화'}"를 삭제하시겠습니까?`
  );

  if (!confirmed) {
    return;
  }

  deletingConversationId.value = conversationId;

  try {
    await deleteConversation(conversationId);

    conversations.value = conversations.value.filter(
      conversation => conversation.id !== conversationId
    );

    const deletedSelectedConversation
      = selectedConversationId.value === conversationId;

    if (!deletedSelectedConversation) {
      return;
    }

    conversation.value = null;
    messages.value = [];
    selectedConversationId.value = null;

    localStorage.removeItem(STORAGE_KEYS.SELECTED_CONVERSATION_ID);

    const nextConversation = conversations.value[0];

    if (nextConversation) {
      await selectConversation(nextConversation.id);
    }
  } catch (error) {
    console.error(error);
  } finally {
    deletingConversationId.value = null;
  }
}

function updateConversationTitleInState(conversationId, title) {
  const targetConversation = conversations.value.find(
    conversation => conversation.id === conversationId
  );

  if (targetConversation) {
    targetConversation.title = title;
  }

  if (conversation.value?.id === conversationId) {
    conversation.value.title = title;
  }
}

async function renameConversation({conversationId, title}) {
  if (updatingConversationId.value !== null) {
    return;
  }

  updatingConversationId.value = conversationId;

  try {
    // API 호출
    await updateConversationTitle(conversationId, title);

    // 화면 상태 변경
    updateConversationTitleInState(conversationId, title);
  } catch (error) {
    console.error(error);
  } finally {
    updatingConversationId.value = null;
  }
}

function moveConversationToTop(conversationId) {

  const index = conversations.value.findIndex(
    conversation => conversation.id === conversationId
  );

  if (index <= 0) {
    return;
  }

  const conversation =
    conversations.value.splice(index, 1)[0];

  conversations.value.unshift(conversation);
}

// ChatInput의 send 이벤트 처리
async function sendMessage(content) {
  if (!selectedConversationId.value) {
    return;
  }

  if (sendingMessage.value) {
    return;
  }

  const conversationId = selectedConversationId.value;

  const optimisticUserMessage = {
    id: `temp-user-${Date.now()}`,
    role: 'USER',
    content
  };

  const streamingAssistantMessage = reactive({
    id: `temp-assistant-${Date.now()}`,
    role: 'ASSISTANT',
    content: ''
  });

  messages.value.push(
    optimisticUserMessage,
    streamingAssistantMessage
  );

  sendingMessage.value = true;

  await scrollToBottom();

  try {
    // 백엔드 API 호출
    await streamChatMessage(
      conversationId,
      content,
      {
        onToken(token) {
          streamingAssistantMessage.content += token;
          scrollToBottom();
        },

        onComplete() {
          console.log('[CHAT_STREAM] Complete');
        },

        onError(message) {
          throw new Error(message);
        },
      }
    );

    await loadConversation(conversationId);

    moveConversationToTop(conversationId);

    await scrollToBottom();

    refreshConversationTitle(
      conversationId,
    ).catch(error => {
      console.error(
        '대화 제목 갱신 실패:',
        error,
      )
    })
  } catch (error) {
    console.log(error);

    messages.value = messages.value.filter(
      message =>
        message.id !== optimisticUserMessage.id
        && message.id !== streamingAssistantMessage.id
    );
  } finally {
    sendingMessage.value = false;

    await scrollToBottom();
  }
}

function logoutUser() {
  logout();
  router.push('/login');
}

onMounted(async () => {
  try {
    await loadConversations();

    const initialConversationId = findInitialConversationId()

    if (initialConversationId !== null) {
      await selectConversation(initialConversationId);
    }

    await loadCurrentUser()
  } catch (error) {
    console.error(error);
  }
});

</script>

<template>
  <div class="conversation-layout">
    <WorkspaceSidebar
      :conversations="conversations"
      :selected-conversation-id="selectedConversationId"
      :selected-knowledge-id="selectedKnowledgeId"
      :creating="creatingConversation"
      :deleting-conversation-id="deletingConversationId"
      :updating-conversation-id="updatingConversationId"
      :mode="sidebarMode"
      @select-conversation="selectConversation"
      @create-conversation="createNewConversation"
      @delete-conversation="removeConversation"
      @update-conversation-title="renameConversation"
      @select-knowledge="selectKnowledge"
      @change-mode="changeSidebarMode"
    >
      <template #footer>
        <UserProfilePanel
          v-if="currentUser"
          :user="currentUser"
          @logout="logoutUser"
        />
      </template>
    </WorkspaceSidebar>

    <template v-if="sidebarMode === 'conversation'">
      <main
        class="chat-area"
      >
        <header class="chat-header">
          <h1 class="conversation-title">
            {{
              conversation?.title
              ?? selectedConversation?.title
              ?? 'Feldbuch Chat'
            }}
          </h1>
        </header>

        <div
          ref="messageContainer"
          class="messages"
        >
          <MessageList
            :messages="messages"
          />
        </div>

        <ChatInput
          :loading="sendingMessage"
          @send="sendMessage"
        />
      </main>

      <StudyInfoPanel
        :conversation="conversation"
      />
    </template>

    <template v-else>
      <KnowledgeWorkspace
        :knowledge-id="selectedKnowledgeId"
        :knowledge-path="selectedKnowledgePath"
        :selected-note-id="selectedKnowledgeNoteId"
        @select-note="selectKnowledgeNote"
      />
    </template>
  </div>
</template>


<style scoped>
.conversation-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  color: var(--color-text);
  background: var(--color-bg);
}

.chat-area {
  position: relative;
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 24px;
  box-sizing: border-box;
  background: radial-gradient(
    circle at 50% 0%,
    rgba(66, 245, 123, 0.035),
    transparent 34%
  ),
  var(--color-bg);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border-soft);
}

.conversation-title {
  margin: 0;
  overflow: hidden;
  color: var(--color-text);
  font-size: 24px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-title::before {
  margin-right: 10px;
  color: var(--color-primary);
  content: ">_";
  text-shadow: 0 0 14px var(--color-primary-glow);
}

.logout-button {
  flex-shrink: 0;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-small);
  color: var(--color-text-soft);
  background: var(--color-surface);
  cursor: pointer;
  transition: color var(--transition-fast),
  border-color var(--transition-fast),
  background var(--transition-fast);
}

.logout-button:hover {
  color: var(--color-primary);
  border-color: var(--color-border-primary);
  background: var(--color-primary-soft);
}

.messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}
</style>
