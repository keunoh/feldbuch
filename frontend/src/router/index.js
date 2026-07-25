import {createRouter, createWebHistory} from 'vue-router'

import ConversationView from "@/views/ConversationView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'conversation',
      component: ConversationView
    }
  ],
})

export default router
