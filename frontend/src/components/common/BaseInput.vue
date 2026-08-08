<script setup>

import {computed} from "vue";

const model = defineModel({
  type: [String, Number],
  default: '',
})

const props = defineProps({
  type: {
    type: String,
    default: 'text',
  },

  placeholder: {
    type: String,
    default: '',
  },

  autocomplete: {
    type: String,
    default: undefined,
  },

  disabled: {
    type: Boolean,
    default: false,
  },

  required: {
    type: Boolean,
    default: false,
  },

  error: {
    type: Boolean,
    default: false,
  },

  prefix: {
    type: String,
    default: '❯',
  },
})

const inputClasses = computed(() => [
  'base-input',
  {
    error: props.error,
    disabled: props.disabled,
  },
])
</script>

<template>
  <div :class="inputClasses">
    <span
      v-if="$slots.prefix || props.prefix"
      class="input-prefix"
      aria-hidden="true"
    >
      <slot name="prefix">
        {{ props.prefix }}
      </slot>
    </span>

    <input
      v-model="model"
      :type="props.type"
      :placeholder="props.placeholder"
      :autocomplete="props.autocomplete"
      :disabled="props.disabled"
      :required="props.required"
    />

    <span
      v-if="$slots.suffix"
      class="input-suffix"
    >
      <slot name="suffix"/>
    </span>
  </div>
</template>

<style scoped>
.base-input {
  display: flex;
  align-items: center;

  width: 100%;
  min-height: 43px;

  overflow: hidden;

  border: 1px solid var(--color-login-border);

  border-radius: var(--radius-6);

  background: var(--color-terminal-bg-2);

  transition: border-color var(--transition-fast),
  box-shadow var(--transition-fast),
  background var(--transition-fast);
}

.base-input:hover:not(.disabled) {
  border-color: var(--color-login-border-hover);
}

.base-input:focus-within:not(.disabled) {
  border-color: var(--color-login-border-focus);

  background: var(--color-terminal-bg-3);

  box-shadow: var(--shadow-focus-login),
  0 0 18px var(--color-primary-a040);
}

.base-input.error {
  border-color: var(--color-danger-login-border);
}

.base-input.disabled {
  cursor: wait;
  opacity: 0.65;
}

.input-prefix,
.input-suffix {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  flex-shrink: 0;

  color: var(--color-login-prefix);

  font-family: var(--font-family-terminal);

  font-size: var(--font-size-sm);

  user-select: none;
}

.input-prefix {
  padding-left: var(--space-5);
}

.input-suffix {
  padding-right: var(--space-5);
}

input {
  width: 100%;
  min-width: 0;

  padding: 11px var(--space-5) 11px var(--space-3);

  border: 0;

  outline: none;

  color: var(--color-terminal-text);

  background: transparent;

  caret-color: var(--color-login-caret);

  font-family: var(--font-family-terminal);

  font-size: var(--font-size-md);
}

input::placeholder {
  color: rgba(189, 210, 196, 0.25);
}

input:disabled {
  cursor: wait;
}

/*
 * Chrome / Safari 자동완성 배경 제거
 */
input:-webkit-autofill,
input:-webkit-autofill:hover,
input:-webkit-autofill:focus,
input:-webkit-autofill:active {
  -webkit-text-fill-color: var(--color-terminal-text) !important;

  caret-color: var(--color-login-caret);

  box-shadow: var(--shadow-autofill) !important;

  -webkit-box-shadow: var(--shadow-autofill) !important;

  transition: background-color 9999s ease-out 0s;
}
</style>
