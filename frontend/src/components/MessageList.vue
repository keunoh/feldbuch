<script setup>
import {marked} from "marked";
import DOMPurify from "dompurify";

defineProps({
  messages: {
    type: Array,
    default: () => [],
  },

  loading: {
    type: Boolean,
    default: false
  },
});

function renderMessage(message) {
  if (message.role === 'USER') {
    return DOMPurify.sanitize(message.content);
  }

  const html = marked.parse(message.content);

  return DOMPurify.sanitize(html);
}
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

        <div
          class="content"
          v-html="renderMessage(message)"
        >
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
  animation: messageFadeIn 0.22s ease-out;
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
  border-radius: 16px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
  transition: transform 0.16s ease,
  box-shadow 0.16s ease;
}

.bubble:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.1);
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
  line-height: 1.6;
}

.content :deep(p) {
  margin: 0 0 12px;
}

.content :deep(p:last-child) {
  margin-bottom: 0;
}

.content :deep(h1),
.content :deep(h2),
.content :deep(h3),
.content :deep(h4) {
  margin: 20px 0 10px;
  line-height: 1.35;
}

.content :deep(h1:first-child),
.content :deep(h2:first-child),
.content :deep(h3:first-child),
.content :deep(h4:first-child) {
  margin-top: 0;
}

.content :deep(ul),
.content :deep(ol) {
  margin: 10px 0;
  padding-left: 24px;
}

.content :deep(li) {
  margin-bottom: 6px;
}

.content :deep(blockquote) {
  margin: 12px 0;
  padding: 8px 14px;
  border-left: 4px solid #9ca3af;
  background: #e5e7eb;
}

.content :deep(code) {
  padding: 2px 5px;
  border-radius: 4px;
  background: #e5e7eb;
  font-family: monospace;
}

.content :deep(pre) {
  margin: 14px 0;
  padding: 16px;
  border-radius: 10px;
  background: #1f2937;
  overflow-x: auto;
}

.content :deep(pre code) {
  padding: 0;
  background: transparent;
  color: #f9fafb;
}

.content :deep(table) {
  width: 100%;
  margin: 14px 0;
  border-collapse: collapse;
}

.content :deep(th),
.content :deep(td) {
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  text-align: left;
}

.content :deep(th) {
  background: #e5e7eb;
}

.content :deep(a) {
  color: #2563eb;
  text-decoration: underline;
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

@keyframes messageFadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
