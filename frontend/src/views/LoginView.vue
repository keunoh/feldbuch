<script setup>
import {ref} from 'vue';
import {login} from "@/api/authApi.js";
import {useRouter} from "vue-router";

const router = useRouter();

const email = ref('');
const password = ref('');

async function loginUser() {

  try {
    const response = await login({
      email: email.value,
      password: password.value
    });

    localStorage.setItem(
      'accessToken',
      response.data.accessToken
    );

    localStorage.setItem(
      'userId',
      response.data.userId
    );

    await router.push('/conversations');

  } catch (error) {
    console.error(error);
  }

}
</script>

<template>
  <div>
    <h2>로그인</h2>

    <input
      v-model="email"
      placeholder="이메일"
    />

    <input
      v-model="password"
      type="password"
      placeholder="비밀번호"
    />

    <button @click="loginUser">
      로그인
    </button>

  </div>
</template>

<style scoped>

</style>
