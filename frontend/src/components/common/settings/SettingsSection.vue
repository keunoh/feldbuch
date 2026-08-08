<script setup>
defineProps({
  index: {
    type: String,
    required: true,
  },

  title: {
    type: String,
    required: true,
  },

  description: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <section class="settings-section">
    <header class="section-header">
      <span class="section-index">
        {{ index }}
      </span>

      <div>
        <h2>
          {{ title }}
        </h2>

        <p
          v-if="description"
        >
          {{ description }}
        </p>
      </div>
    </header>

    <slot/>
  </section>
</template>

<style scoped>
.settings-section {
  padding: 27px 0;

  border-bottom: 1px solid var(--color-white-a050);
}

.section-header {
  display: flex;

  gap: 13px;

  margin-bottom: 19px;
}

.section-index {
  padding-top: 3px;

  color: var(--color-terminal-primary-muted);

  font-family: var(--font-family-terminal);

  font-size: 9px;
}

.section-header h2 {
  margin: 0;

  color: var(--color-text);

  font-size: 15px;
}

.section-header p {
  margin: 4px 0 0;

  color: var(--color-text-muted);

  font-size: 10px;
}
</style>
