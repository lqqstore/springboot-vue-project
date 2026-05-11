import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': '/src'
    }
  },
  server: {
    port: 5173,
    proxy: {
      // 前端统一用 /api 前缀转发到后端，避免跨域
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 把 /api 前缀剔除，确保后端控制器路径如 /auth/login 能正确命中
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})

