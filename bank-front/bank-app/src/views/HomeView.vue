<template>
  <div class="home">
    <!-- 顶部栏 -->
    <div class="header-section">
      <div class="user-info">
        <h1 class="greeting">早安, 尊贵的客户</h1>
        <p class="date-hint">今日汇率已更新</p>
      </div>
      <div class="header-actions">
        <div class="icon-btn">
          <van-icon name="bell" size="22" />
          <div class="badge"></div>
        </div>
      </div>
    </div>

    <!-- 资产总览卡片 -->
    <div class="asset-card-solid clickable shimmer-effect">
      <div class="texture-overlay"></div> <!-- 磨砂纹理 -->
      <div class="card-header">
        <span class="label">总资产估值 (MOP)</span>
        <div class="eye-toggle clickable">
          <van-icon name="eye-o" />
        </div>
      </div>
      
      <div class="card-body">
        <div v-if="loading" class="loading-block skeleton"></div>
        <div v-else class="balance-wrapper">
          <span class="symbol">MOP</span>
          <span class="amount font-num">{{ summary?.totalMopValue.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</span>
        </div>
      </div>

      <div class="card-footer">
        <div class="stat-row">
          <div class="stat-item">
            <span class="stat-label">今日收益</span>
            <span class="stat-val font-num up">+1,240.50</span>
          </div>
          <div class="v-divider"></div>
          <div class="stat-item">
            <span class="stat-label">本月支出</span>
            <span class="stat-val font-num down">-5,320.00</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷功能 -->
    <div class="section-title">快捷服务</div>
    <div class="app-card quick-actions-card">
      <div class="quick-grid">
        <div class="action-item clickable" @click="goToScan">
          <div class="icon-box bg-purple">
            <van-icon name="scan" />
          </div>
          <span class="label">扫码支付</span>
        </div>
        
        <div class="action-item clickable" @click="$router.push('/transfer')">
          <div class="icon-box bg-blue">
            <van-icon name="balance-pay" />
          </div>
          <span class="label">转账汇款</span>
        </div>
        
        <div class="action-item clickable" @click="$router.push('/account')">
          <div class="icon-box bg-cyan">
            <van-icon name="exchange" />
          </div>
          <span class="label">货币兑换</span>
        </div>
        
        <div class="action-item clickable">
          <div class="icon-box bg-green">
            <van-icon name="chart-trending-o" />
          </div>
          <span class="label">理财投资</span>
        </div>
      </div>
    </div>

    <!-- 账户列表 -->
    <div class="section-header">
      <div class="section-title">我的账户</div>
      <div class="more-link clickable">
        全部 <van-icon name="arrow" />
      </div>
    </div>
    
    <div class="account-list">
      <transition-group name="stagger" tag="div" class="list-wrapper">
        <div 
          v-for="(account, index) in accountStore.accounts" 
          :key="account.id"
          class="app-card account-item clickable card-hover"
          :style="{ transitionDelay: `${index * 0.05}s` }"
          @click="$router.push('/account')"
        >
          <div class="account-main">
            <div class="acc-icon">
              {{ account.currencyCode?.substring(0, 1) }}
            </div>
            <div class="acc-info">
              <div class="acc-name">{{ account.currencyCode }} 储蓄账户</div>
              <div class="acc-no font-num">**** {{ account.id ? String(account.id).slice(-4) : '8888' }}</div>
            </div>
          </div>
          <div class="acc-balance font-num">
            {{ account.balance?.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}
          </div>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAccountStore } from '../stores/account'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const accountStore = useAccountStore()
const router = useRouter()

const summary = ref<any>(null)
const loading = ref(true)

onMounted(async () => {
  const [summaryResult] = await Promise.all([
    accountStore.fetchSummary(),
    accountStore.fetchAccounts()
  ])
  if (summaryResult.success) {
    summary.value = summaryResult.data
  }
  loading.value = false
})

const goToScan = () => {
  router.push('/payment/qrcode/scan')
}
</script>

<style scoped>
.home {
  padding: 16px 20px 40px;
}

/* 顶部 */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-top: 12px;
}

.greeting {
  font-size: 22px;
  font-weight: 800;
  color: var(--text-main);
  margin: 0 0 4px 0;
}

.date-hint {
  font-size: 13px;
  color: var(--text-sub);
  margin: 0;
}

.icon-btn {
  width: 44px;
  height: 44px;
  background: var(--bg-surface);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-card);
  position: relative;
  color: var(--text-main);
}

.badge {
  position: absolute;
  top: 10px;
  right: 12px;
  width: 8px;
  height: 8px;
  background: var(--color-danger);
  border-radius: 50%;
  border: 2px solid var(--bg-surface);
}

/* 资产卡片 - 视觉中心 */
.asset-card-solid {
  background: var(--gradient-brand);
  border-radius: 24px;
  padding: 24px;
  color: white;
  box-shadow: 0 15px 30px -5px rgba(37, 99, 235, 0.4);
  margin-bottom: 32px;
  position: relative;
  overflow: hidden;
}

/* 装饰背景圆 */
.asset-card-solid::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
  border-radius: 50%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header .label {
  font-size: 13px;
  opacity: 0.9;
  font-weight: 500;
}

.eye-toggle {
  width: 32px;
  height: 32px;
  background: rgba(255,255,255,0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.balance-wrapper {
  margin-bottom: 24px;
}

.symbol {
  font-size: 20px;
  font-weight: 600;
  margin-right: 8px;
}

.amount {
  font-size: 36px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.card-footer {
  background: rgba(0,0,0,0.15);
  border-radius: 16px;
  padding: 12px 20px;
}

.stat-row {
  display: flex;
  align-items: center;
}

.stat-item {
  flex: 1;
}

.stat-label {
  display: block;
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 4px;
}

.stat-val {
  font-size: 16px;
  font-weight: 600;
}

.up { color: #6EE7B7; }
.down { color: #FCA5A5; }

.v-divider {
  width: 1px;
  height: 24px;
  background: rgba(255,255,255,0.2);
  margin: 0 20px;
}

/* 快捷功能 */
.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 16px;
  padding-left: 4px;
}

.quick-actions-card {
  padding: 24px 16px;
  margin-bottom: 32px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.icon-box {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  /* 微立体高光 */
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.3), 0 4px 10px rgba(0,0,0,0.1);
  transition: transform 0.1s;
}

.action-item:active .icon-box { transform: scale(0.95); }

/* 实心彩色背景 - 辨识度极高 - 升级为微渐变 */
.bg-purple { background: linear-gradient(135deg, #8B5CF6, #7C3AED); }
.bg-blue { background: linear-gradient(135deg, #3B82F6, #2563EB); }
.bg-cyan { background: linear-gradient(135deg, #06B6D4, #0891B2); }
.bg-green { background: linear-gradient(135deg, #10B981, #059669); }

.action-item .label {
  font-size: 13px;
  color: var(--text-main);
  font-weight: 500;
}

/* 账户列表 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.more-link {
  font-size: 13px;
  color: var(--text-sub);
  display: flex;
  align-items: center;
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.account-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  transition: transform 0.1s;
  cursor: pointer;
}

.account-item:active { transform: scale(0.99); }

.account-main {
  display: flex;
  align-items: center;
  gap: 16px;
}

.acc-icon {
  width: 44px;
  height: 44px;
  background: #EFF6FF; /* 浅蓝背景 */
  color: var(--color-primary);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 18px;
}

.acc-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 2px;
}

.acc-no {
  font-size: 13px;
  color: var(--text-sub);
}

.acc-balance {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-main);
}

.loading-block {
  height: 40px;
  width: 50%;
  background: rgba(255,255,255,0.2);
  border-radius: 8px;
}
</style>