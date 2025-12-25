<template>
  <div class="tech-page dashboard">
    <div class="dashboard__container">
      <!-- 用户信息卡片 -->
      <div class="dashboard__profile">
        <div class="profile-card glass">
          <div class="profile-card__header">
            <div class="profile-card__avatar">
              <el-avatar :size="80">
                <el-icon :size="40"><User /></el-icon>
              </el-avatar>
              <div class="profile-card__badge" v-if="userStore.user?.userRole === 'admin'">
                <el-icon><Medal /></el-icon>
              </div>
            </div>
            <div class="profile-card__info">
              <h1 class="gradient-text">{{ userStore.user?.userName || '用户' }}</h1>
              <p class="profile-card__account">账号：{{ userStore.user?.userAccount }}</p>
              <el-tag 
                :type="userStore.user?.userRole === 'admin' ? 'danger' : 'primary'"
                effect="dark"
                class="profile-card__role"
              >
                {{ userStore.user?.userRole === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </div>
          </div>
          
          <div class="profile-card__bio">
            <el-icon><Document /></el-icon>
            <span>{{ userStore.user?.profile || '这个人很懒，什么都没写~' }}</span>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="dashboard__stats">
        <div class="stat-card glass" @click="goToFavourites">
          <div class="stat-card__icon">
            <el-icon :size="32"><Star /></el-icon>
          </div>
          <div class="stat-card__content">
            <span class="stat-card__value">{{ favouriteCount }}</span>
            <span class="stat-card__label">我的收藏</span>
          </div>
        </div>
        
        <div class="stat-card glass" @click="goToBanks">
          <div class="stat-card__icon">
            <el-icon :size="32"><Collection /></el-icon>
          </div>
          <div class="stat-card__content">
            <span class="stat-card__value">{{ bankCount }}</span>
            <span class="stat-card__label">题库数量</span>
          </div>
        </div>
        
        <div class="stat-card glass" v-if="userStore.isAdmin()" @click="goToAdmin">
          <div class="stat-card__icon admin">
            <el-icon :size="32"><Setting /></el-icon>
          </div>
          <div class="stat-card__content">
            <span class="stat-card__label">管理后台</span>
            <span class="stat-card__hint">点击进入</span>
          </div>
        </div>
      </div>

      <!-- 详细信息 -->
      <div class="dashboard__details glass">
        <h2 class="details__title">
          <el-icon><InfoFilled /></el-icon>
          账户信息
        </h2>
        <div class="details__grid">
          <div class="details__item">
            <span class="details__label">用户ID</span>
            <span class="details__value">{{ userStore.user?.id }}</span>
          </div>
          <div class="details__item">
            <span class="details__label">用户名</span>
            <span class="details__value">{{ userStore.user?.userName }}</span>
          </div>
          <div class="details__item">
            <span class="details__label">账号</span>
            <span class="details__value">{{ userStore.user?.userAccount }}</span>
          </div>
          <div class="details__item">
            <span class="details__label">角色</span>
            <span class="details__value">{{ userStore.user?.userRole === 'admin' ? '管理员' : '普通用户' }}</span>
          </div>
          <div class="details__item" v-if="userStore.user?.createTime">
            <span class="details__label">注册时间</span>
            <span class="details__value">{{ formatDate(userStore.user?.createTime) }}</span>
          </div>
          <div class="details__item" v-if="userStore.user?.updateTime">
            <span class="details__label">更新时间</span>
            <span class="details__value">{{ formatDate(userStore.user?.updateTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="dashboard__actions">
        <el-button class="action-btn gradient-btn" size="large" @click="goToBanks">
          <el-icon><Collection /></el-icon>
          浏览题库
        </el-button>
        <el-button class="action-btn" size="large" @click="goToFavourites">
          <el-icon><Star /></el-icon>
          我的收藏
        </el-button>
        <el-button class="action-btn" size="large" v-if="userStore.isAdmin()" @click="goToAdmin">
          <el-icon><Setting /></el-icon>
          管理后台
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Medal, Document, Star, Collection, Setting, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useBaguStore } from '@/stores/bagu'

const router = useRouter()
const userStore = useUserStore()
const baguStore = useBaguStore()

const favouriteCount = ref(0)
const bankCount = ref(0)

onMounted(async () => {
  if (!userStore.user && userStore.token) {
    await userStore.initUser()
  }
  // 获取统计数据
  await baguStore.fetchBanks({ current: 1, pageSize: 1 })
  bankCount.value = baguStore.total
})

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const goToBanks = () => router.push('/bagu')
const goToFavourites = () => router.push('/bagu/favourites')
const goToAdmin = () => router.push('/admin/bagu')
</script>

<style scoped>
.dashboard {
  padding-top: 24px;
}

.dashboard__container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 用户信息卡片 */
.profile-card {
  padding: 32px;
  margin-bottom: 24px;
}

.profile-card__header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 20px;
}

.profile-card__avatar {
  position: relative;
}

.profile-card__avatar .el-avatar {
  background: var(--primary-gradient);
}

.profile-card__badge {
  position: absolute;
  bottom: -4px;
  right: -4px;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.5);
}

.profile-card__info h1 {
  font-size: 28px;
  margin: 0 0 8px;
}

.profile-card__account {
  color: var(--text-secondary);
  margin: 0 0 12px;
  font-size: 14px;
}

.profile-card__role {
  font-size: 12px;
}

.profile-card__bio {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 14px;
}

/* 统计卡片 */
.dashboard__stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--glow-primary);
}

.stat-card__icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-gradient);
  border-radius: var(--radius-md);
  color: white;
}

.stat-card__icon.admin {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card__content {
  display: flex;
  flex-direction: column;
}

.stat-card__value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-card__label {
  font-size: 14px;
  color: var(--text-secondary);
}

.stat-card__hint {
  font-size: 12px;
  color: var(--text-muted);
}

/* 详细信息 */
.dashboard__details {
  padding: 24px;
  margin-bottom: 24px;
}

.details__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  margin: 0 0 20px;
  color: var(--text-primary);
}

.details__grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.details__item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
}

.details__label {
  font-size: 12px;
  color: var(--text-muted);
}

.details__value {
  font-size: 14px;
  color: var(--text-primary);
}

/* 快捷操作 */
.dashboard__actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-btn {
  min-width: 140px;
  height: 48px;
  border-radius: var(--radius-md);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn:not(.gradient-btn) {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
}

.action-btn:not(.gradient-btn):hover {
  border-color: var(--primary-start);
  color: var(--primary-start);
}

@media (max-width: 768px) {
  .profile-card__header {
    flex-direction: column;
    text-align: center;
  }
  
  .details__grid {
    grid-template-columns: 1fr;
  }
}
</style>
