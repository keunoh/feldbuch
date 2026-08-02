<script setup>
import {computed} from 'vue'

const props = defineProps({
  conversation: {
    type: Object,
    default: null
  }
});

function formatDate(value) {
  if (!value) {
    return '-';
  }

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(value));
}

function formatDateTime(value) {
  if (!value) {
    return '-';
  }

  return new Intl.DateTimeFormat('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(new Date(value));
}

const formattedCreatedAt = computed(() => {
  return formatDate(props.conversation?.createdAt);
});

const formattedUpdatedAt = computed(() => {
  return formatDateTime(props.conversation?.updatedAt);
});

const statusLabel = computed(() => {
  const status = props.conversation?.status

  const labels = {
    ACTIVE: 'ACTIVE',
    COMPLETED: 'COMPLETED',
    ARCHIVED: 'ARCHIVED'
  }

  return labels[status] ?? status ?? '-'
});

const statusClass = computed(() => {
  const status = props.conversation?.status;

  return {
    active: status === 'ACTIVE',
    completed: status === 'COMPLETED',
    archived: status === 'ARCHIVED',
  };
});

</script>

<template>
  <aside class="study-info-panel">
    <div class="panel-header">
      <span
        class="terminal-icon"
        aria-hidden="true">
        &gt;_
      </span>

      <div>
        <p class="panel-eyebrow">
          SESSION
        </p>

        <h2 class="panel-title">
          학습 정보
        </h2>
      </div>

      <span
        class="header-spark"
        aria-hidden="true"
      >
        +
      </span>
    </div>

    <div
      v-if="conversation"
      class="info-list"
    >
      <section class="info-item topic-item">
        <span class="info-label">
          TOPIC
        </span>

        <strong class="info-value title-value">
          {{ conversation.title }}
        </strong>
      </section>

      <section class="info-item">
        <span class="info-label">
          STATUS
        </span>

        <span
          class="status-badge"
          :class="statusClass"
        >
          <span
            class="status-dot"
            aria-hidden="true"
          >
          </span>

          {{ statusLabel }}
        </span>
      </section>

      <section class="info-item">
        <span class="info-label">
          MESSAGES
        </span>

        <span class="metric-value">
          {{ conversation.messageCount ?? 0 }}
        </span>
      </section>

      <section class="info-item">
        <span class="info-label">
          CREATED
        </span>

        <span class="info-value mono-value">
          {{ formattedCreatedAt }}
        </span>
      </section>

      <section class="info-item">
        <span class="info-label">
          UPDATED
        </span>

        <span class="info-value mono-value">
          {{ formattedUpdatedAt }}
        </span>
      </section>

      <div
        class="signal-decoration"
        aria-hidden="true"
      >
        <span></span>
        <span></span>
        <span></span>
        <span></span>
        <span></span>
        <span></span>
        <span></span>
      </div>
    </div>

    <div
      v-else
      class="empty-state"
    >
      <span
        class="empty-prompt"
        aria-hidden="true"
      >
        &gt;_
      </span>

      <p>
        no_session_selected
      </p>

      <small>
        대화를 선택하면<br>
        학습 정보가 표시됩니다.
      </small>
    </div>
  </aside>
</template>

<style scoped>
.study-info-panel {
  position: relative;
  width: var(--study-panel-width);
  min-width: var(--study-panel-width);
  height: 100vh;
  padding: 24px 22px;
  border-left: 1px solid var(--color-border);
  color: var(--color-text);
  background: radial-gradient(
    circle at 70% 12%,
    rgba(66, 245, 123, 0.05),
    transparent 25%
  ),
  linear-gradient(
    180deg,
    rgba(13, 19, 26, 0.98),
    rgba(5, 8, 12, 0.98)
  );
  box-shadow: inset 1px 0 rgba(255, 255, 255, 0.015);
  overflow: hidden;
}

.study-info-panel::before {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(
    rgba(66, 245, 123, 0.018) 1px,
    transparent 1px
  ),
  linear-gradient(
    90deg,
    rgba(66, 245, 123, 0.018) 1px,
    transparent 1px
  );
  background-size: 24px 24px;
  mask-image: linear-gradient(
    to bottom,
    transparent,
    black 20%,
    black 80%,
    transparent
  );
  content: "";
  pointer-events: none;
}

.panel-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border-soft);
}

.terminal-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border: 1px solid var(--color-border-primary);
  border-radius: 10px;
  color: var(--color-primary);
  background: var(--color-primary-soft);
  box-shadow: inset 0 1px rgba(255, 255, 255, 0.03),
  0 0 18px rgba(66, 245, 123, 0.06);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 14px;
  font-weight: 800;
  text-shadow: 0 0 10px var(--color-primary-glow);
}

.panel-eyebrow {
  margin: 0 0 3px;
  color: var(--color-text-muted);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.15em;
}

.panel-title {
  margin: 0;
  color: var(--color-primary);
  font-size: 17px;
  font-weight: 750;
  letter-spacing: -0.01em;
}

.header-spark {
  margin-left: auto;
  color: var(--color-primary);
  font-size: 12px;
  text-shadow: 0 0 8px var(--color-primary),
  0 0 18px var(--color-primary-glow);
  animation: sparkPulse 2.4s ease-in-out infinite;
}

.info-list {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 9px;
  padding: 18px 4px;
  border-bottom: 1px solid var(--color-border-soft);
}

.info-item:first-child {
  padding-top: 0;
}

.info-label {
  color: var(--color-text-muted);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.info-value {
  color: var(--color-text-soft);
  font-size: 14px;
}

.title-value {
  color: var(--color-primary);
  font-size: 15px;
  line-height: 1.55;
  overflow-wrap: anywhere;
  text-shadow: 0 0 16px rgba(66, 245, 123, 0.08);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  padding: 7px 11px;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  color: var(--color-text-muted);
  background: var(--color-surface);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 12px;
  font-weight: 700;
}

.status-badge.active {
  color: var(--color-primary);
  border-color: rgba(66, 245, 123, 0.25);
  background: rgba(66, 245, 123, 0.08);
  box-shadow: 0 0 18px rgba(66, 245, 123, 0.06);
}

.status-badge.completed {
  color: var(--color-accent-cyan);
  border-color: rgba(94, 234, 212, 0.25);
  background: rgba(94, 234, 212, 0.08);
}

.status-badge.archived {
  color: var(--color-text-muted);
  border-color: var(--color-border);
  background: rgba(127, 139, 153, 0.08);
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 9px currentColor;
  animation: statusPulse 1.9s ease-in-out infinite;
}

.metric-value {
  color: var(--color-text);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 23px;
  font-weight: 700;
}

.metric-value small {
  margin-left: 5px;
  color: var(--color-text-muted);
  font-size: 10px;
  font-weight: 500;
}

.mono-value {
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 13px;
  letter-spacing: 0.02em;
}

.signal-decoration {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 5px;
  height: 70px;
  margin-top: 34px;
  opacity: 0.35;
}

.signal-decoration span {
  width: 5px;
  border-radius: 999px 999px 2px 2px;
  background: linear-gradient(
    to top,
    var(--color-primary),
    rgba(66, 245, 123, 0.15)
  );
  box-shadow: 0 0 8px rgba(66, 245, 123, 0.18);
  transform-origin: bottom;
  animation: signalWave 1.8s ease-in-out infinite;
}

.signal-decoration span:nth-child(1),
.signal-decoration span:nth-child(7) {
  height: 16px;
}

.signal-decoration span:nth-child(2),
.signal-decoration span:nth-child(6) {
  height: 30px;
  animation-delay: 0.1s;
}

.signal-decoration span:nth-child(3),
.signal-decoration span:nth-child(5) {
  height: 48px;
  animation-delay: 0.2s;
}

.signal-decoration span:nth-child(4) {
  height: 64px;
  animation-delay: 0.3s;
}

.empty-state {
  position: relative;
  z-index: 1;
  margin-top: 56px;
  padding: 28px 16px;
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-medium);
  color: var(--color-text-muted);
  background: rgba(255, 255, 255, 0.015);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  text-align: center;
}

.empty-prompt {
  display: block;
  margin-bottom: 10px;
  color: var(--color-primary);
  font-size: 18px;
  text-shadow: 0 0 12px var(--color-primary-glow);
}

.empty-state p {
  margin: 0 0 12px;
  color: var(--color-text-soft);
  font-size: 12px;
}

.empty-state small {
  color: var(--color-text-disabled);
  font-size: 11px;
  line-height: 1.7;
}

@keyframes statusPulse {
  0%,
  100% {
    opacity: 0.55;
    transform: scale(0.88);
  }

  50% {
    opacity: 1;
    transform: scale(1.08);
  }
}

@keyframes sparkPulse {
  0%,
  100% {
    opacity: 0.35;
    transform: scale(0.85) rotate(0deg);
  }

  50% {
    opacity: 1;
    transform: scale(1.1) rotate(45deg);
  }
}

@keyframes signalWave {
  0%,
  100% {
    opacity: 0.35;
    transform: scaleY(0.65);
  }

  50% {
    opacity: 1;
    transform: scaleY(1);
  }
}

@media (max-width: 1100px) {
  .study-info-panel {
    width: 240px;
    min-width: 240px;
    padding-inline: 18px;
  }
}
</style>
