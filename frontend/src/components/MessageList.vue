<script setup>

defineProps({
  messages: {
    type: Array,
    default: () => [],
  },

  loading: {
    type: Boolean,
    default: false
  },
})
</script>

<template>
  <div class="message-list">
    <p
      v-if="messages.length === 0 && !loading"
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

    <div
      v-if="loading"
      class="message-row assistant"
    >
      <div class="bubble loading-bubble">
        <div class="role">
          🤖 Feldbuch
        </div>

        <div class="loading-content">
          <span>답변을 작성하고 있습니다</span>

          <span class="dots">
            <span>.</span>
            <span>.</span>
            <span>.</span>
          </span>
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
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: bold;
}

.content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.empty-message {
  text-align: center;
  color: #888;
}

.loading-bubble {
  color: #6b7280;
}

.loading-content {
  display: flex;
  align-items: center;
  gap: 2px;
  line-height: 1.6;
}

.dots span {
  display: inline-block;
  animation: blink 1.4s infinite;
}

.dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes blink {
  0%,
  80%,
  100% {
    opacity: 0.25;
  }

  40% {
    opacity: 1;
  }
}
</style>
