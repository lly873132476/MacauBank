<template>
  <div class="home">
    <!-- Unverified Banner -->
    <div v-if="!authStore.isAccountOpened" class="verify-banner" @click="goToOpenAccount">
      <div class="banner-content">
        <div class="banner-text">
          <h3>完成身份认证</h3>
          <p>开通数字账户，解锁转账与理财服务</p>
        </div>
        <div class="banner-btn">
          立即开户 <van-icon name="arrow" />
        </div>
      </div>
    </div>

    <!-- 顶部栏 (Verified) -->
    <div v-else class="header-section">
      <div class="user-info">
        <h1 class="greeting">早安, {{ authStore.userInfo?.name || '尊贵的客户' }}</h1>
        <p class="date-hint">今日汇率已更新</p>
      </div>
      <div class="header-actions">
        <div class="icon-btn" @click="$router.push('/profile')">
          <van-icon name="bell" size="22" />
          <div v-if="userStore.unreadCount > 0" class="badge"></div>
        </div>
      </div>
    </div>

    <!-- 资产总览卡片 -->
    <div class="asset-card-solid clickable shimmer-effect" :class="{ 'disabled-card': !authStore.isAccountOpened }">
      <div class="texture-overlay"></div> <!-- 磨砂纹理 -->
      <div class="card-header">
        <span class="label">总资产估值 (MOP)</span>
        <div class="eye-toggle clickable" @click="toggleBalance">
          <van-icon :name="showBalance ? 'eye-o' : 'closed-eye'" />
        </div>
      </div>
      
      <div class="card-body">
        <div v-if="loading" class="loading-block skeleton"></div>
        <div v-else class="balance-wrapper">
          <span class="symbol">MOP</span>
          <span class="amount font-num">
            {{ authStore.isAccountOpened && showBalance ? summary?.totalMopValue.toLocaleString('en-US', { minimumFractionDigits: 2 }) : '******' }}
          </span>
        </div>
      </div>

      <div class="card-footer">
        <div class="stat-row">
          <div class="stat-item">
            <span class="stat-label">今日收益</span>
            <span class="stat-val font-num up">{{ authStore.isAccountOpened ? '+1,240.50' : '---' }}</span>
          </div>
          <div class="v-divider"></div>
          <div class="stat-item">
            <span class="stat-label">本月支出</span>
            <span class="stat-val font-num down">{{ authStore.isAccountOpened ? '-5,320.00' : '---' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷功能 -->
    <div class="section-title">快捷服务</div>
    <div class="app-card quick-actions-card">
      <div class="quick-grid">
        <div class="action-item clickable" @click="handleAction('/payment/qrcode/scan')">
          <div class="icon-box bg-purple" :class="{ grayscale: !authStore.isAccountOpened }">
            <van-icon name="scan" />
          </div>
          <span class="label">扫码支付</span>
        </div>
        
        <div class="action-item clickable" @click="handleAction('/transfer')">
          <div class="icon-box bg-blue" :class="{ grayscale: !authStore.isAccountOpened }">
            <van-icon name="balance-pay" />
          </div>
          <span class="label">转账汇款</span>
        </div>
        
        <div class="action-item clickable" @click="handleAction('/account')">
          <div class="icon-box bg-cyan" :class="{ grayscale: !authStore.isAccountOpened }">
            <van-icon name="exchange" />
          </div>
          <span class="label">货币兑换</span>
        </div>
        
        <div class="action-item clickable" @click="handleAction('/investment')">
          <div class="icon-box bg-green" :class="{ grayscale: !authStore.isAccountOpened }">
            <van-icon name="chart-trending-o" />
          </div>
          <span class="label">理财投资</span>
        </div>
      </div>
    </div>

    <!-- 实时汇率 (所有人可见) -->
    <div class="rate-ticker shimmer-effect clickable">
      <div class="rate-header">
        <div class="rate-title-group">
          <van-icon name="chart-trending-o" class="trend-icon" />
          <span class="label">实时汇率参考</span>
        </div>
        <span class="update-time">刚刚更新</span>
      </div>
      <div class="rate-scroll-container">
        <div 
          v-for="rate in referenceRates" 
          :key="rate.currencyPair" 
          class="rate-card"
        >
          <div class="pair-info">
            <span class="curr-code">{{ rate.baseCurrency }}</span>
            <van-icon name="exchange" class="exchange-icon" />
            <span class="curr-code">{{ rate.targetCurrency }}</span>
          </div>
          <div class="rate-value font-num">{{ rate.buyRate }}</div>
          <div 
            class="rate-change" 
            :class="rate.changePercent.startsWith('+') ? 'up' : 'down'"
          >
            {{ rate.changePercent }}%
          </div>
        </div>
      </div>
    </div>

    <!-- 账户列表 (已开户可见) -->
    <div v-if="authStore.isAccountOpened">
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
            :key="account.displayId"
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
                <div class="acc-no font-num">**** {{ account.accountNo ? account.accountNo.slice(-4) : '8888' }}</div>
                </div>
            </div>
            <div class="acc-balance font-num">
                {{ account.balance?.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}
            </div>
            </div>
        </transition-group>
        </div>
    </div>
    
    <!-- 未开户时的特权预览 -->
    <div v-else class="feature-section">
        <div class="feature-header">
            <span class="title">开户即享权益</span>
            <span class="badge">限时</span>
        </div>
                    <div class="feature-grid">
                    <div class="f-card glass-panel" @click="handleFeatureClick">
                        <div class="f-icon-box gradient-blue">
                            <van-icon name="guide-o" style="color: white; z-index: 1;" />
                        </div>
                        <div class="f-text">
                            <span class="f-title">全球汇款</span>                    <span class="f-sub">实时到账</span>
                </div>
            </div>
            <div class="f-card glass-panel" @click="handleFeatureClick">
                <div class="f-icon-box gradient-purple">
                    <van-icon name="balance-list-o" />
                </div>
                <div class="f-text">
                    <span class="f-title">多币种</span>
                    <span class="f-sub">一卡通行</span>
                </div>
            </div>
            <div class="f-card glass-panel" @click="handleFeatureClick">
                <div class="f-icon-box gradient-orange">
                    <van-icon name="gold-coin-o" />
                </div>
                <div class="f-text">
                    <span class="f-title">高息存款</span>
                    <span class="f-sub">年化 4.5%</span>
                </div>
            </div>
        </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../stores/user'
import { useAccountStore } from '../stores/account'
import { useAuthStore } from '../stores/auth'
import { currencyApi, type ExchangeRateReferenceResponse } from '../services/api'
import { useRouter } from 'vue-router'
import { showDialog, showToast } from 'vant'

const { t } = useI18n()
const userStore = useUserStore()
const accountStore = useAccountStore()
const authStore = useAuthStore()
const router = useRouter()

const summary = ref<any>(null)
const loading = ref(true)
const showBalance = ref(false)
const referenceRates = ref<ExchangeRateReferenceResponse[]>([])

onMounted(async () => {
  // 每次进入首页，强制同步最新的用户状态 (KYC Level)
  await authStore.fetchUserInfo()
  
  // 获取未读消息数
  if (authStore.isLoggedIn) {
    userStore.fetchUnreadCount()
  }

  // 加载参考汇率 (所有人可见)
  currencyApi.getReferenceList().then(res => {
    if (res.code === 200) {
      referenceRates.value = res.data
    }
  })

  if (authStore.isAccountOpened) {
    const summaryResult = await accountStore.fetchSummary()
    if (summaryResult.success) {
        summary.value = summaryResult.data
    }
  } else {
    // 未开户时也可以加载一些通用配置或模拟数据
    summary.value = { totalMopValue: 0 }
  }
  loading.value = false
})

const toggleBalance = () => {
    if (authStore.isAccountOpened) {
        showBalance.value = !showBalance.value
    }
}

const goToOpenAccount = () => {
  router.push('/open-account')
}

// 处理底部特性卡片的点击
const handleFeatureClick = () => {
    showDialog({
        title: '功能介绍',
        message: '该服务为澳门银行开户用户专享权益。完成开户后，您即可体验全球汇款、多币种账户及高息理财服务。',
        confirmButtonText: '立即开户',
        showCancelButton: true,
        cancelButtonText: '稍后',
        confirmButtonColor: '#2563EB'
    }).then((action) => {
        if (action === 'confirm') {
            router.push('/open-account')
        }
    })
}

const handleAction = (path: string) => {
    // 如果是理财投资，暂时提示功能建设中
    if (path === '/investment') {
        showToast('理财服务功能建设中，敬请期待')
        return
    }

    if (!authStore.isAccountOpened) {
        showDialog({
            title: '需要身份认证',
            message: '该功能仅对实名开户用户开放。是否立即前往开户？',
            showCancelButton: true,
            confirmButtonColor: '#6C5DD3'
        }).then((action) => {
            if (action === 'confirm') {
                router.push('/open-account')
            }
        })
    } else {
        router.push(path)
    }
}
</script>

<style scoped>
.home {
  padding: 16px 20px 40px;
}

/* Banner */
.verify-banner {
    background: linear-gradient(90deg, #FF6B6B, #FF8E53);
    border-radius: 16px;
    padding: 16px;
    margin-bottom: 24px;
    color: white;
    cursor: pointer;
    box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
}
.banner-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.banner-text h3 { margin: 0 0 4px 0; font-size: 16px; font-weight: 700; }
.banner-text p { margin: 0; font-size: 12px; opacity: 0.9; }
.banner-btn { 
    background: rgba(255,255,255,0.2); 
    padding: 6px 12px; 
    border-radius: 20px; 
    font-size: 12px; 
    display: flex; 
    align-items: center; 
    gap: 4px; 
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

/* 资产卡片 */
.asset-card-solid {
  background: var(--gradient-brand);
  border-radius: 24px;
  padding: 24px;
  color: white;
  box-shadow: 0 15px 30px -5px rgba(37, 99, 235, 0.4);
  margin-bottom: 32px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.asset-card-solid.disabled-card {
    filter: grayscale(0.2);
    opacity: 0.9;
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

.icon-box.grayscale {
    background: #333 !important;
    filter: grayscale(1);
    opacity: 0.5;
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

/* 汇率看板 (Premium) */
.rate-ticker {
  margin-bottom: 24px;
}
.rate-header { 
  display: flex; justify-content: space-between; align-items: center; 
  margin-bottom: 12px; padding: 0 4px;
}
.rate-title-group { display: flex; align-items: center; gap: 6px; }
.trend-icon { color: var(--color-primary); font-size: 16px; }
.rate-header .label { font-size: 14px; font-weight: 700; color: var(--text-main); }
.rate-header .update-time { font-size: 11px; color: var(--text-sub); background: rgba(255,255,255,0.1); padding: 2px 8px; border-radius: 10px; }

.rate-scroll-container {
  display: flex; gap: 12px; overflow-x: auto; padding-bottom: 4px;
  /* Hide scrollbar */
  scrollbar-width: none; 
}
.rate-scroll-container::-webkit-scrollbar { display: none; }

.rate-card {
  min-width: 150px;
  background: var(--bg-surface);
  border-radius: 20px;
  padding: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(255,255,255,0.05);
  display: flex; flex-direction: column; gap: 8px;
  align-items: center; /* 整体居中 */
  text-align: center;
}
.pair-info { display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 15px; color: var(--text-main); font-weight: 800; width: 100%; }
.curr-code { font-size: 14px; letter-spacing: 0.5px; }
.exchange-icon { color: var(--text-sub); font-size: 14px; opacity: 0.6; }
.rate-value { font-size: 24px; font-weight: 800; color: var(--text-main); letter-spacing: -0.5px; margin: 4px 0; }
.rate-change { font-size: 12px; font-weight: 700; display: inline-block; padding: 2px 8px; border-radius: 8px; }
.rate-change.up { color: #10B981; background: rgba(16, 185, 129, 0.15); }
.rate-change.down { color: #EF4444; background: rgba(239, 68, 68, 0.15); }

/* 特权预览 (Feature Section) */
.feature-section {
  padding: 8px 4px;
}
.feature-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.feature-header .title { font-size: 16px; font-weight: 700; color: var(--text-main); }
.feature-header .badge { font-size: 10px; background: linear-gradient(90deg, #FF5E62, #FF9966); color: white; padding: 2px 6px; border-radius: 6px; font-weight: 600; }

.feature-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }

.f-card {
  display: flex; flex-direction: column; align-items: center; text-align: center;
  padding: 16px 12px;
  border-radius: 20px;
  background: var(--bg-surface);
  border: 1px solid rgba(255,255,255,0.05);
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s;
  cursor: pointer;
}
.f-card:active { transform: scale(0.96); }

.f-icon-box {
  width: 48px; height: 48px;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; color: white;
  margin-bottom: 12px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}
.gradient-blue { background: linear-gradient(135deg, #3B82F6, #2563EB); }
.gradient-purple { background: linear-gradient(135deg, #8B5CF6, #7C3AED); }
.gradient-orange { background: linear-gradient(135deg, #F59E0B, #EA580C); }

.f-text { display: flex; flex-direction: column; gap: 2px; }
.f-title { font-size: 13px; font-weight: 700; color: var(--text-main); }
.f-sub { font-size: 10px; color: var(--text-sub); }
</style>