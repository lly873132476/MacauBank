<script setup lang="ts">
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useThemeStore } from './stores/theme'
import { useUiStore } from './stores/ui' // Import UI store
import { computed } from 'vue'

const { t, locale } = useI18n()
const themeStore = useThemeStore()
const uiStore = useUiStore() // Use UI store
const route = useRoute()

// 判断是否为认证页面 (用于内容全屏布局)
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
          <div class="icon-btn-light pill-btn" @click="toggleTheme" title="切换主题">
            <van-icon :name="themeIcon" size="18" />
            <span class="btn-label">主题</span>
          </div>
          <div class="icon-btn-light pill-btn" @click="switchLanguage" title="切换语言">
            <span class="lang-text">{{ locale === 'zh-CN' ? '繁' : '简' }}</span>
            <span class="btn-label">语言</span>
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
    <div v-if="!isAuthPage" class="floating-tabbar-wrapper" :class="{ 'ui-hidden': uiStore.isGlobalPopupOpen }">
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
  pointer-events: none;
}

.floating-navbar {
  width: 100%;
  max-width: 480px;
  height: 56px; /* Slightly smaller height for elegance */
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 6px 0 16px;
  pointer-events: auto;
  border-radius: 28px; /* Full pill shape */
  background: rgba(255, 255, 255, 0.85); /* More opaque */
  backdrop-filter: blur(20px) saturate(180%); /* Stronger blur */
  box-shadow: 
    0 8px 24px -6px rgba(0,0,0,0.08), /* Softer shadow */
    0 0 0 1px rgba(0,0,0,0.03) inset; /* Softer border */
  border: 1px solid rgba(255,255,255,0.4); /* Softer white border */
}

.dark .floating-navbar {
  background: rgba(43, 45, 48, 0.85); /* IDEA Surface with transparency */
  box-shadow: 
    0 8px 24px -6px rgba(0,0,0,0.6), /* Softer dark shadow */
    0 0 0 1px rgba(255,255,255,0.03) inset; /* Softer dark border */
  border-color: rgba(255,255,255,0.1); /* Dark mode border */
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-icon {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
}

.brand-text {
  font-size: 17px;
  font-weight: 800;
  color: var(--text-main); /* Theme aware */
  letter-spacing: -0.5px;
}

.navbar-right {
  display: flex;
  gap: 6px;
}

.icon-btn-light {
  width: 40px;
  height: 40px;
  border-radius: 50%; /* Circular buttons */
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-main);
  cursor: pointer;
  transition: all 0.2s ease;
  background: rgba(255, 255, 255, 0.8); /* Softer white */
  border: 1px solid rgba(0,0,0,0.05); /* Softer border */
  box-shadow: 0 2px 8px rgba(0,0,0,0.03); /* Softer shadow */
}

.dark .icon-btn-light {
  background: rgba(43, 45, 48, 0.8); /* IDEA Surface for dark mode */
  border-color: rgba(255,255,255,0.08); /* Softer dark border */
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
}

.pill-btn {
  width: auto;
  padding: 0 12px;
  border-radius: 20px;
  gap: 6px;
}

.btn-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-main);
}

.icon-btn-light:active {
  transform: scale(0.92);
  background: white;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  color: var(--color-primary);
}

.lang-text {
  font-size: 13px;
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
  z-index: 90;
  display: flex;
  justify-content: center;
  padding: 0 16px;
  pointer-events: none;
  transition: all 0.3s ease; /* Add transition */
}

.floating-navbar-wrapper {
  transition: all 0.3s ease; /* Add transition for top bar too */
}

.ui-hidden {
  opacity: 0;
  pointer-events: none;
  transform: translateY(20px) scale(0.95); /* Move down and shrink slightly */
}
.floating-navbar-wrapper.ui-hidden {
  transform: translateY(-20px) scale(0.95); /* Move up for top bar */
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
  background: var(--bg-surface); /* Use theme surface color, opaque */
  box-shadow: var(--shadow-card); /* Use theme shadow */
  border: 1px solid rgba(255,255,255,0.5); /* Light border */
}

.dark .floating-tabbar {
  border-color: rgba(255,255,255,0.1); /* Dark mode border */
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
  background: var(--gradient-brand); /* Fix: use defined gradient */
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white; /* Ensure icon is white */
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