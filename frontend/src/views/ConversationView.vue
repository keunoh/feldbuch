<script setup>
import ChatInput from '@/components/ChatInput.vue'

function sendMessage(message) {
  console.log(message)
}
</script>

<template>

  <ChatInput @send="sendMessage"/>

</template>

<style scoped>

</style>
