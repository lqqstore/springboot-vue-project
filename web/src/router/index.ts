import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'
import LoginView from '@/views/login/index.vue'
import DormRoomsView from '@/views/dorm/rooms/index.vue'
import DormBuildingsView from '@/views/dorm/buildings/index.vue'
import NoticeView from '@/views/notice/index.vue'
import RepairView from '@/views/repair/index.vue'
import StudentView from '@/views/student/index.vue'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dorm/rooms'
      },
      {
        path: '/dorm/rooms',
        name: 'dormRooms',
        component: DormRoomsView
      },
      {
        path: '/dorm/buildings',
        name: 'dormBuildings',
        component: DormBuildingsView
      },
      {
        path: '/notice',
        name: 'notice',
        component: NoticeView
      },
      {
        path: '/repair',
        name: 'repair',
        component: RepairView
      },
      {
        path: '/student',
        name: 'student',
        component: StudentView
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  // 确保 store 与 localStorage 同步
  userStore.hydrate()

  // 已登录时不允许重复访问登录页
  if (to.path === '/login') {
    return userStore.isLogin ? '/' : undefined
  }

  const requiresAuth = Boolean(to.meta.requiresAuth)
  if (requiresAuth && !userStore.isLogin) {
    return '/login'
  }

  return undefined
})

export default router

