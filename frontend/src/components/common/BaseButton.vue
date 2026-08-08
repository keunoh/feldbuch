<script setup>

import {computed} from "vue";

const props = defineProps({
  type: {
    type: String,
    default: 'button',
  },

  variant: {
    type: String,
    default: 'primary',
    validator: value =>
      [
        'primary',
        'secondary',
        'ghost',
        'danger',
      ].includes(value),
  },

  size: {
    type: String,
    default: 'md',
    validator: value =>
      ['sm', 'md', 'lg'].includes(value),
  },

  disabled: {
    type: Boolean,
    default: false,
  },

  loading: {
    type: Boolean,
    default: false,
  },

  block: {
    type: Boolean,
    default: false,
  }
})

const emit = defineEmits([
  'click'
])

const buttonClasses = computed(() => [
  'base-button',
  `variant-${props.variant}`,
  `size-${props.size}`,
  {
    block: props.block,
    loading: props.loading,
  }
])

function handleClick(event) {
  if (props.disabled || props.loading) {
    return
  }

  emit('click', event)
}

</script>

<template>
  <button
    :type="props.type"
    :class="buttonClasses"
    :disabled="
      props.disabled
      || props.loading
    "
    @click="handleClick"
  >
    <span
      v-if="$slots.prefix"
      class="button-prefix"
    >
      <slot name="prefix"/>
    </span>

    <span class="button-content">
      <slot/>
    </span>

    <span
      v-if="$slots.suffix"
      class="button-suffix"
    >
      <slot name="suffix"/>
    </span>
  </button>
</template>

<style scoped>
.base-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  gap: var(--space-4);

  min-height: 40px;

  padding: 0 var(--space-5);

  border: 1px solid transparent;

  border-radius: var(--radius-6);

  font-family: var(--font-family-terminal);

  font-size: var(--font-size-md);

  font-weight: var(--font-weight-semibold);

  line-height: 1;

  cursor: pointer;

  user-select: none;

  transition: color var(--transition-fast),
  background var(--transition-fast),
  border-color var(--transition-fast),
  box-shadow var(--transition-fast),
  transform var(--transition-fast),
  opacity var(--transition-fast);
}

.base-button:hover:not(:disabled) {
  transform: translateY(-1px);
}

.base-button:active:not(:disabled) {
  transform: translateY(0);
}

.base-button:disabled {
  cursor: not-allowed;

  opacity: 0.5;
}

.base-button.block {
  width: 100%;
}

.base-button.loading {
  cursor: wait;
}

/*
 * Primary
 */
.variant-primary {
  color: var(--color-terminal-green-button);

  border-color: var(--color-login-button-border);

  background: linear-gradient(
    180deg,
    var(--color-terminal-button-top),
    var(--color-terminal-button-bottom)
  );
}

.variant-primary:hover:not(:disabled) {
  border-color: var(--color-login-button-border-hover);

  box-shadow: var(--shadow-glow-login);
}

/*
 * Secondary
 */
.variant-secondary {
  color: var(--color-terminal-text);

  border-color: var(--color-white-a095);

  background: var(--color-white-a025);
}

.variant-secondary:hover:not(:disabled) {
  color: var(--color-terminal-green-text);

  border-color: var(--color-login-border-hover);

  background: var(--color-terminal-primary-soft);
}

/*
 * Ghost
 */
.variant-ghost {
  color: var(--color-text-soft);

  border-color: transparent;

  background: transparent;
}

.variant-ghost:hover:not(:disabled) {
  color: var(--color-primary);

  background: var(--color-primary-a050);
}

/*
 * Danger
 */
.variant-danger {
  color: var(--color-danger-login);

  border-color: var(--color-danger-action-border);

  background: var(--color-danger-action-soft);
}

.variant-danger:hover:not(:disabled) {
  border-color: var(--color-danger-border);

  background: var(--color-danger-action-soft-strong);
}

/*
 * Size
 */
.size-sm {
  min-height: 32px;

  padding: 0 var(--space-4);

  font-size: var(--font-size-sm);
}

.size-md {
  min-height: 40px;
}

.size-lg {
  min-height: 46px;

  padding: 0 var(--space-7);

  font-size: var(--font-size-lg);
}

.button-prefix,
.button-suffix {
  display: inline-flex;

  align-items: center;

  justify-content: center;
}

.button-content {
  display: inline-flex;

  align-items: center;

  justify-content: center;
}

.google-login-button {
  display: flex;
  width: 100%;
  min-height: 43px;
  align-items: center;
  justify-content: center;
  gap: 9px;
  border: 1px solid var(--color-white-a095);
  border-radius: var(--radius-6);
  color: var(--color-terminal-text);
  background: var(--color-white-a025);
  box-sizing: border-box;
  font-size: 12px;
  font-weight: 600;
  text-decoration: none;
  transition: border-color 0.16s ease,
  background 0.16s ease,
  transform 0.16s ease;

  cursor: pointer;
  font-family: inherit;
}

.google-login-button:disabled {
  cursor: wait;
  opacity: 0.58;
}

.google-login-button:hover {
  border-color: var(--color-login-border-hover);
  background: rgba(
    102,
    255,
    157,
    0.035
  );
  transform: translateY(-1px);
}

@media (prefers-reduced-motion: reduce) {
  .base-button {
    transition: none;
  }
}
</style>
