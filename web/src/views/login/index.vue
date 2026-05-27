<template>
  <div class="login-wrap">
    <div class="login-bg-shapes">
      <div class="shape shape-1" />
      <div class="shape shape-2" />
      <div class="shape shape-3" />
    </div>

    <div class="login-card">
      <div class="login-brand">
        <div class="brand-icon">
          <el-icon :size="36"><HomeFilled /></el-icon>
        </div>
        <h1 class="brand-title">宿舍管理系统</h1>
        <p class="brand-subtitle">Dormitory Management System</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            type="password"
            show-password
            :prefix-icon="Lock"
            @keyup.enter="onSubmit"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-btn"
            round
            @click="onSubmit"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="login-footer">
      <span>&copy; {{ new Date().getFullYear() }} DMS. All rights reserved.</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { HomeFilled, User, Lock } from '@element-plus/icons-vue'
import request, { type Result } from '@/utils/request'
import { useUserStore, type UserInfo } from '@/stores/user'

interface LoginResponseDTO {
  token: string
  userId: number
  role: string
}

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance | null>(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

onMounted(() => {
  userStore.hydrate()
})

const onSubmit = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      loading.value = true
      const res = await request.post<Result<LoginResponseDTO>>('/auth/login', form)
      const body = res.data
      if (body.code !== 0) {
        ElMessage.error(body.msg || '登录失败')
        return
      }

      const data = body.data
      const userInfo: UserInfo = { userId: data.userId, role: data.role }
      userStore.setLogin({ token: data.token, userInfo })
      ElMessage.success('登录成功')
      router.push('/')
    } catch (e: any) {
      const backend = e?.response?.data as Partial<{ msg: string; code: number; data: any }> | undefined
      if (backend?.msg) {
        ElMessage.error(backend.msg)
      } else {
        ElMessage.error(e?.message || '登录失败')
      }
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-wrap {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #e8e8e8;
  overflow: hidden;
}

.login-bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  background: #fff;
}

.shape-1 {
  width: 500px;
  height: 500px;
  top: -150px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.shape-2 {
  width: 300px;
  height: 300px;
  bottom: -80px;
  left: -60px;
  animation: float 6s ease-in-out infinite reverse;
}

.shape-3 {
  width: 200px;
  height: 200px;
  top: 40%;
  left: 60%;
  animation: float 10s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-20px) scale(1.05); }
}

.login-card {
  position: relative;
  width: 420px;
  padding: 40px 36px 32px;
  background: var(--app-login-card-bg);
  border-radius: 8px;
  box-shadow: var(--app-shadow-login);
  transition: background 0.3s ease;
}

.login-brand {
  text-align: center;
  margin-bottom: 32px;
}

.brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 6px;
  background: var(--app-brand-primary);
  color: #fff;
  margin-bottom: 16px;
}

.brand-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--app-text-primary);
}

.brand-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--app-text-secondary);
  letter-spacing: 1px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
}

.login-footer {
  position: absolute;
  bottom: 16px;
  color: var(--app-text-secondary);
  font-size: 12px;
}


</style>
