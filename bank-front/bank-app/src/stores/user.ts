import { defineStore } from 'pinia'
import { authApi, messageApi, userApi, type UserCertificationRequest } from '../services/api'

interface UserProfile {
  userId: string;
  userName: string;
  name: string;
  nameCn?: string; // 中文名
  nameEn?: string; // 英文名
  mobile: string;
  idCard: string;
  expiryDate?: string; // 证件有效期
  issueCountry?: string; // 发证地
  issueOrg?: string; // 发证机关
  status: number;
  hasTransactionPassword: boolean;
  createTime: Date;
  employmentStatus?: string; // 就业状态
  occupation?: string; // 职业
  addressRegion?: string; // 地址区域
  addressDetail?: string; // 详细地址
  taxId?: string; // 税务编号
  sourceOfFunds?: string; // 资金来源
  openPurpose?: string; // 开户目的
  kycLevelDesc?: string; // KYC等级描述
  kycLevel?: number; // KYC等级数值
  nationality?: string; // 国籍
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
    transactionMessages: [] as Message[],
    unreadCount: 0
  }),

  actions: {
    // 获取未读消息数量
    async fetchUnreadCount() {
      try {
        const response = await messageApi.getUnreadCount()
        if (response.code === 200) {
          this.unreadCount = response.data
          return { success: true, data: response.data }
        }
        return { success: false, message: response.message }
      } catch (error) {
        return { success: false, message: '获取未读消息数失败' }
      }
    },

    // 标记消息为已读
    async markAsRead(messageId: string) {
      try {
        const response = await messageApi.markAsRead(messageId)
        if (response.code === 200) {
          // 更新本地状态
          const msg = [...this.messages, ...this.transactionMessages].find(m => m.id === messageId)
          if (msg && !msg.read) {
            msg.read = true
            if (this.unreadCount > 0) this.unreadCount--
          }
          return { success: true }
        }
        return { success: false, message: response.message }
      } catch (error) {
        return { success: false, message: '标记已读失败' }
      }
    },
    // 提交开户申请
    async submitOpenAccount(form: any) {
      try {
        console.log('开始提交开户申请...', form)
        
        // 1. 模拟上传证件照 (后端暂未实现，暂用占位字符串代替)
        const frontUrl = '/oss/mock/idcard_front.jpg'
        const backUrl = '/oss/mock/idcard_back.jpg'
        
        // 2. 构造提交数据 (映射到 UserCertificationRequest)
        // 注意：由于演示Demo表单字段有限，部分必填字段使用默认值模拟，实际应由OCR获取
        const payload: UserCertificationRequest = {
          realNameCn: form.nameCn,
          realNameEn: form.nameEn,
          idCardType: form.idCardType || 1, 
          idCardNo: form.idCardNo,
          idCardExpiry: form.expiryDate,
          idCardIssueCountry: form.issueCountry,
          idCardIssueOrg: form.issueOrg,
          idCardImgFront: frontUrl,
          idCardImgBack: backUrl,
          gender: form.gender, 
          birthday: form.birthday || '1990-01-01', 
          nationality: 'Macau', // 默认
          occupation: form.occupation,
          employmentStatus: parseInt(form.employmentStatus || '1'),
          taxId: form.taxId || undefined,
          addressRegion: form.addressRegion,
          addressDetail: form.addressDetail
        }
        
        // 3. 提交KYC
        const response = await userApi.submitKyc(payload)
        
        if (response.code === 200) {
          return { success: true, message: response.message }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        console.error('开户申请异常:', error)
        const errorMessage = error instanceof Error ? error.message : '提交失败，请重试';
        return { success: false, message: errorMessage }
      }
    },

    // 获取用户信息
    async fetchProfile() {
      try {
        console.log('开始获取用户信息...')
        const response = await authApi.getUserInfo()
        console.log('用户信息响应:', response)
        if (response.code === 200) {
          // 这里假设后端返回的数据可能已经包含了这些扩展字段，或者将来会包含
          // 前端先做好类型定义和映射准备
          this.profile = {
            ...response.data,
            // 如果后端返回的字段名不一致，可以在这里手动映射，例如：
            // nameCn: response.data.nameCn || response.data.name,
          } as unknown as UserProfile
          return { success: true, data: this.profile }
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

    // 获取消息中心数据
    async fetchMessages() {
      try {
        const response = await messageApi.getMessagePage(1, 100)
        console.log('[Debug] fetchMessages response:', response) // Debug Log
        
        if (response.code === 200) {
          // 兼容 records 或 list 字段
          const records = (response.data as any).records || (response.data as any).list || []
          
          const allMessages = records.map((m: any) => ({
            id: m.messageId,
            title: m.title,
            content: m.content,
            time: m.createTime,
            read: m.isRead === 1,
            type: m.type
          }))
          
          // 动账通知 (Category 1)
          this.transactionMessages = allMessages.filter((m: any) => m.type === 1)
          // 系统消息 (Category 2, 3)
          this.messages = allMessages.filter((m: any) => m.type === 2 || m.type === 3)
          
          return { success: true, data: this.messages }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '获取消息失败';
        return { success: false, message: errorMessage }
      }
    },

    // 获取交易提醒 (复用 fetchMessages，保持接口兼容)
    async fetchTransactionMessages() {
      // 始终确保数据是最新的，如果为空则拉取
      if (this.transactionMessages.length === 0) {
        await this.fetchMessages()
      }
      // 显式返回 transactionMessages
      return { success: true, data: this.transactionMessages }
    }
  }
})
