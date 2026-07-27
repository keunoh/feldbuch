<script setup>

defineProps({
  messages: {
    type: Array,
    default: () => [],
  }
})
</script>

<template>
  <div class="message-list">
    <p
      v-if="messages.length === 0"
      class="empty-message"
    >
      아직 메시지가 없습니다.
    </p>

    <div
      v-for="message in messages"
      :key="message.id"
      class="message-row"
      :class="{
        user: message.role === 'USER',
        assistant: message.role === 'ASSISTANT'
      }"
    >
      <div class="bubble">
        <div class="role">
          {{ message.role === 'USER' ? '👤 나' : '🤖 Feldbuch' }}
        </div>

        <div class="content">
          {{ message.content }}
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>

.message-list {

  display: flex;

  flex-direction: column;

  gap: 18px;

  padding: 20px;

  overflow-y: auto;

}

.message-row {

  display: flex;

}

.user {

  justify-content: flex-end;

}

.assistant {

  justify-content: flex-start;

}

.bubble {

  max-width: 70%;

  padding: 14px 18px;

  border-radius: 14px;

}

.user .bubble {

  background: #dbeafe;

}

.assistant .bubble {

  background: #f3f4f6;

}

.role {

  font-size: 13px;

  font-weight: bold;

  margin-bottom: 8px;

}

.content {

  white-space: pre-wrap;

  line-height: 1.6;

}

.empty-message {

  text-align: center;

  color: #888;

}

</style>
