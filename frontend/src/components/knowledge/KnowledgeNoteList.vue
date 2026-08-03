<script setup>

import {computed, ref, watch} from 'vue'

import {getKnowledgeNotes} from "@/api/knowledgeApi.js";
import SearchHighlight from "@/components/common/SearchHighlight.vue";

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
const searchKeyword = ref('')

const emit = defineEmits([
  'select-note'
]);

const filteredNotes = computed(() => {
  const keyword =
    searchKeyword.value
      .trim()
      .toLowerCase()

  if (!keyword) {
    return notes.value;
  }

  return notes.value.filter(note => {
    const title =
      note.title?.toLowerCase() ?? ''

    const summary =
      note.summary?.toLowerCase() ?? ''

    return title.includes(keyword)
      || summary.includes(keyword)
  })
})

const filteredNoteCount = computed(() => {
  return filteredNotes.value.length
})

watch(
  () => props.knowledgeId,
  async (knowledgeId) => {
    if (!knowledgeId) {
      notes.value = [];
      return;
    }

    loading.value = true;
    errorMessage.value = '';

    try {
      const response = await getKnowledgeNotes(knowledgeId);

      notes.value =
        Array.isArray(response.data)
          ? response.data
          : []
    } catch (error) {
      console.log(error);

      errorMessage.value =
        '노트 목록을 불러오지 못했습니다.'
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
    <div class="note-search">
      <input
        v-model="searchKeyword"
        type="search"
        placeholder="노트 검색"
        aria-label="Knowledge 노트 검색"
      />

      <p
        v-if="searchKeyword"
        class="search-result"
      >
        {{ filteredNoteCount }}
        개의 노트
      </p>

      <button
        v-if="searchKeyword"
        type="button"
        class="clear-search-button"
        aria-label="검색어 지우기"
        @click="searchKeyword = ''"
      >
        ×
      </button>
    </div>

    <p
      v-if="loading"
    >
      불러오는 중...
    </p>

    <p
      v-else-if="notes.length === 0"
      class="note-state"
    >
      이 폴더에는 노트가 없습니다.
    </p>

    <p
      v-else-if="filteredNotes.length === 0"
      class="note-state"
    >
      검색 결과가 없습니다.
    </p>

    <div
      v-else
    >

      <button
        v-for="note in filteredNotes"
        :key="note.id"
        type="button"
        class="note-item"
        :class="{
          active: note.id === selectedNoteId,
        }"
        @click="emit('select-note', note.id)"
      >
        <strong class="note-title">
          <SearchHighlight
            :text="note.title"
            :keyword="searchKeyword"
          />
        </strong>

        <p class="note-summary">
          <SearchHighlight
            :text="note.summary"
            :keyword="searchKeyword"
          />
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

.note-search {
  position: sticky;
  top: 0;
  z-index: 1;
  padding: 12px;
  border-bottom: 1px solid var(--color-border-soft);
  background: var(--color-surface);
}

.note-search input {
  width: 100%;
  padding: 9px 34px 9px 11px;
  border: 1px solid var(--color-border);
  border-radius: 7px;
  color: var(--color-text);
  background: var(--color-surface-raised);
  box-sizing: border-box;
  outline: none;
}

.note-search input:focus {
  border-color: var(--color-border-primary);
  box-shadow: 0 0 0 3px var(--color-primary-soft);
}

.clear-search-button {
  position: absolute;
  top: 50%;
  right: 20px;
  width: 22px;
  height: 22px;
  padding: 0;
  border: 0;
  color: var(--color-text-muted);
  background: transparent;
  cursor: pointer;
  transform: translateY(-50%);
}

.note-state {
  padding: 24px 16px;
  color: var(--color-text-muted);
  font-size: 13px;
  text-align: center;
}

.search-result {
  padding: 6px 4px 0;
  color: var(--color-text-muted);
  font-size: 12px;
}
</style>
