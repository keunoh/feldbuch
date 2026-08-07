<script setup>
import {ref,} from 'vue'

import {useRouter,} from 'vue-router'

import {login,} from '@/api/authApi.js'

import {saveAccessToken, saveUserId,} from '@/utils/auth.js'

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

      <section class="login-shell">
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
              feldbuch://auth/login
            </span>

            <span
              class="terminal-toolbar-spacer"
              aria-hidden="true"
            />
          </div>

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
        </header>

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

              <div class="terminal-input">
                <span
                  class="input-prefix"
                  aria-hidden="true"
                >
                  ❯
                </span>

                <input
                  v-model="email"
                  type="email"
                  autocomplete="email"
                  placeholder="name@example.com"
                  :disabled="isSubmitting"
                  required
                />
              </div>
            </label>

            <label class="field">
              <span class="field-label">
                password
              </span>

              <div class="terminal-input">
                <span
                  class="input-prefix"
                  aria-hidden="true"
                >
                  ❯
                </span>

                <input
                  v-model="password"
                  type="password"
                  autocomplete="current-password"
                  placeholder="••••••••"
                  :disabled="isSubmitting"
                  required
                />
              </div>
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

            <button
              class="login-button"
              type="submit"
              :disabled="isSubmitting"
            >
              <span
                class="button-prompt"
                aria-hidden="true"
              >
                ❯
              </span>

              <span>
                {{
                  isSubmitting
                    ? 'Authenticating...'
                    : 'Run Authentication'
                }}
              </span>
            </button>
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

          <button
            class="google-login-button"
            type="button"
            :disabled="isSubmitting"
            @click="loginWithGoogle"
          >
            <span
              class="google-mark"
              aria-hidden="true"
            >
              G
            </span>

            <span>
              {{
                isSubmitting
                  ? 'Connecting...'
                  : 'Sign in with Google'
              }}
            </span>
          </button>

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
      </section>
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
    rgba(66, 245, 123, .025),
    transparent 45%
  ),
  linear-gradient(
    180deg,
    #050706 0%,
    #020302 100%
  );
}

.login-page::before {
  content: "";

  position: absolute;
  inset: 0;

  pointer-events: none;

  opacity: .03;

  background-image: radial-gradient(#fff 0.4px, transparent 0.4px);

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
  position: relative;

  width: min(100%, 420px);

  overflow: hidden;

  border: 1px solid rgba(90, 255, 150, .16);

  border-radius: var(--radius-10);

  background: rgba(6, 9, 7, .97);

  box-shadow: 0 24px 70px rgba(0, 0, 0, .62),
  0 0 18px rgba(66, 245, 123, .04);
}

.login-shell::before {
  content: "";

  position: absolute;

  inset: 0;

  pointer-events: none;

  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, .025),
    transparent 18%
  );
}

.terminal-header {
  border-bottom: 1px solid rgba(80, 255, 140, 0.09);
  background: rgba(
    3,
    8,
    5,
    0.97
  );
}

.terminal-toolbar {
  display: grid;
  grid-template-columns:
    64px
    1fr
    64px;
  align-items: center;
  min-height: 40px;
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
  opacity: 0.92;
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
  color: rgba(
    224,
    240,
    230,
    0.55
  );
  font-family: var(--font-family-terminal);
  font-size: 10px;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.login-command {
  color: #6fffad;

  opacity: .82;
}

.login-header h1 {
  font-size: 26px;

  letter-spacing: .03em;

  color: #f2fff7;
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

.terminal-input {
  display: flex;
  min-height: 43px;
  align-items: center;
  overflow: hidden;
  border: 1px solid rgba(102, 255, 157, 0.14);
  border-radius: var(--radius-6);
  background: #020503;
  transition: border-color 0.16s ease,
  box-shadow 0.16s ease,
  background 0.16s ease;
}

.terminal-input:hover {
  border-color: rgba(102, 255, 157, 0.23);
}

.terminal-input:focus-within {
  border-color: rgba(91, 255, 151, 0.7);
  background: #030704;
  box-shadow: var(--shadow-focus-login),
  0 0 18px rgba(66, 255, 136, 0.035);
}

.input-prefix {
  padding-left: 13px;
  color: rgba(88, 255, 148, 0.82);
  font-family: var(--font-family-terminal);
  font-size: 11px;
  user-select: none;
}

.terminal-input input {
  width: 100%;
  min-width: 0;
  padding: 11px 13px 11px var(--space-3);
  border: 0;
  color: #dcebe1;
  background: transparent;
  caret-color: #64ffa1;
  font-family: var(--font-family-terminal);
  font-size: 12px;
  outline: none;
}

.terminal-input input::placeholder {
  color: rgba(
    189,
    210,
    196,
    0.25
  );
}

.terminal-input input:disabled {
  cursor: wait;
  opacity: 0.65;
}

/*
 * Chrome / Safari 자동완성 배경 제거
 */
.terminal-input input:-webkit-autofill,
.terminal-input input:-webkit-autofill:hover,
.terminal-input input:-webkit-autofill:focus,
.terminal-input input:-webkit-autofill:active {
  -webkit-text-fill-color: #dcebe1 !important;
  caret-color: #64ffa1;
  box-shadow: var(--shadow-autofill) !important;
  -webkit-box-shadow: var(--shadow-autofill) !important;
  transition: background-color 9999s ease-out 0s;
}

.login-error {
  margin: 0;
  padding: var(--space-4) var(--space-5);
  border: 1px solid rgba(255, 92, 92, 0.3);
  border-radius: var(--radius-6);
  color: #ff8585;
  background: rgba(255, 75, 75, 0.055);
  font-family: var(--font-family-terminal);
  font-size: 10px;
  line-height: 1.6;
}

.login-error span {
  font-weight: 700;
}

.login-button {
  display: flex;
  width: 100%;
  min-height: 45px;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  border: 1px solid rgba(82, 255, 143, 0.3);
  border-radius: var(--radius-6);
  color: #77ffb2;
  background: linear-gradient(
    180deg,
    #173221,
    #0d1d13
  );
  font-family: var(--font-family-terminal);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.16s ease,
  border-color 0.16s ease,
  box-shadow 0.16s ease,
  filter 0.16s ease;
}

.login-button:hover:not(:disabled) {
  border-color: rgba(82, 255, 143, 0.52);
  box-shadow: var(--shadow-glow-login);
  filter: brightness(1.08);
  transform: translateY(-1px);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  cursor: wait;
  opacity: 0.58;
}

.button-prompt {
  color: #4cff91;
}

.terminal-log {
  margin-top: 21px;
  padding: 14px 15px;
  border: 1px solid rgba(80, 255, 140, 0.09);
  border-radius: var(--radius-6);
  background: rgba(1, 4, 2, 0.9);
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
  color: #63ff9f;
}

.log-symbol {
  width: 12px;
  flex-shrink: 0;
  color: var(--color-primary);
}

.terminal-cursor {
  margin-left: 1px;
  color: #72ffae;
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
  background: rgba(
    255,
    255,
    255,
    0.055
  );
  content: '';
  flex: 1;
}

.google-login-button {
  display: flex;
  width: 100%;
  min-height: 43px;
  align-items: center;
  justify-content: center;
  gap: 9px;
  border: 1px solid rgba(
    255,
    255,
    255,
    0.095
  );
  border-radius: var(--radius-6);
  color: rgba(
    232,
    242,
    235,
    0.88
  );
  background: rgba(
    255,
    255,
    255,
    0.025
  );
  box-sizing: border-box;
  font-size: 12px;
  font-weight: 600;
  text-decoration: none;
  transition: border-color 0.16s ease,
  background 0.16s ease,
  transform 0.16s ease;

  cursor: pointer;
  font-family: inherit;
}

.google-login-button:disabled {
  cursor: wait;
  opacity: 0.58;
}

.google-login-button:hover {
  border-color: rgba(
    102,
    255,
    157,
    0.23
  );
  background: rgba(
    102,
    255,
    157,
    0.035
  );
  transform: translateY(-1px);
}

.google-mark {
  display: grid;
  width: 20px;
  height: 20px;
  place-items: center;
  border: 1px solid rgba(
    255,
    255,
    255,
    0.1
  );
  border-radius: var(--radius-5);
  color: rgba(
    240,
    247,
    242,
    0.9
  );
  background: #080c09;
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
  border-top: 1px solid rgba(
    255,
    255,
    255,
    0.045
  );
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
  background: #56ff98;
  box-shadow: var(--shadow-glow-login-status);
}

.terminal-log-enter-active,
.terminal-log-leave-active {
  transition: opacity 0.22s ease,
  transform 0.22s ease;
}

.terminal-log-enter-from {
  opacity: 0;
  transform: translateY(5px);
}

.terminal-log-leave-to {
  opacity: 0;
  transform: translateY(-4px);
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

  .login-shell {
    width: 100%;
  }

  .login-content {
    padding: 26px 21px 22px;
  }

  .terminal-session {
    padding: 15px 17px;
  }

  .terminal-toolbar {
    grid-template-columns:
      54px
      1fr
      54px;
    padding: 0 11px;
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

  color: rgba(95, 255, 155, .45);

  font-family: var(--font-family-terminal);

  font-size: 12px;

  letter-spacing: .04em;

  user-select: none;
}

.cursor {

  color: rgba(120, 255, 170, .75);

  animation: cursor-blink .9s infinite;
}

@media (
prefers-reduced-motion: reduce
) {
  .terminal-cursor {
    animation: none;
  }

  .login-button,
  .google-login-button,
  .terminal-log-enter-active,
  .terminal-log-leave-active {
    transition: none;
  }
}
</style>
