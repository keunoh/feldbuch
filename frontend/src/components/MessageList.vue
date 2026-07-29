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
  gap: 28px;
  padding: 16px 20px 28px;
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

/* 공통 말풍선 */
.bubble {
  position: relative;
  max-width: min(76%, 820px);
  padding: 16px 18px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-medium);
  color: var(--color-text-soft);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
  transition: transform var(--transition-fast),
  border-color var(--transition-fast),
  box-shadow var(--transition-fast);
}

.bubble:hover {
  transform: translateY(-1px);
  border-color: rgba(66, 245, 123, 0.3);
  box-shadow: var(--shadow-card),
  0 0 24px rgba(66, 245, 123, 0.05);
}

/* Feldbuch 메시지 */
.assistant .bubble {
  border-left: 3px solid var(--color-primary);
  border-top-left-radius: 4px;
  background: linear-gradient(
    135deg,
    rgba(66, 245, 123, 0.035),
    transparent 42%
  ),
  var(--color-surface);
}

/* 사용자 메시지 */
.user .bubble {
  max-width: min(58%, 540px);
  border-color: var(--color-border-primary);
  border-bottom-right-radius: 4px;
  background: linear-gradient(
    135deg,
    rgba(139, 92, 246, 0.16),
    rgba(66, 245, 123, 0.07)
  ),
  var(--color-surface-raised);
  box-shadow: var(--shadow-card),
  0 0 20px rgba(139, 92, 246, 0.07);
}

/* 작성자 */
.role {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-bottom: 10px;
  color: var(--color-primary);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.01em;
}

.assistant .role::before {
  color: var(--color-primary);
  content: ">_";
  text-shadow: 0 0 12px var(--color-primary-glow);
}

.user .role {
  color: var(--color-accent-purple);
}

/* 메시지 본문 */
.content {
  color: var(--color-text-soft);
  line-height: 1.72;
  overflow-wrap: anywhere;
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
  margin: 22px 0 10px;
  color: var(--color-text);
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
  margin: 12px 0;
  padding-left: 24px;
}

.content :deep(li) {
  margin-bottom: 7px;
}

.content :deep(li::marker) {
  color: var(--color-primary);
}

/* 인용문 */
.content :deep(blockquote) {
  margin: 14px 0;
  padding: 10px 14px;
  border-left: 3px solid var(--color-accent-cyan);
  border-radius: 0 var(--radius-small) var(--radius-small) 0;
  color: var(--color-text-soft);
  background: rgba(94, 234, 212, 0.06);
}

/* 인라인 코드 */
.content :deep(code) {
  padding: 2px 6px;
  border: 1px solid var(--color-border-soft);
  border-radius: 5px;
  color: var(--color-primary);
  background: var(--color-bg-deep);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 0.9em;
}

/* 코드 블록 */
.content :deep(pre) {
  margin: 16px 0;
  padding: 18px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-medium);
  background: linear-gradient(
    180deg,
    rgba(66, 245, 123, 0.025),
    transparent 35%
  ),
  var(--color-bg-deep);
  box-shadow: inset 0 1px rgba(255, 255, 255, 0.025);
  overflow-x: auto;
}

.content :deep(pre code) {
  padding: 0;
  border: 0;
  color: var(--color-text-soft);
  background: transparent;
  line-height: 1.65;
}

/* 표 */
.content :deep(table) {
  width: 100%;
  margin: 16px 0;
  border-collapse: collapse;
  overflow: hidden;
}

.content :deep(th),
.content :deep(td) {
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  text-align: left;
}

.content :deep(th) {
  color: var(--color-primary);
  background: var(--color-surface-raised);
}

.content :deep(td) {
  background: rgba(13, 19, 26, 0.65);
}

.content :deep(a) {
  color: var(--color-accent-cyan);
  text-decoration-color: rgba(94, 234, 212, 0.45);
  text-underline-offset: 3px;
}

.content :deep(hr) {
  margin: 22px 0;
  border: 0;
  border-top: 1px solid var(--color-border);
}

/* 빈 대화 */
.empty-message {
  margin: auto;
  padding: 36px;
  color: var(--color-text-muted);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  text-align: center;
}

.empty-message::before {
  display: block;
  margin-bottom: 10px;
  color: var(--color-primary);
  content: "> waiting_for_input";
}

/* 로딩 메시지 */
.loading-bubble {
  color: var(--color-text-muted);
}

.loading-content {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-muted);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  line-height: 1.6;
}

.loading-content::before {
  color: var(--color-primary);
  content: "$";
}

.dots {
  display: inline-flex;
  gap: 2px;
}

.dots span {
  display: inline-block;
  color: var(--color-primary);
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
    opacity: 0.2;
  }

  40% {
    opacity: 1;
    text-shadow: 0 0 8px var(--color-primary-glow);
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

@media (max-width: 900px) {
  .message-list {
    padding-inline: 10px;
  }

  .bubble {
    max-width: 88%;
  }

  .user .bubble {
    max-width: 76%;
  }
}
</style>
