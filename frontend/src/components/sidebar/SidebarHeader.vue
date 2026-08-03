<script setup>
defineProps({
  creating: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'create-conversation'
])
</script>

<template>
  <header class="sidebar-header">
    <div class="brand">
      <span class="brand-name">
        FELDBUCH
      </span>

      <span
        class="brand-cursor"
        aria-hidden="true"
      >
        &gt;_
      </span>
    </div>

    <button
      type="button"
      class="create-button"
      :disabled="creating"
      aria-label="새 대화 만들기"
      @click="$emit('create-conversation')"
    >
      +
    </button>
  </header>
</template>

<style scoped>
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  padding: 22px var(--sidebar-padding-x) 18px;
  box-sizing: border-box;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.brand-name {
  flex-shrink: 0;
  color: var(--color-primary);
  font-family: "JetBrains Mono", monospace;
  font-size: 23px;
  font-weight: 800;
  letter-spacing: 0.08em;
  line-height: 1;
  text-shadow: 0 0 16px var(--color-primary-glow);
}

.brand-cursor {
  flex-shrink: 0;
  color: var(--color-primary);
  font-family: "JetBrains Mono", monospace;
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
  opacity: 0.35;
  animation: cursor-glow 1.4s ease-in-out infinite;
}

.create-button {
  display: grid;
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  padding: 0;
  place-items: center;
  border: 1px solid transparent;
  border-radius: var(--radius-small);
  color: var(--color-primary);
  background: transparent;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  transition: color var(--transition-fast),
  border-color var(--transition-fast),
  background var(--transition-fast);
}

.create-button:hover:not(:disabled) {
  border-color: var(--color-border-primary);
  background: var(--color-primary-soft);
}

.create-button:disabled {
  cursor: wait;
  opacity: 0.45;
}

@keyframes cursor-glow {
  0%,
  100% {
    opacity: 0.28;
    text-shadow: none;
  }

  50% {
    opacity: 0.9;
    text-shadow: 0 0 8px var(--color-primary-glow),
    0 0 18px var(--color-primary-glow);
  }
}

@media (prefers-reduced-motion: reduce) {
  .brand-cursor {
    animation: none;
    opacity: 0.55;
  }
}
</style>
