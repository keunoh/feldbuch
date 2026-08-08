<template>
  <div class="settings-list">
    <slot/>
  </div>
</template>

<style scoped>
.settings-list {
  overflow: hidden;

  border: 1px solid var(--color-white-a055);

  border-radius: var(--radius-7);

  background: var(--color-white-a012);
}
</style>
