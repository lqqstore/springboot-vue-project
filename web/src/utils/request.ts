import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'

export interface Result<T> {
  code: number
  msg: string
  data: T
}

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 自动携带 Token
request.interceptors.request.use((config: AxiosRequestConfig) => {
  const token = localStorage.getItem('dms_token')
  if (token) {
    config.headers = config.headers || {}
    // Spring Boot 使用 Authorization 头
    ;(config.headers as Record<string, string>)['Authorization'] = token
  }
  return config
})

// 统一的错误处理（最基础版，后续可扩展为提示/重试等）
request.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      // 未登录/Token 失效：清理本地状态由路由守卫接管
      localStorage.removeItem('dms_token')
      localStorage.removeItem('dms_user')
      if (window.location.pathname !== '/login') {
        window.location.replace('/login')
      }
    }
    return Promise.reject(err)
  }
)

export default request

