<script setup>
defineProps({
  conversations: {
    type: Array,
    required: true
  },
  selectedConversationId: {
    type: Number,
    default: null
  }
});

const emit = defineEmits(['select']);
</script>

<template>
  <aside class="conversation-sidebar">
    <h2>대화 목록</h2>

    <ul>
      <li
        v-for="conversation in conversations"
        :key="conversation.id"
      >
        <button
          type="button"
          :class="{
            selected: conversation.id === selectedConversationId
          }"
          @click="emit('select', conversation.id)"
        >
          {{ conversation.title }}
        </button>
      </li>
    </ul>
  </aside>
</template>

<style scoped>
ul {
  padding: 0;
  list-style: none;
}

button {
  padding: 8px 0;
  border: none;
  background: none;
  cursor: pointer;
  text-align: left;
}

button.selected {
  font-weight: bold;
  text-decoration: underline;
}
</style>
