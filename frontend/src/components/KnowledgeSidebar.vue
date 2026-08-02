<script setup>
import {onMounted, ref} from 'vue'

import {getKnowledgeTree} from '@/api/knowledgeApi.js'
import KnowledgeTreeNode from '@/components/KnowledgeTreeNode.vue'

const emit = defineEmits([
  'select-knowledge',
])

const knowledgeTree = ref([])
const loading = ref(false)
const errorMessage = ref('')

async function loadKnowledgeTree() {
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await getKnowledgeTree()

    knowledgeTree.value = response.data
  } catch (error) {
    console.error(error)

    errorMessage.value =
      '지식 폴더를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

function selectKnowledge(knowledgeId) {
  emit(
    'select-knowledge',
    knowledgeId,
  )
}

onMounted(loadKnowledgeTree)
</script>

<template>
  <aside class="knowledge-sidebar">
    <header class="sidebar-header">
      <div>
        <p class="eyebrow">
          KNOWLEDGE
        </p>

        <h2>
          지식 폴더
        </h2>
      </div>

      <button
        class="refresh-button"
        :disabled="loading"
        @click="loadKnowledgeTree"
      >
        ↻
      </button>
    </header>

    <p
      v-if="loading"
      class="sidebar-state"
    >
      폴더를 불러오는 중...
    </p>

    <p
      v-else-if="errorMessage"
      class="sidebar-state error"
    >
      {{ errorMessage }}
    </p>

    <p
      v-else-if="knowledgeTree.length === 0"
      class="sidebar-state"
    >
      아직 정리된 지식이 없습니다.
    </p>

    <div
      v-else
      class="tree"
    >
      <KnowledgeTreeNode
        v-for="node in knowledgeTree"
        :key="node.id"
        :node="node"
        @select="selectKnowledge"
      />
    </div>
  </aside>
</template>

<style scoped>
.knowledge-sidebar {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  padding: 20px 14px;
  overflow-y: auto;
  background: var(--color-surface);
  box-sizing: border-box;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
  padding: 0 8px 14px;
  border-bottom: 1px solid var(--color-border-soft);
}

.sidebar-header h2 {
  margin: 2px 0 0;
  color: var(--color-text);
  font-size: 17px;
}

.eyebrow {
  margin: 0;
  color: var(--color-primary);
  font-family: "JetBrains Mono", monospace;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.refresh-button {
  width: 34px;
  height: 34px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-small);
  color: var(--color-text-muted);
  background: var(--color-surface-raised);
  cursor: pointer;
}

.refresh-button:hover {
  color: var(--color-primary);
  border-color: var(--color-border-primary);
}

.sidebar-state {
  padding: 20px 10px;
  color: var(--color-text-muted);
  font-size: 13px;
  line-height: 1.6;
}

.sidebar-state.error {
  color: var(--color-danger);
}

.tree {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
</style>
