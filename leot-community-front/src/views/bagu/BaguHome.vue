<template>
  <div class="tech-page bagu-home">
    <TechHeader 
      title="八股文题库" 
      subtitle="系统化学习，高效备战面试"
      :show-search="true"
      @search="handleSearch"
    />
    
    <div class="bagu-home__content">
      <div v-if="loading" class="card-grid">
        <div v-for="i in 8" :key="i" class="skeleton-card">
          <div class="skeleton" style="height: 140px;"></div>
          <div style="padding: 16px;">
            <div class="skeleton" style="height: 24px; margin-bottom: 8px;"></div>
            <div class="skeleton" style="height: 40px; margin-bottom: 12px;"></div>
            <div class="skeleton" style="height: 20px; width: 60%;"></div>
          </div>
        </div>
      </div>
      
      <div v-else-if="banks.length === 0" class="empty-state">
        <el-icon :size="64"><Folder /></el-icon>
        <p>暂无题库</p>
      </div>
      
      <div v-else class="card-grid">
        <BankCard 
          v-for="(bank, index) in banks" 
          :key="bank.id" 
          :bank="bank"
          class="animate-fade-in-up"
          :style="{ animationDelay: `${index * 0.1}s` }"
          @click="goToBank"
        />
      </div>
      
      <div v-if="total > pageSize" class="bagu-home__pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Folder } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useBaguStore } from '@/stores/bagu'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import TechHeader from '@/components/bagu/TechHeader.vue'
import BankCard from '@/components/bagu/BankCard.vue'

const router = useRouter()
const baguStore = useBaguStore()
const userStore = useUserStore()
const { banks, loading, total } = storeToRefs(baguStore)

const currentPage = ref(1)
const pageSize = ref(12)

onMounted(() => {
  fetchData()
})

const fetchData = () => {
  baguStore.fetchBanks({
    current: currentPage.value,
    pageSize: pageSize.value
  })
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

const handleSearch = (keyword: string) => {
  router.push({ path: '/bagu/search', query: { keyword } })
}

const goToBank = (id: number) => {
  // 检查是否登录
  if (!userStore.token) {
    ElMessage.warning('请先登录后查看题库详情')
    router.push('/login')
    return
  }
  router.push(`/bagu/bank/${id}`)
}
</script>

<style scoped>
.bagu-home__content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.skeleton-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.bagu-home__pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.bagu-home__pagination :deep(.el-pagination) {
  --el-pagination-bg-color: var(--bg-card);
  --el-pagination-text-color: var(--text-secondary);
  --el-pagination-button-disabled-bg-color: var(--bg-secondary);
}

.bagu-home__pagination :deep(.el-pager li) {
  background: var(--bg-card);
  color: var(--text-secondary);
}

.bagu-home__pagination :deep(.el-pager li.is-active) {
  background: var(--primary-gradient);
  color: white;
}
</style>
