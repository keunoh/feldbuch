<script setup>
const props = defineProps({
  variant: {
    type: String,
    default: 'primary',
  },
})
</script>

<template>
  <span
    class="base-badge"
    :class="`is-${props.variant}`"
  >
    <slot/>
  </span>
</template>

<style scoped>
.base-badge {
  display: inline-flex;
  align-items: center;

  padding: 3px 7px;

  border-radius: var(--radius-4);

  font-family: var(--font-family-terminal);
  font-size: 7px;
  letter-spacing: .05em;

  border: 1px solid var(--color-terminal-border);

  white-space: nowrap;
}

.is-primary {
  color: var(--color-terminal-primary-text-soft);
  background: var(--color-terminal-primary-soft);
}

.is-success {
  color: var(--color-terminal-green-text-soft);
  background: var(--color-terminal-primary-soft);
}

.is-danger {
  color: var(--color-danger-login);
  background: var(--color-danger-login-soft);
}
</style>
