<script setup>
import {onBeforeUnmount, onMounted} from "vue";

import "highlight.js/styles/github-dark.css"

import {renderMarkdown, renderPlainText} from "@/utils/markdownRenderer.js";

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
      <div
        v-if="message.role === 'ASSISTANT'"
        class="assistant-avatar"
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
              {{ message.role === 'USER' ? '나' : 'Feldbuch' }}
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

        <div class="bubble loading-bubble">
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
  </div>
</template>

<style scoped>
.message-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
  padding: 16px 20px 28px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
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
  border: 1px solid rgba(66, 245, 123, 0.34);
  border-radius: 13px;
  color: var(--color-primary);
  background: linear-gradient(
    135deg,
    rgba(66, 245, 123, 0.11),
    rgba(66, 245, 123, 0.025)
  ),
  var(--color-surface);
  box-shadow: inset 0 1px rgba(255, 255, 255, 0.035),
  0 0 20px rgba(66, 245, 123, 0.07);
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
  box-shadow: 7px 0 0 #facc15,
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
  background: rgba(66, 245, 123, 0.16);
  content: "";
}

.terminal-symbol {
  margin-top: 10px;
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 17px;
  font-weight: 800;
  letter-spacing: -0.08em;
  text-shadow: 0 0 8px var(--color-primary),
  0 0 16px var(--color-primary-glow);
}

.assistant-avatar:hover .terminal-symbol {
  animation: terminalBlink 0.9s steps(1) infinite;
}

/* 작성자 */
.role {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  min-height: 22px;
  margin-bottom: 8px;
  color: var(--color-primary);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
}

.role-identity {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.message-time {
  flex-shrink: 0;
  color: var(--color-text-disabled);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.assistant .message-time {
  color: var(--color-text-muted);
}

.user .message-time {
  color: rgba(196, 181, 253, 0.7);
}

.role-name {
  font-size: 13px;
  font-weight: 750;
  letter-spacing: 0.015em;
}

.assistant .role-name {
  color: var(--color-primary);
  text-shadow: 0 0 14px rgba(66, 245, 123, 0.15);
}

.user .role {
  color: var(--color-accent-purple);
}

.user-symbol {
  padding: 2px 6px;
  border: 1px solid rgba(139, 92, 246, 0.24);
  border-radius: 5px;
  color: var(--color-accent-purple);
  background: rgba(139, 92, 246, 0.07);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.processing-label {
  padding: 2px 6px;
  border: 1px solid rgba(66, 245, 123, 0.2);
  border-radius: 5px;
  color: var(--color-primary);
  background: rgba(66, 245, 123, 0.055);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.08em;
  animation: processingPulse 1.6s ease-in-out infinite;
}

/* 공통 말풍선 */
.bubble {
  position: relative;
  width: 100%;
  padding: 16px 18px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-medium);
  color: var(--color-text-soft);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
  box-sizing: border-box;
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

.content :deep(blockquote) {
  margin: 14px 0;
  padding: 10px 14px;
  border-left: 3px solid var(--color-accent-cyan);
  border-radius: 0 var(--radius-small) var(--radius-small) 0;
  color: var(--color-text-soft);
  background: rgba(94, 234, 212, 0.06);
}

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

/* 코드 블록 전체 */
.content :deep(.code-block) {
  margin: 16px 0;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-medium);
  background: var(--color-bg-deep);
  box-shadow: inset 0 1px rgba(255, 255, 255, 0.025),
  0 8px 28px rgba(0, 0, 0, 0.16);
  overflow: hidden;
}

/* 코드 블록 헤더 */
.content :deep(.code-block-header) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 38px;
  padding: 0 12px 0 14px;
  border-bottom: 1px solid var(--color-border);
  background: linear-gradient(
    90deg,
    rgba(66, 245, 123, 0.055),
    transparent 55%
  ),
  var(--color-surface-raised);
}

/* 언어 표시 */
.content :deep(.code-language) {
  color: var(--color-primary);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-shadow: 0 0 10px rgba(66, 245, 123, 0.18);
}

/* COPY 버튼 자리 */
.content :deep(.code-copy-button) {
  min-width: 58px;
  padding: 5px 8px;
  border: 1px solid var(--color-border-soft);
  border-radius: 6px;
  color: var(--color-text-muted);
  background: rgba(255, 255, 255, 0.025);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
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
  border-color: rgba(66, 245, 123, 0.38);
  background: rgba(66, 245, 123, 0.08);
  box-shadow: 0 0 14px rgba(66, 245, 123, 0.08);
}

.content :deep(.code-copy-button.copied) {
  color: var(--color-primary);
  border-color: rgba(66, 245, 123, 0.48);
  background: rgba(66, 245, 123, 0.12);
  box-shadow: inset 0 0 10px rgba(66, 245, 123, 0.04),
  0 0 16px rgba(66, 245, 123, 0.1);
  text-shadow: 0 0 8px var(--color-primary-glow);
}

.content :deep(.code-copy-button:active) {
  transform: translateY(0);
}

/* 실제 코드 영역 */
.content :deep(.code-block pre) {
  margin: 0;
  padding: 18px;
  border: 0;
  border-radius: 0;
  background: linear-gradient(
    180deg,
    rgba(66, 245, 123, 0.02),
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

  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;

  font-size: 13px;
  line-height: 1.65;
  white-space: pre;
}

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
.loading-avatar {
  animation: avatarPulse 1.8s ease-in-out infinite;
}

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
    border-color: rgba(66, 245, 123, 0.25);
    box-shadow: inset 0 1px rgba(255, 255, 255, 0.035),
    0 0 12px rgba(66, 245, 123, 0.04);
  }

  50% {
    border-color: rgba(66, 245, 123, 0.55);
    box-shadow: inset 0 1px rgba(255, 255, 255, 0.035),
    0 0 24px rgba(66, 245, 123, 0.13);
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
    padding-inline: 10px;
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

.content :deep(.hljs) {
  background: transparent !important;
  padding: 0;
}

.content :deep(.code-copy-button.copy-failed) {
  color: var(--color-danger);
  border-color: rgba(248, 113, 113, 0.48);
  background: rgba(248, 113, 113, 0.1);
  box-shadow: 0 0 14px rgba(248, 113, 113, 0.08);
}
</style>
