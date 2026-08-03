<script setup>

import {ref, watch} from 'vue'

import {getKnowledgeNotes} from "@/api/knowledgeApi.js";

const props = defineProps({
  knowledgeId: {
    type: Number,
    default: null,
  },
  selectedNoteId: {
    type: Number,
    default: null,
  }
})


const notes = ref([]);
const loading = ref(false);
const errorMessage = ref('');

const emit = defineEmits([
  'select-note'
]);

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
        type="button"
        class="note-item"
        :class="{
          active: note.id === selectedNoteId,
        }"
        @click="emit('select-note', note.id)"
      >
        <strong class="note-title">
          {{ note.title }}
        </strong>

        <p class="note-summary">
          {{ note.summary }}
        </p>

        <time
          v-if="note.createdAt"
          class="note-created-at"
        >
          {{ note.createdAt }}
        </time>
      </button>

    </div>

  </div>

</template>

<style scoped>
.note-item {
  display: block;
  width: 100%;
  padding: 16px;
  border: 0;
  border-bottom: 1px solid var(--color-border-soft);
  color: var(--color-text);
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.note-item:hover {
  background: var(--color-surface-raised);
}

.note-item.active {
  background: var(--color-primary-soft);
  box-shadow: inset 3px 0 0 var(--color-primary);
}

.note-title {
  display: block;
  margin-bottom: 7px;
  font-size: 14px;
}

.note-summary {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: var(--color-text-muted);
  font-size: 12px;
  line-height: 1.55;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.note-created-at {
  display: block;
  margin-top: 8px;
  color: var(--color-text-muted);
  font-family: "JetBrains Mono", monospace;
  font-size: 10px;
}
</style>
