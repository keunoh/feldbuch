<script setup>
import {computed, ref,} from 'vue'

const props = defineProps({
  user: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits([
  'logout',
])

const sessionStartedAt =
  ref(
    new Date(),
  )

const nickname =
  computed(() => {
    return props.user?.nickname
      ?? props.user?.name
      ?? 'Feldbuch User'
  })

const email =
  computed(() => {
    return props.user?.email
      ?? 'unknown'
  })

const formattedSessionStartedAt =
  computed(() => {
    return sessionStartedAt.value
      .toLocaleString(
        'ko-KR',
        {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          hour12: false,
        },
      )
  })

function logout() {
  emit(
    'logout',
  )
}
</script>

<template>
  <section class="user-profile-panel">
    <div class="profile-command">
      <span class="prompt-symbol">
        $
      </span>

      <span>
        whoami
      </span>

      <span
        class="command-cursor"
        aria-hidden="true"
      >
        █
      </span>
    </div>

    <div class="profile-result">
      <strong class="profile-name">
        {{ nickname }}
      </strong>

      <span class="profile-email">
        {{ email }}
      </span>
    </div>

    <div class="profile-meta">
        <span class="provider-badge">
          {{ user.provider }}
        </span>

      <span class="role-badge">
          {{ user.role }}
        </span>
    </div>


    <div class="auth-status">
      <span
        class="status-dot"
        aria-hidden="true"
      />

      <span>
        authenticated
      </span>
    </div>

    <div class="session-info">
      <span class="session-label">
        Session started
      </span>

      <time>
        {{ formattedSessionStartedAt }}
      </time>
    </div>

    <div class="profile-divider"/>

    <button
      type="button"
      class="logout-button"
      @click="logout"
    >
      <span
        class="logout-prompt"
        aria-hidden="true"
      >
        ❯
      </span>

      <span>
        logout
      </span>
    </button>
  </section>
</template>

<style scoped>
.user-profile-panel {
  flex-shrink: 0;
  padding: 16px 14px 14px;
  border-top: 1px solid var(--color-border-soft);
  background: linear-gradient(
    180deg,
    rgba(8, 13, 10, 0.9),
    rgba(3, 7, 5, 0.97)
  );
  font-family: "JetBrains Mono",
  monospace;
}

.profile-command {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 13px;
  color: var(--color-primary);
  font-size: 10px;
  font-weight: 700;
}

.prompt-symbol {
  color: rgba(96, 255, 157, 0.75);
}

.command-cursor {
  color: rgba(103, 255, 164, 0.78);
  font-size: 7px;
  animation: cursor-blink 0.85s steps(1) infinite;
}

.profile-result {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.profile-name {
  overflow: hidden;
  color: var(--color-text);
  font-family: inherit;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-email {
  overflow: hidden;
  color: var(--color-text-muted);
  font-size: 9px;
  line-height: 1.5;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.auth-status {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-top: 12px;
  color: rgba(103, 255, 164, 0.72);
  font-size: 9px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: #59ff99;
  box-shadow: 0 0 8px rgba(89, 255, 153, 0.55);
  animation: status-pulse 2.4s ease-in-out infinite;
}

.session-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 13px;
  color: rgba(190, 211, 197, 0.42);
  font-size: 8px;
  line-height: 1.5;
}

.session-label {
  color: rgba(104, 255, 164, 0.35);
}

.session-info time {
  color: rgba(210, 229, 216, 0.52);
}

.profile-divider {
  height: 1px;
  margin: 14px 0 10px;
  background: rgba(255, 255, 255, 0.05);
}

.logout-button {
  display: flex;
  width: 100%;
  min-height: 32px;
  align-items: center;
  gap: 7px;
  padding: 6px 8px;
  border: 0;
  border-radius: 5px;
  color: rgba(203, 223, 209, 0.55);
  background: transparent;
  font-family: inherit;
  font-size: 9px;
  text-align: left;
  cursor: pointer;
  transition: color 0.15s ease,
  background 0.15s ease;
}

.logout-prompt {
  width: 10px;
  color: transparent;
  transition: color 0.15s ease;
}

.logout-button:hover {
  color: #6effaa;
  background: rgba(82, 255, 143, 0.045);
}

.logout-button:hover
.logout-prompt {
  color: #58ff99;
}

.profile-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.provider-badge,
.role-badge {
  padding: 3px 6px;
  border: 1px solid rgba(96, 255, 157, 0.14);
  border-radius: 4px;
  color: rgba(112, 255, 169, 0.72);
  background: rgba(82, 255, 143, 0.035);
  font-family: "JetBrains Mono", monospace;
  font-size: 7px;
  letter-spacing: 0.05em;
}

.role-badge {
  color: rgba(205, 224, 211, 0.42);
  border-color: rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.02);
}

@keyframes cursor-blink {
  0%,
  48% {
    opacity: 1;
  }

  49%,
  100% {
    opacity: 0;
  }
}

@keyframes status-pulse {
  0%,
  100% {
    opacity: 0.45;
    box-shadow: 0 0 4px rgba(89, 255, 153, 0.3);
  }

  50% {
    opacity: 1;
    box-shadow: 0 0 10px rgba(89, 255, 153, 0.65);
  }
}

@media (
prefers-reduced-motion: reduce
) {
  .command-cursor,
  .status-dot {
    animation: none;
  }
}
</style>
