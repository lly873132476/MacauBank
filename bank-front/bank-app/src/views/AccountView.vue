<template>
  <div class="account">
    <!-- 未开户拦截 -->
    <div v-if="!authStore.isAccountOpened" class="verify-blocker">
      <div class="blocker-content">
        <div class="icon-circle">
          <van-icon name="lock" />
        </div>
        <h2>账户服务未开通</h2>
        <p>请先完成身份认证以查看您的资产和交易记录</p>
        <button class="btn-primary mt-6" @click="goToOpenAccount">
          立即开通
        </button>
      </div>
    </div>

    <!-- 已开户内容 -->
    <div v-else>
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-content">
          <div class="header-icon-box bg-blue-soft">
            <van-icon name="balance-list" />
          </div>
          <div class="header-info">
            <h1 class="page-title">{{ t('account.title') }}</h1>
            <p class="page-subtitle">
              <van-icon name="shield-o" />
              安全资产托管
            </p>
          </div>
        </div>
      </div>

      <!-- Tab标签页 -->
      <van-tabs v-model:active="activeTab" class="clean-tabs" animated swipeable>
        <!-- 卡片列表 -->
        <van-tab name="cards" :title="t('account.cardList')">
          <div class="tab-content">
            <div class="cards-container">
              <div
                v-for="(account, index) in accountStore.accounts"
                :key="account.displayId"
                class="bank-card clickable shimmer-effect"
                :class="`card-theme-${index % 3}`"
                @click="onAccountChange(account.displayId)"
              >
                <div class="card-bg-decoration"></div>
                <div class="texture-overlay"></div>
                <div class="card-content">
                  <div class="card-row-top">
                    <div class="chip-icon"></div>
                    <van-icon name="rss" class="contactless-icon" />
                  </div>
                  
                  <div class="card-row-middle">
                    <div class="balance-group">
                      <span class="label">当前余额</span>
                      <div class="amount-wrapper">
                        <span class="currency-symbol">{{ account.currencyCode }}</span>
                        <span class="amount-value font-num">{{ account.balance?.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</span>
                      </div>
                    </div>
                  </div>
                  
                  <div class="card-row-bottom">
                    <div class="card-number font-num">
                      <span>••••</span> <span>••••</span> <span>••••</span>
                      <span class="last-digits">{{ account.accountNo ? account.accountNo.slice(-4) : '8888' }}</span>
                    </div>
                    <div class="card-holder">{{ account.openBranchName || 'MACAU BANK MEMBER' }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </van-tab>

        <!-- 交易流水 -->
        <van-tab name="transactions" :title="t('account.transactionHistory')">
          <div class="tab-content">
            <div class="app-card filter-card">
              <div class="section-title-small">近期交易</div>
              <div class="currency-selector clickable" @click="showFilterSheet = true">
                <span class="selected-currency">{{ selectedCurrency }}</span>
                <van-icon name="arrow-down" size="12" />
              </div>
            </div>
            
            <div class="transactions-list">
              <div
                v-for="transaction in transactions"
                :key="transaction.id"
                class="transaction-item app-card"
              >
                <div class="tx-icon-box" :class="transaction.direction === 'C' ? 'income' : 'expense'">
                  <van-icon :name="transaction.direction === 'C' ? 'down' : 'up'" />
                </div>
                <div class="tx-info">
                  <div class="tx-desc">{{ transaction.bizDesc || '一般交易' }}</div>
                  <div class="tx-date">{{ transaction.transTime }}</div>
                </div>
                <div class="tx-amount font-num" :class="{ 'positive': transaction.direction === 'C' }">
                  {{ transaction.direction === 'C' ? '+' : '-' }}{{ transaction.amount?.toFixed(2) }}
                </div>
              </div>
            </div>
          </div>
        </van-tab>

        <!-- 外币兑换 -->
        <van-tab name="exchange" :title="t('account.currencyExchange')">
          <div class="tab-content">
            <div class="exchange-wrapper">
              <div class="rate-board app-card">
                <div class="rate-header">
                  <span class="rate-title">实时汇率</span>
                  <div class="live-indicator"><span class="dot"></span> Live</div>
                </div>
                <div class="rate-display">
                  <div class="currency-pair font-num">
                    1 <span class="highlight">{{ exchangeData.fromCurrency }}</span> ≈ 
                    <span class="rate-num">{{ exchangeRate?.toFixed(4) }}</span> 
                    <span class="highlight">{{ exchangeData.toCurrency }}</span>
                  </div>
                </div>
              </div>
              
              <div class="exchange-card app-card">
                <div class="input-group">
                  <div class="input-header">
                    <span>卖出</span>
                    <span class="balance-hint">余额: {{ getAccountByCurrency(exchangeData.fromCurrency)?.balance.toFixed(2) || '0.00' }}</span>
                  </div>
                  <div class="input-body">
                    <input type="number" v-model.number="exchangeData.amount" placeholder="0.00" class="amount-input font-num" @input="calculateExchangeResult">
                    <div class="currency-trigger clickable" @click="showFromSheet = true">
                      <span class="curr-text">{{ exchangeData.fromCurrency }}</span>
                      <van-icon name="arrow-down" size="12" />
                    </div>
                  </div>
                </div>

                <div class="divider">
                  <div class="swap-btn" @click="swapCurrencies"><van-icon name="exchange" /></div>
                </div>

                <div class="input-group">
                  <div class="input-header"><span>买入 (估算)</span></div>
                  <div class="input-body">
                    <div class="amount-display font-num">{{ exchangeResult?.toFixed(2) || '0.00' }}</div>
                    <div class="currency-trigger clickable" @click="showToSheet = true">
                      <span class="curr-text">{{ exchangeData.toCurrency }}</span>
                      <van-icon name="arrow-down" size="12" />
                    </div>
                  </div>
                </div>
                <button class="btn-primary mt-4" @click="executeExchange">立即兑换</button>
              </div>
            </div>
          </div>
        </van-tab>
      </van-tabs>

      <!-- 核心修复：将弹窗移出 Tabs 容器，放在页面根部 -->
      <van-action-sheet 
        v-model:show="showFromSheet" 
        :actions="currencyList" 
        @select="onSelectFrom" 
        cancel-text="取消" 
        close-on-click-action
        title="选择卖出币种"
      />
      <van-action-sheet 
        v-model:show="showToSheet" 
        :actions="currencyList" 
        @select="onSelectTo" 
        cancel-text="取消" 
        close-on-click-action
        title="选择买入币种"
      />
      <van-action-sheet 
        v-model:show="showFilterSheet" 
        :actions="currencyList" 
        @select="onSelectFilter" 
        cancel-text="取消" 
        close-on-click-action
        title="筛选币种"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAccountStore } from '../stores/account'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import { forexApi, ForexDirectionEnum } from '../services/api'
import { showToast, showSuccessToast, showFailToast } from 'vant'

const { t } = useI18n()
const accountStore = useAccountStore()
const authStore = useAuthStore()
const router = useRouter()

const activeTab = ref('cards')
const transactions = ref<any[]>([])
const selectedCurrency = ref('MOP')

const showFromSheet = ref(false)
const showToSheet = ref(false)
const showFilterSheet = ref(false)

const goToOpenAccount = () => router.push('/open-account')

const exchangeData = ref({ fromCurrency: 'MOP', toCurrency: 'HKD', amount: 0 })
const exchangeRate = ref(0)
const exchangeResult = ref(0)

// 静态数据，确保 ActionSheet 永远有内容
const currencyList = [
  { name: 'MOP', subname: '澳门元', value: 'MOP' },
  { name: 'HKD', subname: '港币', value: 'HKD' },
  { name: 'CNY', subname: '人民币', value: 'CNY' },
  { name: 'USD', subname: '美元', value: 'USD' },
  { name: 'EUR', subname: '欧元', value: 'EUR' }
]

const CURRENCY_WEIGHTS: Record<string, number> = {
  'USD': 100,
  'EUR': 90,
  'GBP': 80,
  'AUD': 70,
  'HKD': 60,
  'CNY': 50,
  'MOP': 10
}

const getPairCode = (c1: string, c2: string): string => {
  const w1 = CURRENCY_WEIGHTS[c1] || 0
  const w2 = CURRENCY_WEIGHTS[c2] || 0
  // 权重高的在前作为 Base
  if (w1 >= w2) return `${c1}_${c2}`
  return `${c2}_${c1}`
}

const onSelectFrom = (item: any) => {
  exchangeData.value.fromCurrency = item.value
  showFromSheet.value = false
  loadExchangeRate()
}

const onSelectTo = (item: any) => {
  exchangeData.value.toCurrency = item.value
  showToSheet.value = false
  loadExchangeRate()
}

const onSelectFilter = (item: any) => {
  selectedCurrency.value = item.value
  showFilterSheet.value = false
  loadTransactions()
}

const swapCurrencies = () => {
  const temp = exchangeData.value.fromCurrency
  exchangeData.value.fromCurrency = exchangeData.value.toCurrency
  exchangeData.value.toCurrency = temp
  loadExchangeRate()
}

watch(activeTab, (newTab) => {
  if (newTab === 'transactions') {
    loadTransactions()
  }
})

onMounted(async () => {
  if (authStore.isAccountOpened) {
    await Promise.all([accountStore.fetchAccounts(), loadExchangeRate()])
    if (accountStore.accounts.length > 0) {
        selectedCurrency.value = accountStore.accounts[0]?.currencyCode || 'MOP'
        // Lazy load: do not call loadTransactions() here
    }
  }
})

const loadTransactions = async () => {
  if (selectedCurrency.value) {
    const result = await accountStore.fetchTransactions(selectedCurrency.value)
    if (result.success) transactions.value = result.data || []
  }
}

const onAccountChange = async (displayId: string) => {
  const account = accountStore.accounts.find(acc => acc.displayId === displayId)
  if (account) {
    selectedCurrency.value = account.currencyCode || 'MOP'
    if (activeTab.value === 'transactions') {
      await loadTransactions()
    }
  }
}

const loadExchangeRate = async () => {
  const result = await accountStore.fetchExchangeRate(exchangeData.value.fromCurrency, exchangeData.value.toCurrency)
  if (result.success) {
    exchangeRate.value = result.rate || 0
    calculateExchangeResult()
  }
}

const calculateExchangeResult = () => {
  exchangeResult.value = (exchangeData.value.amount || 0) * (exchangeRate.value || 0)
}

const executeExchange = async () => {
  if (exchangeData.value.amount <= 0) {
     showToast('请输入有效的卖出金额')
     return
  }
  
  const fromAcc = accountStore.accounts.find(a => a.currencyCode === exchangeData.value.fromCurrency)
  
  if (!fromAcc || !fromAcc.accountNo) {
     showFailToast('未找到付款账户')
     return
  }

  // 1. 确定 PairCode (例如 HKD_MOP)
  const pairCode = getPairCode(exchangeData.value.fromCurrency, exchangeData.value.toCurrency)
  
  // 2. 确定 Direction
  // Pair 是 Base_Quote.
  // 如果 sellCurrency == Base, 则是 SELL (卖出基准)
  // 如果 sellCurrency == Quote, 则是 BUY (买入基准/卖出非基准)
  const baseCurrency = pairCode.split('_')[0]
  const direction = exchangeData.value.fromCurrency === baseCurrency 
      ? ForexDirectionEnum.SELL 
      : ForexDirectionEnum.BUY

  try {
      showToast({ message: '交易处理中...', type: 'loading', duration: 0, forbidClick: true })
      
      // 生成幂等请求ID
      const requestId = `REQ-${Date.now()}-${Math.floor(Math.random() * 1000).toString().padStart(3, '0')}`

      const res = await forexApi.exchange({
          requestId,
          pairCode,
          direction,
          sellCurrency: exchangeData.value.fromCurrency,
          sellAmount: exchangeData.value.amount,
          buyCurrency: exchangeData.value.toCurrency,
          accountNo: fromAcc.accountNo
      })

      if (res.code === 200) {
          showSuccessToast(`兑换成功！成交汇率: ${res.data.dealRate}`)
          // 刷新账户余额
          await accountStore.fetchAccounts()
          // 清空输入
          exchangeData.value.amount = 0
          exchangeResult.value = 0
      } else {
          showFailToast(res.message || '兑换失败')
      }
  } catch (e) {
      console.error('Forex exchange error:', e)
      showFailToast('系统繁忙，请稍后再试')
  }
}

const getAccountByCurrency = (currency: string) => accountStore.getAccountByCurrency(currency)
</script>

<style scoped>
.account { padding: 0 16px 40px; min-height: 100vh; background-color: var(--bg-app); }
.verify-blocker { display: flex; align-items: center; justify-content: center; height: 80vh; }
.blocker-content { text-align: center; background: var(--bg-surface); padding: 40px 24px; border-radius: 24px; box-shadow: var(--shadow-card); }
.icon-circle { width: 80px; height: 80px; background: rgba(37, 99, 235, 0.1); color: var(--color-primary); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 32px; margin: 0 auto 20px; }
.btn-primary { background: var(--gradient-brand); border: none; border-radius: 12px; height: 48px; width: 100%; color: white; font-weight: 600; box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3); cursor: pointer; }
.page-header { padding: 24px 4px; }
.header-content { display: flex; align-items: center; gap: 16px; }
.header-icon-box { width: 52px; height: 52px; border-radius: 18px; display: flex; align-items: center; justify-content: center; font-size: 26px; color: var(--color-primary); background: var(--bg-surface); }
.page-title { font-size: 24px; font-weight: 800; margin: 0; }
.clean-tabs :deep(.van-tabs__wrap) { margin-bottom: 24px; }
.clean-tabs :deep(.van-tabs__nav) { background: transparent !important; }
.clean-tabs :deep(.van-tabs__line) { background: var(--gradient-brand); width: 24px; height: 4px; bottom: 20px; }
.cards-container { display: flex; flex-direction: column; gap: 20px; }
.bank-card { height: 200px; border-radius: 28px; padding: 24px; position: relative; color: white; overflow: hidden; box-shadow: 0 12px 24px -8px rgba(0,0,0,0.2); }
.card-theme-0 { background: linear-gradient(120deg, #2563EB, #1E40AF); }
.card-theme-1 { background: linear-gradient(120deg, #7C3AED, #5B21B6); }
.card-theme-2 { background: linear-gradient(120deg, #EA580C, #9A3412); }
.amount-value { font-size: 28px; font-weight: 700; }
.card-row-bottom { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 40px; }
.filter-card { display: flex; justify-content: space-between; align-items: center; padding: 16px; margin-bottom: 16px; background: var(--bg-surface); border-radius: 20px; }
.currency-selector { display: flex; align-items: center; gap: 6px; background: var(--bg-app); padding: 6px 12px; border-radius: 16px; font-size: 12px; font-weight: 700; cursor: pointer; }
.transaction-item { padding: 16px; display: flex; align-items: center; gap: 12px; background: var(--bg-surface); border-radius: 20px; margin-bottom: 10px; }
.tx-icon-box { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.tx-icon-box.income { background: rgba(16, 185, 129, 0.1); color: #10B981; }
.tx-icon-box.expense { background: rgba(239, 68, 68, 0.1); color: #EF4444; }
.tx-info { flex: 1; }
.tx-amount { font-size: 16px; font-weight: 800; }
.tx-amount.positive { color: #10B981; }
.rate-board { background: var(--gradient-brand); color: white; border-radius: 24px; padding: 20px; margin-bottom: 16px; }
.rate-num { font-size: 32px; font-weight: 800; margin: 0 8px; }
.exchange-card { padding: 20px; background: var(--bg-surface); border-radius: 24px; }
.input-group { background: var(--bg-app); border-radius: 16px; padding: 12px; margin-bottom: 8px; }
.input-body { display: flex; justify-content: space-between; align-items: center; }
.amount-input { border: none; background: transparent; font-size: 20px; font-weight: 800; width: 60%; color: var(--text-main); }
.amount-input:focus { outline: none; }
.currency-trigger { display: flex; align-items: center; gap: 6px; background: var(--bg-surface); padding: 6px 10px; border-radius: 10px; cursor: pointer; }
.curr-text { font-weight: 700; font-size: 14px; }
.divider { display: flex; justify-content: center; margin: -10px 0; position: relative; z-index: 2; }
.swap-btn { width: 36px; height: 36px; background: var(--bg-surface); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: var(--color-primary); box-shadow: 0 2px 8px rgba(0,0,0,0.1); cursor: pointer; }
</style>