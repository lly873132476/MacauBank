<template>
  <div class="account">
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
              :key="account.id"
              class="bank-card clickable shimmer-effect"
              :class="`card-theme-${index % 3}`"
              @click="onAccountChange(String(account.id))"
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
                    <span class="last-digits">{{ account.id ? String(account.id).slice(-4) : '8888' }}</span>
                  </div>
                  <div class="card-holder">{{ account.name || 'PREMIUM MEMBER' }}</div>
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
            <div class="currency-selector" @click="toggleCurrencyDropdown">
              <span class="selected-currency">{{ getCurrentCurrencyText() }}</span>
              <van-icon name="arrow-down" size="12" />
            </div>
          </div>
          
          <!-- 交易记录列表 -->
          <div class="transactions-list">
            <div
              v-for="transaction in transactions"
              :key="transaction.id"
              class="transaction-item app-card"
            >
              <div class="tx-icon-box" :class="isIncome(transaction.amount) ? 'income' : 'expense'">
                <van-icon :name="isIncome(transaction.amount) ? 'down' : 'up'" />
              </div>
              <div class="tx-info">
                <div class="tx-desc">{{ transaction.description || '一般交易' }}</div>
                <div class="tx-date">{{ transaction.date || '2023-12-01' }}</div>
              </div>
              <div class="tx-amount font-num" :class="{ 'positive': isIncome(transaction.amount) }">
                {{ isIncome(transaction.amount) ? '+' : '' }}{{ transaction.amount?.toFixed(2) }}
              </div>
            </div>
          </div>
        </div>
      </van-tab>

      <!-- 外币兑换 -->
      <van-tab name="exchange" :title="t('account.currencyExchange')">
        <div class="tab-content">
          <div class="exchange-wrapper">
            <!-- 汇率看板 -->
            <div class="rate-board app-card">
              <div class="rate-header">
                <span class="rate-title">实时汇率</span>
                <div class="live-indicator">
                  <span class="dot"></span> Live
                </div>
              </div>
              <div class="rate-display">
                <div class="currency-pair font-num">
                  1 <span class="highlight">{{ exchangeData.fromCurrency }}</span> ≈ 
                  <span class="rate-num">{{ exchangeRate?.toFixed(4) }}</span> 
                  <span class="highlight">{{ exchangeData.toCurrency }}</span>
                </div>
              </div>
            </div>
            
            <!-- 兑换卡片 -->
            <div class="exchange-card app-card">
              <div class="input-group">
                <div class="input-header">
                  <span>卖出</span>
                  <span class="balance-hint">余额: {{ getAccountByCurrency(exchangeData.fromCurrency)?.balance.toFixed(2) || '0.00' }}</span>
                </div>
                <div class="input-body">
                  <input 
                    type="number" 
                    v-model.number="exchangeData.amount" 
                    placeholder="0.00" 
                    class="amount-input font-num"
                    @input="calculateExchangeResult"
                  >
                  <div class="currency-select">
                    <select v-model="exchangeData.fromCurrency" @change="onExchangeFormChange(($event.target as HTMLSelectElement).value, 'from')">
                      <option v-for="opt in currencyOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
                    </select>
                    <van-icon name="arrow-down" />
                  </div>
                </div>
              </div>

              <div class="divider">
                <div class="swap-btn">
                  <van-icon name="exchange" />
                </div>
              </div>

              <div class="input-group">
                <div class="input-header">
                  <span>买入 (估算)</span>
                </div>
                <div class="input-body">
                  <div class="amount-display font-num">{{ exchangeResult?.toFixed(2) || '0.00' }}</div>
                  <div class="currency-select">
                    <select v-model="exchangeData.toCurrency" @change="onExchangeFormChange(($event.target as HTMLSelectElement).value, 'to')">
                      <option v-for="opt in currencyOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
                    </select>
                    <van-icon name="arrow-down" />
                  </div>
                </div>
              </div>

              <button class="btn-primary mt-4" @click="executeExchange">
                立即兑换
              </button>
            </div>
          </div>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAccountStore } from '../stores/account'

const { t } = useI18n()
const accountStore = useAccountStore()

const activeTab = ref('cards')
const transactions = ref<any[]>([])
const selectedAccount = ref('')
const selectedCurrency = ref('MOP')
const showCurrencyDropdown = ref(false)

// 外币兑换相关
const exchangeData = ref({
  fromCurrency: 'MOP',
  toCurrency: 'HKD',
  amount: 0
})
const exchangeRate = ref(0)
const exchangeResult = ref(0)

const currencyOptions = computed(() => {
  return accountStore.currencies.map(c => ({ text: c, value: c }))
})

onMounted(async () => {
  await Promise.all([
    accountStore.fetchAccounts(),
    loadExchangeRate()
  ])
  
  if (accountStore.accounts.length > 0) {
    const firstAccount = accountStore.accounts[0]
    if (firstAccount) {
      selectedAccount.value = String(firstAccount.id || '')
      selectedCurrency.value = firstAccount.currencyCode || 'MOP'
      await loadTransactions()
    }
  }
})

watch(activeTab, (newTab, oldTab) => {
  if (newTab === 'transactions' && oldTab !== 'transactions') {
    // Data already loaded
  }
})

const loadTransactions = async () => {
  if (selectedAccount.value && selectedCurrency.value) {
    const result = await accountStore.fetchTransactions(
      selectedAccount.value,
      selectedCurrency.value
    )
    if (result.success) {
      transactions.value = result.data || []
    }
  }
}

const onAccountChange = async (accountId: string) => {
  selectedAccount.value = accountId || ''
  const account = accountStore.accounts.find(acc => String(acc.id) === accountId)
  if (account) {
    selectedCurrency.value = account.currencyCode || 'MOP'
    await loadTransactions()
  }
}

const onCurrencyChange = async (currency: string) => {
  if (selectedCurrency.value !== currency) {
    selectedCurrency.value = currency || 'MOP'
    await loadTransactions()
  }
}

const loadExchangeRate = async () => {
  const result = await accountStore.fetchExchangeRate(
    exchangeData.value.fromCurrency || 'MOP',
    exchangeData.value.toCurrency || 'HKD'
  )
  if (result.success) {
    exchangeRate.value = result.rate || 0
    calculateExchangeResult()
  }
}

const calculateExchangeResult = () => {
  const amount = exchangeData.value.amount || 0
  const rate = exchangeRate.value || 0
  exchangeResult.value = amount * rate
}

const executeExchange = async () => {
  const amount = exchangeData.value.amount || 0
  if (amount <= 0) return
  
  const fromAccount = accountStore.accounts.find(
    acc => acc.currencyCode === (exchangeData.value.fromCurrency || 'MOP')
  )
  const toAccount = accountStore.accounts.find(
    acc => acc.currencyCode === (exchangeData.value.toCurrency || 'HKD')
  )
  
  if (!fromAccount || !toAccount) return
  
  const result = await accountStore.exchangeCurrency(
    String(fromAccount.id || ''),
    String(toAccount.id || ''),
    amount
  )
  
  if (result.success) {
    await accountStore.fetchAccounts()
  }
}

const onExchangeFormChange = async (value: string, type: 'from' | 'to') => {
  if (type === 'from') {
    exchangeData.value.fromCurrency = value
  } else if (type === 'to') {
    exchangeData.value.toCurrency = value
  }
  await loadExchangeRate()
}

const isIncome = (amount: number) => {
  return (amount || 0) > 0
}

const toggleCurrencyDropdown = () => {
  showCurrencyDropdown.value = !showCurrencyDropdown.value
}

const getCurrentCurrencyText = () => {
  const option = currencyOptions.value.find(opt => opt.value === selectedCurrency.value)
  return option ? option.text : 'MOP'
}

const selectCurrency = async (currency: string) => {
  showCurrencyDropdown.value = false
  await onCurrencyChange(currency)
}

const getAccountByCurrency = (currency: string) => {
  return accountStore.getAccountByCurrency(currency)
}
</script>

<style scoped>
.account {
  padding: 0 16px 40px;
  min-height: 100vh;
  background-color: var(--bg-app);
  /* Modern Aurora Gradient - Theme Aware via opacity/blend */
  background-image: 
    radial-gradient(circle at 0% 0%, rgba(37, 99, 235, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 100% 0%, rgba(236, 72, 153, 0.05) 0%, transparent 50%);
  animation: fadeIn 0.5s ease-out;
}

/* Dark mode specific gradient adjustments could be added in global CSS, 
   but here we rely on the base color being dark which makes the gradient subtle. */

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

/* 头部样式 */
.page-header {
  padding: 24px 4px 24px;
  position: relative;
  z-index: 2;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon-box {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: var(--color-primary);
  background: var(--bg-surface);
  box-shadow: var(--shadow-sm);
}

/* ... (rest of styles using variables) ... */
/* I will do a targeted replace for specific hardcoded blocks to be safe and efficient */

.page-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-main);
  margin: 0 0 4px 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 13px;
  color: var(--text-sub);
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
}

/* Clean Tabs */
.clean-tabs :deep(.van-tabs__wrap) {
  margin-bottom: 24px;
}

.clean-tabs :deep(.van-tabs__nav) {
  background: transparent !important;
}

.clean-tabs :deep(.van-tab) {
  font-weight: 600;
  color: var(--text-sub);
  font-size: 15px;
}

.clean-tabs :deep(.van-tab--active) {
  font-weight: 800;
  color: var(--text-main);
  transform: scale(1.05);
}

.clean-tabs :deep(.van-tabs__line) {
  background: var(--gradient-brand);
  width: 24px;
  height: 4px;
  border-radius: 4px;
  bottom: 20px;
}

.tab-content {
  padding-bottom: 20px;
}

/* 银行卡片 */
.cards-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  perspective: 1000px;
}

.bank-card {
  height: 220px;
  border-radius: 28px;
  padding: 28px;
  position: relative;
  color: white;
  overflow: hidden;
  box-shadow: 
    0 16px 32px -8px rgba(0,0,0,0.25),
    inset 0 1px 0 rgba(255,255,255,0.2);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.bank-card:active { transform: scale(0.96) rotateX(2deg); }

/* 卡片主题 - 渐变色 */
.card-theme-0 { background: linear-gradient(120deg, #2563EB, #1D4ED8, #1E40AF); }
.card-theme-1 { background: linear-gradient(120deg, #7C3AED, #6D28D9, #5B21B6); }
.card-theme-2 { background: linear-gradient(120deg, #EA580C, #C2410C, #9A3412); }

.card-bg-decoration {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.card-row-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.chip-icon {
  width: 44px;
  height: 32px;
  background: linear-gradient(135deg, #e2e8f0 0%, #94a3b8 100%);
  border-radius: 8px;
  border: 1px solid rgba(255,255,255,0.4);
  position: relative;
  overflow: hidden;
}
.chip-icon::after {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  border: 1px solid rgba(0,0,0,0.1);
  border-radius: 8px;
  background-image: repeating-linear-gradient(90deg, transparent, transparent 10px, rgba(0,0,0,0.1) 10px, rgba(0,0,0,0.1) 11px);
}

.contactless-icon {
  font-size: 24px;
  opacity: 0.8;
  transform: rotate(90deg);
}

.balance-group .label {
  font-size: 13px;
  opacity: 0.8;
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
}

.amount-wrapper {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.currency-symbol {
  font-size: 18px;
  font-weight: 600;
  background: rgba(255,255,255,0.2);
  padding: 2px 8px;
  border-radius: 8px;
}

.amount-value {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -1px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.card-row-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.card-number {
  font-size: 18px;
  letter-spacing: 3px;
  display: flex;
  gap: 12px;
  align-items: center;
  opacity: 0.95;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.card-holder {
  font-size: 12px;
  font-weight: 700;
  opacity: 0.8;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 交易列表 */
.filter-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  margin-bottom: 20px;
  background: var(--bg-surface);
  backdrop-filter: blur(12px);
  border-radius: 24px;
  box-shadow: var(--shadow-card);
}

.section-title-small {
  font-size: 18px;
  font-weight: 800;
  color: var(--text-main);
}

.currency-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-app);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-main);
  transition: background 0.2s;
}
.currency-selector:active { opacity: 0.8; }

.transactions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.transaction-item {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--bg-surface);
  border-radius: 24px;
  box-shadow: var(--shadow-card);
  border: 1px solid rgba(255,255,255,0.05);
}

.tx-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.tx-icon-box.income { background: rgba(16, 185, 129, 0.1); color: #10B981; }
.tx-icon-box.expense { background: rgba(239, 68, 68, 0.1); color: #EF4444; }

.tx-info { flex: 1; }

.tx-desc {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 4px;
}

.tx-date {
  font-size: 12px;
  color: var(--text-sub);
  font-weight: 500;
}

.tx-amount {
  font-size: 17px;
  font-weight: 800;
  color: var(--text-main);
}

.tx-amount.positive { color: #10B981; }

/* 外币兑换 */
.exchange-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.rate-board {
  background: var(--gradient-brand);
  border: none;
  color: white;
  box-shadow: 
    0 12px 32px -8px rgba(37, 99, 235, 0.4),
    inset 0 1px 0 rgba(255,255,255,0.2);
  border-radius: 28px;
  padding: 24px;
}

.rate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  opacity: 0.9;
}

.rate-title { font-weight: 600; font-size: 14px; }

.live-indicator {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.2);
  padding: 4px 10px;
  border-radius: 12px;
  color: white;
  font-weight: 700;
  backdrop-filter: blur(4px);
}

.dot {
  width: 6px;
  height: 6px;
  background: #10B981;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.3);
}

.rate-display { text-align: center; padding: 8px 0; }

.currency-pair { font-size: 20px; font-weight: 600; opacity: 0.9; }

.rate-num {
  font-size: 36px;
  font-weight: 800;
  color: white;
  margin: 0 8px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.exchange-card { 
  padding: 24px;
  background: var(--bg-surface);
  border-radius: 28px;
  box-shadow: var(--shadow-card);
}

.input-group {
  background: var(--bg-app);
  border-radius: 20px;
  padding: 16px;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.input-group:focus-within {
  background: var(--bg-surface);
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.1);
}

.input-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-sub);
  margin-bottom: 8px;
  font-weight: 600;
}

.input-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.amount-input {
  border: none;
  background: transparent;
  font-size: 24px;
  font-weight: 800;
  width: 60%;
  color: var(--text-main);
  padding: 0;
}

.amount-input:focus { outline: none; }

.amount-display {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-sub);
}

.currency-select {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--bg-surface);
  padding: 6px 12px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.currency-select select {
  appearance: none;
  border: none;
  background: transparent;
  font-weight: 700;
  color: var(--text-main);
  padding-right: 4px;
  font-size: 14px;
}

.divider {
  height: 24px;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: -12px 0;
  z-index: 2;
}

.swap-btn {
  width: 40px;
  height: 40px;
  background: var(--bg-surface);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.2s;
}
.swap-btn:active { transform: scale(0.9) rotate(180deg); }

.mt-4 { margin-top: 24px; }
</style>