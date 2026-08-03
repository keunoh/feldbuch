<script setup>
import {computed} from 'vue';
import SearchHighlight from "@/components/common/SearchHighlight.vue";

const props = defineProps({
  node: {
    type: Object,
    required: true,
  },
  depth: {
    type: Number,
    default: 0,
  },
  selectedId: {
    type: Number,
    default: null,
  },
  expandedIds: {
    type: Object,
    required: true,
  },
  searchKeyword: {
    type: String,
    default: '',
  }
});

const emit = defineEmits([
  'select'
]);


const hasChildren = computed(() => {
  return Array.isArray(props.node.children)
    && props.node.children.length > 0;
});

// expanded는 읽기 전용 computed이기 때문이다.
const expanded = computed(() => {
  return props.expandedIds.has(
    props.node.id
  )
})

function toggle() {
  if (!hasChildren.value) {
    return;
  }

  emit(
    'toggle',
    props.node.id,
  )
}

function selectNode() {
  emit(
    'select',
    props.node.id
  );
}
</script>

<template>
  <div
    class="tree-node"
    :class="{
      active: node.id === selectedId
    }"
  >
    <div
      class="node-row"
      :style="{
        paddingLeft: `${depth * 14 + 6}px`
      }"
    >
      <button
        class="toggle-button"
        :class="{
          invisible: !hasChildren
        }"
        @click.stop="toggle"
      >
        {{ expanded ? '⌄' : '›' }}
      </button>

      <button
        type="button"
        class="node-button"
        @click="selectNode"
      >
        <span
          class="folder-icon"
          aria-hidden="true"
        >
          {{ expanded && hasChildren ? '▾' : '▸' }}
        </span>

        <span class="node-name">
          <SearchHighlight
            :text="node.name"
            :keyword="searchKeyword"
          />
        </span>
      </button>
    </div>

    <div
      v-if="hasChildren && expanded"
      class="children"
    >
      <KnowledgeTreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :depth="depth + 1"
        :selected-id="selectedId"
        :expanded-ids="expandedIds"
        :search-keyword="searchKeyword"
        @select="emit('select', $event)"
        @toggle="emit('toggle', $event)"
      />
    </div>
  </div>
</template>

<style scoped>
.node-row {
  display: flex;
  align-items: center;
  min-height: 36px;
  border-radius: 7px;
  transition: color var(--transition-fast),
  background var(--transition-fast);
}

.node-row:hover {
  background: rgba(66, 245, 123, 0.055);
}

.toggle-button {
  flex-shrink: 0;
  width: 22px;
  padding: 0;
  border: 0;
  color: var(--color-text-muted);
  background: transparent;
  cursor: pointer;
}

.toggle-button.invisible {
  visibility: hidden;
}

.node-button {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1;
  padding: 7px 8px 7px 0;
  border: 0;
  color: var(--color-text-soft);
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.folder-icon {
  flex-shrink: 0;
  color: var(--color-primary);
  font-size: 11px;
}

.node-name {
  overflow: hidden;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-node.active > .node-row {
  background: var(--color-primary-soft);
}

.tree-node.active > .node-row .node-name {
  color: var(--color-primary);
  font-weight: 700;
}
</style>
