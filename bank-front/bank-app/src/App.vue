<script setup lang="ts">
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useThemeStore } from './stores/theme'
import { computed } from 'vue'

const { t, locale } = useI18n()
const themeStore = useThemeStore()
const route = useRoute()

// 判断是否为认证页面
const isAuthPage = computed(() => route.path === '/auth')

// 切换语言
const switchLanguage = () => {
  locale.value = locale.value === 'zh-CN' ? 'zh-TW' : 'zh-CN'
}

// 切换主题
const toggleTheme = () => {
  themeStore.toggleTheme()
}

// 获取主题图标
const themeIcon = computed(() => themeStore.isDark ? 'sun-o' : 'moon-o')
</script>

<template>
  <div class="app-layout">
    <!-- 悬浮顶部导航栏 -->
    <div v-if="!isAuthPage" class="floating-navbar-wrapper">
      <div class="floating-navbar soft-card">
        <div class="navbar-left">
          <div class="brand-icon">
            <van-icon name="gem-o" size="20" />
          </div>
          <span class="brand-text">{{ t('app.title') }}</span>
        </div>
        <div class="navbar-right">
          <div class="icon-btn-light" @click="toggleTheme">
            <van-icon :name="themeIcon" size="20" />
          </div>
          <div class="icon-btn-light" @click="switchLanguage">
            <span class="lang-text">{{ locale === 'zh-CN' ? '繁' : '简' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 页面内容区域 -->
    <div class="content" :class="{ 'content-full': isAuthPage }">
      <router-view v-slot="{ Component }">
        <transition name="fade-scale" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <!-- 悬浮底部标签栏 -->
    <div v-if="!isAuthPage" class="floating-tabbar-wrapper">
      <div class="floating-tabbar soft-card">
        <router-link to="/home" class="tab-item" active-class="active">
          <div class="tab-icon">
            <van-icon name="wap-home" />
          </div>
          <span class="tab-text">{{ t('home.title') }}</span>
        </router-link>
        
        <router-link to="/account" class="tab-item" active-class="active">
          <div class="tab-icon">
            <van-icon name="card" />
          </div>
          <span class="tab-text">{{ t('account.title') }}</span>
        </router-link>

        <div class="tab-item-center-wrapper">
          <router-link to="/transfer" class="tab-item-center">
            <van-icon name="exchange" size="24" />
          </router-link>
        </div>
        
        <router-link to="/profile" class="tab-item" active-class="active">
          <div class="tab-icon">
            <van-icon name="manager" />
          </div>
          <span class="tab-text">{{ t('profile.title') }}</span>
        </router-link>
        
        <router-link to="/settings" class="tab-item" active-class="active">
          <div class="tab-icon">
            <van-icon name="setting" />
          </div>
          <span class="tab-text">设置</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 悬浮导航栏 */
.floating-navbar-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 12px 16px;
  display: flex;
  justify-content: center;
  pointer-events: none; /* 让点击穿透padding区域 */
}

.floating-navbar {
  width: 100%;
  max-width: 480px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  pointer-events: auto;
  border-radius: var(--radius-md);
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-icon {
  width: 32px;
  height: 32px;
  background: var(--gradient-main);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: var(--shadow-sm);
}

.brand-text {
  font-size: 18px;
  font-weight: 800;
  color: var(--text-main);
  letter-spacing: -0.5px;
}

.navbar-right {
  display: flex;
  gap: 8px;
}

.icon-btn-light {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-main);
  cursor: pointer;
  transition: all 0.2s;
  background: var(--bg-body); /* 浅色背景下的按钮底色 */
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(0,0,0,0.05);
}

.icon-btn-light:hover {
  background: rgba(0,0,0,0.05);
  transform: translateY(-2px);
}

.lang-text {
  font-size: 12px;
  font-weight: 700;
}

/* 内容区域 */
.content {
  padding-top: 80px; /* 导航栏高度 + 间距 */
  padding-bottom: 100px; /* 底部栏高度 + 间距 */
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

.content-full {
  padding: 0;
}

/* 悬浮底部标签栏 */
.floating-tabbar-wrapper {
  position: fixed;
  bottom: 20px;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  justify-content: center;
  padding: 0 16px;
  pointer-events: none;
}

.floating-tabbar {
  width: 100%;
  max-width: 480px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 0 10px;
  pointer-events: auto;
  border-radius: var(--radius-md);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: var(--text-sub);
  text-decoration: none;
  width: 50px;
  transition: all 0.3s;
}

.tab-icon {
  font-size: 22px;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.tab-text {
  font-size: 10px;
  font-weight: 600;
}

.tab-item.active {
  color: var(--color-primary);
}

.tab-item.active .tab-icon {
  transform: translateY(-2px);
  color: var(--color-primary);
}

/* 中间突出的按钮 */
.tab-item-center-wrapper {
  position: relative;
  top: -20px;
}

.tab-item-center {
  width: 56px;
  height: 56px;
  background: var(--gradient-main);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: var(--shadow-float);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform: rotate(45deg); /* 菱形风格 */
}

.tab-item-center .van-icon {
  transform: rotate(-45deg); /* 图标转回来 */
}

.tab-item-center:active {
  transform: rotate(45deg) scale(0.9);
}

/* 路由切换动画 */
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.3s ease;
}

.fade-scale-enter-from {
  opacity: 0;
  transform: scale(0.98);
}

.fade-scale-leave-to {
  opacity: 0;
  transform: scale(1.02);
}
</style>