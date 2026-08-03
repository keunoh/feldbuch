<script setup>
import {ref, watch} from "vue";
import {getKnowledgeNote} from "@/api/knowledgeApi.js";

const props = defineProps({
  noteId: {
    type: Number,
    required: true,
  }
})

const note = ref(null);
const loading = ref(false);
const errorMessage = ref('');

async function loadNote(noteId) {
  loading.value = true;
  errorMessage.value = '';
  note.value = '';

  try {
    const response = await getKnowledgeNote(noteId);

    note.value = response.data;
  } catch (error) {
    console.log(error);

    errorMessage.value = '지식 노트를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
}

watch(
  () => props.noteId,
  async (noteId) => {
    if (!noteId) {
      note.value = null
      return;
    }

    await loadNote(noteId);
  },
  {
    immediate: true,
  }
)
</script>

<template>
  <article class="knowledge-note-detail">
    <div
      v-if="loading"
      class="detail-state"
    >
      노트를 불러오는 중...
    </div>

    <div
      v-else-if="errorMessage"
      class="detail-state error"
    >
      {{ errorMessage }}
    </div>

    <div
      v-else-if="note"
      class="note-content"
    >
      <header class="note-header">
        <p class="note-eyebrow">
          KNOWLEDGE NOTE
        </p>

        <h2>
          {{ note.title }}
        </h2>

        <p class="note-description">
          {{ note.description }}
        </p>
      </header>

      <section class="note-section">
        <h3>
          Summary
        </h3>

        <p class="note-summary">
          {{ note.summary }}
        </p>
      </section>

      <section
        v-if="note.keywords?.length"
        class="note-section"
      >
        <h3>
          Keywords
        </h3>

        <div class="keyword-list">
          <span
            v-for="keyword in note.keywords"
            :key="keyword"
            class="keyword"
          >
            #{{ keyword }}
          </span>
        </div>
      </section>
    </div>
  </article>
</template>

<style scoped>
.knowledge-note-detail {
  min-height: 100%;
  padding: 28px;
  box-sizing: border-box;
}

.detail-state {
  display: grid;
  min-height: 280px;
  place-items: center;
  color: var(--color-text-muted);
  font-size: 14px;
}

.detail-state.error {
  color: var(--color-danger);
}

.note-content {
  max-width: 860px;
  margin: 0 auto;
}

.note-header {
  padding-bottom: 24px;
  border-bottom: 1px solid var(--color-border-soft);
}

.note-eyebrow {
  margin: 0 0 8px;
  color: var(--color-primary);
  font-family: "JetBrains Mono", monospace;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.note-header h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 28px;
  line-height: 1.35;
}

.note-description {
  margin: 14px 0 0;
  color: var(--color-text-soft);
  font-size: 14px;
  line-height: 1.7;
}

.note-section {
  padding: 24px 0;
  border-bottom: 1px solid var(--color-border-soft);
}

.note-section h3 {
  margin: 0 0 14px;
  color: var(--color-text);
  font-family: "JetBrains Mono", monospace;
  font-size: 13px;
}

.note-summary {
  margin: 0;
  color: var(--color-text-soft);
  font-size: 15px;
  line-height: 1.9;
  white-space: pre-wrap;
}

.keyword-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword {
  padding: 6px 10px;
  border: 1px solid var(--color-border-primary);
  border-radius: 999px;
  color: var(--color-primary);
  background: var(--color-primary-soft);
  font-size: 12px;
}
</style>
