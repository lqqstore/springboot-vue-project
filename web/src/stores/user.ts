import { defineStore } from 'pinia'

export interface UserInfo {
  userId: number
  role: string
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('dms_token') || '',
    userInfo: (() => {
      const userRaw = localStorage.getItem('dms_user')
      if (!userRaw) return null
      try {
        return JSON.parse(userRaw) as UserInfo
      } catch {
        return null
      }
    })()
  }),
  actions: {
    hydrate() {
      const userRaw = localStorage.getItem('dms_user')
      this.token = localStorage.getItem('dms_token') || ''
      this.userInfo = userRaw ? (JSON.parse(userRaw) as UserInfo) : null
    },
    setLogin(payload: { token: string; userInfo: UserInfo }) {
      this.token = payload.token
      this.userInfo = payload.userInfo
      localStorage.setItem('dms_token', payload.token)
      localStorage.setItem('dms_user', JSON.stringify(payload.userInfo))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('dms_token')
      localStorage.removeItem('dms_user')
    }
  },
  getters: {
    isLogin: (state) => Boolean(state.token)
  }
})

