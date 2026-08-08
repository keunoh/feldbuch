<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },

  mono: {
    type: Boolean,
    default: false,
  },
})
</script>

<template>
  <div class="setting-row">
    <span class="setting-key">
      {{ label }}
    </span>

    <span
      class="setting-value"
      :class="{
        mono,
      }"
    >
      <slot/>
    </span>
  </div>
</template>

<style scoped>
.setting-row {
  display: grid;

  grid-template-columns:
    170px
    1fr;

  min-height: 43px;

  align-items: center;

  padding: 0 14px;

  border-bottom: 1px solid var(--color-white-a040);
}

.setting-row:last-child {
  border-bottom: 0;
}

.setting-key {
  color: rgba(105, 255, 165, 0.46);

  font-family: var(--font-family-terminal);

  font-size: 9px;
}

.setting-value {
  overflow: hidden;

  color: var(--color-terminal-text-soft);

  font-size: 11px;

  text-overflow: ellipsis;

  white-space: nowrap;
}

.setting-value.mono {
  font-family: var(--font-family-terminal);

  font-size: 10px;
}

@media (max-width: 650px) {
  .setting-row {
    grid-template-columns:
      1fr;

    gap: 5px;

    padding: var(--space-4) var(--space-5);
  }
}
</style>
