<script setup>

import {useRoute, useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import {saveAccessToken, saveUserId} from "@/utils/auth.js";

const route = useRoute()
const router = useRouter()

const errorMessage = ref('')

onMounted(async () => {
  const token = route.query.token
  const userId = route.query.userId

  if (!token || typeof token !== 'string' || !userId) {
    errorMessage.value = '로그인 정보를 확인할 수 없습니다.'

    return
  }

  saveAccessToken(token)
  saveUserId(Number(userId))

  await router.replace(
    '/conversations'
  )
})
</script>

<template>
  <main class="oauth2-success-view">
    <section class="login-state-card">
      <template v-if="errorMessage">
        <p class="state-eyebrow">
          LOGIN ERROR
        </p>

        <h1>
          로그인에 실패했습니다.
        </h1>

        <p class="state-message">
          {{ errorMessage }}
        </p>

        <RouterLink
          to="/login"
          class="login-link"
        >
          로그인 화면으로 돌아가기
        </RouterLink>
      </template>

      <template v-else>
        <p class="state-eyebrow">
          FELDBUCH
        </p>

        <h1>
          로그인 중입니다.
        </h1>

        <p class="state-message">
          Google 계정을 Feldbuch에 연결하고 있습니다.
        </p>
      </template>
    </section>
  </main>
</template>

<style scoped>
.oauth2-success-view {
  display: grid;
  min-height: 100vh;
  padding: 24px;
  place-items: center;
  background: var(--color-bg);
  box-sizing: border-box;
}

.login-state-card {
  width: min(
    100%,
    420px
  );
  padding: 36px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-medium);
  background: var(--color-surface);
  text-align: center;
}

.state-eyebrow {
  margin: 0 0 8px;
  color: var(--color-primary);
  font-family: "JetBrains Mono", monospace;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.login-state-card h1 {
  margin: 0;
  color: var(--color-text);
  font-size: 24px;
}

.state-message {
  margin: 14px 0 0;
  color: var(--color-text-muted);
  font-size: 14px;
  line-height: 1.7;
}

.login-link {
  display: inline-block;
  margin-top: 24px;
  color: var(--color-primary);
  font-size: 14px;
  text-decoration: none;
}

.login-link:hover {
  text-decoration: underline;
}
</style>
