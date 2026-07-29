<script setup>
import {nextTick, ref} from "vue";

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
  },

  updatingConversationId: {
    type: Number,
    default: null
  }
});

const emit = defineEmits([
  'select',
  'create',
  'delete',
  'update-title'
]);

// 현재 어떤 대화를 수정하고 있는지 저장
const editingConversationId = ref(null);
// 입력창에 작성 중인 제목 저장
const editingTitle = ref('');
const titleInput = ref(null);

function setTitleInput(element) {
  titleInput.value = element;
}

async function startEditing(conversation) {
  if (editingConversationId.value !== null) {
    return;
  }

  editingConversationId.value = conversation.id;
  editingTitle.value = conversation.title;

  await nextTick();

  titleInput.value?.focus();
  titleInput.value?.select();
}

function cancelEditing() {
  editingConversationId.value = null;
  editingTitle.value = '';
}

function submitEditing(conversation) {
  const title = editingTitle.value.trim();

  if (!title) {
    cancelEditing();
    return;
  }

  if (title === conversation.title) {
    cancelEditing();
    return;
  }

  emit('update-title', {
    conversationId: conversation.id,
    title
  });

  cancelEditing();
}

function handleBlur(conversation) {
  submitEditing(conversation);
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
        <input
          v-if="editingConversationId === conversation.id"
          :ref="setTitleInput"
          v-model="editingTitle"
          type="text"
          maxlength="100"
          class="conversation-title-input"
          :disabled="updatingConversationId === conversation.id"
          @keydown.enter.prevent="submitEditing(conversation)"
          @keydown.esc.prevent="cancelEditing"
          @blur="handleBlur(conversation)"
        />

        <button
          v-else
          type="button"
          class="conversation-button"
          :class="{
            selected: conversation.id === selectedConversationId
          }"
          title="더블클릭하여 제목 수정"
          @click="emit('select', conversation.id)"
          @dblclick="startEditing(conversation)"
        >
          {{ conversation.title }}
        </button>

        <button
          type="button"
          class="delete-button"
          :disabled="
           deletingConversationId === conversation.id
           || updatingConversationId === conversation.id
          "
          aria-label="대화 삭제"
          @click.stop="emit('delete', conversation.id)"
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

.conversation-title-input {
  flex: 1;
  min-width: 0;
  padding: 9px 11px;
  border: 1px solid #2563eb;
  border-radius: 8px;
  outline: none;
  color: #111827;
  font: inherit;
  box-sizing: border-box;
}

.conversation-title-input:focus {
  box-shadow: 0 0 0 2px #dbeafe;
}

.conversation-title-input:disabled {
  cursor: not-allowed;
  opacity: 0.6;
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
