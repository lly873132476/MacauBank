<template>
  <div class="settings">
    <!-- 顶部导航 -->
    <div class="settings-header">
      <h1 class="header-title">应用设置</h1>
      <div class="header-right"></div>
    </div>

    <!-- 设置内容 -->
    <div class="settings-content">
      <!-- 外观设置 -->
      <div class="settings-section">
        <h3 class="section-title">
          <van-icon name="star" />
          外观设置
        </h3>
        
        <div class="setting-item soft-card" @click="toggleTheme">
          <div class="item-left">
            <div class="item-icon bg-blue">
              <van-icon :name="themeIconName" size="20" />
            </div>
            <div class="item-info">
              <div class="item-label">主题模式</div>
              <div class="item-desc">切换浅色/深色模式</div>
            </div>
          </div>
          <div class="item-right">
            <span class="item-value">{{ currentThemeText }}</span>
            <van-icon name="arrow" />
          </div>
        </div>
      </div>

      <!-- 语言设置 -->
      <div class="settings-section">
        <h3 class="section-title">
          <van-icon name="globe" />
          语言设置
        </h3>
        
        <div class="setting-item soft-card" @click="switchLanguage">
          <div class="item-left">
            <div class="item-icon bg-indigo">
              <van-icon name="comment-o" size="20" />
            </div>
            <div class="item-info">
              <div class="item-label">系统语言</div>
              <div class="item-desc">切换简体/繁体中文</div>
            </div>
          </div>
          <div class="item-right">
            <span class="item-value">{{ currentLanguageText }}</span>
            <van-icon name="arrow" />
          </div>
        </div>
      </div>

      <!-- 关于 -->
      <div class="settings-section">
        <h3 class="section-title">
          <van-icon name="info" />
          关于应用
        </h3>
        
        <div class="setting-item soft-card">
          <div class="item-left">
            <div class="item-icon bg-cyan">
              <van-icon name="service-o" size="20" />
            </div>
            <div class="item-info">
              <div class="item-label">版本号</div>
              <div class="item-desc">当前版本信息</div>
            </div>
          </div>
          <div class="item-right">
            <span class="item-value">v1.0.0</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useThemeStore } from '../stores/theme'
import { useRouter } from 'vue-router'
import { computed } from 'vue'

const { t, locale } = useI18n()
const themeStore = useThemeStore()
const router = useRouter()

// 切换主题
const toggleTheme = () => {
  themeStore.toggleTheme()
}

// 切换语言
const switchLanguage = () => {
  locale.value = locale.value === 'zh-CN' ? 'zh-TW' : 'zh-CN'
}

// 获取当前主题文本
const currentThemeText = computed(() => {
  return themeStore.isDark ? '深色模式' : '浅色模式'
})

// 获取主题图标
const themeIconName = computed(() => {
  return themeStore.isDark ? 'moon' : 'sun-o'
})

// 获取当前语言文本
const currentLanguageText = computed(() => {
  return locale.value === 'zh-CN' ? '简体中文' : '繁体中文'
})
</script>

<style scoped>
.settings {
  min-height: 100vh;
  background-color: var(--bg-app);
  /* Modern Aurora Gradient */
  background-image: 
    radial-gradient(circle at 0% 0%, rgba(37, 99, 235, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 100% 0%, rgba(236, 72, 153, 0.05) 0%, transparent 50%);
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

/* 顶部导航 */
.settings-header {
  background: var(--bg-surface);
  backdrop-filter: blur(20px);
  padding: 16px 20px; /* Adjust padding for no left button */
  display: flex;
  align-items: center;
  justify-content: center; /* Center content horizontally */
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  box-shadow: 0 4px 20px rgba(0,0,0,0.02);
}

.header-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--text-main);
  margin: 0;
  /* flex: 1; Remove flex to allow auto center */
  text-align: center;
}

.header-right {
  /* width: 40px; Should be removed or made invisible if no right content */
  /* Keep it as empty for now for balance if needed, or remove later if not. */
}

/* 设置内容 */
.settings-content {
  padding: 24px 20px;
}

.settings-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 14px;
  font-weight: 800;
  color: var(--text-sub);
  margin: 0 0 16px 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 设置项 */
.setting-item {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  margin-bottom: 16px;
  background: var(--bg-surface);
  border-radius: 24px;
  box-shadow: var(--shadow-card);
  transition: transform 0.2s;
  border: 1px solid rgba(255,255,255,0.05);
}

.setting-item:active {
  transform: scale(0.98);
}

.item-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.item-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.bg-blue { background: linear-gradient(135deg, #3B82F6, #2563EB); }
.bg-indigo { background: linear-gradient(135deg, #6366F1, #4F46E5); }
.bg-cyan { background: linear-gradient(135deg, #06B6D4, #0891B2); }

.item-info {
  flex: 1;
}

.item-label {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 4px;
}

.item-desc {
  font-size: 12px;
  color: var(--text-sub);
  font-weight: 500;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-value {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-primary);
  background: var(--bg-app);
  padding: 4px 10px;
  border-radius: 8px;
}

.item-right .van-icon {
  color: var(--text-sub);
  font-size: 16px;
}
</style>