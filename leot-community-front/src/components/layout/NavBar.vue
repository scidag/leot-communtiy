<template>
  <header class="navbar">
    <div class="navbar__container">
      <!-- Logo 区域 -->
      <div class="navbar__brand" @click="goHome">
        <div class="navbar__logo">
          <el-icon :size="24"><Reading /></el-icon>
        </div>
        <span class="navbar__title">Leot社区</span>
      </div>
      
      <!-- 导航菜单 -->
      <nav class="navbar__menu">
        <router-link to="/bagu" class="navbar__link" :class="{ active: isActive('/bagu') }">
          <el-icon><Collection /></el-icon>
          <span>题库</span>
        </router-link>
        <router-link v-if="isLoggedIn" to="/bagu/favourites" class="navbar__link" :class="{ active: isActive('/bagu/favourites') }">
          <el-icon><Star /></el-icon>
          <span>收藏</span>
        </router-link>
        <router-link v-if="isAdmin" to="/admin/bagu" class="navbar__link" :class="{ active: isActive('/admin') }">
          <el-icon><Setting /></el-icon>
          <span>管理</span>
        </router-link>
      </nav>
      
      <!-- 用户区域 -->
      <div class="navbar__user">
        <!-- 主题切换按钮 -->
        <button class="navbar__theme-btn" @click="toggleTheme" :title="isDark ? '切换到亮色模式' : '切换到暗色模式'">
          <el-icon :size="18">
            <Sunny v-if="isDark" />
            <Moon v-else />
          </el-icon>
        </button>
        
        <template v-if="isLoggedIn">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="navbar__avatar">
              <el-avatar :size="32">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="navbar__username">{{ userName }}</span>
              <el-icon class="navbar__arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" command="admin" divided>
                  <el-icon><Setting /></el-icon>
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button class="navbar__login-btn" @click="goLogin">
            <el-icon><User /></el-icon>
            登录
          </el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Reading, Collection, Star, Setting, User, 
  ArrowDown, SwitchButton, Sunny, Moon 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()

const isLoggedIn = computed(() => !!userStore.token)
const isAdmin = computed(() => userStore.user?.userRole === 'admin')
const userName = computed(() => userStore.user?.userName || '用户')
const isDark = computed(() => themeStore.theme === 'dark')

const toggleTheme = () => {
  themeStore.toggleTheme()
}

const isActive = (path: string) => {
  return route.path.startsWith(path)
}

const goHome = () => {
  router.push('/bagu')
}

const goLogin = () => {
  router.push('/login')
}

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/dashboard')
      break
    case 'admin':
      router.push('/admin/bagu')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/bagu')
      } catch {
        // 用户取消
      }
      break
  }
}
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 20px rgba(102, 126, 234, 0.3);
}

.navbar__container {
  max-width: 1400px;
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* Logo 区域 */
.navbar__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.navbar__brand:hover {
  opacity: 0.9;
}

.navbar__logo {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: white;
}

.navbar__title {
  font-size: 18px;
  font-weight: 600;
  color: white;
  letter-spacing: 1px;
}

/* 导航菜单 */
.navbar__menu {
  display: flex;
  align-items: center;
  gap: 8px;
}

.navbar__link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  font-size: 14px;
  border-radius: 8px;
  transition: all 0.2s;
}

.navbar__link:hover {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

.navbar__link.active {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: 500;
}

/* 用户区域 */
.navbar__user {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 主题切换按钮 */
.navbar__theme-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
}

.navbar__theme-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: rotate(15deg);
}

.navbar__avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.2s;
}

.navbar__avatar:hover {
  background: rgba(255, 255, 255, 0.25);
}

.navbar__username {
  color: white;
  font-size: 14px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.navbar__arrow {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

.navbar__login-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  border-radius: 20px;
  padding: 8px 20px;
  transition: all 0.2s;
}

.navbar__login-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
  color: white;
}

/* 响应式 */
@media (max-width: 768px) {
  .navbar__menu {
    display: none;
  }
  
  .navbar__username {
    display: none;
  }
}
</style>
