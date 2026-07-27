<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from "vue-router";

import {getConversation, getConversations} from "@/api/conversationApi.js";
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

async function selectConversation(conversationId) {
  selectedConversationId.value = conversationId;

  try {
    const response = await getConversation(conversationId);

    conversation.value = response.data;
    messages.value = response.data.messages;
  } catch (error) {
    console.log(error);

    conversation.value = null;
    messages.value = [];
  }
}

function logoutUser() {
  logout();
  router.push('/login');
}

function sendMessage(content) {
  messages.value.push({
    id: Date.now(),
    role: 'USER',
    content,
    createdAt: new Date().toISOString(),
  })
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

      <MessageList
        :messages="messages"
      />

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
  flex: 1;
  padding: 24px;
}
</style>
