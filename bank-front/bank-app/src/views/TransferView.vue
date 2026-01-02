<template>
  <div class="transfer-page">
    <!-- 未开户拦截遮罩 -->
    <div v-if="!authStore.isAccountOpened" class="access-denied-mask">
      <div class="mask-content">
        <div class="icon-circle-large">
          <van-icon name="lock" />
        </div>
        <h2>服务未开通</h2>
        <p>转账汇款功能仅对实名开户用户开放。<br>请先完成身份认证。</p>
        <button class="btn-primary btn-block clickable mt-6" @click="$router.push('/open-account')">
          立即开通数字账户
        </button>
        <div class="back-link clickable" @click="$router.back()">
          返回上一页
        </div>
      </div>
    </div>

    <!-- 已开户正常显示内容 -->
    <template v-else>
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

        <!-- 1. 核心卡片：付款与金额 (Out) -->
        <div class="app-card out-card">
          <!-- 付款账户 -->
          <div class="payer-section clickable" @click="showAccountSelector = true">
            <div class="section-label">付款账户</div>
            <div class="account-row">
              <div class="bank-icon-circle bg-blue-soft">
                <van-icon name="card" />
              </div>
              <div class="info-content">
                <div class="main-text">{{ selectedAccount?.accountName || '请选择付款账户' }}</div>
                <div class="sub-text font-num" v-if="selectedAccount">
                   可用余额 {{ selectedAccount.currencyCode }} {{ selectedAccount.balance?.toLocaleString() }}
                </div>
              </div>
              <van-icon name="arrow" class="arrow-gray" />
            </div>
          </div>

          <div class="divider-dashed"></div>

          <!-- 金额输入 -->
          <div class="amount-section">
            <div class="amount-header">
              <span class="label-text">转账金额</span>
            </div>
            <div class="amount-display">
              <div class="currency-switcher clickable" @click="showCurrencySelector = true">
                <span class="curr-code">{{ currentForm.currency }}</span>
                <van-icon name="arrow-down" />
              </div>
              <input
                v-model.number="currentForm.amount"
                type="number"
                class="hero-input font-num"
                placeholder="0.00"
              />
            </div>
          </div>
        </div>

        <!-- 流程连接符 -->
        <div class="transfer-flow-connector">
          <div class="connector-line"></div>
          <div class="connector-arrow">
            <van-icon name="down" />
          </div>
        </div>

        <!-- 2. 详情卡片：收款与备注 (In) -->
        <div class="app-card in-card">
          <div class="section-label">
            收款信息
            <span class="link-action clickable" @click="$router.push('/transfer/recipient/add')">
              <van-icon name="plus" /> 管理
            </span>
          </div>

          <!-- 常用收款人列表 -->
          <div v-if="recipients.length > 0" class="payee-scroll-box">
            <div
              v-for="recipient in recipients"
              :key="recipient.id"
              class="payee-avatar-item"
              :class="{ active: currentForm.recipientId === recipient.id }"
              @click="selectRecipient(recipient)"
            >
              <div class="avatar-circle">
                {{ recipient.payeeName ? recipient.payeeName.charAt(0) : '?' }}
                <div class="check-mark" v-if="currentForm.recipientId === recipient.id">
                  <van-icon name="success" />
                </div>
              </div>
              <span class="avatar-name">{{ recipient.payeeName }}</span>
            </div>
          </div>
          <!-- 空状态提示 -->
          <div v-else class="empty-recipient-tip clickable" @click="$router.push('/transfer/recipient/add')">
            <van-icon name="friends-o" /> 暂无常用收款人，点击添加
          </div>

          <!-- 账号输入 -->
          <div class="account-input-box">
            <div class="input-icon"><van-icon name="debit-pay" /></div>
            <input 
              v-model="currentForm.toAccount"
              type="text" 
              class="account-input font-num"
              placeholder="请输入收款人账号"
              @input="handleAccountInput"
            />
            <van-icon v-if="currentForm.toAccount" name="cross" class="clear-icon" @click="clearAccount" />
          </div>

          <!-- 姓名输入 (新增) -->
          <div class="account-input-box mt-3">
            <div class="input-icon"><van-icon name="user-o" /></div>
            <input 
              v-model="currentForm.toAccountName"
              type="text" 
              class="account-input"
              :placeholder="t('transfer.recipientNamePlaceholder')"
            />
          </div>

          <!-- 选中收款人后的确认条 -->
          <div v-if="selectedRecipient" class="payee-confirm-strip">
            <div class="confirm-info">
              <van-icon name="manager-o" /> 
              <span>{{ selectedRecipient.payeeName }} · {{ currentForm.bankName || selectedRecipient.bankName }}</span>
            </div>
            <div class="edit-btn clickable" @click.stop="goToEditRecipient">
              <van-icon name="edit" />
            </div>
          </div>

          <div class="divider-line mt-4"></div>

          <!-- 银行选择 (手动模式下显示) -->
          <div v-if="!currentForm.recipientId" class="form-row clickable" @click="showBankSelector = true">
            <span class="row-label">收款银行</span>
            <div class="row-value">
              <span :class="{ 'text-placeholder': !currentForm.bankName }">
                {{ currentForm.bankName || '请选择' }}
              </span>
              <van-icon name="arrow" class="arrow-gray" />
            </div>
          </div>

          <!-- 跨境模式：银行代码输入 (已移除，改为选择) -->
          
          <div v-if="!currentForm.recipientId" class="divider-line"></div>

          <!-- 备注输入 -->
          <div class="form-row">
            <span class="row-label">转账备注</span>
            <input
              v-model="currentForm.remark"
              type="text"
              class="row-input"
              placeholder="选填 (最多20字)"
              maxlength="20"
            />
          </div>

          <!-- 跨境汇款特有字段 -->
          <template v-if="activeTab === 'crossborder'">
            <div class="divider-line"></div>
            <div class="form-row">
               <span class="row-label">SWIFT 代码</span>
               <input v-model="crossBorderForm.swiftCode" class="row-input font-num" placeholder="必填" />
            </div>
          </template>
        </div>

        <!-- 底部按钮 -->
        <div class="action-footer">
          <button class="btn-primary btn-block clickable" @click="preCheckAndPay" :disabled="loading">
            <van-loading v-if="loading" size="24" color="#fff" />
            <template v-else>
              <span class="btn-text">确认转账</span>
            </template>
          </button>
        </div>

      </div>

      <!-- 弹窗组件 -->
      <van-action-sheet 
        v-model:show="showAccountSelector" 
        title="选择付款账户"
        safe-area-inset-bottom
      >
        <div class="account-list-sheet">
          <div 
              v-for="acc in accountStore.accounts" 
              :key="acc.displayId"
              class="account-item clickable"
              @click="onAccountSelect(acc)"
          >
              <div class="acc-icon-wrap">
                <van-icon name="card" />
              </div>
              <div class="acc-info-wrap">
                  <div class="acc-name-text">{{ acc.accountName }}</div>
                  <div class="acc-no-text font-num">{{ acc.accountNo }}</div>
              </div>
              <div class="acc-bal-text font-num">
                {{ acc.currencyCode }} {{ acc.balance?.toLocaleString() }}
              </div>
              <div class="check-icon-wrap" v-if="selectedAccount?.displayId === acc.displayId">
                <van-icon name="success" />
              </div>
          </div>
        </div>
      </van-action-sheet>

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

      <!-- 交易密码键盘 -->
      <van-action-sheet v-model:show="showPasswordSheet" title="请输入交易密码">
        <div class="password-sheet-content">
          <div class="sheet-amount-display font-num">{{ currentForm.currency }} {{ currentForm.amount }}</div>
          <div class="sheet-info-row">
            <span class="label">收款人</span>
            <span class="value">{{ selectedRecipient?.payeeName || currentForm.toAccount }}</span>
          </div>
          <div class="sheet-info-row">
            <span class="label">付款账户</span>
            <span class="value">MOP 储蓄账户</span>
          </div>
          
          <div class="password-input-wrapper" @click.stop="showKeyboard = true">
            <van-password-input
              :value="tempPassword"
              :focused="showKeyboard"
              :length="6"
              :gutter="10"
            />
          </div>
        </div>
      </van-action-sheet>

      <van-number-keyboard
        v-model="tempPassword"
        :show="showKeyboard"
        theme="custom"
        :maxlength="6"
        close-button-text="完成"
        :z-index="5000"
        teleport="body"
        @blur="showKeyboard = false"
      />

    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { transferApi, payeeApi } from '../services/api'
import { useAuthStore } from '../stores/auth'
import { useAccountStore } from '../stores/account'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()
const accountStore = useAccountStore()

const loading = ref(false)
const activeTab = ref('quick')
const recipients = ref<any[]>([])
const showBankSelector = ref(false)
const showAccountSelector = ref(false)
const showCurrencySelector = ref(false)
const selectedAccount = ref<any>(null)

// 密码相关状态
const showPasswordSheet = ref(false)
const showKeyboard = ref(false)
const tempPassword = ref('')

// 监听密码输入，满6位自动提交
watch(tempPassword, async (val) => {
  // 实时同步
  currentForm.value.transactionPassword = val
  
  if (val.length === 6) {
    // 1. 先关闭键盘和弹窗，避免视觉干扰
    showKeyboard.value = false
    showPasswordSheet.value = false
    
    // 2. 确保 DOM 更新后再提交
    setTimeout(() => {
      if (activeTab.value === 'quick') {
        quickTransfer()
      } else {
        crossBorderTransfer()
      }
    }, 100)
  }
})

// 弹窗关闭时重置
watch(showPasswordSheet, (val) => {
  if (val) {
    tempPassword.value = ''
    setTimeout(() => showKeyboard.value = true, 300)
  } else {
    showKeyboard.value = false
  }
})

// 统一表单状态管理
const quickTransferForm = ref({
  recipientId: '',
  toAccount: '', 
  toAccountName: '',
  bankName: '',
  bankCode: '',
  currency: 'MOP',
  amount: null as number | null,
  transactionPassword: '',
  remark: ''
})

const crossBorderForm = ref({
  recipientId: '',
  toAccount: '',
  toAccountName: '',
  currency: 'HKD',
  amount: null as number | null,
  transactionPassword: '',
  swiftCode: '',
  toBankCode: '',
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
    return ['MOP', 'HKD', 'CNY', 'USD'].map(c => ({ name: c, value: c }))
})

onMounted(async () => {
  await authStore.fetchUserInfo()
  if (authStore.isAccountOpened) {
    await loadRecipients()
    const res = await accountStore.fetchAccounts()
    if (res.success && accountStore.accounts.length > 0) {
      // 默认选中第一个账户
      selectedAccount.value = accountStore.accounts[0]
      // 同步币种
      quickTransferForm.value.currency = selectedAccount.value.currencyCode
      crossBorderForm.value.currency = selectedAccount.value.currencyCode
    }
  }
})

const onAccountSelect = (account: any) => {
  selectedAccount.value = account
  showAccountSelector.value = false
  // 切换账户时，自动切换转账币种
  currentForm.value.currency = account.currencyCode
  // 重置金额，避免币种切换后金额数值不合理
  currentForm.value.amount = null
}

const loadRecipients = async () => {
  try {
    const res = await payeeApi.getPage(1, 50)
    if (res.code === 200) {
      recipients.value = res.data.records
    }
  } catch (error) {
    console.error('获取收款人列表异常:', error)
  }
}

const selectRecipient = (recipient: any) => {
  if (currentForm.value.recipientId === recipient.id) {
    currentForm.value.recipientId = ''
    currentForm.value.toAccount = ''
    currentForm.value.toAccountName = ''
    if (activeTab.value === 'quick') {
       quickTransferForm.value.bankName = ''
       quickTransferForm.value.bankCode = ''
    }
  } else {
    currentForm.value.recipientId = recipient.id
    currentForm.value.currency = recipient.currencyCode
    currentForm.value.toAccount = recipient.accountNo
    currentForm.value.toAccountName = recipient.payeeName
    
    if (recipient.bankName) {
      currentForm.value.bankName = recipient.bankName
      if (activeTab.value === 'quick') {
        quickTransferForm.value.bankCode = recipient.bankCode
      } else {
        // 如果收款人有 bankCode，尝试自动填入跨境的 toBankCode
        crossBorderForm.value.toBankCode = recipient.bankCode || ''
      }
    }
  }
}

const handleAccountInput = () => {
  if (currentForm.value.recipientId) {
    currentForm.value.recipientId = ''
  }
}

const clearAccount = () => {
  currentForm.value.toAccount = ''
  currentForm.value.toAccountName = ''
  currentForm.value.recipientId = ''
  currentForm.value.bankName = ''
  if (activeTab.value === 'quick') {
     quickTransferForm.value.bankCode = ''
  } else {
     crossBorderForm.value.toBankCode = ''
  }
}

const onBankSelect = (bank: any) => {
  currentForm.value.bankName = bank.selectedOptions[0].text
  if (activeTab.value === 'quick') {
    quickTransferForm.value.bankCode = bank.selectedOptions[0].value
  } else {
    // 跨境模式：从选择器获取 BankCode 并赋值
    crossBorderForm.value.toBankCode = bank.selectedOptions[0].value
  }
  showBankSelector.value = false
}

const onCurrencySelect = (action: any) => {
  currentForm.value.currency = action.value
  showCurrencySelector.value = false
}

const goToEditRecipient = () => {
  if (!selectedRecipient.value) return
  const r = selectedRecipient.value
  router.push({
    name: 'recipient-edit',
    params: { id: r.id },
    query: {
      name: r.payeeName,
      bankName: r.bankName,
      bankCode: r.bankCode,
      account: r.accountNo,
      currency: r.currencyCode
    }
  })
}

const preCheckAndPay = () => {
  const form = currentForm.value
  if (!form.toAccount) { showToast('请输入或选择收款账户'); return }
  if (!form.toAccountName) { showToast('请输入收款人姓名'); return }
  if (!form.amount || form.amount <= 0) { showToast('请输入有效金额'); return }
  
  currentForm.value.transactionPassword = ''
  showPasswordSheet.value = true
  showKeyboard.value = true
}

const quickTransfer = async () => {
  const form = quickTransferForm.value
  // 移除密码校验，因为 handleTransfer 只有在密码满6位时才会被触发
  
  loading.value = true
  try {
    if (!selectedAccount.value) { showToast('请选择付款账户'); loading.value = false; return }

    const res = await transferApi.executeTransfer({
        fromAccountNo: selectedAccount.value.accountNo,
        toAccountNo: form.toAccount,
        toAccountName: form.toAccountName,
        amount: form.amount!,
        currencyCode: form.currency,
        transactionPassword: tempPassword.value, // 核心修复：直接使用键盘输入的密码
        idempotentKey: Date.now().toString(),
        transferType: 'INTERNAL',
        remark: form.remark
    })
    
    if (res.code === 200) {
        showDialog({
            title: '转账成功',
            message: `成功向 ${form.toAccountName || form.toAccount} 转账 ${form.amount} ${form.currency}`,
            theme: 'round-button'
        }).then(() => {
            resetForm()
        })
    } else {
        showDialog({ title: '转账失败', message: res.message })
    }
  } catch (error) {
    showToast('交易异常，请重试')
  } finally {
    loading.value = false
  }
}

const crossBorderTransfer = async () => {
  const form = crossBorderForm.value
  
  loading.value = true
  try {
    if (!selectedAccount.value) { showToast('请选择付款账户'); loading.value = false; return }

    // 调用执行转账接口，透传跨境参数
    const res = await transferApi.executeTransfer({
        fromAccountNo: selectedAccount.value.accountNo,
        toAccountNo: form.toAccount,
        toAccountName: form.toAccountName,
        amount: form.amount!,
        currencyCode: form.currency,
        transactionPassword: tempPassword.value,
        idempotentKey: Date.now().toString(),
        transferType: 'CROSS_BORDER',
        remark: form.remark,
        // 跨境特有参数
        swiftCode: form.swiftCode,
        toBankCode: form.toBankCode
    })
    
    if (res.code === 200) {
        showDialog({
            title: '跨境汇款提交成功',
            message: `成功向 ${form.toAccountName || form.toAccount} 汇出 ${form.amount} ${form.currency}`,
            theme: 'round-button'
        }).then(() => {
            resetForm()
        })
    } else {
        showDialog({ title: '汇款失败', message: res.message })
    }
  } catch (error) {
    showToast('交易异常，请重试')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  quickTransferForm.value.amount = null
  quickTransferForm.value.transactionPassword = ''
  quickTransferForm.value.remark = ''
  quickTransferForm.value.toAccount = ''
  quickTransferForm.value.toAccountName = ''
  quickTransferForm.value.recipientId = ''
  
  crossBorderForm.value.amount = null
  crossBorderForm.value.transactionPassword = ''
  crossBorderForm.value.swiftCode = ''
  crossBorderForm.value.toBankCode = ''
  crossBorderForm.value.toAccount = ''
  crossBorderForm.value.toAccountName = ''
  crossBorderForm.value.recipientId = ''
}
</script>

<style scoped>
.transfer-page {
  min-height: 100vh;
  background-color: #F8F9FA;
  background-image:
    radial-gradient(circle at 10% 20%, rgba(37, 99, 235, 0.04) 0%, transparent 40%),
    radial-gradient(circle at 90% 80%, rgba(236, 72, 153, 0.04) 0%, transparent 40%);
  padding-bottom: 40px;
  max-width: 600px;
  margin: 0 auto;
  font-family: var(--font-body);
}

/* Access Denied Mask */
.access-denied-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: #F8F9FA;
  z-index: 999;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 40px; text-align: center;
}
.mask-content {
  background: white; padding: 40px 32px;
  border-radius: 32px; box-shadow: 0 12px 40px -12px rgba(0,0,0,0.1);
  width: 100%; max-width: 320px;
}
.icon-circle-large {
  width: 80px; height: 80px; background: #EFF6FF; color: var(--color-primary);
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 36px; margin: 0 auto 24px;
}
.mask-content h2 { font-size: 20px; font-weight: 800; color: var(--text-main); margin-bottom: 8px; }
.mask-content p { color: var(--text-sub); font-size: 14px; line-height: 1.6; margin-bottom: 32px; }
.back-link { margin-top: 16px; color: var(--text-sub); font-size: 14px; text-decoration: underline; }

/* Header */
.page-header-simple {
  padding: 16px 24px; display: flex; justify-content: space-between; align-items: center;
  background: rgba(248, 249, 250, 0.8); backdrop-filter: blur(10px);
  position: sticky; top: 0; z-index: 100;
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
.segment-wrapper { padding: 8px 24px 20px; position: relative; z-index: 10; }
.segment-control {
  display: flex; padding: 5px; border-radius: 18px;
  background: rgba(255, 255, 255, 0.6); backdrop-filter: blur(12px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.02); border: 1px solid rgba(255,255,255,0.4);
}
.segment-item {
  flex: 1; text-align: center; padding: 10px;
  font-size: 14px; font-weight: 600; color: var(--text-sub);
  border-radius: 14px; cursor: pointer; transition: all 0.3s;
}
.segment-item.active {
  background: white; color: var(--color-primary); font-weight: 700;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}

/* Main Content */
.main-content { padding: 0 20px; }

/* Out Card: Payer + Amount */
.out-card {
  padding: 24px; background: linear-gradient(135deg, #F0F7FF 0%, #FFFFFF 100%);
  border: 1px solid rgba(37, 99, 235, 0.08); border-radius: 24px; margin-bottom: 16px;
}

.payer-section { margin-bottom: 24px; }
.section-label { font-size: 13px; color: var(--text-sub); font-weight: 700; margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center; }
.account-row { display: flex; align-items: center; gap: 14px; }
.bank-icon-circle {
  width: 44px; height: 44px; border-radius: 50%;
  background: white; color: var(--color-primary);
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}
.info-content { flex: 1; }
.main-text { font-size: 15px; font-weight: 700; color: var(--text-main); }
.sub-text { font-size: 12px; color: #6B7280; margin-top: 2px; }
.arrow-gray { color: #D1D5DB; font-size: 16px; }

.divider-dashed { height: 1px; background-image: linear-gradient(to right, #E5E7EB 50%, transparent 50%); background-size: 12px 1px; background-repeat: repeat-x; margin: 0 0 24px; }

.amount-section { display: flex; flex-direction: column; gap: 12px; }
.amount-header { display: flex; align-items: center; }
.label-text { font-size: 14px; color: var(--text-sub); font-weight: 600; }
.amount-display { display: flex; align-items: center; gap: 12px; }
.currency-switcher {
  display: flex; align-items: center; gap: 4px;
  font-size: 14px; font-weight: 700; color: var(--color-primary);
  background: #EFF6FF; padding: 6px 12px; border-radius: 12px;
}
.hero-input {
  flex: 1; border: none; background: transparent;
  font-size: 40px; font-weight: 800; color: var(--text-main);
  text-align: right; outline: none; padding: 0; min-width: 0;
}
.hero-input::placeholder { color: #E5E7EB; font-weight: 700; }

/* Flow Connector */
.transfer-flow-connector {
  height: 40px; display: flex; flex-direction: column; align-items: center; justify-content: center;
  margin: -4px 0; position: relative; z-index: 1;
}
.connector-line { width: 2px; height: 100%; background: #E5E7EB; }
.connector-arrow {
  position: absolute; width: 24px; height: 24px;
  background: white; border: 1px solid #E5E7EB; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #9CA3AF; font-size: 12px; box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

/* In Card: Payee + Details */
.in-card {
  padding: 24px; background: white; border-radius: 24px;
  box-shadow: 0 4px 25px rgba(0,0,0,0.04); margin-bottom: 20px;
}

.payee-scroll-box {
  display: flex; gap: 12px; overflow-x: auto; padding: 4px 0 16px; scrollbar-width: none;
}
.payee-scroll-box::-webkit-scrollbar { display: none; }

.payee-avatar-item {
  display: flex; flex-direction: column; align-items: center; gap: 6px; min-width: 60px; cursor: pointer;
}
.avatar-circle {
  width: 50px; height: 50px; border-radius: 18px;
  background: #F3F4F6; color: #9CA3AF;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; transition: all 0.3s; position: relative;
}
.payee-avatar-item.active .avatar-circle {
  background: var(--color-primary); color: white;
  box-shadow: 0 6px 15px rgba(37, 99, 235, 0.25);
}
.check-mark {
  position: absolute; bottom: -4px; right: -4px;
  background: white; color: var(--color-primary);
  border-radius: 50%; width: 18px; height: 18px;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; border: 2px solid white;
}
.avatar-name { font-size: 11px; color: var(--text-sub); font-weight: 600; }

.link-action { color: var(--color-primary); font-size: 12px; font-weight: 600; display: flex; align-items: center; gap: 2px; }

.account-input-box {
  background: #F9FAFB; border-radius: 14px;
  padding: 12px 16px; display: flex; align-items: center; gap: 12px;
  border: 1px solid transparent;
}
.account-input-box:focus-within { border-color: var(--color-primary); background: white; }
.input-icon { color: var(--color-primary); font-size: 18px; }
.account-input { flex: 1; border: none; background: transparent; outline: none; font-size: 15px; font-weight: 700; color: var(--text-main); }
.clear-icon { color: #9CA3AF; font-size: 14px; }

.payee-confirm-strip {
  margin-top: 12px; padding: 10px 14px;
  background: #F0FDF4; color: #166534;
  border-radius: 12px; display: flex; align-items: center; justify-content: space-between;
  animation: slideDown 0.3s ease;
}
.confirm-info { display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 600; }
.edit-btn { padding: 4px; color: #166534; opacity: 0.6; font-size: 16px; }

.empty-recipient-tip {
  padding: 14px; background: #F9FAFB; border: 1px dashed #E5E7EB;
  border-radius: 14px; color: #9CA3AF; font-size: 13px;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  margin-bottom: 12px;
}

.divider-line { height: 1px; background: #F3F4F6; margin: 0 0; }
.mt-4 { margin-top: 16px; margin-bottom: 16px; }

.form-row { display: flex; align-items: center; justify-content: space-between; padding: 8px 4px; font-size: 14px; }
.row-label { color: var(--text-sub); font-weight: 600; min-width: 80px; }
.row-value { display: flex; align-items: center; gap: 8px; color: var(--text-main); font-weight: 600; justify-content: flex-end; flex: 1; }
.row-input { border: none; text-align: right; font-weight: 600; outline: none; background: transparent; flex: 1; font-size: 14px; color: var(--text-main); }
.text-placeholder { color: #9CA3AF; font-weight: 400; }

/* Footer */
.action-footer { margin-top: 32px; padding: 0 4px; }
.btn-block {
  width: 100%; height: 56px; border-radius: 28px;
  background: var(--gradient-brand); color: white;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  border: none; font-weight: 700; transition: transform 0.1s;
}
.btn-block:active { transform: scale(0.98); }

/* Password Sheet */
.password-sheet-content { padding: 32px 24px 320px; text-align: center; }
.sheet-amount-display { font-size: 32px; font-weight: 800; color: var(--text-main); margin-bottom: 24px; }
.sheet-info-row { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 12px; color: var(--text-sub); }
.sheet-info-row .value { color: var(--text-main); font-weight: 600; }
.password-input-wrapper { margin-top: 32px; cursor: pointer; }

/* 核心修复：强制给密码框加上边框和背景，防止被 Tailwind 重置 */
:deep(.van-password-input__security) {
  height: 50px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}
:deep(.van-password-input__security li) {
  position: relative !important;
  width: 46px !important;
  height: 46px !important;
  border: 1px solid #ebedf0 !important; /* 强制边框 */
  background-color: #f7f8fa !important; /* 强制背景 */
  margin: 0 4px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  border-radius: 8px !important;
  box-sizing: border-box !important;
}
:deep(.van-password-input__cursor) {
  background-color: var(--color-primary) !important;
  width: 1px !important;
  height: 40% !important;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Account Selector Sheet */
.account-list-sheet { 
  padding: 10px 0 80px; /* 大幅增加底部留白 */
  min-height: 200px; /* 确保有足够高度 */
  max-height: 60vh;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
.account-item {
  display: flex; align-items: center; padding: 16px 24px;
  border-bottom: 1px solid #F3F4F6; transition: background 0.2s;
  flex-shrink: 0; /* 防止被压缩 */
}
.account-item:active { background: #F9FAFB; }
.acc-icon-wrap {
  width: 40px; height: 40px; border-radius: 50%;
  background: #EFF6FF; color: var(--color-primary);
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; margin-right: 12px;
}
.acc-info-wrap { flex: 1; }
.acc-name-text { font-size: 15px; font-weight: 700; color: var(--text-main); }
.acc-no-text { font-size: 12px; color: var(--text-sub); margin-top: 2px; }
.acc-bal-text { font-size: 14px; font-weight: 700; color: var(--text-main); margin-right: 12px; }
.check-icon-wrap { color: var(--color-primary); font-size: 18px; }
</style>