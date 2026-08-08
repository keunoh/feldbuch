<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },

  showControls: {
    type: Boolean,
    default: true,
  },
})
</script>

<template>
  <header class="terminal-header">
    <div class="terminal-toolbar">
      <div
        v-if="showControls"
        class="terminal-dots"
        aria-hidden="true"
      >
        <span class="dot red"/>
        <span class="dot yellow"/>
        <span class="dot green"/>
      </div>

      <span
        v-else
        class="toolbar-spacer"
        aria-hidden="true"
      />

      <span class="terminal-title">
        {{ title }}
      </span>

      <div class="terminal-actions">
        <slot name="actions"/>
      </div>
    </div>

    <div
      v-if="$slots.default"
      class="terminal-content"
    >
      <slot/>
    </div>
  </header>
</template>

<style scoped>
.terminal-header {
  border-bottom: 1px solid var(--color-login-section-border);

  background: var(--color-terminal-surface);
}

.terminal-toolbar {
  display: grid;

  grid-template-columns:
    64px
    1fr
    64px;

  align-items: center;

  min-height: 40px;

  padding: 0 14px;

  border-bottom: 1px solid var(--color-white-a035);
}

.terminal-dots {
  display: flex;

  gap: 7px;
}

.dot {
  width: 9px;

  height: 9px;

  border-radius: var(--radius-round);

  opacity: 0.92;
}

.dot.red {
  background: var(--color-terminal-red);
}

.dot.yellow {
  background: var(--color-terminal-yellow);
}

.dot.green {
  background: var(--color-terminal-green);
}

.terminal-title {
  overflow: hidden;

  color: var(--color-terminal-text-muted);

  font-family: var(--font-family-terminal);

  font-size: var(--font-size-sm);

  text-align: center;

  text-overflow: ellipsis;

  white-space: nowrap;
}

.terminal-actions {
  display: flex;

  align-items: center;

  justify-content: flex-end;
}

.terminal-content {
  font-family: var(--font-family-terminal);
}

@media (max-width: 520px) {
  .terminal-toolbar {
    grid-template-columns:
      54px
      1fr
      54px;

    padding: 0 11px;
  }
}
</style>
