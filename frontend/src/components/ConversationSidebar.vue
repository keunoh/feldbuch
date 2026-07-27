<script setup>
defineProps({
  conversations: {
    type: Array,
    required: true
  }
});
</script>

<template>
  <aside class="sidebar">
    <h2>대화 목록</h2>

    <ul>
      <li
        v-for="conversation in conversations"
        :key="conversation.id"
      >
        {{ conversation.title }}
      </li>
    </ul>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 260px;
  border-right: 1px solid #ddd;
  padding: 16px;
}

ul {
  list-style: none;
  padding: 0;
}

li {
  padding: 8px 0;
}
</style>
