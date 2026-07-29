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
    <span
      class="prompt"
      aria-hidden="true"
    >
      &gt;_
    </span>

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
      {{ loading ? "●" : "➜" }}
    </button>
  </div>
</template>

<style scoped>
.chat-input {

  position: relative;

  display: flex;
  align-items: center;

  margin-top: 20px;
}

.prompt {

  position: absolute;

  left: 18px;

  color: var(--color-primary);

  font-family: "JetBrains Mono",
  monospace;

  font-weight: 700;

  pointer-events: none;

  text-shadow: 0 0 10px var(--color-primary-glow);

  animation: promptBlink 1.2s steps(1) infinite;
}

input {

  width: 100%;

  padding: 16px 64px 16px 56px;

  border: 1px solid var(--color-border);

  border-radius: 18px;

  background: linear-gradient(
    135deg,
    rgba(66, 245, 123, .03),
    transparent 45%
  ),
  var(--color-surface);

  color: var(--color-text);

  box-shadow: var(--shadow-card);

  transition: border-color var(--transition-fast),
  box-shadow var(--transition-fast),
  background var(--transition-fast);
}

input::placeholder {

  color: var(--color-text-muted);
}

input:focus {

  border-color: var(--color-border-primary);

  box-shadow: 0 0 0 4px rgba(66, 245, 123, .08),
  0 0 24px rgba(66, 245, 123, .08);

  outline: none;
}

input:disabled {

  cursor: not-allowed;

  opacity: .7;
}

button {

  position: absolute;

  right: 10px;

  width: 42px;
  height: 42px;

  border: none;

  border-radius: 50%;

  background: linear-gradient(
    135deg,
    rgba(66, 245, 123, .18),
    rgba(66, 245, 123, .06)
  );

  color: var(--color-primary);

  font-size: 18px;

  cursor: pointer;

  transition: transform var(--transition-fast),
  background var(--transition-fast),
  box-shadow var(--transition-fast);
}

button:hover:not(:disabled) {

  transform: translateY(-1px);

  box-shadow: 0 0 18px rgba(66, 245, 123, .15);
}

button:disabled {

  color: var(--color-text-disabled);

  cursor: not-allowed;

  opacity: .7;
}

@keyframes promptBlink {

  0%, 45% {

    opacity: 1;
  }

  46%, 100% {

    opacity: .25;
  }
}
</style>
