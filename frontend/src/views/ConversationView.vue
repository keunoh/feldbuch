<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from "vue-router";

import {getConversations} from "@/api/conversationApi.js";
import {logout} from "@/utils/auth.js";

import ChatInput from "@/components/ChatInput.vue";
import MessageList from "@/components/MessageList.vue";


const router = useRouter();

function logoutUser() {
  logout();
  router.push('/login');
}

const messages = ref([])

function sendMessage(content) {
  messages.value.push({
    id: Date.now(),
    content
  })
}

onMounted(async () => {
  try {
    const conversations = await getConversations();

    console.log(conversations);
  } catch (error) {
    console.error(error);
  }
});

</script>

<template>
  <div>
    <h1>Feldbuch Chat</h1>

    <MessageList :messages="messages"/>

    <ChatInput @send="sendMessage"/>

    <button @click="logoutUser">
      로그아웃
    </button>

  </div>
</template>

<style scoped></style>
