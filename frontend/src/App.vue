<script setup>
import {ref} from 'vue'

import ChatInput from "@/components/ChatInput.vue";
import MessageList from "@/components/MessageList.vue";

const messages = ref([])

function sendMessage(content) {
  messages.value.push({
    id: Date.now(),
    content
  })
}

</script>

<template>
  <div>
    <h1>Feldbuch Chat</h1>

    <MessageList :messages="messages"/>

    <ChatInput @send="sendMessage"/>

  </div>
</template>

<style scoped></style>
