<script setup>
defineProps({
  variant: {
    type: String,
    default: 'default',
  },
})
</script>

<template>
  <section
    class="base-card"
    :class="`base-card--${variant}`"
  >
    <slot/>
  </section>
</template>

<style scoped>
.base-card {
  position: relative;

  overflow: hidden;

  border-radius: var(--radius-10);

  background: var(--color-surface-panel);

  box-shadow: var(--shadow-panel);
}

.base-card::before {
  content: "";

  position: absolute;
  inset: 0;

  pointer-events: none;

  background: linear-gradient(
    180deg,
    var(--color-white-a025),
    transparent 18%
  );
}

.base-card--terminal {
  border: 1px solid var(--color-login-border);
  background: var(--color-login-panel);
}
</style>
