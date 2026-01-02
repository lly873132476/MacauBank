<template>
  <div class="transfer-record-page">
    <!-- 顶部导航栏 (占位，如果全局有则不需要，这里为了对其TransferView) -->
    <div class="header-placeholder"></div>

    <!-- 页面标题区域 -->
    <div class="page-header">
      <h1 class="page-title">转账记录</h1>
      <p class="page-subtitle">查看您的资金往来详情</p>
    </div>

    <!-- 筛选条件 - 玻璃拟态卡片 -->
    <div class="filter-bar glass-panel">
      <div class="filter-item" @click="showCurrencyPicker = true">
        <span class="filter-label">币种</span>
        <div class="filter-value">
          {{ filterForm.currency || '全部' }}
          <van-icon name="arrow-down" />
        </div>
      </div>
      <div class="divider-vertical"></div>
      <div class="filter-item" @click="showStatusPicker = true">
        <span class="filter-label">状态</span>
        <div class="filter-value">
          {{ getStatusLabel(filterForm.status) || '全部' }}
          <van-icon name="arrow-down" />
        </div>
      </div>
      <!-- 查询按钮 -->
      <div class="search-btn clickable" @click="loadTransferRecords">
        <van-icon name="search" />
      </div>
    </div>

    <!-- 转账记录列表 -->
    <div class="records-list">
      <van-empty v-if="transferRecords.length === 0" description="暂无转账记录" class="empty-state" />
      
      <div v-else class="record-card app-card clickable" v-for="record in transferRecords" :key="record.id">
        <div class="card-top">
          <div class="record-main-info">
            <div class="avatar-icon" :class="getTransferTypeClass(record.transferType)">
              <van-icon :name="getTransferTypeIcon(record.transferType)" />
            </div>
            <div class="text-info">
              <div class="payee-name">{{ formatAccountNumber(record.toAccountNumber) }}</div>
              <div class="time-text">{{ formatTime(record.createTime) }}</div>
            </div>
          </div>
          <div class="amount-info">
            <span class="amount-text font-num">-{{ record.amount.toFixed(2) }}</span>
            <span class="currency-badge">{{ record.currencyCode }}</span>
          </div>
        </div>
        
        <div class="card-divider"></div>
        
        <div class="card-bottom">
          <div class="status-tag" :class="getStatusClass(record.status)">
            {{ getStatusText(record.status) }}
          </div>
          <div class="fee-info font-num" v-if="record.fee > 0">
            手续费 {{ record.fee.toFixed(2) }}
          </div>
        </div>
      </div>
    </div>

    <!-- 底部留白 -->
    <div class="bottom-spacer"></div>

    <!-- 币种选择器 -->
    <van-popup v-model:show="showCurrencyPicker" round position="bottom" class="glass-popup">
      <van-picker
        title="选择币种"
        :columns="currencyColumns"
        @confirm="onCurrencyConfirm"
        @cancel="showCurrencyPicker = false"
      />
    </van-popup>

    <!-- 状态选择器 -->
    <van-popup v-model:show="showStatusPicker" round position="bottom" class="glass-popup">
      <van-picker
        title="选择状态"
        :columns="statusColumns"
        @confirm="onStatusConfirm"
        @cancel="showStatusPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAccountStore } from '../stores/account'
import { useUiStore } from '../stores/ui'
import { showToast } from 'vant'

const { t } = useI18n()
const accountStore = useAccountStore()
const uiStore = useUiStore()

const transferRecords = ref<any[]>([])
const showCurrencyPicker = ref(false)
const showStatusPicker = ref(false)

watch([showCurrencyPicker, showStatusPicker], ([c, s]) => {
  uiStore.setPopupOpen(c || s)
})

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
    if (result.success && result.data) {
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
  loadTransferRecords() // Auto search
}

// 状态选择确认
const onStatusConfirm = ({ selectedOptions }: any) => {
  filterForm.value.status = selectedOptions[0].value
  showStatusPicker.value = false
  loadTransferRecords() // Auto search
}

const getStatusLabel = (val: string) => {
  const found = statusColumns.find(c => c.value === val)
  return found ? found.text : val
}

// 获取转账类型图标
const getTransferTypeIcon = (type: string) => {
  return type === 'CROSS_BORDER' ? 'globe-o' : 'exchange'
}

// 获取转账类型样式类
const getTransferTypeClass = (type: string) => {
  return type === 'CROSS_BORDER' ? 'icon-purple' : 'icon-blue'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'SUCCESS': '转账成功',
    'FAILED': '转账失败',
    'PROCESSING': '处理中'
  }
  return map[status] || status
}

// 获取状态样式类
const getStatusClass = (status: string) => {
  const map: Record<string, string> = {
    'SUCCESS': 'tag-success',
    'FAILED': 'tag-failed',
    'PROCESSING': 'tag-warning'
  }
  return map[status] || ''
}

// 格式化账户号码
const formatAccountNumber = (accountNumber: string) => {
  if (!accountNumber) return '未知账户'
  if (accountNumber.length <= 4) return accountNumber
  return '账户 *' + accountNumber.slice(-4)
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.transfer-record-page {
  min-height: calc(100vh - 80px - 100px);
  background-color: #F8F9FA;
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(37, 99, 235, 0.04) 0%, transparent 40%),
    radial-gradient(circle at 90% 80%, rgba(236, 72, 153, 0.04) 0%, transparent 40%);
  padding: 0 20px;
  overflow-y: auto;
}

.header-placeholder { height: 20px; }

.page-header { margin-bottom: 24px; }
.page-title { font-size: 28px; font-weight: 800; color: var(--text-main); margin: 0 0 4px 0; letter-spacing: -0.5px; }
.page-subtitle { font-size: 14px; color: var(--text-sub); font-weight: 500; }

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  padding: 8px 8px;
  margin-bottom: 24px;
  border-radius: 16px;
  gap: 12px;
}
.filter-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 8px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.filter-item:active { background: rgba(0,0,0,0.03); }
.filter-label { font-size: 11px; color: var(--text-sub); font-weight: 600; margin-bottom: 2px; }
.filter-value { display: flex; align-items: center; gap: 4px; font-size: 14px; font-weight: 700; color: var(--text-main); }
.divider-vertical { width: 1px; height: 24px; background: rgba(0,0,0,0.06); }
.search-btn {
  width: 40px; height: 40px;
  border-radius: 12px;
  background: var(--color-primary);
  color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}
.search-btn:active { transform: scale(0.95); }

/* 列表 */
.records-list { display: flex; flex-direction: column; gap: 16px; }
.record-card { padding: 20px; border-radius: 24px; border: 1px solid rgba(255,255,255,0.6); }

.card-top { display: flex; justify-content: space-between; align-items: flex-start; }
.record-main-info { display: flex; gap: 12px; align-items: center; }
.avatar-icon {
  width: 44px; height: 44px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px;
}
.icon-blue { background: #EFF6FF; color: #2563EB; }
.icon-purple { background: #FDF4FF; color: #C026D3; }

.text-info { display: flex; flex-direction: column; gap: 2px; }
.payee-name { font-size: 15px; font-weight: 700; color: var(--text-main); }
.time-text { font-size: 12px; color: var(--text-sub); font-weight: 500; }

.amount-info { text-align: right; }
.amount-text { display: block; font-size: 18px; font-weight: 700; color: var(--text-main); }
.currency-badge { 
  font-size: 10px; font-weight: 700; color: var(--text-sub); 
  background: #F3F4F6; padding: 2px 6px; border-radius: 6px;
}

.card-divider { height: 1px; background: #F3F4F6; margin: 16px 0; }

.card-bottom { display: flex; justify-content: space-between; align-items: center; }
.status-tag { font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 8px; }
.tag-success { background: #F0FDF4; color: #15803D; }
.tag-failed { background: #FEF2F2; color: #B91C1C; }
.tag-warning { background: #FFFBEB; color: #B45309; }

.fee-info { font-size: 12px; color: var(--text-sub); }

.bottom-spacer { height: 40px; }
.empty-state { padding: 40px 0; }
</style>