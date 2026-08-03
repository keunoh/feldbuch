<script setup>

import {computed} from "vue";

const props = defineProps({
  text: {
    type: String,
    default: '',
  },
  keyword: {
    type: String,
    default: '',
  }
})

function escapeRegExp(value) {
  return value.replace(
    /[.*+?^${}()|[\]\\]/g,
    '\\$&'
  )
}

const segments = computed(() => {
  const text = props.text ?? ''
  const keyword = props.keyword.trim()

  if (!keyword) {
    return [
      {
        text,
        matched: false
      }
    ]
  }

  const pattern = new RegExp(
    `(${escapeRegExp(keyword)})`,
    'gi'
  )

  return text
    .split(pattern)
    .filter(Boolean)
    .map(segment => ({
      text: segment,
      matched:
        segment.toLowerCase() === keyword.toLowerCase()
    }))
})
</script>

<template>
  <span>
    <template
      v-for="(segment, index) in segments"
      :key="`${index}-${segment.text}`"
    >
      <mark
        v-if="segment.matched"
        class="search-highlight"
      >
        {{ segment.text }}
      </mark>

      <span v-else>
        {{ segment.text }}
      </span>
    </template>
  </span>
</template>

<style scoped>
.search-highlight {
  padding: 0 2px;
  border-radius: 3px;
  color: var(--color-text);
  background: rgba(255, 214, 10, 0.35);
}
</style>
