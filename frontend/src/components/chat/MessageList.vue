<script setup>
import {onBeforeUnmount, onMounted} from "vue";

import "highlight.js/styles/github-dark.css"

import {renderMarkdown, renderPlainText} from "@/utils/markdownRenderer.js";

defineProps({
  messages: {
    type: Array,
    default: () => [],
  },
});

let copyResetTimer = null;

function handleCodeCopy(event) {

  const target = event.target;

  if (!(target instanceof Element)) {
    return;
  }

  const button = event.target.closest(".code-copy-button");

  if (!button) {
    return;
  }

  const encoded = button.dataset.code;

  if (!encoded) {
    return;
  }

  const code = decodeURIComponent(encoded);

  try {
    navigator.clipboard.writeText(code);

    button.textContent = "✓ COPIED";
    button.classList.add("copied");

    if (copyResetTimer) {
      window.clearTimeout(copyResetTimer);
    }

    copyResetTimer = window.setTimeout(() => {
      button.textContent = "COPY";
      button.classList.remove("copied");

      copyResetTimer = null;
    }, 2000);
  } catch (error) {
    console.error("코드 복사에 실패했습니다.", error);

    button.textContent = "FAILED";
    button.classList.remove("copy-failed");

    if (copyResetTimer) {
      window.clearTimeout(copyResetTimer);
    }

    copyResetTimer = window.setTimeout(() => {
      button.textContent = "COPY";
      button.classList.remove("copy-failed");

      copyResetTimer = null;
    }, 2000);
  }
}

function renderMessage(message) {
  if (message.role === "USER") {
    return renderPlainText(message.content);
  }

  return renderMarkdown(message.content);
}

function isWaitingForFirstToken(message) {
  return (
    message.role === 'ASSISTANT'
    && !message.content
  );
}

function formatMessageTime(createdAt) {
  if (!createdAt) {
    return '';
  }

  const date = new Date(createdAt);

  return new Intl.DateTimeFormat('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(date);
}

onMounted(() => {
  document.addEventListener(
    "click",
    handleCodeCopy
  );
});

onBeforeUnmount(() => {
  document.removeEventListener(
    "click",
    handleCodeCopy
  );

  if (copyResetTimer) {
    window.clearTimeout(copyResetTimer);
  }
});

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
      <div
        v-if="message.role === 'ASSISTANT'"
        class="assistant-avatar"
        :class="{
          'loading-avatar': isWaitingForFirstToken(message)
        }"
        aria-hidden="true"
      >
        <span class="terminal-symbol">
          &gt;_
        </span>
      </div>

      <div class="message-column">
        <div class="role">
          <div class="role-identity">
            <span
              v-if="message.role === 'USER'"
              class="user-symbol"
              aria-hidden="true"
            >
              USER
            </span>

            <span class="role-name">
              {{ message.role === 'USER' ? '' : 'Feldbuch' }}
            </span>

            <span
              v-if="isWaitingForFirstToken(message)"
              class="processing-label"
            >
              PROCESSING
            </span>
          </div>

          <time
            v-if="message.createdAt"
            class="message-time"
            :datetime="message.createdAt"
          >
            {{ formatMessageTime(message.createdAt) }}
          </time>
        </div>

        <div class="bubble">
          <div
            v-if="isWaitingForFirstToken(message)"
            class="loading-content"
          >
            <span>답변을 준비하고 있습니다</span>
            <span
              class="dots"
              aria-hidden="true"
            >
              <span>.</span>
              <span>.</span>
              <span>.</span>
            </span>
          </div>
          <div
            v-else
            class="content"
            v-html="renderMessage(message)"
          >
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="loading"
      class="message-row assistant"
    >
      <div
        class="assistant-avatar loading-avatar"
        aria-hidden="true"
      >
        <span class="terminal-symbol">
          &gt;_
        </span>
      </div>

      <div class="message-column">
        <div class="role">
          <div class="role-identity">
            <span class="role-name">
              Feldbuch
            </span>

            <span class="processing-label">
              PROCESSING
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.message-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
  padding: var(--space-7) var(--space-9) 28px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: var(--space-6);
  animation: messageFadeIn 0.22s ease-out;
}

.user {
  justify-content: flex-end;
}

.assistant {
  justify-content: flex-start;
}

.message-column {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.assistant .message-column {
  width: min(76%, 820px);
}

.user .message-column {
  align-items: flex-end;
  width: min(58%, 540px);
}

/* Feldbuch 터미널 아바타 */
.assistant-avatar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  margin-top: 2px;
  border: 1px solid var(--color-primary-a340);
  border-radius: var(--radius-13);
  color: var(--color-primary);
  background: linear-gradient(
    135deg,
    var(--color-primary-a110),
    var(--color-primary-a025)
  ),
  var(--color-surface);
  box-shadow: var(--shadow-inset-top-medium),
  0 0 20px var(--color-primary-a070);
  overflow: hidden;
}

.assistant-avatar::before {
  position: absolute;
  top: 7px;
  left: 9px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--color-danger);
  box-shadow: var(--shadow-warning-accent),
  14px 0 0 var(--color-primary);
  content: "";
  opacity: 0.7;
}

.assistant-avatar::after {
  position: absolute;
  top: 16px;
  right: 7px;
  left: 7px;
  height: 1px;
  background: var(--color-primary-a160);
  content: "";
}

.terminal-symbol {
  margin-top: var(--space-4);
  font-family: var(--font-family-terminal);
  font-size: 17px;
  font-weight: 800;
  letter-spacing: -0.08em;
  text-shadow: var(--text-shadow-primary),
  var(--text-shadow-primary-16);
}

.assistant-avatar:hover .terminal-symbol {
  animation: terminalBlink 0.9s steps(1) infinite;
}

/* 작성자 */
.role {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-7);
  width: 100%;
  min-height: 22px;
  margin-bottom: var(--space-3);
  color: var(--color-primary);
  font-family: var(--font-family-terminal);
}

.role-identity {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.message-time {
  flex-shrink: 0;
  color: var(--color-text-disabled);
  font-family: var(--font-family-terminal);
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.assistant .message-time {
  color: var(--color-text-muted);
}

.user .message-time {
  color: var(--color-purple-text);
}

.role-name {
  font-size: 13px;
  font-weight: 750;
  letter-spacing: 0.015em;
}

.assistant .role-name {
  color: var(--color-primary);
  text-shadow: var(--text-shadow-primary-soft-14);
}

.user .role {
  color: var(--color-accent-purple);
}

.user-symbol {
  padding: 2px 6px;
  border: 1px solid var(--color-purple-border);
  border-radius: var(--radius-5);
  color: var(--color-accent-purple);
  background: var(--color-purple-soft);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.processing-label {
  padding: 2px 6px;
  border: 1px solid var(--color-primary-a200);
  border-radius: var(--radius-5);
  color: var(--color-primary);
  background: var(--color-primary-a055);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.08em;
  animation: processingPulse 1.6s ease-in-out infinite;
}

/* 공통 말풍선 */
.bubble {
  position: relative;
  width: 100%;
  padding: var(--space-7) var(--space-8);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-medium);
  color: var(--color-text-soft);
  background: var(--color-surface);
  box-shadow: var(--shadow-sm);
  box-sizing: border-box;
  transition: transform var(--transition-fast),
  border-color var(--transition-fast),
  box-shadow var(--transition-fast);
}

.bubble:hover {
  transform: translateY(-1px);
  border-color: var(--color-primary-a300);
  box-shadow: var(--shadow-sm),
  0 0 24px var(--color-primary-a050);
}

/* Feldbuch 메시지 */
.assistant .bubble {
  border-left: 3px solid var(--color-primary);
  border-top-left-radius: 4px;
  background: linear-gradient(
    135deg,
    var(--color-primary-a035),
    transparent 42%
  ),
  var(--color-surface);
}

/* 사용자 메시지 */
.user .bubble {
  border-color: var(--color-border-primary);
  border-bottom-right-radius: 4px;
  background: linear-gradient(
    135deg,
    var(--color-purple-medium),
    var(--color-primary-a070)
  ),
  var(--color-surface-raised);
  box-shadow: var(--shadow-sm),
  0 0 20px var(--color-purple-soft);
}

/* 메시지 본문 */
.content {
  color: var(--color-text-soft);
  line-height: 1.72;
  overflow-wrap: anywhere;
}

.content :deep(p) {
  margin: 0 0 var(--space-5);
}

.content :deep(p:last-child) {
  margin-bottom: 0;
}

.content :deep(h1),
.content :deep(h2),
.content :deep(h3),
.content :deep(h4) {
  margin: 22px 0 var(--space-4);
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
  margin: var(--space-5) 0;
  padding-left: var(--space-10);
}

.content :deep(li) {
  margin-bottom: 7px;
}

.content :deep(li::marker) {
  color: var(--color-primary);
}

.content :deep(blockquote) {
  margin: var(--space-6) 0;
  padding: var(--space-4) 14px;
  border-left: 3px solid var(--color-accent-cyan);
  border-radius: 0 var(--radius-small) var(--radius-small) 0;
  color: var(--color-text-soft);
  background: var(--color-cyan-soft);
}

.content :deep(code) {
  padding: 2px 6px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-5);
  color: var(--color-primary);
  background: var(--color-bg-deep);
  font-family: var(--font-family-terminal);
  font-size: 0.9em;
}

/* 코드 블록 전체 */
.content :deep(.code-block) {
  margin: var(--space-7) 0;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-medium);
  background: var(--color-bg-deep);
  box-shadow: var(--shadow-inset-top),
  0 8px 28px rgba(0, 0, 0, 0.16);
  overflow: hidden;
}

/* 코드 블록 헤더 */
.content :deep(.code-block-header) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 38px;
  padding: 0 var(--space-5) 0 14px;
  border-bottom: 1px solid var(--color-border);
  background: linear-gradient(
    90deg,
    var(--color-primary-a055),
    transparent 55%
  ),
  var(--color-surface-raised);
}

/* 언어 표시 */
.content :deep(.code-language) {
  color: var(--color-primary);
  font-family: var(--font-family-terminal);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-shadow: var(--text-shadow-primary-soft-10);
}

/* COPY 버튼 */
.content :deep(.code-copy-button) {
  min-width: 58px;
  padding: 5px var(--space-3);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-6);
  color: var(--color-text-muted);
  background: var(--color-white-a025);
  font-family: var(--font-family-terminal);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.06em;
  cursor: pointer;
  transition: color var(--transition-fast),
  border-color var(--transition-fast),
  background var(--transition-fast),
  box-shadow var(--transition-fast),
  transform var(--transition-fast);
}

.content :deep(.code-copy-button:hover) {
  transform: translateY(-1px);
  color: var(--color-primary);
  border-color: var(--color-primary-a380);
  background: var(--color-primary-a080);
  box-shadow: var(--shadow-glow-14);
}

.content :deep(.code-copy-button.copied) {
  color: var(--color-primary);
  border-color: var(--color-primary-a480);
  background: var(--color-primary-a120);
  box-shadow: var(--shadow-inset-glow-soft),
  0 0 16px var(--color-primary-a100);
  text-shadow: var(--text-shadow-primary-soft);
}

.content :deep(.code-copy-button:active) {
  transform: translateY(0);
}

/* 실제 코드 영역 */
.content :deep(.code-block pre) {
  margin: 0;
  padding: var(--space-8);
  border: 0;
  border-radius: 0;
  background: linear-gradient(
    180deg,
    var(--color-primary-a020),
    transparent 35%
  ),
  var(--color-bg-deep);
  box-shadow: none;
  overflow-x: auto;
}

/* 코드 블록 내부 code */
.content :deep(.code-block pre code) {
  display: block;
  padding: 0;
  border: 0;
  border-radius: 0;
  color: inherit;
  background: transparent;
  font-family: var(--font-family-terminal);
  font-size: 13px;
  line-height: 1.65;
  white-space: pre;
}

.content :deep(table) {
  width: 100%;
  margin: var(--space-7) 0;
  border-collapse: collapse;
  overflow: hidden;
}

.content :deep(th),
.content :deep(td) {
  padding: var(--space-4) var(--space-5);
  border: 1px solid var(--color-border);
  text-align: left;
}

.content :deep(th) {
  color: var(--color-primary);
  background: var(--color-surface-raised);
}

.content :deep(td) {
  background: var(--color-surface-translucent);
}

.content :deep(a) {
  color: var(--color-accent-cyan);
  text-decoration-color: var(--color-cyan-decoration);
  text-underline-offset: 3px;
}

.content :deep(hr) {
  margin: 22px 0;
  border: 0;
  border-top: 1px solid var(--color-border);
}

.content :deep(.hljs) {
  padding: 0;
  background: transparent !important;
}

.content :deep(.code-copy-button.copy-failed) {
  color: var(--color-danger);
  border-color: var(--color-danger-strong-border);
  background: var(--color-danger-strong-soft);
  box-shadow: var(--shadow-glow-error);
}

/* 빈 대화 */
.empty-message {
  margin: auto;
  padding: 36px;
  color: var(--color-text-muted);
  font-family: var(--font-family-terminal);
  text-align: center;
}

.empty-message::before {
  display: block;
  margin-bottom: var(--space-4);
  color: var(--color-primary);
  content: "> waiting_for_input";
}

/* 첫 토큰 대기 화면 */
.loading-avatar {
  animation: avatarPulse 1.8s ease-in-out infinite;
}

.loading-content {
  display: flex;
  align-items: center;
  gap: 6px;
  min-height: 28px;
  color: var(--color-text-muted);
  font-family: var(--font-family-terminal);
  font-size: 13px;
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
    text-shadow: var(--text-shadow-primary-soft);
  }
}

@keyframes terminalBlink {
  0%,
  48% {
    opacity: 1;
  }

  49%,
  100% {
    opacity: 0.35;
  }
}

@keyframes avatarPulse {
  0%,
  100% {
    border-color: var(--color-primary-a250);
    box-shadow: var(--shadow-inset-top-medium),
    0 0 12px var(--color-primary-a040);
  }

  50% {
    border-color: var(--color-primary-a550);
    box-shadow: var(--shadow-inset-top-medium),
    0 0 24px var(--color-primary-a130);
  }
}

@keyframes processingPulse {
  0%,
  100% {
    opacity: 0.45;
  }

  50% {
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

@media (max-width: 900px) {
  .message-list {
    padding-inline: var(--space-4);
  }

  .assistant-avatar {
    width: 44px;
    height: 44px;
  }

  .assistant .message-column {
    width: calc(88% - 58px);
  }

  .user .message-column {
    width: 76%;
  }
}
</style>
