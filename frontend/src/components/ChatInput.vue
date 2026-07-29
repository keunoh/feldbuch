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
      {{ loading ? "…" : "➜" }}
    </button>
  </div>
</template>

<style scoped>
.chat-input {
  position: relative;
  display: flex;
  align-items: center;
}

input {
  width: 100%;
  padding: 14px 56px 14px 16px;

  border: 1px solid #d1d5db;
  border-radius: 16px;

  font-size: 15px;

  transition: border-color .15s,
  box-shadow .15s;
}

input:focus {

  border-color: #2563eb;

  box-shadow: 0 0 0 4px rgba(37, 99, 235, .12);
}

input:disabled {
  background: #f3f4f6;
  cursor: not-allowed;
}

button {

  position: absolute;

  right: 8px;

  width: 40px;
  height: 40px;

  border: none;

  border-radius: 50%;

  padding: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  background: #2563eb;

  color: white;

  transition: background .15s,
  transform .15s;
}

button:hover:not(:disabled) {

  transform: scale(1.05);
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
