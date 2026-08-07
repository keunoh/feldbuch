<script setup>
defineProps({
  mode: {
    type: String,
    default: 'conversation',
    validator(value) {
      return [
        'conversation',
        'knowledge',
      ].includes(value);
    }
  }
})

const emit = defineEmits([
  'change-mode'
]);

function selectMode(mode) {
  emit(
    'change-mode',
    mode
  )
}

</script>

<template>
  <nav
    class="sidebar-tabs"
    aria-label="Workspace 메뉴"
  >
    <button
      type="button"
      class="tab-button"
      :class="{
        active: mode === 'conversation',
      }"
      @click="selectMode('conversation')"
    >
      대화
    </button>

    <button
      type="button"
      class="tab-button"
      :class="{
        active: mode === 'knowledge',
      }"
      @click="selectMode('knowledge')"
    >
      지식
    </button>
  </nav>
</template>

<style scoped>
.sidebar-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);

  padding: 0 var(--sidebar-padding-x) var(--space-7);

  border-bottom: 1px solid var(--color-border-soft);
}

.tab-button {
  min-height: 42px;
  padding: 9px var(--space-5);
  border: 1px solid transparent;
  border-radius: var(--radius-9);
  color: var(--color-text-muted);
  background: transparent;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: color var(--transition-fast),
  border-color var(--transition-fast),
  background var(--transition-fast);
}

.tab-button:hover {
  color: var(--color-text);
  background: var(--color-surface-raised);
}

.tab-button.active {
  color: var(--color-primary);
  border-color: var(--color-border-primary);
  background: var(--color-primary-soft);
}
</style>
