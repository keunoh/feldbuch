<script setup>
import {onMounted, ref,} from 'vue'

import {useRoute, useRouter,} from 'vue-router'

import {saveAccessToken, saveUserId,} from '@/utils/tokenStorage.js'
import BaseTerminalHeader from "@/components/common/BaseTerminalHeader.vue";
import BaseTerminalCommand from "@/components/common/BaseTerminalCommand.vue";

const route = useRoute()
const router = useRouter()

const errorMessage = ref('')
const logs = ref([])

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
  logs.value.push({
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
    logs.value[
    logs.value.length - 1
      ]

  if (!last) {
    return
  }

  last.message = message
  last.status = 'success'
}

async function completeOAuthLogin() {
  const token =
    route.query.token

  const userId =
    route.query.userId

  if (
    !token
    || typeof token !== 'string'
    || !userId
  ) {
    errorMessage.value =
      'OAuth2 로그인 정보를 확인할 수 없습니다.'

    return
  }

  saveAccessToken(
    token,
  )

  saveUserId(
    Number(userId),
  )

  await appendLog(
    'verifying google identity...',
  )

  await wait(
    220,
  )

  completeLastLog(
    'google identity verified',
  )

  await appendLog(
    'binding feldbuch account...',
  )

  await wait(
    220,
  )

  completeLastLog(
    'feldbuch account linked',
  )

  await appendLog(
    'storing access token...',
  )

  await wait(
    220,
  )

  completeLastLog(
    'access token stored',
  )

  await appendLog(
    'opening knowledge workspace...',
  )

  await wait(
    450,
  )

  await router.replace(
    '/conversations',
  )
}

onMounted(() => {
  completeOAuthLogin()
})
</script>

<template>
  <div class="oauth-success-page">
    <main class="oauth-success-view">
      <section class="oauth-terminal">
        <BaseTerminalHeader
          title="feldbuch://auth/oauth2"
        />

        <div class="terminal-content">
          <BaseTerminalCommand>
            oauth2 complete
          </BaseTerminalCommand>

          <template v-if="errorMessage">
            <div class="oauth-error">
              <p>
                <span>
                  error:
                </span>

                {{ errorMessage }}
              </p>

              <RouterLink
                to="/login"
                class="back-link"
              >
                ❯ return to login
              </RouterLink>
            </div>
          </template>

          <template v-else>
            <div class="oauth-status">
              <h1>
                Authentication complete
              </h1>

              <p>
                Preparing your Feldbuch workspace.
              </p>
            </div>

            <div
              class="terminal-log"
              aria-live="polite"
            >
              <p
                v-for="(log, index) in logs"
                :key="index"
                class="terminal-log-line"
                :class="log.status"
              >
                <span
                  v-if="log.status === 'success'"
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
                    && index === logs.length - 1
                  "
                  class="terminal-cursor"
                  aria-hidden="true"
                >
                  █
                </span>
              </p>
            </div>
          </template>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.oauth-success-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: radial-gradient(
    circle at 50% -10%,
    var(--color-oauth-background-glow),
    transparent 42%
  ),
  var(--color-terminal-bg-1);
}

.oauth-success-view {
  position: relative;
  z-index: 5;
  display: flex;
  min-height: 100vh;
  justify-content: center;
  align-items: flex-start;
  padding: 92px var(--space-10) 48px;
  box-sizing: border-box;
}

.oauth-terminal {
  width: min(
    100%,
    430px
  );
  overflow: hidden;
  border: 1px solid var(--color-oauth-border);
  border-radius: var(--radius-10);
  background: var(--color-terminal-surface-muted);
  box-shadow: var(--shadow-lg),
  0 0 45px rgba(74, 255, 143, 0.035);
  backdrop-filter: blur(12px);
}

.terminal-content {
  padding: 30px 32px;
}

.oauth-status h1 {
  margin: 0;
  color: var(--color-text);
  font-size: 23px;
}

.oauth-status p {
  margin: 9px 0 22px;
  color: var(--color-text-muted);
  font-size: 13px;
}

.terminal-log {
  padding: 15px var(--space-7);
  border: 1px solid var(--color-login-section-border);
  border-radius: var(--radius-6);
  background: var(--color-login-terminal);
  font-family: var(--font-family-terminal);
  font-size: 10px;
  line-height: 1.9;
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

.oauth-error {
  padding: 14px var(--space-7);
  border: 1px solid var(--color-danger-login-border);
  border-radius: var(--radius-6);
  color: var(--color-danger-login);
  background: var(--color-danger-login-soft);
  font-family: var(--font-family-terminal);
  font-size: 11px;
  line-height: 1.7;
}

.oauth-error p {
  margin: 0;
}

.oauth-error span {
  font-weight: 700;
}

.back-link {
  display: inline-block;
  margin-top: var(--space-6);
  color: var(--color-terminal-green-cursor);
  text-decoration: none;
}

.back-link:hover {
  text-decoration: underline;
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
  .oauth-success-view {
    padding: 48px 15px var(--space-10);
  }

  .terminal-content {
    padding: 26px 21px;
  }
}

@media (
prefers-reduced-motion: reduce
) {
  .terminal-cursor {
    animation: none;
  }
}
</style>
