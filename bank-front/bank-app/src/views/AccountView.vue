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
              <div class="texture-overlay"></div> <!-- 纹理层 -->
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
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

/* 头部样式 */
.page-header {
  padding: 20px 0 24px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
  background: white;
}

.bg-blue-soft { background: white; }

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-main);
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 13px;
  color: var(--text-sub);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Clean Tabs */
.clean-tabs :deep(.van-tabs__wrap) {
  margin-bottom: 20px;
}

.clean-tabs :deep(.van-tabs__nav) {
  background: transparent !important;
}

.clean-tabs :deep(.van-tab) {
  font-weight: 600;
  color: var(--text-sub);
}

.clean-tabs :deep(.van-tab--active) {
  font-weight: 700;
  color: var(--color-primary);
}

.clean-tabs :deep(.van-tabs__line) {
  background-color: var(--color-primary);
  width: 20px;
}

.tab-content {
  padding-bottom: 20px;
}

/* 银行卡片 */
.cards-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.bank-card {
  height: 200px;
  border-radius: 20px;
  padding: 24px;
  position: relative;
  color: white;
  overflow: hidden;
  box-shadow: var(--shadow-float);
  transition: transform 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.bank-card:active { transform: scale(0.98); }

/* 卡片主题 - 渐变色 */
.card-theme-0 { background: linear-gradient(135deg, #2563EB 0%, #06B6D4 100%); }
.card-theme-1 { background: linear-gradient(135deg, #4F46E5 0%, #8B5CF6 100%); }
.card-theme-2 { background: linear-gradient(135deg, #EA580C 0%, #F59E0B 100%); }

.card-bg-decoration {
  position: absolute;
  top: -20%;
  right: -10%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 60%);
  border-radius: 50%;
}

.card-row-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chip-icon {
  width: 36px;
  height: 26px;
  background: rgba(255,255,255,0.2);
  border-radius: 6px;
  border: 1px solid rgba(255,255,255,0.3);
}

.contactless-icon {
  font-size: 20px;
  opacity: 0.8;
  transform: rotate(90deg);
}

.balance-group .label {
  font-size: 12px;
  opacity: 0.8;
  display: block;
  margin-bottom: 4px;
}

.amount-wrapper {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.currency-symbol {
  font-size: 16px;
  font-weight: 600;
}

.amount-value {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.card-row-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.card-number {
  font-size: 16px;
  letter-spacing: 2px;
  display: flex;
  gap: 8px;
  align-items: center;
  opacity: 0.9;
}

.card-holder {
  font-size: 12px;
  font-weight: 600;
  opacity: 0.9;
  text-transform: uppercase;
}

/* 交易列表 */
.filter-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  margin-bottom: 16px;
}

.section-title-small {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
}

.currency-selector {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--bg-app);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-main);
}

.transactions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.transaction-item {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.tx-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.tx-icon-box.income { background: #ECFDF5; color: var(--color-success); }
.tx-icon-box.expense { background: #FEF2F2; color: var(--color-danger); }

.tx-info { flex: 1; }

.tx-desc {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 4px;
}

.tx-date {
  font-size: 12px;
  color: var(--text-sub);
}

.tx-amount {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
}

.tx-amount.positive { color: var(--color-success); }

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
  box-shadow: var(--shadow-float);
}

.rate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  opacity: 0.9;
}

.rate-title { font-weight: 500; font-size: 13px; }

.live-indicator {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.2);
  padding: 4px 8px;
  border-radius: 10px;
  color: white;
  font-weight: 600;
}

.dot {
  width: 6px;
  height: 6px;
  background: var(--color-success);
  border-radius: 50%;
}

.rate-display { text-align: center; }

.currency-pair { font-size: 18px; font-weight: 500; }

.rate-num {
  font-size: 24px;
  font-weight: 800;
  color: white;
}

.exchange-card { padding: 24px; }

.input-group {
  background: var(--bg-app);
  border-radius: 12px;
  padding: 12px;
  border: 1px solid transparent;
  transition: all 0.3s;
}

.input-group:focus-within {
  background: white;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.input-header {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-sub);
  margin-bottom: 4px;
}

.input-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.amount-input {
  border: none;
  background: transparent;
  font-size: 20px;
  font-weight: 700;
  width: 60%;
  color: var(--text-main);
}

.amount-input:focus { outline: none; }

.amount-display {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-sub);
}

.currency-select {
  display: flex;
  align-items: center;
  gap: 4px;
  background: white;
  padding: 4px 8px;
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
}

.currency-select select {
  appearance: none;
  border: none;
  background: transparent;
  font-weight: 700;
  color: var(--text-main);
  padding-right: 4px;
}

.divider {
  height: 20px;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: -10px 0;
  z-index: 2;
}

.swap-btn {
  width: 32px;
  height: 32px;
  background: white;
  border: 1px solid #E2E8F0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
}

.mt-4 { margin-top: 24px; }
</style>