<template>
  <div class="profile">
    <!-- 个人信息头部 -->
    <div class="profile-header soft-card">
      <div class="profile-info-row">
        <div class="profile-avatar">
          <img v-if="profile?.avatar" :src="profile.avatar" alt="avatar" />
          <span v-else class="avatar-text">{{ profile?.userName?.charAt(0).toUpperCase() || 'U' }}</span>
        </div>
        <div class="profile-text">
          <h2 class="profile-nickname">{{ profile?.userName || '未设置' }}</h2>
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
          @click="handleMessageClick(message)"
        >
          <div class="message-icon-status">
            <div v-if="!message.read" class="dot"></div>
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
      <button class="logout-btn" @click="logout">
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
    if (profileResult.code && (isAuthError(profileResult.code) || profileResult.code === 102002)) {
      await authStore.logout()
      setTimeout(() => {
        router.replace('/auth')
      }, 1000)
    }
  }

  if (messagesResult.success && messagesResult.data) {
    messages.value = messagesResult.data
  }

  if (transactionResult.success && transactionResult.data) {
    transactionMessages.value = transactionResult.data
  }
})

const onMessageTabChange = (name: string) => {}

const handleMessageClick = async (message: any) => {
  if (!message.read) {
    await userStore.markAsRead(message.id)
  }
}

const logout = async () => {
  await authStore.logout()
  router.push('/auth')
}
</script>

<style scoped>
.profile {
  padding: 0 20px 120px;
  min-height: 100vh;
  background-color: var(--bg-app);
  /* Modern Aurora Gradient */
  background-image:
    radial-gradient(circle at 0% 0%, rgba(37, 99, 235, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 100% 0%, rgba(236, 72, 153, 0.05) 0%, transparent 50%);
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

/* 个人信息头部 */
.profile-header {
  background: var(--bg-surface);
  backdrop-filter: blur(20px);
  border-radius: 32px;
  padding: 32px 24px 24px;
  margin: 20px 0 32px;
  box-shadow:
    0 12px 32px -8px rgba(0,0,0,0.08),
    0 4px 12px -4px rgba(0,0,0,0.02),
    inset 0 1px 0 rgba(255,255,255,0.1);
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.1);
}

.profile-info-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  position: relative;
  z-index: 2;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563EB 0%, #60A5FA 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.25);
  border: 4px solid var(--bg-surface);
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-text {
  font-size: 32px;
  font-weight: 800;
  color: white;
}

.profile-text {
  flex: 1;
}

.profile-nickname {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-main);
  margin: 0 0 4px 0;
  letter-spacing: -0.5px;
}

.profile-name {
  font-size: 14px;
  color: var(--text-sub);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.vip-badge {
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
  color: white;
  font-size: 10px;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(245, 158, 11, 0.3);
}

.profile-stats {
  display: flex;
  justify-content: space-between;
  background: var(--bg-app);
  border-radius: 24px;
  padding: 16px 24px;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-num {
  font-size: 20px;
  font-weight: 800;
  color: var(--text-main);
}

.stat-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-sub);
}

/* 菜单区域 */
.menu-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--text-main);
  margin: 0 0 16px 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  letter-spacing: -0.02em;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.menu-card {
  background: var(--bg-surface);
  border-radius: 24px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-card);
}

.menu-card:active {
  transform: scale(0.96);
}

.menu-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.bg-blue { background: linear-gradient(135deg, #3B82F6, #2563EB); box-shadow: 0 8px 16px rgba(59, 130, 246, 0.25); }
.bg-indigo { background: linear-gradient(135deg, #6366F1, #4F46E5); box-shadow: 0 8px 16px rgba(99, 102, 241, 0.25); }
.bg-cyan { background: linear-gradient(135deg, #06B6D4, #0891B2); box-shadow: 0 8px 16px rgba(6, 182, 212, 0.25); }
.bg-teal { background: linear-gradient(135deg, #14B8A6, #0D9488); box-shadow: 0 8px 16px rgba(20, 184, 166, 0.25); }

.menu-label {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-main);
}

/* 消息列表 */
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-card {
  background: var(--bg-surface);
  border-radius: 20px;
  padding: 20px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
  box-shadow: var(--shadow-card);
}

.message-icon-status {
  padding-top: 6px;
}

.message-icon-status .dot {
  width: 10px;
  height: 10px;
  background: var(--color-primary);
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.15);
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
  font-size: 15px;
  font-weight: 700;
  color: var(--text-main);
  margin: 0;
}

.message-time {
  font-size: 12px;
  color: var(--text-sub);
  font-weight: 500;
}

.message-content {
  font-size: 14px;
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
  background: var(--bg-surface);
  border-radius: 20px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  text-decoration: none;
  color: var(--text-main);
  box-shadow: var(--shadow-card);
  transition: transform 0.2s;
}

.gov-link:active { transform: scale(0.98); }

.gov-icon {
  width: 40px;
  height: 40px;
  background: var(--bg-app);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-main);
  font-size: 20px;
}

.gov-text {
  flex: 1;
  font-weight: 700;
  font-size: 15px;
}

.arrow-icon {
  color: var(--text-sub);
}

/* 退出登录 */
.logout-section {
  margin-top: 48px;
  padding-bottom: 40px;
}

.logout-btn {
  width: 100%;
  background: rgba(220, 38, 38, 0.1);
  color: #EF4444;
  border: none;
  border-radius: 24px;
  padding: 18px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.logout-btn:active { background: rgba(220, 38, 38, 0.2); }
</style>
