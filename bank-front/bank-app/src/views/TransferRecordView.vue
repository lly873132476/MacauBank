<template>
  <div class="transfer-record">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <van-icon name="records" size="28" />
        </div>
        <div class="header-info">
          <h1 class="page-title">转账记录</h1>
          <p class="page-subtitle">
            <van-icon name="arrow" size="12" />
            查看您的转账历史记录
          </p>
        </div>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <div class="filter-item">
        <van-field
          v-model="filterForm.currency"
          label="币种"
          placeholder="请选择币种"
          readonly
          @click="showCurrencyPicker = true"
        />
      </div>
      <div class="filter-item">
        <van-field
          v-model="filterForm.status"
          label="状态"
          placeholder="请选择状态"
          readonly
          @click="showStatusPicker = true"
        />
      </div>
      <div class="filter-actions">
        <van-button type="primary" @click="loadTransferRecords">查询</van-button>
        <van-button @click="resetFilter">重置</van-button>
      </div>
    </div>

    <!-- 转账记录列表 -->
    <div class="records-list">
      <van-empty v-if="transferRecords.length === 0" description="暂无转账记录" />
      <div v-else class="record-item" v-for="record in transferRecords" :key="record.id">
        <div class="record-header">
          <div class="record-type" :class="getTransferTypeClass(record.transferType)">
            {{ getTransferTypeText(record.transferType) }}
          </div>
          <div class="record-status" :class="getStatusClass(record.status)">
            {{ getStatusText(record.status) }}
          </div>
        </div>
        <div class="record-content">
          <div class="record-amount">
            <span class="currency">{{ record.currencyCode }}</span>
            <span class="amount">{{ record.amount.toFixed(2) }}</span>
          </div>
          <div class="record-info">
            <div class="info-item">
              <span class="label">收款账户:</span>
              <span class="value">{{ formatAccountNumber(record.toAccountNumber) }}</span>
            </div>
            <div class="info-item">
              <span class="label">手续费:</span>
              <span class="value">{{ record.fee.toFixed(2) }} {{ record.currencyCode }}</span>
            </div>
            <div class="info-item">
              <span class="label">时间:</span>
              <span class="value">{{ formatTime(record.createTime) }}</span>
            </div>
            <div class="info-item" v-if="record.remark">
              <span class="label">备注:</span>
              <span class="value">{{ record.remark }}</span>
            </div>
          </div>
        </div>
        <div class="record-footer">
          <span class="transaction-id">交易ID: {{ record.id }}</span>
        </div>
      </div>
    </div>

    <!-- 币种选择器 -->
    <van-popup v-model:show="showCurrencyPicker" round position="bottom">
      <van-picker
        :columns="currencyColumns"
        @confirm="onCurrencyConfirm"
        @cancel="showCurrencyPicker = false"
      />
    </van-popup>

    <!-- 状态选择器 -->
    <van-popup v-model:show="showStatusPicker" round position="bottom">
      <van-picker
        :columns="statusColumns"
        @confirm="onStatusConfirm"
        @cancel="showStatusPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAccountStore } from '../stores/account'
import { showToast } from 'vant'

const { t } = useI18n()
const accountStore = useAccountStore()

const transferRecords = ref<any[]>([])
const showCurrencyPicker = ref(false)
const showStatusPicker = ref(false)

// 筛选表单
const filterForm = ref({
  currency: '',
  status: ''
})

// 币种选项
const currencyColumns = [
  { text: '全部', value: '' },
  { text: 'MOP', value: 'MOP' },
  { text: 'HKD', value: 'HKD' },
  { text: 'CNY', value: 'CNY' },
  { text: 'USD', value: 'USD' }
]

// 状态选项
const statusColumns = [
  { text: '全部', value: '' },
  { text: '成功', value: 'SUCCESS' },
  { text: '失败', value: 'FAILED' },
  { text: '处理中', value: 'PROCESSING' }
]

// 页面加载时获取转账记录
onMounted(async () => {
  await loadTransferRecords()
})

// 加载转账记录
const loadTransferRecords = async () => {
  try {
    const result = await accountStore.fetchTransferRecords(
      undefined,
      filterForm.value.currency || undefined,
      filterForm.value.status || undefined
    )
    if (result.success) {
      transferRecords.value = result.data
    } else {
      showToast('获取转账记录失败: ' + result.message)
    }
  } catch (error) {
    console.error('获取转账记录异常:', error)
    showToast('获取转账记录异常')
  }
}

// 重置筛选条件
const resetFilter = () => {
  filterForm.value.currency = ''
  filterForm.value.status = ''
  loadTransferRecords()
}

// 币种选择确认
const onCurrencyConfirm = ({ selectedOptions }: any) => {
  filterForm.value.currency = selectedOptions[0].value
  showCurrencyPicker.value = false
}

// 状态选择确认
const onStatusConfirm = ({ selectedOptions }: any) => {
  filterForm.value.status = selectedOptions[0].value
  showStatusPicker.value = false
}

// 获取转账类型文本
const getTransferTypeText = (type: string) => {
  switch (type) {
    case 'NORMAL':
      return '普通转账'
    case 'CROSS_BORDER':
      return '跨境汇款'
    default:
      return type
  }
}

// 获取转账类型样式类
const getTransferTypeClass = (type: string) => {
  switch (type) {
    case 'NORMAL':
      return 'type-normal'
    case 'CROSS_BORDER':
      return 'type-cross-border'
    default:
      return ''
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'SUCCESS':
      return '成功'
    case 'FAILED':
      return '失败'
    case 'PROCESSING':
      return '处理中'
    default:
      return status
  }
}

// 获取状态样式类
const getStatusClass = (status: string) => {
  switch (status) {
    case 'SUCCESS':
      return 'status-success'
    case 'FAILED':
      return 'status-failed'
    case 'PROCESSING':
      return 'status-processing'
    default:
      return ''
  }
}

// 格式化账户号码
const formatAccountNumber = (accountNumber: string) => {
  if (accountNumber.length <= 4) return accountNumber
  return '****' + accountNumber.slice(-4)
}

// 格式化时间
const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.transfer-record {
  padding: 12px;
  background: transparent;
  min-height: 100vh;
}

/* 页面标题 */
.page-header {
  padding: 16px 4px 20px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 20px rgba(79, 172, 254, 0.3);
  flex-shrink: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 800;
  color: #1a1a2e;
  margin: 0 0 4px 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
  font-weight: 400;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 筛选条件 */
.filter-section {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.filter-item {
  margin-bottom: 12px;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.filter-actions .van-button {
  flex: 1;
}

/* 转账记录列表 */
.records-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-item {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.record-type {
  font-size: 14px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
}

.type-normal {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.type-cross-border {
  background: rgba(240, 147, 251, 0.1);
  color: #f093fb;
}

.record-status {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 10px;
  border-radius: 12px;
}

.status-success {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.status-failed {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.status-processing {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.record-content {
  margin-bottom: 12px;
}

.record-amount {
  display: flex;
  align-items: baseline;
  margin-bottom: 12px;
}

.currency {
  font-size: 14px;
  color: #6b7280;
  margin-right: 6px;
}

.amount {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
}

.record-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.label {
  color: #6b7280;
  margin-right: 8px;
}

.value {
  color: #1a1a2e;
  text-align: right;
  flex: 1;
}

.record-footer {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.transaction-id {
  font-size: 12px;
  color: #9ca3af;
}

/* 响应式优化 */
@media (max-width: 767px) {
  .transfer-record {
    padding: 12px;
  }
  
  .page-header {
    padding: 12px 4px 16px;
  }
  
  .filter-section {
    padding: 12px;
  }
  
  .record-item {
    padding: 12px;
  }
  
  .amount {
    font-size: 20px;
  }
}
</style>