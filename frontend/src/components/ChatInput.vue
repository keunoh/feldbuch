<script setup>

import {ref} from 'vue'

const emit = defineEmits(["send"])

const input = ref("")

function submit() {

  if (!input.value.trim()) {
    return
  }

  // 부모로 데이터를 보낸다.
  emit("send", input.value)

  // 현재 인풋을 초기화 한다.
  input.value = ""

}
</script>

<template>

  <input
    v-model="input"
    @keyup.enter="submit"
    placeholder="메시지를 입력하세요."
  />

  <button @click="submit">
    전송
  </button>

</template>

<style scoped>

</style>
