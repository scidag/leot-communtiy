import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types/user'
import { getLoginUser, logout as logoutApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string>(localStorage.getItem('token') || '')

  // 设置用户信息
  const setUser = (userInfo: User) => {
    user.value = userInfo
  }

  // 设置 token
  const setToken = (tokenValue: string) => {
    token.value = tokenValue
    localStorage.setItem('token', tokenValue)
  }

  // 初始化用户信息（从后端获取）
  const initUser = async () => {
    try {
      const res = await getLoginUser()
      if (res.data) {
        user.value = res.data
        localStorage.setItem('userInfo', JSON.stringify(res.data))
      }
    } catch (error) {
      console.error('初始化用户信息失败:', error)
      clearUser()
    }
  }

  // 清除用户信息
  const clearUser = () => {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 登出
  const logout = async () => {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      clearUser()
    }
  }

  // 判断是否为管理员
  const isAdmin = () => {
    return user.value?.userRole === 'admin'
  }

  // 从 localStorage 恢复用户信息
  const restoreUser = () => {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      try {
        user.value = JSON.parse(userInfoStr)
      } catch (error) {
        console.error('恢复用户信息失败:', error)
      }
    }
  }

  return {
    user,
    token,
    setUser,
    setToken,
    initUser,
    clearUser,
    logout,
    isAdmin,
    restoreUser
  }
})

