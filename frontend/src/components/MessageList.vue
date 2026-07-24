<script setup>

const props = defineProps({
  messages: Array
})
</script>

<template>

  <div
    v-for="message in props.messages"
    :key="message.id">

    {{ message.content }}
  </div>

</template>

<style scoped>

</style>
