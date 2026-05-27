<template>
  <el-container class="dms-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="dms-aside">
      <div class="aside-brand">
        <div class="brand-logo">
          <el-icon :size="isCollapse ? 24 : 28"><HomeFilled /></el-icon>
        </div>
        <transition name="fade">
          <span v-show="!isCollapse" class="brand-text">DMS 宿舍管理</span>
        </transition>
      </div>

      <el-menu
        router
        :default-active="activePath"
        :collapse="isCollapse"
        :collapse-transition="false"
        class="aside-menu"
      >
        <el-sub-menu index="/dorm">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>宿舍管理</span>
          </template>
          <el-menu-item index="/dorm/buildings">
            <el-icon><OfficeBuilding /></el-icon>
            <span>楼栋管理</span>
          </el-menu-item>
          <el-menu-item index="/dorm/rooms">
            <el-icon><House /></el-icon>
            <span>房间管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/notice">
          <el-icon><Notification /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
        <el-menu-item index="/repair">
          <el-icon><Tools /></el-icon>
          <span>报修管理</span>
        </el-menu-item>
        <el-menu-item index="/student">
          <el-icon><UserFilled /></el-icon>
          <span>学生管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="dms-right">
      <el-header class="dms-header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="20"
            @click="isCollapse = !isCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumbParent">{{ breadcrumbParent }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ breadcrumbCurrent }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-avatar :size="32" :icon="UserFilled" />
          <span class="user-role">{{ roleText }}</span>
          <el-divider direction="vertical" />
          <el-button link type="danger" @click="onLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </el-header>

      <el-main class="dms-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  HomeFilled, OfficeBuilding, House,
  Notification, Tools, UserFilled, Fold, Expand, SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activePath = computed(() => route.path)

const breadcrumbMap: Record<string, { parent?: string; current: string }> = {
  '/dorm/buildings': { parent: '宿舍管理', current: '楼栋管理' },
  '/dorm/rooms': { parent: '宿舍管理', current: '房间管理' },
  '/notice': { current: '公告管理' },
  '/repair': { current: '报修管理' },
  '/student': { current: '学生管理' }
}

const breadcrumbParent = computed(() => breadcrumbMap[route.path]?.parent || '')
const breadcrumbCurrent = computed(() => breadcrumbMap[route.path]?.current || '')

const roleMap: Record<string, string> = { admin: '管理员', dorm_admin: '宿管' }
const roleText = computed(() => {
  const role = userStore.userInfo?.role || ''
  return roleMap[role] || role || '用户'
})

const onLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.dms-layout {
  height: 100vh;
  background: var(--app-bg);
}

.dms-aside {
  background: linear-gradient(180deg, var(--app-sidebar-bg-start) 0%, var(--app-sidebar-bg-end) 100%);
  overflow: hidden;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.aside-brand {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid var(--app-sidebar-divider);
  padding: 0 16px;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 4px;
  background: var(--app-brand-primary);
  color: #fff;
  flex-shrink: 0;
}

.brand-text {
  font-size: 15px;
  font-weight: 700;
  color: var(--app-text-primary);
  white-space: nowrap;
  overflow: hidden;
}

.aside-menu {
  border-right: none !important;
  background: transparent !important;
}
.aside-menu :deep(.el-menu-item),
.aside-menu :deep(.el-sub-menu__title) {
  color: var(--app-sidebar-text) !important;
  border-radius: 8px;
  margin: 2px 8px;
  transition: all 0.25s ease;
}
.aside-menu :deep(.el-menu-item:hover),
.aside-menu :deep(.el-sub-menu__title:hover) {
  color: var(--app-sidebar-text-hover) !important;
  background: var(--app-sidebar-hover-bg) !important;
}
.aside-menu :deep(.el-menu-item.is-active) {
  color: var(--app-sidebar-text-hover) !important;
  background: var(--app-brand-gradient-h) !important;
  border-right: 3px solid var(--app-brand-primary);
  border-radius: 0 6px 6px 0;
  margin-right: 4px;
}
.aside-menu :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  color: var(--app-sidebar-text-hover) !important;
}

.dms-right {
  flex-direction: column;
}

.dms-header {
  height: 56px;
  background: var(--app-bg-header);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: var(--app-shadow-header);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  cursor: pointer;
  color: var(--app-text-regular);
  transition: color 0.2s, transform 0.2s;
}
.collapse-btn:hover {
  color: var(--app-brand-primary);
  transform: scale(1.15);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-role {
  font-size: 13px;
  color: var(--app-text-secondary);
}

.dms-main {
  background: var(--app-bg);
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
