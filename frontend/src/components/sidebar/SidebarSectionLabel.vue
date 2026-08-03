<template>
  <p class="section-label">
    <slot/>
  </p>
</template>

<style scoped>
.section-label {
  margin: 0 0 20px;
  color: var(--color-text-muted);
  font-family: "JetBrains Mono",
  "SFMono-Regular",
  Consolas,
  monospace;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}
</style>
