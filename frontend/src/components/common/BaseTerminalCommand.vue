<script setup>
const props = defineProps({
  prompt: {
    type: String,
    default: '$',
  },

  cursor: {
    type: Boolean,
    default: true,
  },
})
</script>

<template>
  <div class="terminal-command">
    <span class="prompt">
      {{ prompt }}
    </span>

    <span class="command">
      <slot/>
    </span>

    <span
      v-if="cursor"
      class="cursor"
      aria-hidden="true"
    >
      █
    </span>
  </div>
</template>

<style scoped>
.terminal-command {
  display: flex;
  align-items: center;
  gap: 7px;

  padding: var(--space-7) 21px;

  color: var(--color-terminal-primary-text-soft);

  font-family: var(--font-family-terminal);

  font-size: 10px;
}

.prompt {
  color: var(--color-primary);

  font-weight: 700;
}

.command {
  display: inline;
}

.cursor {
  color: var(--color-terminal-green-text-alt);

  font-size: 7px;

  animation: cursor-blink .8s steps(1) infinite;
}

@keyframes cursor-blink {

  0%,
  48% {
    opacity: 1;
  }

  49%,
  100% {
    opacity: 0;
  }
}

@media (prefers-reduced-motion: reduce) {

  .cursor {
    animation: none;
  }
}
</style>
