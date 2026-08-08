<script setup>
const props = defineProps({
  variant: {
    type: String,
    default: 'success',
    validator: value =>
      [
        'success',
        'warning',
        'danger',
        'muted',
      ].includes(value),
  },

  pulse: {
    type: Boolean,
    default: false,
  },

  size: {
    type: String,
    default: 'md',
    validator: value =>
      [
        'sm',
        'md',
        'lg',
      ].includes(value),
  },
})
</script>

<template>
  <span
    class="base-status-dot"
    :class="[
      `is-${props.variant}`,
      `size-${props.size}`,
      {
        pulse: props.pulse,
      },
    ]"
    aria-hidden="true"
  />
</template>

<style scoped>
.base-status-dot {
  display: inline-block;
  flex-shrink: 0;

  border-radius: var(--radius-round);
}

.size-sm {
  width: 5px;
  height: 5px;
}

.size-md {
  width: 6px;
  height: 6px;
}

.size-lg {
  width: 8px;
  height: 8px;
}

.is-success {
  background: var(--color-terminal-green-bright);

  box-shadow: var(--shadow-glow-status-soft);
}

.is-warning {
  background: var(--color-warning);
}

.is-danger {
  background: var(--color-danger);
}

.is-muted {
  background: var(--color-text-disabled);

  box-shadow: none;
}

.pulse {
  animation: status-pulse 2.4s ease-in-out infinite;
}

@keyframes status-pulse {
  0%,
  100% {
    opacity: 0.4;
  }

  50% {
    opacity: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .pulse {
    animation: none;
  }
}
</style>
