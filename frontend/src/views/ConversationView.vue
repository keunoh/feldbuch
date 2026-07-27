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
      <h1>
        {{ conversation?.title ?? selectedConversation?.title ?? 'Feldbuch Chat' }}
      </h1>

      <div class="messages">
        <MessageList
          :messages="messages"
        />
      </div>

      <ChatInput
        @send="sendMessage"
      />

      <button @click="logoutUser">
        로그아웃
      </button>
    </main>

  </div>
</template>

<style scoped>
.conversation-layout {
  display: flex;
  min-height: 100vh;
}

.chat-area {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 24px;
}

.messages {
  flex: 1;
  overflow-y: auto;
}
</style>
