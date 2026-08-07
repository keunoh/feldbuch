<script setup>
import {computed, onMounted, ref} from 'vue'

import {getKnowledgeTree} from '@/api/knowledgeApi.js'
import KnowledgeTreeNode from '@/components/knowledge/KnowledgeTreeNode.vue'
import SidebarSectionLabel from "@/components/sidebar/SidebarSectionLabel.vue";

defineProps({
  selectedKnowledgeId: {
    type: Number,
    default: null,
  }
})

const emit = defineEmits([
  'select-knowledge',
])

const knowledgeTree = ref([])
const loading = ref(false)
const errorMessage = ref('')
const expandedIds = ref(new Set());
const searchKeyword = ref('')

const filteredKnowledgeTree = computed(() => {
  return filterTree(
    knowledgeTree.value,
    searchKeyword.value
  )
})

const visibleExpandedIds = computed(() => {
  if (!searchKeyword.value.trim()) {
    return expandedIds.value
  }

  return collectExpandableIds(
    filteredKnowledgeTree.value,
  )
})

const filteredKnowledgeCount = computed(() => {

  function count(nodes) {

    return nodes.reduce(
      (total, node) =>
        total + 1 + count(node.children ?? []),
      0
    )
  }

  return count(filteredKnowledgeTree.value)
})

function filterTree(nodes, keyword) {
  const normalizedKeyword =
    keyword.trim().toLowerCase()

  if (!normalizedKeyword) {
    return nodes
  }

  return nodes
    .map(node => {
      const filteredChildren =
        filterTree(
          node.children ?? [],
          normalizedKeyword
        )

      const matchesSelf =
        node.name
          .toLowerCase()
          .includes(normalizedKeyword)

      if (!matchesSelf && filteredChildren.length === 0) {
        return null
      }

      return {
        ...node,
        children: filteredChildren,
      }
    })
    .filter(Boolean)
}

function collectExpandableIds(nodes, result = new Set()) {
  for (const node of nodes) {
    const children = node.children ?? []

    if (children.length > 0) {
      result.add(node.id)

      collectExpandableIds(
        children,
        result
      )
    }
  }

  return result
}

function initializeExpandedNodes() {
  expandedIds.value = new Set(
    knowledgeTree.value.map(node => node.id)
  )
}

function toggleNode(id) {

  // 여기서 Set을 새로 만드는 이유는 Vue의 반응성 때문이다.
  const next =
    new Set(expandedIds.value)

  if (next.has(id)) {
    next.delete(id)
  } else {
    next.add(id)
  }

  expandedIds.value = next
}

async function loadKnowledgeTree() {
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await getKnowledgeTree()

    knowledgeTree.value =
      Array.isArray(response.data)
        ? response.data
        : []

    initializeExpandedNodes()
  } catch (error) {
    console.error(error)

    errorMessage.value =
      '지식 폴더를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

function selectKnowledge(selection) {
  emit(
    'select-knowledge',
    selection,
  )
}

onMounted(loadKnowledgeTree)
</script>

<template>
  <aside class="knowledge-sidebar">
    <SidebarSectionLabel>
      KNOWLEDGE
    </SidebarSectionLabel>

    <div class="knowledge-toolbar">
      <div class="search-box">
        <input
          v-model="searchKeyword"
          type="search"
          placeholder="지식 폴더 검색"
          aria-label="지식 폴더 검색"
        />

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

      <button
        type="button"
        class="refresh-button"
        :disabled="loading"
        aria-label="지식 폴더 새로고침"
        @click="loadKnowledgeTree"
      >
        ↻
      </button>
    </div>
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

    <p
      v-else-if="filteredKnowledgeTree.length === 0"
      class="sidebar-state"
    >
      검색 결과가 없습니다.
    </p>

    <div
      v-else
      class="tree"
    >
      <KnowledgeTreeNode
        v-for="node in filteredKnowledgeTree"
        :key="node.id"
        :node="node"
        :path="[]"
        :selected-id="selectedKnowledgeId"
        :expanded-ids="visibleExpandedIds"
        :search-keyword="searchKeyword"
        @select="selectKnowledge"
        @toggle="toggleNode"
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

  padding: var(--space-7) var(--sidebar-padding-x) var(--space-9);

  overflow-y: auto;
  background: var(--color-surface);
  box-sizing: border-box;
}

.knowledge-toolbar {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-top: 0;
  margin-bottom: var(--space-9);
}


.search-box {
  position: relative;
  flex: 1;
  min-width: 0;
}

.search-box input {
  width: 100%;
  padding: 9px 34px 9px 11px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-7);
  color: var(--color-text);
  background: var(--color-surface-raised);
  box-sizing: border-box;
  outline: none;
}

.search-box input:focus {
  border-color: var(--color-border-primary);
  box-shadow: var(--shadow-focus);
}

.clear-search-button {
  position: absolute;
  top: 50%;
  right: 8px;
  width: 22px;
  height: 22px;
  padding: 0;
  border: 0;
  color: var(--color-text-muted);
  background: transparent;
  cursor: pointer;
  transform: translateY(-50%);
}

.refresh-button {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-8);
  color: var(--color-text-muted);
  background: var(--color-surface-raised);
  cursor: pointer;
}
</style>
