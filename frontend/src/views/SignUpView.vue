<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'

import BaseCard from '@/components/common/BaseCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseTerminalHeader from '@/components/common/BaseTerminalHeader.vue'
import BaseTerminalCommand from '@/components/common/BaseTerminalCommand.vue'

import {signup} from '@/api/authApi.js'

const router = useRouter()

const nickname = ref('')
const email = ref('')
const password = ref('')

const errorMessage = ref('')
const isSubmitting = ref(false)

async function signupUser() {
  if (isSubmitting.value) {
    return
  }

  errorMessage.value = ''
  isSubmitting.value = true

  try {
    await signup({
      email: email.value.trim(),
      password: password.value,
      nickname: nickname.value.trim(),
    })

    await router.replace('/login')
  } catch (error) {
    console.error(
      '회원가입 실패',
      error,
    )

    errorMessage.value =
      '회원가입에 실패했습니다. 입력 정보를 확인해 주세요.'
  } finally {
    isSubmitting.value = false
  }
}

function goToLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="signup-page">
    <main class="signup-view">
      <div
        class="background-terminal"
        aria-hidden="true"
      >
        <div class="terminal-prompt">
          adduser@feldbuch:~$
          <span class="background-cursor">
            _
          </span>
        </div>
      </div>

      <BaseCard
        class="signup-shell"
        variant="terminal"
      >
        <BaseTerminalHeader
          title="feldbuch://auth/signup"
        >
          <BaseTerminalCommand>
            useradd --interactive
          </BaseTerminalCommand>
        </BaseTerminalHeader>

        <div class="signup-content">
          <header class="signup-header">
            <h1>
              Create Account
            </h1>

            <p>
              Create your Feldbuch identity.
            </p>
          </header>

          <form
            class="signup-form"
            @submit.prevent="signupUser"
          >
            <label class="field">
              <span class="field-label">
                nickname
              </span>

              <BaseInput
                v-model="nickname"
                type="text"
                autocomplete="nickname"
                placeholder="nickname"
                :disabled="isSubmitting"
                required
              />
            </label>

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
                autocomplete="new-password"
                placeholder="8-20 characters"
                :disabled="isSubmitting"
                required
              />

              <span class="field-hint">
                password must be between 8 and 20 characters
              </span>
            </label>

            <p
              v-if="errorMessage"
              class="signup-error"
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
                  ? 'Creating account...'
                  : 'Create Account'
              }}
            </BaseButton>
          </form>

          <div class="login-navigation">
            <span>
              already registered?
            </span>

            <button
              type="button"
              class="login-link"
              @click="goToLogin"
            >
              return to login
            </button>
          </div>

          <footer class="signup-footer">
            account registration ready
          </footer>
        </div>
      </BaseCard>
    </main>
  </div>
</template>

<style scoped>
.signup-page {
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

.signup-view {
  position: relative;
  z-index: 5;

  display: flex;

  min-height: 100vh;

  align-items: flex-start;

  justify-content: center;

  padding: 72px var(--space-10) 48px;

  box-sizing: border-box;
}

.signup-shell {
  width: min(
    100%,
    420px
  );
}

.signup-content {
  padding: 30px 32px 26px;
}

.signup-header {
  margin-bottom: 25px;
}

.signup-header h1 {
  margin: 0;

  color: var(--color-terminal-title);

  font-size: 26px;

  letter-spacing: 0.03em;
}

.signup-header p {
  margin: 9px 0 0;

  color: var(--color-text-muted);

  font-size: 13px;

  line-height: 1.65;
}

.signup-form {
  display: grid;

  gap: 17px;
}

.field {
  display: grid;

  gap: 7px;
}

.field-label {
  color: var(--color-terminal-text-soft);

  font-family: var(--font-family-terminal);

  font-size: var(--font-size-sm);

  font-weight: var(--font-weight-semibold);
}

.field-hint {
  color: var(--color-text-disabled);

  font-family: var(--font-family-terminal);

  font-size: var(--font-size-xs);
}

.signup-error {
  margin: 0;

  padding: var(--space-4) var(--space-5);

  border: 1px solid var(--color-danger-login-border);

  border-radius: var(--radius-6);

  color: var(--color-danger-login);

  background: var(--color-danger-login-soft);

  font-family: var(--font-family-terminal);

  font-size: var(--font-size-sm);

  line-height: 1.6;
}

.signup-error span {
  font-weight: var(--font-weight-bold);
}

.login-navigation {
  display: flex;

  align-items: center;

  justify-content: center;

  gap: var(--space-3);

  margin-top: var(--space-9);

  color: var(--color-text-muted);

  font-family: var(--font-family-terminal);

  font-size: var(--font-size-sm);
}

.login-link {
  padding: 0;

  border: 0;

  color: var(--color-terminal-green-text);

  background: transparent;

  font: inherit;

  cursor: pointer;
}

.login-link:hover {
  text-decoration: underline;
}

.signup-footer {
  margin-top: var(--space-9);

  padding-top: var(--space-7);

  border-top: 1px solid var(--color-white-a045);

  color: var(--color-text-disabled);

  font-family: var(--font-family-terminal);

  font-size: var(--font-size-xs);

  text-align: center;
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

  font-size: var(--font-size-md);

  letter-spacing: 0.04em;

  user-select: none;
}

.background-cursor {
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

@media (max-width: 520px) {
  .signup-view {
    padding: 40px 15px var(--space-10);
  }

  .signup-content {
    padding: 26px 21px 22px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .background-cursor {
    animation: none;
  }
}
</style>
