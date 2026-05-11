<template>
  <el-container class="dms-layout" style="min-height: 100vh;">
    <el-aside width="220px">
      <div class="aside-title">DMS 宿舍管理</div>
      <el-menu
        router
        :default-active="activePath"
        class="aside-menu"
      >
        <el-sub-menu index="/dorm">
          <template #title>宿舍管理</template>
          <el-menu-item index="/dorm/buildings">楼栋管理</el-menu-item>
          <el-menu-item index="/dorm/rooms">房间管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/notice">公告管理</el-menu-item>
        <el-menu-item index="/repair">报修管理</el-menu-item>
        <el-menu-item index="/student">学生管理</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="dms-header">
        <div class="header-left">
          <span style="font-weight: 600;">系统后台</span>
        </div>
        <div class="header-right">
          <el-button link type="primary" @click="onLogout" v-if="isLogin">
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activePath = computed(() => route.path)
const isLogin = computed(() => userStore.isLogin)

const onLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.dms-layout {
  background: #f6f7fb;
}

.aside-title {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #fff;
  background: #409eff;
}

.aside-menu {
  border-right: none;
  background: transparent;
}

.dms-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eef1f6;
}

.dms-main {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  margin: 16px;
}
</style>

