<script setup>
const props = defineProps({
  user: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits([
  'close',
  'logout',
])

function close() {
  emit(
    'close',
  )
}

function logout() {
  emit(
    'logout',
  )
}
</script>

<template>
  <Teleport to="body">
    <Transition name="settings-modal">
      <div
        class="settings-overlay"
        @click.self="close"
      >
        <section
          class="settings-modal"
          role="dialog"
          aria-modal="true"
          aria-label="Feldbuch 설정"
        >
          <header class="terminal-header">
            <div class="terminal-toolbar">
              <div
                class="terminal-dots"
                aria-hidden="true"
              >
                <span class="dot red"/>
                <span class="dot yellow"/>
                <span class="dot green"/>
              </div>

              <span class="terminal-title">
                feldbuch://settings
              </span>

              <button
                type="button"
                class="close-button"
                aria-label="설정 닫기"
                @click="close"
              >
                ×
              </button>
            </div>

            <div class="terminal-command">
              <span class="prompt">
                $
              </span>

              <span>
                feldbuch config --list
              </span>

              <span
                class="cursor"
                aria-hidden="true"
              >
                █
              </span>
            </div>
          </header>

          <div
            v-if="props.user"
            class="settings-content"
          >
            <section class="settings-section">
              <header class="section-header">
                <span class="section-index">
                  01
                </span>

                <div>
                  <h2>
                    Account
                  </h2>

                  <p>
                    Feldbuch 사용자 계정 정보
                  </p>
                </div>
              </header>

              <div class="setting-list">
                <div class="setting-row">
                  <span class="setting-key">
                    nickname
                  </span>

                  <span class="setting-value">
                    {{ props.user.nickname }}
                  </span>
                </div>

                <div class="setting-row">
                  <span class="setting-key">
                    email
                  </span>

                  <span class="setting-value">
                    {{ props.user.email }}
                  </span>
                </div>

                <div class="setting-row">
                  <span class="setting-key">
                    user_id
                  </span>

                  <span class="setting-value mono">
                    {{ props.user.userId }}
                  </span>
                </div>

                <div class="setting-row">
                  <span class="setting-key">
                    role
                  </span>

                  <span class="setting-value">
                    <span class="terminal-badge">
                      {{ props.user.role }}
                    </span>
                  </span>
                </div>
              </div>
            </section>

            <section class="settings-section">
              <header class="section-header">
                <span class="section-index">
                  02
                </span>

                <div>
                  <h2>
                    Authentication
                  </h2>

                  <p>
                    현재 세션의 인증 정보
                  </p>
                </div>
              </header>

              <div class="setting-list">
                <div class="setting-row">
                  <span class="setting-key">
                    provider
                  </span>

                  <span class="setting-value">
                    <span class="terminal-badge provider">
                      {{ props.user.provider }}
                    </span>
                  </span>
                </div>

                <div class="setting-row">
                  <span class="setting-key">
                    status
                  </span>

                  <span class="setting-value auth-status">
                    <span
                      class="status-dot"
                      aria-hidden="true"
                    />

                    authenticated
                  </span>
                </div>

                <div class="setting-row">
                  <span class="setting-key">
                    token_type
                  </span>

                  <span class="setting-value mono">
                    Bearer
                  </span>
                </div>
              </div>
            </section>

            <section class="settings-section">
              <header class="section-header">
                <span class="section-index">
                  03
                </span>

                <div>
                  <h2>
                    Appearance
                  </h2>

                  <p>
                    Feldbuch 인터페이스 설정
                  </p>
                </div>
              </header>

              <div class="setting-list">
                <div class="setting-row">
                  <span class="setting-key">
                    theme
                  </span>

                  <span class="setting-value">
                    <span class="terminal-badge">
                      TERMINAL DARK
                    </span>
                  </span>
                </div>

                <div class="setting-row">
                  <span class="setting-key">
                    accent
                  </span>

                  <span class="setting-value accent-value">
                    <span
                      class="accent-dot"
                      aria-hidden="true"
                    />

                    feldbuch green
                  </span>
                </div>

                <div class="setting-row">
                  <span class="setting-key">
                    font
                  </span>

                  <span class="setting-value mono">
                    JetBrains Mono
                  </span>
                </div>
              </div>

              <p class="coming-soon">
                # appearance customization coming soon
              </p>
            </section>

            <footer class="settings-footer">
              <button
                type="button"
                class="close-action-button"
                @click="close"
              >
                <span>
                  ❮
                </span>

                close
              </button>

              <button
                type="button"
                class="logout-button"
                @click="logout"
              >
                <span>
                  ❯
                </span>

                logout
              </button>
            </footer>
          </div>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.settings-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  padding: var(--space-10);
  place-items: center;
  background: rgba(
    0,
    3,
    1,
    0.72
  );
  backdrop-filter: blur(7px);
  box-sizing: border-box;
}

.settings-modal {
  width: min(
    100%,
    760px
  );
  max-height: min(
    86vh,
    820px
  );
  overflow-y: auto;
  border: 1px solid rgba(102, 255, 157, 0.16);
  border-radius: var(--radius-10);
  color: var(--color-text);
  background: rgba(
    4,
    9,
    6,
    0.98
  );
  box-shadow: var(--shadow-modal-strong),
  0 0 50px rgba(65, 255, 139, 0.035);
}

.terminal-header {
  position: sticky;
  top: 0;
  z-index: 2;
  border-bottom: 1px solid rgba(80, 255, 140, 0.08);
  background: rgba(
    3,
    8,
    5,
    0.985
  );
}

.terminal-toolbar {
  display: grid;
  grid-template-columns:
    70px
    1fr
    70px;
  min-height: 42px;
  align-items: center;
  padding: 0 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.035);
}

.terminal-dots {
  display: flex;
  gap: 7px;
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: var(--radius-round);
}

.dot.red {
  background: #ff5f57;
}

.dot.yellow {
  background: #febc2e;
}

.dot.green {
  background: #28c840;
}

.terminal-title {
  overflow: hidden;
  color: rgba(222, 238, 227, 0.52);
  font-family: var(--font-family-terminal);
  font-size: 10px;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.close-button {
  justify-self: end;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 0;
  border-radius: var(--radius-5);
  color: rgba(210, 229, 216, 0.42);
  background: transparent;
  font-size: 17px;
  cursor: pointer;
}

.close-button:hover {
  color: var(--color-primary);
  background: rgba(82, 255, 143, 0.05);
}

.terminal-command {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: var(--space-7) 21px;
  color: rgba(104, 255, 164, 0.72);
  font-family: var(--font-family-terminal);
  font-size: 10px;
}

.prompt {
  color: var(--color-primary);
}

.cursor {
  color: #6dffaa;
  font-size: 7px;
  animation: cursor-blink 0.8s steps(1) infinite;
}

.settings-content {
  padding: 0 27px 27px;
}

.settings-section {
  padding: 27px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.section-header {
  display: flex;
  gap: 13px;
  margin-bottom: 19px;
}

.section-index {
  padding-top: 3px;
  color: rgba(89, 255, 153, 0.38);
  font-family: var(--font-family-terminal);
  font-size: 9px;
}

.section-header h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 15px;
}

.section-header p {
  margin: 4px 0 0;
  color: var(--color-text-muted);
  font-size: 10px;
}

.setting-list {
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.055);
  border-radius: var(--radius-7);
  background: rgba(
    255,
    255,
    255,
    0.012
  );
}

.setting-row {
  display: grid;
  grid-template-columns:
    170px
    1fr;
  min-height: 43px;
  align-items: center;
  padding: 0 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.setting-row:last-child {
  border-bottom: 0;
}

.setting-key {
  color: rgba(105, 255, 165, 0.46);
  font-family: var(--font-family-terminal);
  font-size: 9px;
}

.setting-value {
  overflow: hidden;
  color: rgba(218, 234, 223, 0.75);
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.setting-value.mono {
  font-family: var(--font-family-terminal);
  font-size: 10px;
}

.terminal-badge {
  display: inline-flex;
  padding: 3px 7px;
  border: 1px solid rgba(96, 255, 157, 0.14);
  border-radius: var(--radius-4);
  color: rgba(112, 255, 169, 0.7);
  background: rgba(82, 255, 143, 0.035);
  font-family: var(--font-family-terminal);
  font-size: 7px;
  letter-spacing: 0.05em;
}

.terminal-badge.provider {
  color: #75ffaf;
}

.auth-status,
.accent-value {
  display: flex;
  align-items: center;
  gap: 7px;
  color: rgba(106, 255, 165, 0.7);
  font-family: var(--font-family-terminal);
  font-size: 9px;
}

.status-dot,
.accent-dot {
  width: 6px;
  height: 6px;
  border-radius: var(--radius-round);
  background: #59ff99;
  box-shadow: var(--shadow-glow-status-soft);
}

.status-dot {
  animation: status-pulse 2.4s ease-in-out infinite;
}

.coming-soon {
  margin: var(--space-5) 0 0;
  color: rgba(177, 201, 185, 0.27);
  font-family: var(--font-family-terminal);
  font-size: 8px;
}

.settings-footer {
  display: flex;
  justify-content: space-between;
  gap: var(--space-5);
  padding-top: var(--space-10);
}

.close-action-button,
.logout-button {
  display: flex;
  min-height: 35px;
  align-items: center;
  gap: 7px;
  padding: var(--space-3) var(--space-5);
  border: 1px solid rgba(255, 255, 255, 0.065);
  border-radius: var(--radius-6);
  color: rgba(203, 223, 209, 0.55);
  background: rgba(255, 255, 255, 0.015);
  font-family: var(--font-family-terminal);
  font-size: 9px;
  cursor: pointer;
}

.close-action-button:hover {
  border-color: rgba(89, 255, 153, 0.2);
  color: #6effaa;
  background: rgba(82, 255, 143, 0.04);
}

.logout-button:hover {
  border-color: rgba(255, 100, 100, 0.18);
  color: #ff8585;
  background: rgba(255, 90, 90, 0.04);
}

.settings-modal-enter-active,
.settings-modal-leave-active {
  transition: opacity 0.18s ease;
}

.settings-modal-enter-active
.settings-modal,
.settings-modal-leave-active
.settings-modal {
  transition: transform 0.18s ease,
  opacity 0.18s ease;
}

.settings-modal-enter-from,
.settings-modal-leave-to {
  opacity: 0;
}

.settings-modal-enter-from
.settings-modal,
.settings-modal-leave-to
.settings-modal {
  opacity: 0;
  transform: translateY(9px) scale(0.985);
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
    opacity: 0.4;
  }

  50% {
    opacity: 1;
  }
}

@media (
max-width: 650px
) {
  .settings-overlay {
    padding: var(--space-5);
  }

  .settings-modal {
    max-height: 92vh;
  }

  .settings-content {
    padding: 0 var(--space-8) var(--space-9);
  }

  .setting-row {
    grid-template-columns:
      1fr;
    gap: 5px;
    padding: var(--space-4) var(--space-5);
  }

  .settings-footer {
    flex-direction: column;
  }

  .close-action-button,
  .logout-button {
    width: 100%;
    justify-content: center;
  }
}

@media (
prefers-reduced-motion: reduce
) {
  .cursor,
  .status-dot {
    animation: none;
  }

  .settings-modal-enter-active,
  .settings-modal-leave-active,
  .settings-modal-enter-active
  .settings-modal,
  .settings-modal-leave-active
  .settings-modal {
    transition: none;
  }
}
</style>
