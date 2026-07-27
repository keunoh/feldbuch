<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from "vue-router";

import {getConversations} from "@/api/conversationApi.js";
import {logout} from "@/utils/auth.js";

import ChatInput from "@/components/ChatInput.vue";
import MessageList from "@/components/MessageList.vue";
import ConversationSidebar from "@/components/ConversationSidebar.vue";

const router = useRouter();

const conversations = ref([]);
const messages = ref([])

function logoutUser() {
  logout();
  router.push('/login');
}

function sendMessage(content) {
  messages.value.push({
    id: Date.now(),
    content
  })
}

onMounted(async () => {
  try {
    const response = await getConversations();

    conversations.value = response.data;
  } catch (error) {
    console.error(error);
  }
});

</script>

<template>
  <div class="container">

    <ConversationSidebar
      :conversations="conversations"
    />

    <main class="content">
      <h1>Feldbuch Chat</h1>

      <MessageList :messages="messages"/>

      <ChatInput @send="sendMessage"/>

      <button @click="logoutUser">
        로그아웃
      </button>
    </main>

  </div>
</template>

<style scoped>
.container {
  display: flex;
  height: 100vh;
}

.content {
  flex: 1;
  padding: 20px;
}

</style>
