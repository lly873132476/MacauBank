import { defineStore } from 'pinia'
import { authApi, messageApi } from '../services/api'

interface UserProfile {
  userId: string;
  username: string;
  name: string;
  mobile: string;
  idCard: string;
  status: number;
  hasTransactionPassword: boolean;
  createTime: Date;
}

interface Message {
  id: string;
  title: string;
  content: string;
  time: string;
  read?: boolean;
}

export const useUserStore = defineStore('user', {
  state: () => ({
    profile: null as UserProfile | null,
    messages: [] as Message[],
    transactionMessages: [] as Message[]
  }),

  actions: {
    // 获取用户信息
    async fetchProfile() {
      try {
        console.log('开始获取用户信息...')
        const response = await authApi.getUserInfo()
        console.log('用户信息响应:', response)
        if (response.code === 200) {
          this.profile = response.data
          return { success: true, data: response.data }
        } else {
          console.error('获取用户信息失败:', response.message)
          return { success: false, code: response.code, message: response.message }
        }
      } catch (error) {
        console.error('获取用户信息异常:', error)
        const errorMessage = error instanceof Error ? error.message : '获取用户信息失败';
        return { success: false, code: 500, message: errorMessage }
      }
    },

    // 更新用户信息
    async updateProfile(data: Partial<UserProfile>) {
      try {
        // TODO: 后端需要实现用户信息更新接口
        // const response = await authApi.updateUserInfo(data)
        // if (response.code === 200) {
        //   this.profile = response.data
        //   return { success: true, message: response.message }
        // } else {
        //   return { success: false, message: response.message }
        // }
        return { success: false, message: '该功能暂未实现' }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '更新用户信息失败';
        return { success: false, message: errorMessage }
      }
    },

    // 获取系统消息
    async fetchMessages() {
      try {
        const response = await messageApi.getMessages('system')
        if (response.code === 200) {
          this.messages = response.data
          return { success: true, data: response.data }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '获取消息失败';
        return { success: false, message: errorMessage }
      }
    },

    // 获取交易提醒
    async fetchTransactionMessages() {
      try {
        const response = await messageApi.getMessages('transaction')
        if (response.code === 200) {
          this.transactionMessages = response.data
          return { success: true, data: response.data }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '获取交易提醒失败';
        return { success: false, message: errorMessage }
      }
    }
  }
})