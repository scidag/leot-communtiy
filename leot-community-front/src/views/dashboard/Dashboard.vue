<template>
  <el-container class="dashboard-container">
    <el-header class="dashboard-header">
      <div class="header-left">
        <h2>leot社区</h2>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ userStore.user?.userName || '未登录' }}
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="admin" v-if="userStore.isAdmin()">
                用户管理
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="dashboard-main">
      <el-card class="welcome-card">
        <template #header>
          <div class="card-header">
            <span>欢迎，{{ userStore.user?.userName }}！</span>
          </div>
        </template>
        <div class="user-info-content">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户ID">
              {{ userStore.user?.id }}
            </el-descriptions-item>
            <el-descriptions-item label="用户名">
              {{ userStore.user?.userName }}
            </el-descriptions-item>
            <el-descriptions-item label="账号">
              {{ userStore.user?.userAccount }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="userStore.user?.userRole === 'admin' ? 'danger' : 'primary'">
                {{ userStore.user?.userRole === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="简介" :span="2">
              {{ userStore.user?.profile || '暂无简介' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" v-if="userStore.user?.createTime">
              {{ userStore.user?.createTime }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间" v-if="userStore.user?.updateTime">
              {{ userStore.user?.updateTime }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="action-buttons" v-if="userStore.isAdmin()">
          <el-button type="primary" size="large" @click="goToUserManagement">
            <el-icon><Setting /></el-icon>
            进入用户管理
          </el-button>
        </div>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, ArrowDown, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

onMounted(async () => {
  // 如果用户信息不存在，尝试初始化
  if (!userStore.user && userStore.token) {
    await userStore.initUser()
  }
})

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {
      // 用户取消
    }
  } else if (command === 'admin') {
    router.push('/admin/users')
  }
}

const goToUserManagement = () => {
  router.push('/admin/users')
}
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #409eff;
  color: white;
  padding: 0 20px;
}

.header-left h2 {
  margin: 0;
  color: white;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: white;
}

.user-info .el-icon {
  margin-right: 5px;
}

.dashboard-main {
  background-color: #f5f5f5;
  padding: 20px;
}

.welcome-card {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.user-info-content {
  margin-bottom: 20px;
}

.action-buttons {
  text-align: center;
  margin-top: 20px;
}
</style>

