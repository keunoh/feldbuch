<script setup>
defineProps({
  noteId: {
    type: Number,
    required: true,
  }
})
</script>

<template>
  <div class="knowledge-note-detail">
    선택된 노트 ID: {{ noteId }}
  </div>
</template>

<style scoped>

</style>
