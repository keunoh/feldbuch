<script setup>
import ConversationSidebar from '@/components/conversation/ConversationSidebar.vue'

import KnowledgeSidebar from '@/components/knowledge/KnowledgeSidebar.vue'

const props = defineProps({
  conversations: {
    type: Array,
    default: () => [],
  },
  selectedConversationId: {
    type: Number,
    default: null,
  },
  selectedKnowledgeId: {
    type: Number,
    default: null,
  },
  creating: {
    type: Boolean,
    default: false,
  },
  deletingConversationId: {
    type: Number,
    default: null,
  },
  updatingConversationId: {
    type: Number,
    default: null,
  },
  mode: {
    type: String,
    default: 'conversation',
  }
})

const emit = defineEmits([
  'select-conversation',
  'create-conversation',
  'delete-conversation',
  'update-conversation-title',
  'select-knowledge',
  'change-mode',
])

function changeMode(nextMode) {
  emit('change-mode', nextMode)
}
</script>

<template>
  <aside class="workspace-sidebar">
    <div class="sidebar-tabs">
      <button
        type="button"
        :class="{ active: props.mode === 'conversation' }"
        @click="changeMode('conversation')"
      >
        대화
      </button>

      <button
        type="button"
        :class="{ active: mode === 'knowledge' }"
        @click="changeMode('knowledge')"
      >
        지식
      </button>
    </div>

    <div class="sidebar-body">
      <ConversationSidebar
        v-if="props.mode === 'conversation'"
        class="sidebar-content"
        :conversations="props.conversations"
        :selected-conversation-id="props.selectedConversationId"
        :creating="props.creating"
        :deleting-conversation-id="props.deletingConversationId"
        :updating-conversation-id="props.updatingConversationId"
        @select="emit('select-conversation', $event)"
        @create="emit('create-conversation')"
        @delete="emit('delete-conversation', $event)"
        @update-title="emit('update-conversation-title', $event)"
      />

      <KnowledgeSidebar
        v-else
        class="sidebar-content"
        :selected-knowledge-id="props.selectedKnowledgeId"
        @select-knowledge="
          emit('select-knowledge', $event)
        "
      />
    </div>
  </aside>
</template>

<style scoped>
.workspace-sidebar {
  display: flex;
  flex-direction: column;
  width: 290px;
  min-width: 290px;
  height: 100vh;
  border-right: 1px solid var(--color-border);
  background: var(--color-surface);
  box-sizing: border-box;
}

.sidebar-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
  padding: 10px;
  border-bottom: 1px solid var(--color-border-soft);
}

.sidebar-tabs button {
  padding: 8px;
  border: 1px solid transparent;
  border-radius: 7px;
  color: var(--color-text-muted);
  background: transparent;
  cursor: pointer;
  transition: color var(--transition-fast),
  border-color var(--transition-fast),
  background var(--transition-fast);
}

.sidebar-tabs button:hover {
  color: var(--color-text);
  background: var(--color-surface-raised);
}

.sidebar-tabs button.active {
  color: var(--color-primary);
  border-color: var(--color-border-primary);
  background: var(--color-primary-soft);
}

.sidebar-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.sidebar-content {
  width: 100%;
  min-width: 0;
  height: 100%;
  border-right: 0;
  box-sizing: border-box;
}
</style>
