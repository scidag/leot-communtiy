import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/users',
    name: 'UserManagement',
    component: () => import('@/views/admin/UserManagement.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  // 八股文模块路由
  {
    path: '/bagu',
    name: 'BaguHome',
    component: () => import('@/views/bagu/BaguHome.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/bagu/bank/:id',
    name: 'BankDetail',
    component: () => import('@/views/bagu/BankDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/bagu/question/:id',
    name: 'QuestionDetail',
    component: () => import('@/views/bagu/QuestionDetail.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/bagu/search',
    name: 'QuestionSearch',
    component: () => import('@/views/bagu/QuestionSearch.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/bagu/favourites',
    name: 'FavoriteList',
    component: () => import('@/views/bagu/FavoriteList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/bagu',
    name: 'BaguManage',
    component: () => import('@/views/admin/BaguManage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/',
    redirect: '/bagu'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  // 如果 token 存在但用户信息不存在，尝试恢复
  if (token && !userStore.user) {
    userStore.restoreUser()
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin()) {
    next('/dashboard')
    return
  }

  // 如果已登录，访问登录/注册页时重定向到仪表盘
  if ((to.path === '/login' || to.path === '/register') && token) {
    next('/dashboard')
    return
  }

  next()
})

export default router

