<script setup>
import {ref} from 'vue'

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(["send"])

const input = ref("")

function submit() {
  if (props.loading) {
    return;
  }

  const message = input.value.trim();

  if (!message) {
    return;
  }

  // 부모로 데이터를 보낸다.
  emit("send", message);

  // 현재 인풋을 초기화 한다.
  input.value = ""
}
</script>

<template>
  <div class="chat-input">
    <input
      v-model="input"
      type="text"
      :disabled="loading"
      :placeholder="
        loading
          ? 'AI의 답변을 기다리고 있습니다.'
          : '메시지를 입력하세요.'
      "
      @keyup.enter="submit"
    />

    <button
      type="button"
      :disabled="loading || !input.trim()"
      @click="submit"
    >
      {{ loading ? '답변 중...' : '전송' }}
    </button>
  </div>
</template>

<style scoped>
.chat-input {
  display: flex;
  gap: 10px;
  padding-top: 16px;
}

input {
  flex: 1;
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  font-size: 15px;
}

input:focus {
  outline: none;
  border-color: #2563eb;
}

input:disabled {
  background: #f3f4f6;
  cursor: not-allowed;
}

button {
  flex-shrink: 0;
  padding: 0 20px;
  border: none;
  border-radius: 10px;
  background: #2563eb;
  color: white;
  font-weight: 600;
  cursor: pointer;
}

button:hover:not(:disabled) {
  background: #1d4ed8;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
