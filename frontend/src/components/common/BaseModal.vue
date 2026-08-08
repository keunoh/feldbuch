<script setup>
const props = defineProps({
  open: {
    type: Boolean,
    default: true,
  },

  ariaLabel: {
    type: String,
    default: 'Dialog',
  },

  closeOnOverlay: {
    type: Boolean,
    default: true,
  },

  maxWidth: {
    type: String,
    default: '760px',
  },
})

const emit = defineEmits([
  'close',
])

function handleOverlayClick() {
  if (!props.closeOnOverlay) {
    return
  }

  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <Transition name="base-modal">
      <div
        v-if="props.open"
        class="base-modal-overlay"
        @click.self="handleOverlayClick"
      >
        <section
          class="base-modal"
          role="dialog"
          aria-modal="true"
          :aria-label="props.ariaLabel"
          :style="{
            '--base-modal-max-width': props.maxWidth,
          }"
        >
          <slot/>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.base-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;

  display: grid;
  place-items: center;

  padding: var(--space-10);

  box-sizing: border-box;

  background: var(--color-terminal-overlay);

  backdrop-filter: blur(7px);
}

.base-modal {
  width: min(
    100%,
    var(--base-modal-max-width)
  );

  max-height: min(
    86vh,
    820px
  );

  overflow-y: auto;

  border: 1px solid var(--color-terminal-border);

  border-radius: var(--radius-10);

  color: var(--color-text);

  background: var(--color-terminal-surface-raised);

  box-shadow: var(--shadow-modal-strong);
}

.base-modal-enter-active,
.base-modal-leave-active {
  transition: opacity 0.18s ease;
}

.base-modal-enter-active
.base-modal,
.base-modal-leave-active
.base-modal {
  transition: transform 0.18s ease,
  opacity 0.18s ease;
}

.base-modal-enter-from,
.base-modal-leave-to {
  opacity: 0;
}

.base-modal-enter-from
.base-modal,
.base-modal-leave-to
.base-modal {
  opacity: 0;

  transform: translateY(9px) scale(0.985);
}

@media (max-width: 650px) {
  .base-modal-overlay {
    padding: var(--space-5);
  }

  .base-modal {
    max-height: 92vh;
  }
}

@media (prefers-reduced-motion: reduce) {
  .base-modal-enter-active,
  .base-modal-leave-active,
  .base-modal-enter-active
  .base-modal,
  .base-modal-leave-active
  .base-modal {
    transition: none;
  }
}
</style>
