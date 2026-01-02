import { defineStore } from 'pinia'
import { accountApi, currencyApi, transferApi, type AccountResponse, type AssetSummaryResponse } from '../services/api'

export interface DisplayAccount extends AccountResponse {
  displayId: string;
  currencyCode: string;
  balance: number;
  accountName: string;
}

interface TransactionRecord {
  id: number;
  txnId: string;
  currencyCode: string;
  direction: 'D' | 'C';
  amount: number;
  balance: number;
  bizType: string;
  bizDesc: string;
  transTime: string;
}

interface Recipient {
  id: number;
  name: string;
  account: string;
  currency: string;
  bankName?: string;
  bankCode?: string;
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
    accounts: [] as DisplayAccount[],
    summary: null as AssetSummaryResponse | null,
    transactions: [] as TransactionRecord[],
    recipients: [] as Recipient[],
    transferRecords: [] as TransferRecord[], // 添加转账记录状态
    currencies: ['MOP', 'HKD', 'CNY', 'USD'] // 支持的币种
  }),

  actions: {
    // 获取资产总览
    async fetchSummary() {
      try {
        const response = await accountApi.getSummary()
        if (response.code === 200) {
          this.summary = response.data
          // 总是同步更新打平后的账户列表，确保数据一致且无需额外请求
          if (response.data.accounts) {
            const flattened: any[] = []
            response.data.accounts.forEach(acc => {
              if (acc.balances && acc.balances.length > 0) {
                acc.balances.forEach(bal => {
                  flattened.push({
                    ...acc,
                    // 覆盖/添加兼容字段
                    id: acc.id,
                    displayId: `${acc.accountNo}_${bal.currencyCode}`, // 唯一标识
                    currencyCode: bal.currencyCode,
                    balance: bal.availableBalance,
                    accountName: `${bal.currencyCode} 储蓄账户`
                  })
                })
              }
            })
            this.accounts = flattened
          }
          return { success: true, data: this.summary }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '获取资产总览失败';
        return { success: false, message: errorMessage }
      }
    },

    // 获取账户列表
    async fetchAccounts() {
      try {
        const response = await accountApi.getAccountList()
        if (response.code === 200) {
          const flattened: any[] = []
          response.data.forEach(acc => {
            if (acc.balances && acc.balances.length > 0) {
              acc.balances.forEach(bal => {
                flattened.push({
                  ...acc,
                  // 覆盖/添加兼容字段
                  id: acc.id,
                  displayId: `${acc.accountNo}_${bal.currencyCode}`,
                  currencyCode: bal.currencyCode,
                  balance: bal.availableBalance,
                  accountName: `${bal.currencyCode} 储蓄账户`
                })
              })
            }
          })
          this.accounts = flattened
          return { success: true, data: this.accounts }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '获取账户列表失败';
        return { success: false, message: errorMessage }
      }
    },

    // 获取交易流水
    async fetchTransactions(currencyCode?: string) {
      try {
        const response = await accountApi.getTransactionRecords({
          currencyCode,
          page: 1,
          pageSize: 50
        })
        if (response.code === 200) {
          this.transactions = response.data.records.map(apiRecord => ({
            id: apiRecord.id,
            txnId: apiRecord.txnId,
            currencyCode: apiRecord.currencyCode,
            direction: apiRecord.direction,
            amount: apiRecord.amount,
            balance: apiRecord.balance,
            bizType: apiRecord.bizType,
            bizDesc: apiRecord.bizDesc,
            transTime: apiRecord.transTime
          }))
          return { success: true, data: response.data.records }
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
          { id: 1, name: '张三', account: '12345678', currency: 'MOP', bankName: '中国银行澳门分行', bankCode: 'BCMOMO00' },
          { id: 2, name: '李四', account: '87654321', currency: 'HKD', bankName: '大丰银行', bankCode: 'DMBMMO00' },
          { id: 3, name: '王五', account: '11223344', currency: 'CNY', bankName: '澳门国际银行', bankCode: 'ZBBAMO00' }
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
      return this.accounts.find(account => account.currencyCode === currency)
    },

    // 获取默认账户（MOP账户）
    getDefaultAccount() {
      return this.accounts.find(account => account.currencyCode === 'MOP') || this.accounts[0]
    }
  }
})