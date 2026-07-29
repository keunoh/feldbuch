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
    <div class="brand">
      <div class="brand-title">
        FELDBUCH
        <span class="brand-prompt">&gt;_</span>
      </div>

      <span
        class="brand-spark"
        aria-hidden="true"
      >
        +
      </span>
    </div>

    <div class="sidebar-header">
      <p class="section-label">
        CONVERSATIONS
      </p>

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
          {{ creating ? '생성 중...' : '+ 새 학습 시작' }}
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
  width: var(--sidebar-width);
  min-width: var(--sidebar-width);
  height: 100vh;
  padding: 24px 18px;
  border-right: 1px solid var(--color-border);
  color: var(--color-text);
  background: radial-gradient(
    circle at 20% 0%,
    rgba(66, 245, 123, 0.055),
    transparent 30%
  ),
  linear-gradient(
    180deg,
    rgba(13, 19, 26, 0.98),
    rgba(5, 8, 12, 0.98)
  );
  box-shadow: inset -1px 0 rgba(255, 255, 255, 0.015);
  overflow-y: auto;
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

.brand {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 34px;
  padding: 0 4px;
}

.brand-title {
  color: var(--color-primary);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 21px;
  font-weight: 800;
  letter-spacing: 0.045em;
  text-shadow: 0 0 18px var(--color-primary-glow);
}

.brand-prompt {
  display: inline-block;
  margin-left: 4px;
  animation: promptBlink 1.1s steps(1) infinite;
}

.brand-spark {
  color: var(--color-primary);
  font-size: 15px;
  text-shadow: 0 0 8px var(--color-primary),
  0 0 20px var(--color-primary-glow);
  animation: sparkPulse 2.6s ease-in-out infinite;
}

.sidebar-header {
  margin-bottom: 18px;
}

.section-label {
  margin: 0 0 12px 4px;
  color: var(--color-text-muted);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.create-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  min-height: 42px;
  padding: 10px 14px;
  border: 1px solid var(--color-border-primary);
  border-radius: var(--radius-medium);
  color: var(--color-primary);
  background: linear-gradient(
    135deg,
    rgba(66, 245, 123, 0.08),
    transparent 58%
  ),
  var(--color-surface);
  box-shadow: inset 0 1px rgba(255, 255, 255, 0.025),
  0 0 0 rgba(66, 245, 123, 0);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
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
  box-shadow: 0 0 22px rgba(66, 245, 123, 0.1);
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
  padding: 3px 6px 3px 12px;
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
  border-radius: 999px;
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
  box-shadow: inset 0 0 18px rgba(66, 245, 123, 0.025);
}

.conversation-item.selected::before {
  background: var(--color-primary);
  box-shadow: 0 0 12px var(--color-primary-glow);
}

.conversation-prompt {
  flex-shrink: 0;
  color: var(--color-text-disabled);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 12px;
  transition: color var(--transition-fast),
  text-shadow var(--transition-fast);
}

.conversation-item:hover .conversation-prompt {
  color: var(--color-text-muted);
}

.conversation-item.selected .conversation-prompt {
  color: var(--color-primary);
  text-shadow: 0 0 10px var(--color-primary-glow);
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
  padding: 8px 10px;
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-small);
  outline: none;
  color: var(--color-text);
  background: var(--color-bg-deep);
  box-shadow: 0 0 0 3px rgba(66, 245, 123, 0.08),
  0 0 16px rgba(66, 245, 123, 0.06);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
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
  border-radius: 7px;
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
.conversation-item:focus-within .delete-button,
.conversation-item.selected .delete-button {
  opacity: 1;
  transform: translateX(0);
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
  gap: 8px;
  margin-top: 28px;
  padding: 18px 12px;
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-medium);
  color: var(--color-text-muted);
  background: rgba(255, 255, 255, 0.015);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
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

@keyframes promptBlink {
  0%,
  48% {
    opacity: 1;
  }

  49%,
  100% {
    opacity: 0.2;
  }
}

@keyframes sparkPulse {
  0%,
  100% {
    opacity: 0.45;
    transform: scale(0.85) rotate(0deg);
  }

  50% {
    opacity: 1;
    transform: scale(1.08) rotate(45deg);
  }
}

@media (max-width: 900px) {
  .conversation-sidebar {
    width: 210px;
    min-width: 210px;
    padding-inline: 14px;
  }

  .brand-title {
    font-size: 18px;
  }
}
</style>
