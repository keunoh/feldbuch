<template>
  <p class="section-label">
    <slot/>
  </p>
</template>

<style scoped>
.section-label {
  margin: 0 0 var(--space-9);
  color: var(--color-text-muted);
  font-family: var(--font-family-terminal);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}
</style>
