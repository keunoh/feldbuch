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

    <div
      v-if="!input"
      class="terminal-caret"
      aria-hidden="true"
    />

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

    <div
      class="send-hint"
      :class="{ loading }"
    >
      {{ loading ? "THINKING..." : "↵ ENTER" }}
    </div>
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

  caret-color: transparent;
}

input:not(:placeholder-shown) {

  caret-color: var(--color-primary);
}

.chat-input:not(:focus-within) .terminal-caret {

  opacity: .35;
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

.terminal-caret {

  position: absolute;

  left: 43px;

  top: 50%;

  width: 10px;

  height: 22px;

  transform: translateY(-50%);

  background: var(--color-primary);

  border-radius: 2px;

  box-shadow: 0 0 12px rgba(66, 245, 123, .25);

  pointer-events: none;

  animation: terminalCaret 1s steps(1) infinite;
}

.send-hint {
  position: absolute;

  right: 18px;
  top: 50%;
  transform: translateY(-50%);

  color: var(--color-text-muted);
  opacity: 0.7;

  font-size: 12px;
  font-family: "JetBrains Mono", monospace;
  letter-spacing: 0.08em;

  user-select: none;
}

.send-hint.loading {
  color: var(--color-primary);
  opacity: 1;

  text-shadow: 0 0 10px var(--color-primary-glow);
}

@keyframes terminalCaret {

  0%, 50% {

    opacity: 1;
  }

  51%, 100% {

    opacity: 0;
  }
}


</style>
