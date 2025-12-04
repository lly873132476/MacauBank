<template>
  <div class="profile">
    <!-- 个人信息头部 -->
    <div class="profile-header soft-card">
      <div class="profile-info-row">
        <div class="profile-avatar">
          <img v-if="profile?.avatar" :src="profile.avatar" alt="avatar" />
          <span v-else class="avatar-text">{{ profile?.username?.charAt(0).toUpperCase() || 'U' }}</span>
        </div>
        <div class="profile-text">
          <h2 class="profile-nickname">{{ profile?.username || '未设置' }}</h2>
          <p class="profile-name">
            <span class="vip-badge">VIP</span>
            {{ profile?.name || '未设置' }}
          </p>
        </div>
      </div>
      <div class="profile-stats">
        <div class="stat-item">
          <span class="stat-num font-num">12</span>
          <span class="stat-label">消息</span>
        </div>
        <div class="stat-item">
          <span class="stat-num font-num">3</span>
          <span class="stat-label">卡片</span>
        </div>
        <div class="stat-item">
          <span class="stat-num font-num">850</span>
          <span class="stat-label">积分</span>
        </div>
      </div>
    </div>

    <!-- 功能列表 -->
    <div class="menu-section">
      <h3 class="section-title">
        <van-icon name="apps-o" />
        账户设置
      </h3>
      <div class="menu-grid">
        <div class="menu-card soft-card card-hover" @click="$router.push('/profile/info')">
          <div class="menu-icon-box bg-blue">
            <van-icon name="contact" />
          </div>
          <span class="menu-label">{{ t('profile.personalInfo') }}</span>
        </div>
        
        <div class="menu-card soft-card card-hover" @click="$router.push('/profile/password')">
          <div class="menu-icon-box bg-indigo">
            <van-icon name="lock" />
          </div>
          <span class="menu-label">{{ t('profile.updatePassword') }}</span>
        </div>
        
        <div class="menu-card soft-card card-hover" @click="$router.push('/profile/transaction-password')">
          <div class="menu-icon-box bg-cyan">
            <van-icon name="shield-o" />
          </div>
          <span class="menu-label">{{ t('profile.setTransactionPassword') }}</span>
        </div>
        
        <div class="menu-card soft-card card-hover" @click="$router.push('/profile/settings')">
          <div class="menu-icon-box bg-teal">
            <van-icon name="setting" />
          </div>
          <span class="menu-label">应用设置</span>
        </div>
      </div>
    </div>

    <!-- 消息中心 -->
    <div class="menu-section">
      <h3 class="section-title">
        <van-icon name="bell" />
        {{ t('profile.messageCenter') }}
      </h3>
      
      <van-tabs v-model:active="activeTab" @change="onMessageTabChange" class="clean-tabs">
        <van-tab name="system" :title="t('profile.systemMessage')" />
        <van-tab name="transaction" :title="t('profile.transactionNotice')" />
      </van-tabs>
      
      <div class="messages-list">
        <div
          v-for="message in (activeTab === 'system' ? messages : transactionMessages)"
          :key="message.id"
          class="message-card soft-card"
        >
          <div class="message-icon-status">
            <div class="dot"></div>
          </div>
          <div class="message-body">
            <div class="message-header">
              <h4 class="message-title">{{ message.title || '无标题' }}</h4>
              <span class="message-time">{{ message.time || '刚刚' }}</span>
            </div>
            <p class="message-content">{{ message.content || '暂无内容' }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 电子政务 -->
    <div class="menu-section">
      <h3 class="section-title">
        <van-icon name="cluster-o" />
        {{ t('profile.eGovernment') }}
      </h3>
      <div class="gov-links">
        <a href="https://www.dsf.gov.mo" target="_blank" class="gov-link soft-card">
          <div class="gov-icon">
            <van-icon name="guide-o" />
          </div>
          <span class="gov-text">澳门财政局</span>
          <van-icon name="arrow" class="arrow-icon" />
        </a>
        <a href="https://www.dsat.gov.mo" target="_blank" class="gov-link soft-card">
          <div class="gov-icon">
            <van-icon name="guide-o" />
          </div>
          <span class="gov-text">澳门交通局</span>
          <van-icon name="arrow" class="arrow-icon" />
        </a>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section">
      <button class="btn-primary" @click="logout">
        <van-icon name="logout" />
        退出登录
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../stores/user'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { isAuthError } from '../constants/errorCodes'

const { t } = useI18n()
const userStore = useUserStore()
const authStore = useAuthStore()
const router = useRouter()

const activeTab = ref('system')
const profile = ref<any>(null)
const messages = ref<any[]>([])
const transactionMessages = ref<any[]>([])

onMounted(async () => {
  const [profileResult, messagesResult, transactionResult] = await Promise.all([
    userStore.fetchProfile(),
    userStore.fetchMessages(),
    userStore.fetchTransactionMessages()
  ])
  
  if (profileResult.success) {
    profile.value = profileResult.data
  } else {
    showToast({
      message: profileResult.message || '获取用户信息失败',
      position: 'top'
    })
    if (profileResult.code && isAuthError(profileResult.code)) {
      await authStore.logout()
      setTimeout(() => {
        router.replace('/auth')
      }, 1000)
    }
  }
  
  if (messagesResult.success) {
    messages.value = messagesResult.data
  }

  if (transactionResult.success) {
    transactionMessages.value = transactionResult.data
  }
})

const onMessageTabChange = (name: string) => {}

const logout = async () => {
  await authStore.logout()
  router.push('/auth')
}
</script>

<style scoped>
.profile {
  padding: 0 16px 40px;
  min-height: 100vh;
  animation: fadeIn 0.8s ease;
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

/* 个人信息头部 */
.profile-header {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 24px;
  margin: 20px 0;
  box-shadow: var(--shadow-card);
  position: relative;
  overflow: hidden;
}

.profile-info-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  position: relative;
  z-index: 2;
}

.profile-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--gradient-main);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
  z-index: 2;
  box-shadow: 0 8px 16px rgba(37, 99, 235, 0.3);
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-text {
  font-size: 32px;
  font-weight: 700;
  color: white;
}

.profile-text {
  flex: 1;
}

.profile-nickname {
  font-size: 22px;
  font-weight: 800;
  color: var(--text-main);
  margin: 0 0 6px 0;
  letter-spacing: 0.5px;
}

.profile-name {
  font-size: 14px;
  color: var(--text-sub);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.vip-badge {
  background: var(--gradient-gold);
  color: white;
  font-size: 10px;
  font-weight: 800;
  padding: 2px 6px;
  border-radius: 4px;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  background: var(--bg-body);
  border-radius: var(--radius-sm);
  padding: 12px;
  position: relative;
  z-index: 2;
  box-shadow: var(--shadow-sm);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-num {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-main);
}

.stat-label {
  font-size: 11px;
  color: var(--text-sub);
}

/* 菜单区域 */
.menu-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  margin: 0 0 12px 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.menu-card {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.menu-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.bg-blue { background: linear-gradient(135deg, #3B82F6, #2563EB); }
.bg-indigo { background: linear-gradient(135deg, #6366F1, #4F46E5); }
.bg-cyan { background: linear-gradient(135deg, #06B6D4, #0891B2); }
.bg-teal { background: linear-gradient(135deg, #14B8A6, #0D9488); }

.menu-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
}

/* 消息列表 */
.clean-tabs :deep(.van-tabs__wrap) {
  margin-bottom: 16px;
}

.clean-tabs :deep(.van-tabs__nav) {
  background: var(--bg-body) !important;
  border-radius: var(--radius-sm);
  padding: 4px;
  box-shadow: var(--shadow-sm);
}

.clean-tabs :deep(.van-tab) {
  border-radius: var(--radius-sm);
  font-weight: 600;
  color: var(--text-sub);
}

.clean-tabs :deep(.van-tab--active) {
  background: white;
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-card {
  padding: 16px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-icon-status {
  padding-top: 6px;
}

.message-icon-status .dot {
  width: 8px;
  height: 8px;
  background: var(--color-primary);
  border-radius: 50%;
}

.message-body {
  flex: 1;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.message-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-main);
  margin: 0;
}

.message-time {
  font-size: 11px;
  color: var(--text-sub);
}

.message-content {
  font-size: 13px;
  color: var(--text-sub);
  margin: 0;
  line-height: 1.5;
}

/* 政务链接 */
.gov-links {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.gov-link {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: var(--text-main);
}

.gov-icon {
  width: 36px;
  height: 36px;
  background: var(--bg-body);
  border: 1px solid rgba(0,0,0,0.05);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.gov-text {
  flex: 1;
  font-weight: 600;
  font-size: 14px;
}

.arrow-icon {
  color: var(--text-sub);
}

/* 退出登录 */
.logout-section {
  margin-top: 32px;
  padding-bottom: 32px;
}

.logout-btn {
  width: 100%;
  background: var(--color-danger);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  padding: 14px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.25);
  transition: all 0.2s;
}

.logout-btn:active { opacity: 0.9; transform: translateY(1px); }
</style>