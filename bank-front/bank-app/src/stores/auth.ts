import { defineStore } from 'pinia'
import { authApi } from '../services/api'

interface UserInfo {
  userId: string;
  username: string;
  name: string;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    isAuthenticated: !!localStorage.getItem('token'),
    userInfo: null as UserInfo | null
  }),

  actions: {
    // 登录
    async login(username: string, password: string) {
      try {
        const response = await authApi.login({ username, password })
        if (response.code === 200) {
          this.token = response.data.token
          this.isAuthenticated = true
          this.userInfo = {
            userId: response.data.userId,
            username: response.data.username,
            name: response.data.name
          }
          localStorage.setItem('token', this.token)
          return { success: true, message: response.message }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '登录失败';
        return { success: false, message: errorMessage }
      }
    },

    // 登出
    async logout() {
      try {
        await authApi.logout()
      } catch (error) {
        console.error('登出接口调用失败', error)
      } finally {
        this.token = ''
        this.isAuthenticated = false
        this.userInfo = null
        localStorage.removeItem('token')
      }
    },

    // 获取用户信息
    async fetchUserInfo() {
      try {
        const response = await authApi.getUserInfo()
        if (response.code === 200) {
          this.userInfo = response.data
          return { success: true, data: response.data }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '获取用户信息失败';
        return { success: false, message: errorMessage }
      }
    },

    // 验证Token
    async verifyToken() {
      try {
        const response = await authApi.verifyToken()
        if (response.code === 200 && response.data) {
          return { success: true }
        } else {
          // Token无效，清除本地状态
          this.logout()
          return { success: false }
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '验证Token失败';
        return { success: false, message: errorMessage }
      }
    },

    // 修改登录密码
    async updatePassword(oldPassword: string, newPassword: string) {
      try {
        const response = await authApi.updatePassword({ oldPassword, newPassword })
        return { success: response.code === 200, message: response.message }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '修改密码失败';
        return { success: false, message: errorMessage }
      }
    },

    // 设置/修改交易密码
    async updateTransactionPassword(password: string) {
      try {
        const response = await authApi.updateTransactionPassword({ password })
        return { success: response.code === 200, message: response.message }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '设置交易密码失败';
        return { success: false, message: errorMessage }
      }
    }
  }
})