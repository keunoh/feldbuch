<script setup>
import BaseStatusDot from '@/components/common/BaseStatusDot.vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'success',
  },

  pulse: {
    type: Boolean,
    default: false,
  },

  dotSize: {
    type: String,
    default: 'md',
  },

  mono: {
    type: Boolean,
    default: true,
  },
})
</script>

<template>
  <span
    class="base-status-label"
    :class="{
      mono: props.mono,
    }"
  >
    <BaseStatusDot
      :variant="props.variant"
      :pulse="props.pulse"
      :size="props.dotSize"
    />

    <span class="status-content">
      <slot/>
    </span>
  </span>
</template>

<style scoped>
.base-status-label {
  display: inline-flex;
  align-items: center;

  gap: 7px;

  color: var(--color-terminal-primary-text-soft);

  font-size: var(--font-size-sm);
}

.base-status-label.mono {
  font-family: var(--font-family-terminal);
}

.status-content {
  min-width: 0;
}
</style>
