import {createRouter, createWebHistory} from 'vue-router'

import {isAuthenticated} from "@/utils/auth.js";

import LoginView from "@/views/LoginView.vue";
import ConversationView from "@/views/ConversationView.vue";
import OAuth2SuccessView from "@/views/OAuth2SuccessView.vue";

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
    path: '/oauth2/success',
    component: OAuth2SuccessView
  },
  {
    path: '/conversations',
    component: ConversationView,
    meta: {
      requiresAuth: true
    }
  }
];

// 1. Router 객체 생성
export const router = createRouter({
  history: createWebHistory(),
  routes
});

// 2. 모든 페이지 이동 전에 실행되는 Router Guard
router.beforeEach((to, from, next) => {

  // 인증이 필요한 페이지인데 토큰이 없으면 로그인 페이지로 이동
  if (to.meta.requiresAuth && !isAuthenticated()) {
    next('/login');
    return;
  }

  // 그 외에는 정상 이동
  next();
});

// 3. Router 내보내기
export default router;
