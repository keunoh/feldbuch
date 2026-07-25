import {createRouter, createWebHistory} from 'vue-router'

import LoginView from "@/views/LoginView.vue";
import ConversationView from "@/views/ConversationView.vue";

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    component: LoginView
  },
  {
    path: '/conversation',
    component: ConversationView
  }
];

export default createRouter({
  history: createWebHistory(),
  routes
})
