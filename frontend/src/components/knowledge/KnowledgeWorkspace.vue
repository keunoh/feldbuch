<script setup>

import {ref, watch} from "vue";

import KnowledgeNoteList from "@/components/knowledge/KnowledgeNoteList.vue";
import KnowledgeNoteDetail from "@/components/knowledge/KnowledgeNoteDetail.vue";

const props = defineProps({
  knowledgeId: {
    type: Number,
    default: null,
  }
})

const selectedNoteId = ref(null);

function selectNote(noteId) {
  selectedNoteId.value = noteId;
}

watch(
  () => props.knowledgeId,
  () => {
    selectedNoteId.value = null
  },
)
</script>

<template>
  <main class="knowledge-workspace">
    <header class="workspace-header">
      <div>
        <p class="workspace-eyebrow">
          KNOWLEDGE NOTES
        </p>

        <h1>
          지식 노트
        </h1>
      </div>

      <slot name="header-action"/>
    </header>

    <div
      v-if="knowledgeId === null"
      class="workspace-empty"
    >
      왼쪽에서 지식 폴더를 선택해주세요.
    </div>

    <div
      v-else
      class="workspace-content"
    >
      <section class="note-list-panel">
        <KnowledgeNoteList
          :knowledge-id="knowledgeId"
          :selected-note-id="selectedNoteId"
          @select-note="selectNote"
        />
      </section>

      <section class="note-detail-panel">
        <div
          v-if="selectedNoteId === null"
          class="detail-empty"
        >
          확인할 노트를 선택해주세요.
        </div>

        <KnowledgeNoteDetail
          v-else
          :note-id="selectedNoteId"
        />
      </section>
    </div>
  </main>
</template>

<style scoped>
.knowledge-workspace {
  position: relative;
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  padding: 24px;
  box-sizing: border-box;
  background: radial-gradient(
    circle at 50% 0%,
    rgba(66, 245, 123, 0.035),
    transparent 34%
  ),
  var(--color-bg);
}

.workspace-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border-soft);
}

.workspace-header h1 {
  margin: 2px 0 0;
  color: var(--color-text);
  font-size: 24px;
}

.workspace-eyebrow {
  margin: 0;
  color: var(--color-primary);
  font-family: "JetBrains Mono", monospace;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.workspace-empty,
.detail-empty {
  display: grid;
  place-items: center;
  color: var(--color-text-muted);
  font-size: 14px;
}

.workspace-empty {
  flex: 1;
}

.workspace-content {
  display: grid;
  grid-template-columns: minmax(260px, 34%) minmax(0, 1fr);
  flex: 1;
  min-height: 0;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-medium);
  overflow: hidden;
  background: var(--color-surface);
}

.note-list-panel {
  min-width: 0;
  overflow-y: auto;
  border-right: 1px solid var(--color-border-soft);
}

.note-detail-panel {
  min-width: 0;
  overflow-y: auto;
  background: var(--color-bg);
}

.detail-empty {
  height: 100%;
  min-height: 240px;
}

@media (max-width: 900px) {
  .workspace-content {
    grid-template-columns: 1fr;
  }

  .note-list-panel {
    max-height: 280px;
    border-right: 0;
    border-bottom: 1px solid var(--color-border-soft);
  }
}
</style>
