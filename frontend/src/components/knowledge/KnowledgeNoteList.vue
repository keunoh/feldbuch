<script setup>

import {ref, watch} from 'vue'

import {getKnowledgeNotes} from "@/api/knowledgeApi.js";

const props = defineProps({
  knowledgeId: {
    type: Number,
    default: null,
  },
})

const notes = ref([]);
const loading = ref(false);
const errorMessage = ref('');

watch(
  () => props.knowledgeId,
  async (knowledgeId) => {
    if (!knowledgeId) {
      notes.value = [];
      return;
    }

    loading.value = true;

    try {
      const response = await getKnowledgeNotes(knowledgeId);

      notes.value = response.data;
    } finally {
      loading.value = false;
    }
  },
  {
    immediate: true,
  }
)
</script>

<template>

  <div class="knowledge-note-list">

    <p
      v-if="loading"
    >
      불러오는 중...
    </p>

    <p
      v-else-if="notes.length === 0"
    >
      노트가 없습니다.
    </p>

    <div
      v-else
    >

      <button
        v-for="note in notes"
        :key="note.id"
      >
        {{ note.title }}
      </button>

    </div>

  </div>

</template>

<style scoped>

</style>
