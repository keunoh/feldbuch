<script setup>
import {ref,} from 'vue'

import {useRouter,} from 'vue-router'

import {login,} from '@/api/authApi.js'

import {saveAccessToken, saveUserId,} from '@/utils/auth.js'
import BaseButton from "@/components/common/BaseButton.vue";
import BaseInput from "@/components/common/BaseInput.vue";
import BaseCard from "@/components/common/BaseCard.vue";
import BaseTerminalHeader from "@/components/common/BaseTerminalHeader.vue";

const router = useRouter()

const email = ref('')
const password = ref('')

const errorMessage = ref('')
const isSubmitting = ref(false)

const showTerminalLogs = ref(false)

const loginLogs = ref([])

const googleLoginUrl =
  'http://localhost:8080/oauth2/authorization/google'

async function loginWithGoogle() {
  if (isSubmitting.value) {
    return
  }

  errorMessage.value = ''
  isSubmitting.value = true

  showTerminalLogs.value = true
  loginLogs.value = []

  await appendLog(
    'opening google identity provider...',
  )

  await wait(
    280,
  )

  completeLastLog(
    'oauth2 authorization initialized',
  )

  await appendLog(
    'redirecting to accounts.google.com...',
  )

  await wait(
    450,
  )

  window.location.href =
    googleLoginUrl
}

function wait(ms) {
  return new Promise(resolve =>
    setTimeout(
      resolve,
      ms,
    ),
  )
}

async function appendLog(
  message,
  status = 'progress',
) {
  loginLogs.value.push({
    message,
    status,
  })

  await wait(
    180,
  )
}

function completeLastLog(
  message,
) {
  const last =
    loginLogs.value[
    loginLogs.value.length - 1
      ]

  if (!last) {
    return
  }

  last.message = message
  last.status = 'success'
}

async function runLoginAnimation() {
  showTerminalLogs.value = true
  loginLogs.value = []

  await appendLog(
    'connecting authentication server...',
  )

  await wait(
    220,
  )

  completeLastLog(
    'credentials verified',
  )

  await appendLog(
    'issuing jwt...',
  )

  await wait(
    220,
  )

  completeLastLog(
    'access token created',
  )

  await appendLog(
    'loading knowledge workspace...',
  )

  await wait(
    260,
  )

  completeLastLog(
    'workspace ready',
  )

  await appendLog(
    'redirecting...',
  )

  await wait(
    420,
  )
}

async function loginUser() {
  if (isSubmitting.value) {
    return
  }

  errorMessage.value = ''
  showTerminalLogs.value = false
  loginLogs.value = []
  isSubmitting.value = true

  try {
    const response =
      await login({
        email:
          email.value.trim(),
        password:
        password.value,
      })

    saveAccessToken(
      response.data.accessToken,
    )

    saveUserId(
      response.data.userId,
    )

    await runLoginAnimation()

    await router.replace(
      '/conversations',
    )
  } catch (error) {
    console.error(
      '로그인 실패',
      error,
    )

    showTerminalLogs.value = false
    loginLogs.value = []

    errorMessage.value =
      '이메일 또는 비밀번호를 확인해 주세요.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <main class="login-view">
      <div
        class="background-terminal"
        aria-hidden="true"
      >
        <div class="terminal-prompt">
          whoami@feldbuch:~$
          <span class="cursor">_</span>
        </div>
      </div>

      <BaseCard
        class="login-shell"
        variant="terminal"
      >
        <BaseTerminalHeader
          title="feldbuch://auth/login"
        >
          <div class="terminal-session">
            <p class="session-line">
              <span class="session-label">
                Last login:
              </span>

              <span class="session-value">
                {{
                  new Date().toLocaleString()
                }}
              </span>
            </p>

            <p class="session-status">
              Authentication required

              <span
                class="session-cursor"
                aria-hidden="true"
              >
              █
            </span>
            </p>
          </div>
        </BaseTerminalHeader>

        <div class="login-content">
          <header class="login-header">
            <h1>
              Authenticate
            </h1>

            <p class="login-description">
              Continue your knowledge archive.
            </p>
          </header>

          <form
            class="login-form"
            @submit.prevent="loginUser"
          >
            <label class="field">
              <span class="field-label">
                email
              </span>

              <BaseInput
                v-model="email"
                type="email"
                autocomplete="email"
                placeholder="name@example.com"
                :disabled="isSubmitting"
                required
              />
            </label>

            <label class="field">
              <span class="field-label">
                password
              </span>

              <BaseInput
                v-model="password"
                type="password"
                autocomplete="current-password"
                placeholder="••••••••"
                :disabled="isSubmitting"
                required
              />
            </label>

            <p
              v-if="errorMessage"
              class="login-error"
              role="alert"
            >
              <span>
                error:
              </span>

              {{ errorMessage }}
            </p>

            <BaseButton
              type="submit"
              variant="primary"
              size="lg"
              :loading="isSubmitting"
              block
            >
              <template #prefix>
                ❯
              </template>

              {{
                isSubmitting
                  ? 'Authenticating...'
                  : 'Run Authentication'
              }}
            </BaseButton>
          </form>

          <Transition name="terminal-log">
            <div
              v-if="showTerminalLogs"
              class="terminal-log"
              aria-live="polite"
            >
              <p
                v-for="(log, index) in loginLogs"
                :key="index"
                class="terminal-log-line"
                :class="log.status"
              >
                <span
                  v-if="
                    log.status === 'success'
                  "
                  class="log-symbol"
                  aria-hidden="true"
                >
                  ✓
                </span>

                <span
                  v-else
                  class="log-symbol"
                  aria-hidden="true"
                >
                  &gt;
                </span>

                <span>
                  {{ log.message }}
                </span>

                <span
                  v-if="
                    log.status === 'progress'
                    && index
                      === loginLogs.length - 1
                  "
                  class="terminal-cursor"
                  aria-hidden="true"
                >
                  █
                </span>
              </p>
            </div>
          </Transition>

          <div class="login-divider">
            <span>
              or
            </span>
          </div>

          <BaseButton
            type="button"
            variant="secondary"
            size="md"
            :loading="isSubmitting"
            block
            @click="loginWithGoogle"
          >
            <template #prefix>
              <span
                class="google-mark"
                aria-hidden="true"
              >
                G
              </span>
            </template>

            {{
              isSubmitting
                ? 'Connecting...'
                : 'Sign in with Google'
            }}
          </BaseButton>

          <div class="signup-navigation">
            <span>
              new to feldbuch?
            </span>

            <RouterLink
              to="/signup"
              class="signup-link"
            >
              create account
            </RouterLink>
          </div>

          <footer class="login-footer">
            <span
              class="status-dot"
              aria-hidden="true"
            />

            <span>
              secure session ready
            </span>
          </footer>
        </div>
      </BaseCard>
    </main>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;

  background: radial-gradient(
    circle at top,
    var(--color-primary-a025),
    transparent 45%
  ),
  linear-gradient(
    180deg,
    var(--color-login-bg-start) 0%,
    var(--color-terminal-bg-0) 100%
  );
}

.login-page::before {
  content: "";

  position: absolute;
  inset: 0;

  pointer-events: none;

  opacity: .03;

  background-image: radial-gradient(var(--color-white) 0.4px, transparent 0.4px);

  background-size: 8px 8px;
}

.login-view {
  position: relative;
  z-index: 5;
  display: flex;
  min-height: 100vh;
  justify-content: center;
  align-items: flex-start;
  padding: 72px var(--space-10) 48px;
  box-sizing: border-box;
}

.login-shell {
  width: min(100%, 420px);
}

.terminal-session {
  padding: 17px var(--space-9) var(--space-8);
  font-family: var(--font-family-terminal);
  font-size: 10px;
  line-height: 1.7;
}

.session-line {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin: 0;
}

.session-label {
  color: rgba(
    103,
    255,
    160,
    0.43
  );
}

.session-value {
  color: rgba(
    215,
    239,
    223,
    0.63
  );
}

.session-status {
  margin: 7px 0 0;
  color: rgba(
    100,
    255,
    161,
    0.82
  );
}

.login-content {
  padding: 30px 32px 26px;
}

.login-header {
  margin-bottom: 25px;
}

.login-header h1 {
  font-size: 26px;

  letter-spacing: .03em;

  color: var(--color-terminal-title);
}

.login-description {
  margin: 9px 0 0;
  color: var(--color-text-muted);
  font-size: 13px;
  line-height: 1.65;
}

.login-form {
  display: grid;
  gap: 17px;
}

.field {
  display: grid;
  gap: 7px;
}

.field-label {
  color: rgba(
    207,
    230,
    215,
    0.7
  );
  font-family: var(--font-family-terminal);
  font-size: 10px;
  font-weight: 600;
}

.login-error {
  margin: 0;
  padding: var(--space-4) var(--space-5);
  border: 1px solid var(--color-danger-login-border);
  border-radius: var(--radius-6);
  color: var(--color-danger-login);
  background: var(--color-danger-login-soft);
  font-family: var(--font-family-terminal);
  font-size: 10px;
  line-height: 1.6;
}

.login-error span {
  font-weight: 700;
}

.terminal-log {
  margin-top: 21px;
  padding: 14px 15px;
  border: 1px solid var(--color-login-section-border);
  border-radius: var(--radius-6);
  background: var(--color-login-terminal);
  box-shadow: var(--shadow-login-inset);
  font-family: var(--font-family-terminal);
  font-size: 10px;
  line-height: 1.85;
}

.terminal-log-line {
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0;
}

.terminal-log-line.progress {
  color: rgba(
    125,
    255,
    175,
    0.7
  );
}

.terminal-log-line.success {
  color: var(--color-terminal-green-success);
}

.log-symbol {
  width: 12px;
  flex-shrink: 0;
  color: var(--color-primary);
}

.terminal-cursor {
  margin-left: 1px;
  color: var(--color-terminal-green-cursor);
  font-size: 8px;
  animation: cursor-blink 0.7s steps(1) infinite;
}

.login-divider {
  display: flex;
  align-items: center;
  gap: 11px;
  margin: 22px 0;
  color: rgba(
    189,
    210,
    196,
    0.28
  );
  font-family: var(--font-family-terminal);
  font-size: 9px;
}

.login-divider::before,
.login-divider::after {
  height: 1px;
  background: var(--color-white-a055);
  content: '';
  flex: 1;
}


.google-mark {
  display: grid;
  width: 20px;
  height: 20px;
  place-items: center;
  border: 1px solid var(--color-white-a100);
  border-radius: var(--radius-5);
  color: var(--color-terminal-text-strong);
  background: var(--color-terminal-bg-4);
  font-family: var(--font-family-base);
  font-size: 10px;
  font-weight: 700;
}

.login-footer {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-top: 22px;
  padding-top: 17px;
  border-top: 1px solid var(--color-white-a045);
  color: rgba(
    182,
    205,
    190,
    0.3
  );
  font-family: var(--font-family-terminal);
  font-size: 8px;
  letter-spacing: 0.03em;
}

.status-dot {
  width: 5px;
  height: 5px;
  border-radius: var(--radius-round);
  background: var(--color-terminal-green-dot);
  box-shadow: var(--shadow-glow-login-status);
}

.session-cursor {
  color: var(--color-login-background-cursor);

  animation: cursor-blink 0.9s infinite;
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

@media (
max-width: 520px
) {
  .login-view {
    padding: 40px 15px var(--space-10);
  }

  .login-content {
    padding: 26px 21px 22px;
  }

  .terminal-session {
    padding: 15px 17px;
  }
}

.background-terminal {
  position: absolute;
  inset: 0;

  pointer-events: none;
}

.terminal-prompt {
  position: absolute;

  top: 34px;
  left: 38px;

  color: var(--color-login-background-prompt);

  font-family: var(--font-family-terminal);

  font-size: 12px;

  letter-spacing: .04em;

  user-select: none;
}

.cursor {

  color: var(--color-login-background-cursor);

  animation: cursor-blink .9s infinite;
}

.signup-navigation {
  display: flex;
  align-items: center;
  justify-content: center;

  gap: var(--space-3);

  margin-top: var(--space-9);

  color: var(--color-text-muted);

  font-family: var(--font-family-terminal);

  font-size: var(--font-size-sm);
}

.signup-link {
  color: var(--color-terminal-green-text);

  text-decoration: none;
}

.signup-link:hover {
  text-decoration: underline;
}

@media (
prefers-reduced-motion: reduce
) {
  .terminal-cursor {
    animation: none;
  }
}
</style>
