<template>
  <div class="tech-page favorite-list">
    <TechHeader 
      title="我的收藏" 
      subtitle="收藏的题目，随时复习"
      :animated="true"
    />
    
    <div class="favorite-list__content">
      <div v-if="loading" class="question-list">
        <div v-for="i in 5" :key="i" class="skeleton-question">
          <div class="skeleton" style="height: 24px; width: 70%; margin-bottom: 12px;"></div>
          <div class="skeleton" style="height: 20px; width: 40%; margin-bottom: 16px;"></div>
          <div class="skeleton" style="height: 20px; width: 30%;"></div>
        </div>
      </div>
      
      <div v-else-if="favourites.length === 0" class="empty-state">
        <el-icon :size="64"><Collection /></el-icon>
        <p>暂无收藏</p>
        <p class="empty-hint">去题库看看，收藏感兴趣的题目吧</p>
        <el-button type="primary" class="gradient-btn" @click="goToBagu">
          浏览题库
        </el-button>
      </div>
      
      <div v-else class="question-list">
        <QuestionCard 
          v-for="(question, index) in favourites" 
          :key="question.id"
          :question="question"
          :expanded="expandedId === question.id"
          class="animate-fade-in-up"
          :style="{ animationDelay: `${index * 0.05}s` }"
          @click="toggleExpand"
          @thumb="handleThumb"
          @favour="handleFavour"
        />
      </div>
      
      <div v-if="total > pageSize" class="favorite-list__pagination">
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
import { Collection } from '@element-plus/icons-vue'
import { useBaguStore } from '@/stores/bagu'
import { storeToRefs } from 'pinia'
import TechHeader from '@/components/bagu/TechHeader.vue'
import QuestionCard from '@/components/bagu/QuestionCard.vue'

const router = useRouter()
const baguStore = useBaguStore()
const { favourites, loading, total } = storeToRefs(baguStore)

const currentPage = ref(1)
const pageSize = ref(10)
const expandedId = ref<number | null>(null)

onMounted(() => {
  fetchData()
})

const fetchData = () => {
  baguStore.fetchFavourites({
    current: currentPage.value,
    pageSize: pageSize.value
  })
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  expandedId.value = null
  fetchData()
}

const toggleExpand = (id: number) => {
  expandedId.value = expandedId.value === id ? null : id
}

const handleThumb = (id: number) => {
  baguStore.toggleThumb(id)
}

const handleFavour = (id: number) => {
  baguStore.toggleFavour(id)
  // 取消收藏后从列表移除
  setTimeout(() => {
    fetchData()
  }, 500)
}

const goToBagu = () => {
  router.push('/bagu')
}
</script>

<style scoped>
.favorite-list__content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-question {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 20px;
}

.empty-hint {
  font-size: 14px;
  color: var(--text-muted);
  margin: 8px 0 24px;
}

.favorite-list__pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>
