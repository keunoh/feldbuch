<script setup>
import {computed, ref,} from 'vue'

const props = defineProps({
  user: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits([
  'settings',
  'logout',
])

const menuOpen = ref(false)

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

const provider =
  computed(() => {
    return props.user?.provider
      ?? 'UNKNOWN'
  })

const role =
  computed(() => {
    return props.user?.role
      ?? 'USER'
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

function toggleMenu() {
  menuOpen.value =
    !menuOpen.value
}

function openSettings() {
  menuOpen.value = false

  emit(
    'settings',
  )
}

function logout() {
  menuOpen.value = false

  emit(
    'logout',
  )
}
</script>

<template>
  <section class="user-profile-panel">
    <button
      type="button"
      class="profile-trigger"
      :aria-expanded="menuOpen"
      @click="toggleMenu"
    >
      <div class="profile-main">
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
            {{ provider }}
          </span>

          <span class="role-badge">
            {{ role }}
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
      </div>

      <span
        class="menu-indicator"
        aria-hidden="true"
      >
        {{ menuOpen ? '⌃' : '⌄' }}
      </span>
    </button>

    <Transition name="profile-menu">
      <div
        v-if="menuOpen"
        class="profile-menu"
      >
        <button
          type="button"
          class="profile-menu-item"
          @click="openSettings"
        >
          <span class="menu-prompt">
            ❯
          </span>

          <span>
            settings
          </span>
        </button>

        <button
          type="button"
          class="profile-menu-item logout"
          @click="logout"
        >
          <span class="menu-prompt">
            ❯
          </span>

          <span>
            logout
          </span>
        </button>
      </div>
    </Transition>
  </section>
</template>

<style scoped>
.user-profile-panel {
  position: relative;
  flex-shrink: 0;
  padding: var(--space-5) var(--space-5) 10px;
  border-top: 1px solid var(--color-border-soft);
  background: linear-gradient(
    180deg,
    var(--color-terminal-surface-soft),
    var(--color-terminal-surface-deep)
  );
  font-family: var(--font-family-terminal);
}

.profile-trigger {
  display: flex;
  width: 100%;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-4);
  border: 0;
  border-radius: var(--radius-7);
  color: inherit;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s ease;
}

.profile-trigger:hover {
  background: var(--color-terminal-primary-soft);
}

.profile-main {
  min-width: 0;
  flex: 1;
}

.profile-command {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 11px;
  color: var(--color-primary);
  font-size: 10px;
  font-weight: 700;
}

.prompt-symbol {
  color: rgba(
    96,
    255,
    157,
    0.75
  );
}

.command-cursor {
  color: rgba(
    103,
    255,
    164,
    0.78
  );
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
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-email {
  overflow: hidden;
  color: var(--color-text-muted);
  font-size: 9px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: var(--space-3);
}

.provider-badge,
.role-badge {
  padding: 3px 6px;
  border: 1px solid var(--color-terminal-border);
  border-radius: var(--radius-4);
  color: var(--color-terminal-primary-text);
  background: var(--color-terminal-primary-soft);
  font-size: 7px;
  letter-spacing: 0.05em;
}

.role-badge {
  border-color: var(--color-white-a060);
  color: var(--color-terminal-text-disabled);
  background: var(--color-white-a020);
}

.auth-status {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-top: 11px;
  color: rgba(103, 255, 164, 0.72);
  font-size: 9px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: var(--radius-round);
  background: var(--color-terminal-green-bright);
  box-shadow: var(--shadow-glow-status);
  animation: status-pulse 2.4s ease-in-out infinite;
}

.session-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: var(--space-5);
  color: rgba(190, 211, 197, 0.42);
  font-size: 8px;
}

.session-label {
  color: rgba(104, 255, 164, 0.35);
}

.menu-indicator {
  flex-shrink: 0;
  padding-top: 2px;
  color: rgba(113, 255, 168, 0.45);
  font-size: 11px;
}

.profile-menu {
  display: grid;
  gap: 3px;
  margin-top: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-white-a050);
}

.profile-menu-item {
  display: flex;
  width: 100%;
  min-height: 32px;
  align-items: center;
  gap: 7px;
  padding: 6px 9px;
  border: 0;
  border-radius: var(--radius-5);
  color: var(--color-terminal-text-muted);
  background: transparent;
  font-family: inherit;
  font-size: 9px;
  text-align: left;
  cursor: pointer;
}

.profile-menu-item:hover {
  color: var(--color-terminal-green-text);
  background: var(--color-terminal-primary-hover);
}

.profile-menu-item.logout:hover {
  color: var(--color-danger-login);
  background: var(--color-danger-action-soft-strong);
}

.menu-prompt {
  width: 10px;
  color: rgba(89, 255, 153, 0.65);
}

.profile-menu-enter-active,
.profile-menu-leave-active {
  transition: opacity 0.15s ease,
  transform 0.15s ease;
}

.profile-menu-enter-from,
.profile-menu-leave-to {
  opacity: 0;
  transform: translateY(4px);
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
  }

  50% {
    opacity: 1;
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
