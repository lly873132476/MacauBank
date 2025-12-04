import { defineStore } from 'pinia'
import { accountApi, currencyApi, transferApi } from '../services/api'

interface AccountInfo {
  id: number;
  currency?: string;
  currencyCode?: string;
  name?: string;
  balance: number;
}

interface AccountSummary {
  totalMopValue: number;
  accounts: AccountInfo[];
}

interface TransactionRecord {
  id: number;
  date: string;
  description: string;
  amount: number;
  currency: string;
  mopValue: number;
}

interface Recipient {
  id: number;
  name: string;
  account: string;
  currency: string;
}

interface TransferRecord {
  id: number;
  txnId: string; // 交易流水号
  payerAccountId: number; // 付款账户ID
  payeeAccountNo: string; // 收款账户号
  amount: number; // 金额
  currencyCode: string; // 货币代码
  fee: number; // 手续费
  status: string; // 状态
  transferType: string; // 转账类型
  transferChannel: string; // 转账通道
  createTime: string; // 创建时间
}

export const useAccountStore = defineStore('account', {
  state: () => ({
    accounts: [] as AccountInfo[],
    summary: null as AccountSummary | null,
    transactions: [] as TransactionRecord[],
    recipients: [] as Recipient[],
    transferRecords: [] as TransferRecord[], // 添加转账记录状态
    currencies: ['MOP', 'HKD', 'CNY', 'USD'] // 支持的币种
  }),

  actions: {
    // 获取资产总览
    async fetchSummary() {
      try {
        // Mock data for static preview
        this.summary = {
          totalMopValue: 128500.50,
          accounts: [
            { id: 1, currencyCode: 'MOP', balance: 50000.00 },
            { id: 2, currencyCode: 'HKD', balance: 25000.00 },
            { id: 3, currencyCode: 'CNY', balance: 10000.00 },
            { id: 4, currencyCode: 'USD', balance: 5000.00 }
          ]
        }
        return { success: true, data: this.summary }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '获取资产总览失败';
        return { success: false, message: errorMessage }
      }
    },

    // 获取账户列表
    async fetchAccounts() {
      try {
        // Mock data for static preview
        this.accounts = [
          { id: 1, currencyCode: 'MOP', name: '储蓄账户', balance: 50000.00 },
          { id: 2, currencyCode: 'HKD', name: '往来账户', balance: 25000.00 },
          { id: 3, currencyCode: 'CNY', name: '人民币账户', balance: 10000.00 },
          { id: 4, currencyCode: 'USD', name: '美元账户', balance: 5000.00 }
        ]
        return { success: true, data: this.accounts }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '获取账户列表失败';
        return { success: false, message: errorMessage }
      }
    },

    // 获取交易流水
    async fetchTransactions(accountId: string, currencyCode: string) {
      try {
        const response = await accountApi.getTransactionRecords(accountId, currencyCode)
        if (response.code === 200) {
          this.transactions = response.data
          return { success: true, data: response.data }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '获取交易流水失败';
        return { success: false, message: errorMessage }
      }
    },

    // 获取转账记录
    async fetchTransferRecords(accountId?: string, currencyCode?: string, status?: string) {
      try {
        const response = await transferApi.getTransferRecords(accountId, undefined, currencyCode, status)
        if (response.code === 200) {
          this.transferRecords = response.data
          return { success: true, data: response.data }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '获取转账记录失败';
        return { success: false, message: errorMessage }
      }
    },

    // 获取常用收款人
    async fetchRecipients() {
      try {
        // Mock data for static preview
        this.recipients = [
          { id: 1, name: '张三', account: '12345678', currency: 'MOP' },
          { id: 2, name: '李四', account: '87654321', currency: 'HKD' },
          { id: 3, name: '王五', account: '11223344', currency: 'CNY' }
        ]
        return { success: true, data: this.recipients }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '获取收款人列表失败';
        return { success: false, message: errorMessage }
      }
    },

    // 获取汇率
    async fetchExchangeRate(from: string, to: string) {
      try {
        const response = await currencyApi.getRate(from, to)
        if (response.code === 200) {
          return { success: true, rate: response.data }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '获取汇率失败';
        return { success: false, message: errorMessage }
      }
    },

    // 执行外币兑换
    async exchangeCurrency(fromAccountId: string, toAccountId: string, amount: number) {
      try {
        const response = await currencyApi.exchangeCurrency({ fromAccountId, toAccountId, amount })
        if (response.code === 200) {
          // 更新账户余额
          await this.fetchAccounts()
          return { success: true, data: response.data }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '兑换失败';
        return { success: false, message: errorMessage }
      }
    },

    // 根据币种获取账户
    getAccountByCurrency(currency: string) {
      return this.accounts.find(account => account.currencyCode === currency || account.currency === currency)
    },

    // 获取默认账户（MOP账户）
    getDefaultAccount() {
      return this.accounts.find(account => account.currencyCode === 'MOP' || account.currency === 'MOP') || this.accounts[0]
    }
  }
})