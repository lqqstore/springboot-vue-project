<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="login-title">宿舍管理系统</div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        size="large"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            type="password"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%;"
            @click="onSubmit"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
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
  // 确保刷新后能恢复登录态
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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f6f7fb;
}

.login-card {
  width: 420px;
  padding: 26px 24px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.login-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 20px;
  text-align: center;
}
</style>

