<script setup>
import {ref} from 'vue'
import ChatInput from '@/components/ChatInput.vue'
import MessageList from '@/components/MessageList.vue'

const messages = ref([]);

function sendMessage(content) {

  messages.value.push({
    role: "user",
    content: content
  })
}
</script>

<template>

  <MessageList :messages="messages"/>

  <ChatInput @send="sendMessage"/>

</template>

<style scoped>

</style>
