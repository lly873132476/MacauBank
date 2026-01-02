import { defineStore } from 'pinia'
import { authApi } from '../services/api'

interface UserInfo {
  userId: string;
  userName: string;
  name: string;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    isAuthenticated: !!localStorage.getItem('token'),
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null') as UserInfo | null,
    // KYC 等级 (0: 未认证, 1: 初级认证, 2: 完整开户)
    kycLevel: parseInt(localStorage.getItem('kycLevel') || '0')
  }),

  getters: {
    isAccountOpened: (state) => state.kycLevel >= 2,
    isLoggedIn: (state) => state.isAuthenticated
  },

  actions: {
    setToken(token: string) {
      this.token = token
      this.isAuthenticated = true
      localStorage.setItem('token', token)
    },

    setUserInfo(info: any) {
      this.userInfo = info
      localStorage.setItem('userInfo', JSON.stringify(info))
      // 如果没有 kycLevel，默认为 0
      if (!localStorage.getItem('kycLevel')) {
        this.kycLevel = 0
        localStorage.setItem('kycLevel', '0')
      }
    },

    completeKyc() {
      this.kycLevel = 2
      localStorage.setItem('kycLevel', '2')
    },

    // 登录
    async login(userName: string, password: string) {
      try {
        const response = await authApi.login({ userName, password })
        if (response.code === 200) {
          this.setToken(response.data.token)
          // 模拟用户信息包含 userNo
          const userInfo = {
            userId: response.data.userNo || response.data.userId, // 兼容后端字段
            userName: response.data.userName,
            name: response.data.name
          }
          this.setUserInfo(userInfo)
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
        this.kycLevel = 0
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('kycLevel')
      }
    },

    // 获取用户信息
    async fetchUserInfo() {
      try {
        const response = await authApi.getUserInfo()
        if (response.code === 200) {
          const profile = response.data
          
          // 合并现有信息与新获取的 Profile
          const updatedInfo = {
            ...this.userInfo, // 保留原有的 userId, userName
            name: profile.realNameCn || profile.name || this.userInfo?.name || '',
            // 可以扩展更多字段
          } as UserInfo

          this.userInfo = updatedInfo
          localStorage.setItem('userInfo', JSON.stringify(updatedInfo))
          
          // 同步后端返回的 KYC Level
          if (profile.kycLevel !== undefined) {
            this.kycLevel = profile.kycLevel
            localStorage.setItem('kycLevel', profile.kycLevel.toString())
          }
          
          return { success: true, data: profile }
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
