<template>
  <div class="transfer-page">
    <!-- 顶部导航栏 -->
    <div class="page-header-simple">
      <div class="header-left">
        <div class="back-btn clickable" @click="$router.go(-1)">
          <van-icon name="arrow-left" />
        </div>
        <h1 class="page-title">{{ t('transfer.title') }}</h1>
      </div>
      <div class="header-right">
        <div class="history-btn clickable" @click="$router.push('/transfer/records')">
          <span>记录</span>
        </div>
      </div>
    </div>

    <!-- 顶部类型切换 (Segment Control) -->
    <div class="segment-wrapper">
      <div class="segment-control glass-panel">
        <div 
          class="segment-item" 
          :class="{ active: activeTab === 'quick' }"
          @click="activeTab = 'quick'"
        >
          {{ t('transfer.quickTransfer') }}
        </div>
        <div 
          class="segment-item" 
          :class="{ active: activeTab === 'crossborder' }"
          @click="activeTab = 'crossborder'"
        >
          {{ t('transfer.crossBorder') }}
        </div>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      
      <!-- 1. 核心交易卡片 (Transaction Ticket) -->
      <div class="app-card transaction-card">
        
        <!-- 金额输入区 (最核心) -->
        <div class="amount-header">
          <span class="label-text">转账金额</span>
          <div class="currency-switcher clickable" @click="showCurrencySelector = true">
            <span class="curr-code">{{ currentForm.currency }}</span>
            <van-icon name="arrow-down" />
          </div>
        </div>
        
        <div class="amount-display">
          <span class="symbol">$</span>
          <input
            v-model.number="currentForm.amount"
            type="number"
            class="hero-input font-num"
            placeholder="0.00"
          />
        </div>
        
        <div class="divider-dashed"></div>

        <!-- 资金流向区 (From -> To) -->
        <div class="flow-section">
          <!-- 付款方 -->
          <div class="flow-item from-item clickable" @click="showAccountSelector = true">
            <div class="flow-label">付款账户</div>
            <div class="flow-content">
              <div class="bank-icon-small bg-blue-soft"><van-icon name="card" /></div>
              <div class="info-text">
                <div class="main-text">MOP 储蓄账户</div>
                <div class="sub-text font-num">余额 MOP 50,000.00</div>
              </div>
              <van-icon name="arrow" class="arrow-gray" />
            </div>
          </div>

          <!-- 连接线 -->
          <div class="flow-connector">
            <div class="connector-line"></div>
            <div class="connector-icon"><van-icon name="arrow-down" /></div>
          </div>

          <!-- 收款方 -->
          <div class="flow-item to-item">
            <div class="flow-label">
              收款人
              <span class="link-action" @click="$router.push('/transfer/recipient/add')">
                <van-icon name="plus" /> 新增
              </span>
            </div>
            
            <!-- 如果未选择收款人，显示占位 -->
            <div v-if="!currentForm.recipientId" class="recipient-placeholder clickable">
               <span class="placeholder-text">请选择收款人</span>
            </div>

            <!-- 横向滚动选择列表 (嵌入在卡片内) -->
            <div class="recipient-scroll-box">
              <div 
                v-for="recipient in recipients" 
                :key="recipient.id"
                class="recipient-avatar-item"
                :class="{ active: currentForm.recipientId === recipient.id }"
                @click="selectRecipient(recipient)"
              >
                <div class="avatar-circle">
                  {{ recipient.name.charAt(0) }}
                  <div class="check-mark" v-if="currentForm.recipientId === recipient.id">
                    <van-icon name="success" />
                  </div>
                </div>
                <span class="avatar-name">{{ recipient.name }}</span>
              </div>
            </div>
            
            <!-- 已选收款人详情展示 (选中后显示) -->
            <div v-if="selectedRecipient" class="selected-recipient-info">
              <div class="bank-info">
                 <van-icon name="shop-o" /> {{ currentForm.bankName || '收款银行' }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 2. 补充信息卡片 -->
      <div class="app-card details-card mt-4">
        <!-- 仅在快速转账且未自动填充银行时显示银行选择 -->
        <div v-if="activeTab === 'quick'" class="form-row clickable" @click="showBankSelector = true">
          <span class="row-label">收款银行</span>
          <div class="row-value">
            <span :class="{ 'text-placeholder': !currentForm.bankName }">
              {{ currentForm.bankName || '请选择' }}
            </span>
            <van-icon name="arrow" class="arrow-gray" />
          </div>
        </div>

        <div class="divider-line"></div>

        <div class="form-row">
          <span class="row-label">转账备注</span>
          <input 
            v-model="currentForm.remark" 
            type="text" 
            class="row-input" 
            placeholder="选填"
          />
        </div>
        
        <div class="divider-line"></div>
        
        <div class="form-row">
          <span class="row-label">交易密码</span>
          <input 
            v-model="currentForm.transactionPassword" 
            type="password" 
            class="row-input font-num" 
            placeholder="输入6位密码"
            maxlength="6"
          />
        </div>
      </div>

      <!-- 跨境汇款特有字段 -->
      <div v-if="activeTab === 'crossborder'" class="app-card details-card mt-4">
        <div class="form-row">
           <span class="row-label">SWIFT 代码</span>
           <input v-model="crossBorderForm.swiftCode" class="row-input font-num" placeholder="必填" />
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="action-footer">
        <button class="btn-primary btn-block clickable" @click="handleTransfer">
          <span class="btn-text">确认转账</span>
          <span class="btn-amount font-num" v-if="currentForm.amount">
            {{ currentForm.currency }} {{ currentForm.amount }}
          </span>
        </button>
      </div>

    </div>

    <!-- 弹窗组件 -->
    <van-popup v-model:show="showBankSelector" position="bottom" round class="glass-popup">
      <van-picker
        title="选择收款银行"
        :columns="macauBanks"
        @confirm="onBankSelect"
        @cancel="showBankSelector = false"
      />
    </van-popup>

    <van-action-sheet
      v-model:show="showCurrencySelector"
      :actions="currencyActions"
      cancel-text="取消"
      @select="onCurrencySelect"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAccountStore } from '../stores/account'
import { showToast } from 'vant'
import { transferApi } from '../services/api'

const { t } = useI18n()
const accountStore = useAccountStore()

const activeTab = ref('quick')
const recipients = ref<any[]>([])
const showBankSelector = ref(false)
const showAccountSelector = ref(false)
const showCurrencySelector = ref(false)

// 统一表单状态管理
const quickTransferForm = ref({
  recipientId: '',
  bankName: '',
  bankCode: '',
  currency: 'MOP',
  amount: null as number | null,
  transactionPassword: '',
  remark: ''
})

const crossBorderForm = ref({
  recipientId: '',
  currency: 'MOP',
  amount: null as number | null,
  transactionPassword: '',
  swiftCode: '',
  bankName: '',
  remark: ''
})

// 当前使用的表单
const currentForm = computed(() => {
  return activeTab.value === 'quick' ? quickTransferForm.value : crossBorderForm.value
})

// 当前选中的收款人对象
const selectedRecipient = computed(() => {
  return recipients.value.find(r => r.id === currentForm.value.recipientId)
})

const macauBanks = [
  { text: '中国银行澳门分行', value: 'BCMOMO00' },
  { text: '大丰银行', value: 'DMBMMO00' },
  { text: '澳门国际银行', value: 'ZBBAMO00' },
  { text: '澳门商业银行', value: 'CSBMOM00' },
  { text: '工商银行澳门分行', value: 'ICBMMO00' }
]

const currencyActions = computed(() => {
  return accountStore.currencies.map(currency => ({
    name: currency,
    value: currency
  }))
})

onMounted(async () => {
  await loadRecipients()
})

const loadRecipients = async () => {
  try {
    const result = await accountStore.fetchRecipients()
    if (result.success) {
      recipients.value = accountStore.recipients
    }
  } catch (error) {
    console.error('获取收款人列表异常:', error)
  }
}

const selectRecipient = (recipient: any) => {
  currentForm.value.recipientId = recipient.id
  currentForm.value.currency = recipient.currency
  // 如果是快速转账且有银行信息，自动填充
  if (activeTab.value === 'quick' && recipient.bankName) {
    quickTransferForm.value.bankName = recipient.bankName
  }
}

const onBankSelect = (bank: any) => {
  currentForm.value.bankName = bank.text
  // 只在快速转账记录 bankCode
  if (activeTab.value === 'quick') {
    quickTransferForm.value.bankCode = bank.value
  }
  showBankSelector.value = false
}

const onCurrencySelect = (action: any) => {
  currentForm.value.currency = action.value
  showCurrencySelector.value = false
}

const handleTransfer = async () => {
  if (activeTab.value === 'quick') {
    await quickTransfer()
  } else {
    await crossBorderTransfer()
  }
}

const quickTransfer = async () => {
  if (!quickTransferForm.value.recipientId) { showToast('请选择收款人'); return }
  if (!quickTransferForm.value.amount) { showToast('请输入金额'); return }
  if (!quickTransferForm.value.transactionPassword) { showToast('请输入密码'); return }

  try {
    const payerAccount = accountStore.getDefaultAccount()
    if (!payerAccount) return
    
    // Mock API call
    showToast('转账处理中...')
    setTimeout(() => {
      showToast('转账成功')
      resetForm()
    }, 1000)
  } catch (error) {
    showToast('转账异常')
  }
}

const crossBorderTransfer = async () => {
   if (!crossBorderForm.value.recipientId) { showToast('请选择收款人'); return }
   if (!crossBorderForm.value.amount) { showToast('请输入金额'); return }
   if (!crossBorderForm.value.swiftCode) { showToast('请输入SWIFT代码'); return }
   
   showToast('汇款申请已提交')
   resetForm()
}

const resetForm = () => {
  quickTransferForm.value.amount = null
  quickTransferForm.value.transactionPassword = ''
  quickTransferForm.value.remark = ''
  crossBorderForm.value.amount = null
}
</script>

<style scoped>
.transfer-page {
  min-height: 100vh;
  background-color: #F8F9FA;
  /* Subtle ambient background gradient */
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(37, 99, 235, 0.04) 0%, transparent 40%),
    radial-gradient(circle at 90% 80%, rgba(236, 72, 153, 0.04) 0%, transparent 40%);
  padding-bottom: 40px;
  max-width: 600px;
  margin: 0 auto;
  font-family: var(--font-body);
}

/* Header */
.page-header-simple {
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(248, 249, 250, 0.8);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-left { display: flex; align-items: center; gap: 16px; }
.back-btn { font-size: 22px; color: var(--text-main); transition: opacity 0.2s; }
.back-btn:active { opacity: 0.6; }
.page-title { font-size: 20px; font-weight: 800; color: var(--text-main); margin: 0; letter-spacing: -0.5px; }
.history-btn { 
  font-size: 14px; font-weight: 600; color: var(--text-sub); 
  padding: 8px 16px; background: rgba(0,0,0,0.04); border-radius: 20px;
  transition: all 0.2s;
}
.history-btn:active { transform: scale(0.95); background: rgba(0,0,0,0.08); }

/* Segment Control */
.segment-wrapper { padding: 8px 24px 24px; position: relative; z-index: 10; }
.segment-control {
  display: flex;
  padding: 5px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
  box-shadow: 
    0 4px 20px rgba(0,0,0,0.02),
    inset 0 1px 0 rgba(255,255,255,0.4);
  border: 1px solid rgba(255,255,255,0.4);
}
.segment-item {
  flex: 1;
  text-align: center;
  padding: 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-sub);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
}
.segment-item.active {
  background: white;
  color: var(--color-primary);
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}

/* Main Content */
.main-content { padding: 0 24px; }

/* Transaction Card - The Core Visual Anchor */
.transaction-card {
  padding: 32px 24px;
  background: white;
  border-radius: 32px;
  box-shadow: 
    0 12px 40px -12px rgba(0, 0, 0, 0.08),
    0 2px 12px -4px rgba(0, 0, 0, 0.02),
    0 0 0 1px rgba(0,0,0,0.02);
  position: relative;
  overflow: hidden; /* For potential decorative elements */
}

/* Amount Section */
.amount-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.label-text { font-size: 15px; color: var(--text-sub); font-weight: 600; }
.currency-switcher { 
  display: flex; align-items: center; gap: 6px; 
  font-size: 14px; font-weight: 700; color: var(--color-primary);
  background: #EFF6FF; padding: 6px 14px; border-radius: 16px;
  transition: background 0.2s;
}
.currency-switcher:active { background: #DBEAFE; }

.amount-display { 
  display: flex; align-items: center; justify-content: center; 
  gap: 8px; margin-bottom: 32px; 
  position: relative;
}
.symbol { font-size: 40px; font-weight: 600; color: var(--text-main); margin-top: 8px; opacity: 0.8; }
.hero-input {
  width: 100%;
  border: none;
  background: transparent;
  font-size: 64px; /* Bigger impact */
  font-weight: 800;
  color: var(--text-main);
  text-align: center;
  caret-color: var(--color-primary);
  padding: 0;
  letter-spacing: -2px;
  /* Fix input width issue */
  min-width: 100px; 
}
.hero-input:focus { outline: none; }
.hero-input::placeholder { color: #E5E7EB; font-weight: 700; }

/* Stylish Divider */
.divider-dashed { 
  height: 1px; 
  background-image: linear-gradient(to right, #E5E7EB 50%, transparent 50%);
  background-size: 12px 1px;
  background-repeat: repeat-x;
  margin: 0 0 24px; 
}

/* Flow Section */
.flow-section { position: relative; }
.flow-label { 
  font-size: 13px; color: var(--text-sub); font-weight: 600; 
  margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center;
}
.link-action { 
  color: var(--color-primary); font-size: 13px; font-weight: 600; 
  display: flex; align-items: center; gap: 4px;
  padding: 4px 8px; border-radius: 8px;
  transition: background 0.2s;
}
.link-action:active { background: #EFF6FF; }

/* From Item */
.from-item {
  background: linear-gradient(145deg, #F9FAFB 0%, #F3F4F6 100%);
  padding: 16px;
  border-radius: 20px;
  margin-bottom: 8px; 
  border: 1px solid rgba(0,0,0,0.03);
  transition: all 0.2s ease;
}
.from-item:active { 
  transform: scale(0.98); 
  background: #EFF6FF;
  border-color: rgba(37, 99, 235, 0.1); 
}
.flow-content { display: flex; align-items: center; gap: 16px; }
.bank-icon-small { 
  width: 44px; height: 44px; border-radius: 14px; 
  display: flex; align-items: center; justify-content: center; 
  font-size: 22px; flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.1);
}
.bg-blue-soft { background: white; color: #2563EB; } /* White icon on grey bg looks cleaner */
.info-text { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.main-text { font-size: 16px; font-weight: 700; color: var(--text-main); }
.sub-text { font-size: 13px; color: var(--text-sub); font-weight: 500; }
.arrow-gray { color: #D1D5DB; font-size: 18px; }

/* Connector - Enhanced */
.flow-connector {
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  margin: -8px 0; /* Pull closer */
}
.connector-line { 
  position: absolute; width: 2px; height: 100%; 
  background: repeating-linear-gradient(to bottom, #E5E7EB 0, #E5E7EB 4px, transparent 4px, transparent 8px); 
}
.connector-icon { 
  width: 24px; height: 24px; background: white; 
  border-radius: 50%; border: 1px solid #E5E7EB;
  color: #9CA3AF; font-size: 14px;
  display: flex; align-items: center; justify-content: center;
  position: relative; z-index: 2;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

/* To Item */
.to-item {
  background: white;
  padding: 16px 0 0;
}
.recipient-placeholder {
  padding: 16px;
  background: #F9FAFB;
  border: 2px dashed #E5E7EB;
  border-radius: 16px;
  text-align: center;
  color: #9CA3AF; font-weight: 600; font-size: 14px;
  transition: all 0.2s;
}
.recipient-placeholder:active { background: #EFF6FF; border-color: #BFDBFE; color: var(--color-primary); }

.recipient-scroll-box {
  display: flex; gap: 12px; overflow-x: auto; padding: 8px 4px 16px; /* Extra bottom padding for shadow */
  scrollbar-width: none;
  mask-image: linear-gradient(to right, black 90%, transparent 100%);
}
.recipient-scroll-box::-webkit-scrollbar { display: none; }

.recipient-avatar-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  min-width: 68px; cursor: pointer; transition: transform 0.2s;
}
.recipient-avatar-item:active { transform: scale(0.95); }

.avatar-circle {
  width: 56px; height: 56px; border-radius: 22px;
  background: #F3F4F6; color: var(--text-sub);
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; font-weight: 700;
  position: relative; transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  border: 2px solid transparent;
}
.recipient-avatar-item.active .avatar-circle {
  background: var(--color-primary); color: white;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.3);
  transform: translateY(-4px);
}
.check-mark {
  position: absolute; bottom: -4px; right: -4px;
  background: white; color: var(--color-primary);
  border-radius: 50%; width: 22px; height: 22px;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; border: 3px solid white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.avatar-name { 
  font-size: 12px; color: var(--text-sub); font-weight: 600; 
  max-width: 72px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; 
}
.recipient-avatar-item.active .avatar-name { color: var(--color-primary); font-weight: 700; }

/* Details Card */
.details-card { 
  padding: 0; 
  background: white; border-radius: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.02);
  border: 1px solid rgba(0,0,0,0.02);
  overflow: hidden;
}
.form-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 20px 24px;
  font-size: 15px;
  transition: background 0.2s;
}
.form-row:active { background: #F9FAFB; }

.row-label { color: var(--text-main); font-weight: 600; min-width: 90px; }
.row-value { display: flex; align-items: center; gap: 8px; color: var(--text-main); font-weight: 600; justify-content: flex-end; flex: 1; }
.row-input { 
  border: none; text-align: right; flex: 1; 
  font-weight: 600; color: var(--text-main); 
  font-size: 15px; outline: none; background: transparent;
}
.row-input::placeholder { color: #D1D5DB; font-weight: 400; }
.text-placeholder { color: #9CA3AF; font-weight: 400; }
.divider-line { height: 1px; background: #F3F4F6; margin: 0 24px; }
.mt-4 { margin-top: 20px; }

/* Footer */
.action-footer { margin-top: 32px; padding: 0 8px; }
.btn-block { 
  width: 100%; height: 64px; border-radius: 32px; 
  display: flex; align-items: center; justify-content: center; gap: 10px;
  background: var(--color-primary); /* Fallback */
  background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
  box-shadow: 
    0 12px 30px -8px rgba(37, 99, 235, 0.4),
    inset 0 1px 0 rgba(255,255,255,0.2);
  color: white; border: none;
  transition: transform 0.1s, box-shadow 0.2s;
}
.btn-block:active { transform: scale(0.98); box-shadow: 0 6px 20px -4px rgba(37, 99, 235, 0.3); }
.btn-text { font-size: 18px; font-weight: 700; letter-spacing: 0.5px; }
.btn-amount { font-size: 16px; font-weight: 500; opacity: 0.9; background: rgba(255,255,255,0.15); padding: 2px 10px; border-radius: 12px; }

.selected-recipient-info {
  margin-top: 12px; padding: 10px 16px; 
  background: #F0FDF4; /* Light green for success/confirmed feel */
  color: #15803D;
  border-radius: 14px;
  font-size: 13px; font-weight: 600; 
  display: flex; align-items: center; gap: 8px;
  animation: slideDown 0.3s ease;
}
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>