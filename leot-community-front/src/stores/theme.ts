import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export type ThemeMode = 'dark' | 'light'

export const useThemeStore = defineStore('theme', () => {
  // 从 localStorage 读取，默认暗色
  const savedTheme = localStorage.getItem('theme') as ThemeMode
  const theme = ref<ThemeMode>(savedTheme || 'dark')

  const toggleTheme = () => {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
  }

  const setTheme = (mode: ThemeMode) => {
    theme.value = mode
  }

  // 监听变化，更新 DOM 和 localStorage
  watch(theme, (newTheme) => {
    document.documentElement.setAttribute('data-theme', newTheme)
    localStorage.setItem('theme', newTheme)
  }, { immediate: true })

  return {
    theme,
    toggleTheme,
    setTheme
  }
})
