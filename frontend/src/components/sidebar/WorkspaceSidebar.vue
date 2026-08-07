<script setup>
import ConversationSidebar from '@/components/sidebar/ConversationSidebar.vue'

import KnowledgeSidebar from '@/components/sidebar/KnowledgeSidebar.vue'

import SidebarHeader from '@/components/sidebar/SidebarHeader.vue'

import SidebarTabs from '@/components/sidebar/SidebarTabs.vue'

const props = defineProps({
  mode: {
    type: String,
    default: 'conversation',
  },

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
})

const emit = defineEmits([
  'select-conversation',
  'create-conversation',
  'delete-conversation',
  'update-conversation-title',
  'select-knowledge',
  'change-mode',
])
</script>

<template>
  <aside class="workspace-sidebar">
    <SidebarHeader
      :creating="props.creating"
      @create-conversation="
        emit('create-conversation')
      "
    />

    <SidebarTabs
      :mode="props.mode"
      @change-mode="
        emit('change-mode', $event)
      "
    />

    <div class="sidebar-body">
      <ConversationSidebar
        v-if="props.mode === 'conversation'"
        class="sidebar-content"
        :conversations="props.conversations"
        :selected-conversation-id="
          props.selectedConversationId
        "
        :creating="props.creating"
        :deleting-conversation-id="
          props.deletingConversationId
        "
        :updating-conversation-id="
          props.updatingConversationId
        "
        @select="
          emit('select-conversation', $event)
        "
        @create="
          emit('create-conversation')
        "
        @delete="
          emit('delete-conversation', $event)
        "
        @update-title="
          emit(
            'update-conversation-title',
            $event,
          )
        "
      />

      <KnowledgeSidebar
        v-else
        class="sidebar-content"
        :selected-knowledge-id="
          props.selectedKnowledgeId
        "
        @select-knowledge="
          emit('select-knowledge', $event)
        "
      />
    </div>

    <footer
      v-if="$slots.footer"
      class="sidebar-footer"
    >
      <slot name="footer"/>
    </footer>
  </aside>
</template>

<style scoped>
.workspace-sidebar {
  --sidebar-padding-x: 18px;
  --sidebar-padding-y: 18px;

  display: flex;
  width: 290px;
  min-width: 290px;
  height: 100vh;
  flex-direction: column;
  border-right: 1px solid var(--color-border);
  background: var(--color-surface);
  box-sizing: border-box;
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

.sidebar-footer {
  flex-shrink: 0;
  border-top: 1px solid var(--color-border-soft);
  background: rgba(
    3,
    8,
    5,
    0.96
  );
}
</style>
