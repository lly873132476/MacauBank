<template>
  <div class="transfer-page">
    <!-- 顶部导航栏已移除 -->

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

      <!-- 右上角记录入口，只保留图标 -->
      <div class="record-entry" @click="$router.push('/transfer/records')">
        <van-icon name="orders-o" size="20" />
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

          <!-- 连接线 (已移除) -->
          <div class="flow-gap"></div>

          <!-- 收款方 -->
          <div class="flow-item to-item">
            <div class="flow-label">
              收款人
              <span class="link-action" @click="$router.push('/transfer/recipient/add')">
                <van-icon name="plus" /> 新增
              </span>
            </div>

            <!-- 常用收款人列表 -->
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
          </div>
        </div>
      </div>

      <!-- 2. 补充信息卡片 -->
      <div class="app-card details-card mt-4">
        <!-- 收款账号 (新增到详情卡片中) -->
        <div class="form-row">
          <span class="row-label">收款账号</span>
          <input 
            v-model="currentForm.accountNumber" 
            type="text" 
            class="row-input font-num" 
            placeholder="输入账号"
            :readonly="!!selectedRecipient"
            :class="{ 'input-readonly': !!selectedRecipient }"
          />
        </div>

        <div class="divider-line"></div>

        <!-- 仅在快速转账且未自动填充银行时显示银行选择 -->
        <div v-if="activeTab === 'quick'" 
             class="form-row" 
             :class="{ 'clickable': !selectedRecipient, 'input-readonly': !!selectedRecipient }"
             @click="openBankSelector">
          <span class="row-label">收款银行</span>
          <div class="row-value">
            <span :class="{ 'text-placeholder': !currentForm.bankName }">
              {{ currentForm.bankName || '请选择' }}
            </span>
            <van-icon name="arrow" class="arrow-gray" v-if="!selectedRecipient" />
          </div>
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

        <div class="divider-line"></div>

        <div class="form-row">
          <span class="row-label">转账备注</span>
          <input 
            v-model="currentForm.remark" 
            type="text" 
            class="row-input" 
            placeholder="选填"
            :readonly="!!selectedRecipient"
            :class="{ 'input-readonly': !!selectedRecipient }"
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
        :default-index="bankPickerDefaultIndex"
        @confirm="onBankSelect"
        @cancel="showBankSelector = false"
      />
    </van-popup>

    <van-action-sheet
      v-model:show="showCurrencySelector"
      :actions="currencyActions"
      cancel-text="取消"
      @select="handleCurrencySelect"
    />

    <van-action-sheet
      v-model:show="showAccountSelector"
      :actions="accountActions"
      cancel-text="取消"
      @select="onAccountSelect"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAccountStore } from '../stores/account'
import { useUiStore } from '../stores/ui' // Import UI store
import { showToast } from 'vant'
import { transferApi } from '../services/api'

const { t } = useI18n()
const accountStore = useAccountStore()
const uiStore = useUiStore() // Init UI store

const activeTab = ref('quick')
const recipients = ref<any[]>([])
const showBankSelector = ref(false)
const showAccountSelector = ref(false)
const showCurrencySelector = ref(false)
const bankPickerDefaultIndex = ref(0); // 用于存储van-picker默认选中项的索引

// 监听弹窗状态，控制全局UI隐藏
watch([showBankSelector, showCurrencySelector, showAccountSelector], ([bank, currency, account]) => {
  uiStore.setPopupOpen(bank || currency || account)
})

// 统一表单状态管理
const quickTransferForm = ref({
  recipientId: '',
  accountNumber: '', // Added account number
  bankName: '',
  bankCode: '',
  currency: 'MOP',
  amount: null as number | null,
  transactionPassword: '',
  remark: ''
})

const crossBorderForm = ref({
  recipientId: '',
  accountNumber: '', // Added account number
  currency: 'MOP',
  amount: null as number | null,
  transactionPassword: '',
  swiftCode: '',
  bankName: '',
  bankCode: '', // 添加 bankCode 属性
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

const accountActions = computed(() => {
  return accountStore.accounts.map(acc => ({
    name: `${acc.name} - ${acc.currencyCode} (${acc.balance})`,
    value: acc.id
  }))
})

onMounted(async () => {
  await Promise.all([
    loadRecipients(),
    accountStore.fetchAccounts()
  ])
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
  // Auto-fill account number
  currentForm.value.accountNumber = recipient.account || '' 
  // 如果是快速转账且有银行信息，自动填充
  if (activeTab.value === 'quick' && recipient.bankName) {
    quickTransferForm.value.bankName = recipient.bankName
    quickTransferForm.value.bankCode = recipient.bankCode // 回填 bankCode
  }
}

const onBankSelect = ({ selectedOptions }: { selectedOptions: any[] }) => { // 接收 selectedOptions

  if (selectedOptions.length > 0) {

    currentForm.value.bankName = selectedOptions[0].text

    currentForm.value.bankCode = selectedOptions[0].value // 更新 bankCode

  }

  showBankSelector.value = false

}

const onAccountSelect = (item: any) => {
  showAccountSelector.value = false
  // Update logic if we were storing selected account, but currently just UI mock
  console.log('Selected account:', item)
}

const onCurrencySelect = (option: any, index: number) => { // 修复函数签名
  currentForm.value.currency = option.value
  showCurrencySelector.value = false
}

const handleCurrencySelect = (option: any, index: number) => {
  onCurrencySelect(option, index);
};


// 打开银行选择器

const openBankSelector = () => {

  if (!selectedRecipient.value) { // 只有在未选择收款人时才允许手动选择银行

    const index = macauBanks.findIndex(bank => bank.value === currentForm.value.bankCode);

    bankPickerDefaultIndex.value = index !== -1 ? index : 0; // 找不到则默认选中第一个

    showBankSelector.value = true;

  }

};



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
  min-height: calc(100vh - 80px - 100px); /* 适应单屏，减去App.vue的padding-top和padding-bottom */
  overflow-y: auto; /* 允许内容在需要时滚动 */
  background-color: var(--bg-app);
  /* Subtle ambient background gradient */
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(37, 99, 235, 0.04) 0%, transparent 40%),
    radial-gradient(circle at 90% 80%, rgba(236, 72, 153, 0.04) 0%, transparent 40%);
  padding-bottom: 0; /* Completely removed to prevent whitespace */
  max-width: 600px;
  margin: 0 auto;
  font-family: var(--font-body);
}

/* Header Styles Removed */

/* Segment Control */
.segment-wrapper { 
  padding: 0 16px 24px 16px; /* 移除padding-top，调整padding-bottom */
  position: relative; 
  z-index: 10; 
  display: flex;
  justify-content: center;
  align-items: center;
}

.record-entry {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  background: var(--bg-surface); /* Theme aware */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-sub);
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid rgba(255,255,255,0.1);
}
.record-entry:active { 
  transform: translateY(-50%) scale(0.92);
  background: var(--bg-app);
  color: var(--color-primary); 
}
/* .record-text { font-size: 10px; font-weight: 600; } */ /* 移除文本样式 */

.segment-control {
  flex: 1; /* Allow it to take available space */
  margin: 0 48px; /* Leave space for the button on the right (and keep symmetric on left) */
  display: flex;
  padding: 5px;
  border-radius: 18px;
  background: var(--bg-surface); /* Theme aware */
  backdrop-filter: blur(12px);
  box-shadow: 
    0 4px 20px rgba(0,0,0,0.02),
    inset 0 1px 0 rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.1);
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
  background: var(--bg-app);
  color: var(--color-primary);
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}

/* Main Content */
.main-content { padding: 0 24px; }

/* Transaction Card - The Core Visual Anchor */
.transaction-card {
  padding: 20px 24px; /* 减少顶部内边距 */
  background: var(--bg-surface);
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
  background: var(--bg-app); padding: 6px 14px; border-radius: 16px;
  transition: background 0.2s;
}
.currency-switcher:active { opacity: 0.8; }

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
.hero-input::placeholder { color: var(--text-sub); font-weight: 700; opacity: 0.3; }

/* Stylish Divider */
.divider-dashed { 
  height: 1px; 
  background-image: linear-gradient(to right, var(--text-sub) 50%, transparent 50%);
  background-size: 12px 1px;
  background-repeat: repeat-x;
  margin: 0 0 24px; 
  opacity: 0.2;
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
.link-action:active { background: var(--bg-app); }

/* From Item */
.from-item {
  background: var(--bg-app);
  padding: 16px;
  border-radius: 20px;
  margin-bottom: 8px; 
  border: 1px solid transparent;
  transition: all 0.2s ease;
}
.from-item:active { 
  transform: scale(0.98); 
  border-color: rgba(37, 99, 235, 0.1); 
}
.flow-content { display: flex; align-items: center; gap: 16px; }
.bank-icon-small { 
  width: 44px; height: 44px; border-radius: 14px; 
  display: flex; align-items: center; justify-content: center; 
  font-size: 22px; flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
  background: var(--bg-surface);
  color: var(--color-primary);
}
.bg-blue-soft { background: var(--bg-surface); color: var(--color-primary); } /* Updated for dark mode */
.info-text { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.main-text { font-size: 16px; font-weight: 700; color: var(--text-main); }
.sub-text { font-size: 13px; color: var(--text-sub); font-weight: 500; }
.arrow-gray { color: var(--text-sub); font-size: 18px; opacity: 0.5; }

/* Connector - Removed styles */
.flow-gap { height: 16px; }

/* To Item */
.to-item {
  background: var(--bg-surface);
  padding: 16px 0 0;
}

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
  background: var(--bg-app); color: var(--text-sub);
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
  background: var(--bg-surface); border-radius: 24px;
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
.form-row:active { background: var(--bg-app); }

.row-label { color: var(--text-main); font-weight: 600; min-width: 90px; }
.row-value { display: flex; align-items: center; gap: 8px; color: var(--text-main); font-weight: 600; justify-content: flex-end; flex: 1; }
.row-input { 
  border: none; text-align: right; flex: 1; 
  font-weight: 600; color: var(--text-main); 
  font-size: 15px; outline: none; background: transparent;
}
.row-input::placeholder { color: var(--text-sub); font-weight: 400; opacity: 0.5; }
.text-placeholder { color: var(--text-sub); font-weight: 400; opacity: 0.5; }
.divider-line { height: 1px; background: var(--bg-app); margin: 0 24px; }
.mt-4 { margin-top: 20px; }

/* Footer */
.action-footer { 
  margin-top: 32px; 
  padding: 0 8px; 
  display: flex;
  flex-direction: column;
  gap: 20px; /* Increased gap */
}
.btn-block { 
  width: 100%; height: 64px; border-radius: 32px; 
  display: flex; align-items: center; justify-content: center; gap: 10px;
  transition: transform 0.1s, box-shadow 0.2s;
  border: none;
}

/* Primary Button */
.btn-primary {
  background: var(--color-primary); /* Fallback */
  background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
  box-shadow: 
    0 12px 30px -8px rgba(37, 99, 235, 0.4),
    inset 0 1px 0 rgba(255,255,255,0.2);
  color: white; 
}
.btn-primary:active { transform: scale(0.98); box-shadow: 0 6px 20px -4px rgba(37, 99, 235, 0.3); }

/* Secondary Button - Refined */
.btn-secondary {
  height: auto; /* Fluid height */
  padding: 12px 0;
  background: transparent;
  color: var(--text-sub);
  box-shadow: none;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.btn-secondary:active {
  transform: scale(0.95);
  opacity: 0.7;
  background: transparent; /* Prevent background flash */
}
.btn-secondary-icon {
  width: 32px; height: 32px;
  border-radius: 50%;
  background: rgba(255,255,255,0.8);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  color: var(--color-primary);
}
.btn-text-sm { font-size: 14px; font-weight: 600; color: var(--text-sub); }

.btn-text { font-size: 18px; font-weight: 700; letter-spacing: 0.5px; }
.btn-amount { font-size: 16px; font-weight: 500; opacity: 0.9; background: rgba(255,255,255,0.15); padding: 2px 10px; border-radius: 12px; }

/* Account Input Styles */
.account-input-wrapper {
  margin: 12px 16px 0;
  padding: 12px 16px;
  background: #F9FAFB;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid transparent;
  transition: all 0.2s;
}
.account-input-wrapper:focus-within {
  background: white;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}
.input-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-sub);
  white-space: nowrap;
}
.account-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-main);
  outline: none;
  width: 100%;
}
.account-input::placeholder { color: #D1D5DB; font-weight: 400; }

.input-readonly {
  background-color: var(--bg-app); /* Theme aware */
  color: var(--text-sub); /* Theme aware */
  cursor: default; /* 改变鼠标样式 */
}
.input-readonly .row-value {
  color: var(--text-sub);
}

.selected-recipient-info {
  margin-top: 12px; padding: 10px 16px; 
  background: rgba(16, 185, 129, 0.1); /* Theme aware tint */
  color: #10B981;
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