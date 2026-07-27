<script setup>

const props = defineProps({
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

    <article
      v-for="message in messages"
      :key="message.id"
      class="message"
    >
      <strong class="message-role">
        {{ message.role }}
      </strong>

      <p class="message-content">
        {{ message.content }}
      </p>
    </article>

  </div>
</template>

<style scoped>
.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 24px 0;
}

.message {
  padding: 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.message-role {
  display: block;
  margin-bottom: 8px;
}

.message-content {
  margin: 0;
  white-space: pre-wrap;
}

.empty-message {
  color: #777;
}
</style>
