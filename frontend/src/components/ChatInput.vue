<script setup>

const emit = defineEmits(["send"])

function submit() {

  emit("send", "안녕하세요")

}
</script>

<template>

  <button @click="submit">
    전송
  </button>

</template>

<style scoped>

</style>
