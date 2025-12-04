import { ref, watch } from 'vue'
import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', () => {
  // 从 localStorage 获取保存的主题，默认为浅色
  const isDark = ref(localStorage.getItem('theme') === 'dark')

  // 切换主题
  const toggleTheme = () => {
    isDark.value = !isDark.value
  }

  // 设置主题
  const setTheme = (dark: boolean) => {
    isDark.value = dark
  }

  // 应用主题到 DOM
  const applyTheme = () => {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
      document.body.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      document.body.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  }

  // 监听主题变化，自动应用
  watch(isDark, () => {
    applyTheme()
  }, { immediate: true })

  return {
    isDark,
    toggleTheme,
    setTheme,
    applyTheme
  }
})
