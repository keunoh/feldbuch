<script setup>
import {nextTick, ref} from "vue";
import SidebarSectionLabel from "@/components/sidebar/SidebarSectionLabel.vue";

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

function formatConversationTime(conversation) {
  const value = conversation.updatedAt ?? conversation.createdAt;

  if (!value) {
    return '-';
  }

  const date = new Date(value);
  const now = new Date();

  const startOfToday = new Date(
    now.getFullYear(),
    now.getMonth(),
    now.getDate()
  );

  const startOfTargetDate = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate()
  );

  const millisecondsPerDay = 1000 * 60 * 60 * 24;
  const dayDifference = Math.floor(
    (startOfToday - startOfTargetDate) / millisecondsPerDay
  );

  if (dayDifference === 0) {
    return new Intl.DateTimeFormat('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    }).format(date);
  }

  if (dayDifference === 1) {
    return '어제';
  }

  if (dayDifference >= 2 && dayDifference <= 6) {
    return `${dayDifference}일 전`;
  }

  return new Intl.DateTimeFormat('ko-KR', {
    month: '2-digit',
    day: '2-digit',
  }).format(date);
}

</script>

<template>
  <aside class="conversation-sidebar">
    <div class="sidebar-header">
      <SidebarSectionLabel>
        CONVERSATIONS
      </SidebarSectionLabel>

      <button
        type="button"
        class="create-button"
        :disabled="creating"
        @click="emit('create')"
      >
        <span
          class="create-icon"
          aria-hidden="true"
        >
          +
        </span>
        <span>
          새 학습 시작
        </span>
      </button>
    </div>

    <ul class="conversation-list">
      <li
        v-for="conversation in conversations"
        :key="conversation.id"
        class="conversation-item"
        :class="{
          selected: conversation.id === selectedConversationId
        }"
      >
        <span
          v-if="editingConversationId !== conversation.id"
          class="conversation-prompt"
          aria-hidden="true"
        >
          &gt;_
        </span>

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
          title="더블클릭하여 제목 수정"
          @click="emit('select', conversation.id)"
          @dblclick="startEditing(conversation)"
        >
          {{ conversation.title }}
        </button>

        <span
          v-if="editingConversationId !== conversation.id"
          class="conversation-time"
        >
          {{ formatConversationTime(conversation) }}
        </span>

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

    <div
      v-if="conversations.length === 0"
      class="empty-conversations"
    >
      <span>&gt; no_sessions</span>
      <small>새 학습을 시작해 주세요.</small>
    </div>
  </aside>
</template>

<style scoped>
.conversation-sidebar {
  position: relative;
  width: 100%;
  height: 100%;
  padding: var(--space-7) var(--sidebar-padding-x) var(--space-9);
  overflow-y: auto;
  box-sizing: border-box;
}

.conversation-sidebar::after {
  position: absolute;
  right: 16px;
  bottom: 18px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--color-primary);
  box-shadow: -18px -26px 0 rgba(66, 245, 123, 0.2),
  -42px -8px 0 rgba(66, 245, 123, 0.12),
  -70px -34px 0 rgba(66, 245, 123, 0.08);
  content: "";
  pointer-events: none;
}

.create-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);

  width: 100%;
  min-height: 42px;
  margin: 0 0 var(--space-9);
  padding: var(--space-4) 14px;

  border: 1px solid var(--color-border-primary);
  border-radius: var(--radius-medium);

  color: var(--color-primary);
  background: linear-gradient(
    135deg,
    rgba(66, 245, 123, 0.08),
    transparent 58%
  ),
  var(--color-surface);

  box-shadow: var(--shadow-inset-top),
  0 0 0 rgba(66, 245, 123, 0);

  font-family: var(--font-family-terminal);
  font-size: 13px;
  font-weight: 700;

  cursor: pointer;

  transition: color var(--transition-fast),
  border-color var(--transition-fast),
  background var(--transition-fast),
  box-shadow var(--transition-fast),
  transform var(--transition-fast);
}

.create-button:hover:not(:disabled) {
  transform: translateY(-1px);
  border-color: var(--color-primary);
  background: linear-gradient(
    135deg,
    rgba(66, 245, 123, 0.15),
    transparent 62%
  ),
  var(--color-surface-raised);
  box-shadow: var(--shadow-glow-22);
}

.create-button:active:not(:disabled) {
  transform: translateY(0);
}

.create-button:disabled {
  color: var(--color-text-disabled);
  border-color: var(--color-border);
  cursor: not-allowed;
  opacity: 0.65;
}

.create-icon {
  color: inherit;
  font-size: 18px;
  line-height: 1;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.conversation-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 7px;
  min-height: 44px;
  padding: 3px 6px 3px var(--space-5);
  border: 1px solid transparent;
  border-radius: var(--radius-medium);
  color: var(--color-text-muted);
  transition: color var(--transition-fast),
  border-color var(--transition-fast),
  background var(--transition-fast),
  box-shadow var(--transition-fast);
}

.conversation-item::before {
  position: absolute;
  top: 7px;
  bottom: 7px;
  left: -1px;
  width: 2px;
  border-radius: var(--radius-round);
  background: transparent;
  content: "";
  transition: background var(--transition-fast),
  box-shadow var(--transition-fast);
}

.conversation-item:hover {
  color: var(--color-text-soft);
  border-color: var(--color-border-soft);
  background: rgba(255, 255, 255, 0.025);
}

.conversation-item.selected {
  color: var(--color-primary);
  border-color: rgba(66, 245, 123, 0.09);
  background: linear-gradient(
    90deg,
    rgba(66, 245, 123, 0.13),
    rgba(66, 245, 123, 0.035)
  );
  box-shadow: var(--shadow-inset-glow);
}

.conversation-item.selected::before {
  background: var(--color-primary);
  box-shadow: var(--shadow-glow-medium);
}

.conversation-prompt {
  flex-shrink: 0;
  color: var(--color-text-disabled);
  font-family: var(--font-family-terminal);
  font-size: 12px;
  transition: color var(--transition-fast),
  text-shadow var(--transition-fast);
}

.conversation-item:hover .conversation-prompt {
  color: var(--color-text-muted);
}

.conversation-item.selected .conversation-prompt {
  color: var(--color-primary);
  text-shadow: var(--text-shadow-primary-medium);
}

.conversation-button {
  flex: 1;
  min-width: 0;
  padding: 9px 0;
  border: 0;
  color: inherit;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-item.selected .conversation-button {
  font-weight: 700;
}

.conversation-title-input {
  flex: 1;
  min-width: 0;
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-small);
  outline: none;
  color: var(--color-text);
  background: var(--color-bg-deep);
  box-shadow: var(--shadow-focus-subtle),
  0 0 16px rgba(66, 245, 123, 0.06);
  font-family: var(--font-family-terminal);
  font-size: 12px;
}

.conversation-title-input:disabled {
  color: var(--color-text-disabled);
  cursor: not-allowed;
  opacity: 0.65;
}

.delete-button {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 1px solid transparent;
  border-radius: var(--radius-7);
  color: var(--color-text-disabled);
  background: transparent;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  opacity: 0;
  transform: translateX(3px);
  transition: color var(--transition-fast),
  border-color var(--transition-fast),
  background var(--transition-fast),
  opacity var(--transition-fast),
  transform var(--transition-fast);
}

.conversation-item:hover .delete-button,
.conversation-item:focus-within .delete-button {
  opacity: 1;
  transform: translateX(0);
}

.conversation-item:hover .conversation-time,
.conversation-item:focus-within .conversation-time {
  display: none;
}

.conversation-time {
  flex-shrink: 0;
  color: var(--color-text-disabled);
  font-family: var(--font-family-terminal);
  font-size: 11px;
  letter-spacing: -0.02em;
  transition: color var(--transition-fast),
  opacity var(--transition-fast);
}

.conversation-item:hover .conversation-time {
  color: var(--color-text-muted);
}

.conversation-item.selected .conversation-time {
  color: var(--color-primary);
}

.delete-button:hover:not(:disabled) {
  color: var(--color-danger);
  border-color: rgba(251, 113, 133, 0.24);
  background: rgba(251, 113, 133, 0.09);
}

.delete-button:disabled {
  color: var(--color-text-disabled);
  cursor: not-allowed;
  opacity: 0.45;
}

.empty-conversations {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-top: 28px;
  padding: var(--space-8) var(--space-5);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-medium);
  color: var(--color-text-muted);
  background: rgba(255, 255, 255, 0.015);
  font-family: var(--font-family-terminal);
  text-align: center;
}

.empty-conversations span {
  color: var(--color-primary);
  font-size: 12px;
}

.empty-conversations small {
  color: var(--color-text-disabled);
  font-size: 11px;
}

@media (max-width: 900px) {
  .conversation-sidebar {
    width: 210px;
    min-width: 210px;
    padding-inline: 14px;
  }
}
</style>
