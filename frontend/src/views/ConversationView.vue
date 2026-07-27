<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from "vue-router";

import {
  getConversation,
  getConversations,
  sendMessage as sendChatMessage
} from "@/api/conversationApi.js";

import {logout} from "@/utils/auth.js";

import ChatInput from "@/components/ChatInput.vue";
import MessageList from "@/components/MessageList.vue";
import ConversationSidebar from "@/components/ConversationSidebar.vue";
import StudyInfoPanel from "@/components/StudyInfoPanel.vue";

const router = useRouter();

const conversation = ref(null);
const conversations = ref([]);
const selectedConversationId = ref(null);
const messages = ref([]);

const selectedConversation = computed(() => {
  return conversations.value.find(
    conversation =>
      conversation.id === selectedConversationId.value
  );
})

async function loadConversation(conversationId) {
  const response = await getConversation(conversationId);

  conversation.value = response.data;
  messages.value = response.data.messages;

  updateConversationTitle(
    response.data.id,
    response.data.title
  );
}

async function selectConversation(conversationId) {
  selectedConversationId.value = conversationId;

  try {
    await loadConversation(conversationId);

  } catch (error) {
    console.log(error);

    conversation.value = null;
    messages.value = [];
  }
}

function updateConversationTitle(conversationId, title) {
  const targetConversation = conversations.value.find(
    conversation => conversation.id === conversationId
  );

  if (targetConversation) {
    targetConversation.title = title;
  }
}

// ChatInput의 send 이벤트 처리
async function sendMessage(content) {
  if (!selectedConversationId.value) {
    return;
  }

  try {
    // 백엔드 API 호출
    await sendChatMessage(
      selectedConversationId.value,
      content
    );

    await loadConversation(
      selectedConversationId.value
    );
  } catch (error) {
    console.log(error);
  }
}

function logoutUser() {
  logout();
  router.push('/login');
}

onMounted(async () => {
  try {
    const response = await getConversations();

    conversations.value = response.data;

    if (conversations.value.length > 0) {
      const firstConversationId = conversations.value[0].id;

      await selectConversation(firstConversationId)
    }
  } catch (error) {
    console.error(error);
  }
});

</script>

<template>
  <div class="conversation-layout">

    <ConversationSidebar
      :conversations="conversations"
      :selected-conversation-id="selectedConversationId"
      @select="selectConversation"
    />

    <main class="chat-area">
      <header class="chat-header">
        <h1 class="conversation-title">
          {{
            conversation?.title
            ?? selectedConversation?.title
            ?? 'Feldbuch Chat'
          }}
        </h1>

        <button @click="logoutUser">
          로그아웃
        </button>
      </header>

      <div class="messages">
        <MessageList
          :messages="messages"
        />
      </div>

      <ChatInput
        @send="sendMessage"
      />
    </main>

    <StudyInfoPanel
      :conversation="conversation"
    />

  </div>
</template>

<style scoped>
.conversation-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.chat-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 24px;
  box-sizing: border-box;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.conversation-title {
  margin: 0;
  font-size: 24px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.logout-button {
  flex-shrink: 0;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: white;
  cursor: pointer;
}
</style>
