<script setup>
defineProps({
  conversations: {
    type: Array,
    required: true
  },

  selectedConversationId: {
    type: Number,
    default: null
  },

  creating: {
    type: Boolean,
    default: false
  },

  deletingConversationId: {
    type: Number,
    default: null
  }
});

const emit = defineEmits([
  'select',
  'create',
  'delete'
]);

function deleteConversation(conversationId) {
  emit('delete', conversationId);
}
</script>

<template>
  <aside class="conversation-sidebar">
    <div class="sidebar-header">
      <h2>대화 목록</h2>

      <button
        type="button"
        class="create-button"
        :disabled="creating"
        @click="emit('create')"
      >
        {{ creating ? '생성 중...' : '+ 새 학습 시작' }}
      </button>
    </div>

    <ul class="conversation-list">
      <li
        v-for="conversation in conversations"
        :key="conversation.id"
        class="conversation-item"
      >
        <button
          type="button"
          class="conversation-button"
          :class="{
            selected: conversation.id === selectedConversationId
          }"
          @click="emit('select', conversation.id)"
        >
          {{ conversation.title }}
        </button>

        <button
          type="button"
          class="delete-button"
          :disabled="deletingConversationId === conversation.id"
          aria-label="대화 삭제"
          @click.stop="deleteConversation(conversation.id)"
        >
          {{
            deletingConversationId === conversation.id
              ? '...'
              : 'x'
          }}
        </button>
      </li>
    </ul>
  </aside>
</template>

<style scoped>
.conversation-sidebar {
  width: 240px;
  min-width: 240px;
  height: 100vh;
  padding: 20px;
  border-right: 1px solid #e5e7eb;
  background: #ffffff;
  box-sizing: border-box;
  overflow-y: auto;
}

.sidebar-header {
  margin-bottom: 20px;
}

.sidebar-header h2 {
  margin: 0 0 16px;
  font-size: 20px;
}

.create-button {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #2563eb;
  border-radius: 8px;
  background: #2563eb;
  color: white;
  font-weight: 600;
  cursor: pointer;
  text-align: center;
}

.create-button:hover:not(:disabled) {
  background: #1d4ed8;
}

.create-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.conversation-button {
  flex: 1;
  min-width: 0;
  padding: 10px 12px;
  border: none;
  border-radius: 8px;
  background: none;
  color: #374151;
  cursor: pointer;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-button:hover {
  background: #f3f4f6;
}

.conversation-button.selected {
  background: #dbeafe;
  color: #1d4ed8;
  font-weight: 600;
}

.delete-button {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  padding: 0;
  border: none;
  border-radius: 6px;
  background: none;
  color: #9ca3af;
  font-size: 20px;
  cursor: pointer;
}

.delete-button:hover:not(:disabled) {
  background: #fee2e2;
  color: #dc2626;
}

.delete-button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
</style>
