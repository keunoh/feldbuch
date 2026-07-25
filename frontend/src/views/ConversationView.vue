<script setup>
import {onMounted, ref} from 'vue'

import ChatInput from "@/components/ChatInput.vue";
import MessageList from "@/components/MessageList.vue";

import {getConversations} from "@/api/conversationApi.js";

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

  </div>
</template>

<style scoped></style>
