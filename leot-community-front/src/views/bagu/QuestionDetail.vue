<template>
  <div class="tech-page question-detail">
    <div class="question-detail__container">
      <el-button 
        class="question-detail__back" 
        :icon="ArrowLeft" 
        circle 
        @click="goBack"
      />
      
      <div v-if="loading" class="question-detail__skeleton">
        <div class="skeleton" style="height: 40px; width: 80%; margin-bottom: 20px;"></div>
        <div class="skeleton" style="height: 24px; width: 40%; margin-bottom: 30px;"></div>
        <div class="skeleton" style="height: 200px; margin-bottom: 30px;"></div>
        <div class="skeleton" style="height: 200px;"></div>
      </div>
      
      <GlassCard v-else-if="currentQuestion" class="question-detail__card" :glow="true">
        <div class="question-detail__header">
          <h1 class="gradient-text">{{ currentQuestion.title }}</h1>
          <div class="question-detail__meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ currentQuestion.userName || '匿名' }}
            </span>
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              {{ formatDate(currentQuestion.createTime) }}
            </span>
          </div>
        </div>
        
        <div class="question-detail__tags">
          <span 
            v-for="tag in currentQuestion.tags" 
            :key="tag" 
            class="tech-tag"
          >
            {{ tag }}
          </span>
        </div>
        
        <div class="question-detail__stats">
          <span class="stat-item">
            <el-icon><View /></el-icon>
            {{ currentQuestion.viewNum }} 浏览
          </span>
          <span class="stat-item" :class="{ active: currentQuestion.hasThumb }">
            <el-icon><Star /></el-icon>
            {{ currentQuestion.thumbNum }} 点赞
          </span>
          <span class="stat-item" :class="{ active: currentQuestion.hasFavour }">
            <el-icon><Collection /></el-icon>
            {{ currentQuestion.favourNum }} 收藏
          </span>
        </div>
        
        <div class="question-detail__actions">
          <el-button 
            :type="currentQuestion.hasThumb ? 'primary' : 'default'"
            :icon="currentQuestion.hasThumb ? StarFilled : Star"
            @click="handleThumb"
          >
            {{ currentQuestion.hasThumb ? '已点赞' : '点赞' }}
          </el-button>
          <el-button 
            :type="currentQuestion.hasFavour ? 'warning' : 'default'"
            :icon="currentQuestion.hasFavour ? FolderChecked : Collection"
            @click="handleFavour"
          >
            {{ currentQuestion.hasFavour ? '已收藏' : '收藏' }}
          </el-button>
        </div>
        
        <div class="question-detail__content">
          <div class="content-section">
            <h2>
              <el-icon><Document /></el-icon>
              题目内容
            </h2>
            <MarkdownRenderer :content="currentQuestion.content" />
          </div>
          
          <div v-if="currentQuestion.answer" class="content-section">
            <h2>
              <el-icon><Checked /></el-icon>
              参考答案
            </h2>
            <MarkdownRenderer :content="currentQuestion.answer" />
          </div>
        </div>
      </GlassCard>
      
      <div v-else class="empty-state">
        <el-icon :size="64"><Document /></el-icon>
        <p>题目不存在或已删除</p>
        <el-button type="primary" class="gradient-btn" @click="goBack">
          返回
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, User, Calendar, View, Star, StarFilled, 
  Collection, FolderChecked, Document, Checked 
} from '@element-plus/icons-vue'
import { useBaguStore } from '@/stores/bagu'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import GlassCard from '@/components/bagu/GlassCard.vue'
import MarkdownRenderer from '@/components/bagu/MarkdownRenderer.vue'

const route = useRoute()
const router = useRouter()
const baguStore = useBaguStore()
const userStore = useUserStore()
const { currentQuestion, loading } = storeToRefs(baguStore)

const questionId = Number(route.params.id)

onMounted(() => {
  baguStore.fetchQuestionDetail(questionId)
})

const handleThumb = () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  baguStore.toggleThumb(questionId)
}

const handleFavour = () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  baguStore.toggleFavour(questionId)
}

const goBack = () => {
  router.back()
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.question-detail__container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
  position: relative;
}

.question-detail__back {
  position: absolute;
  top: 24px;
  left: 24px;
  z-index: 10;
  background: var(--bg-card);
  border-color: var(--border-color);
  color: var(--text-primary);
}

.question-detail__skeleton {
  padding: 40px;
}

.question-detail__card {
  padding: 40px;
  margin-top: 40px;
}

.question-detail__header {
  margin-bottom: 20px;
}

.question-detail__header h1 {
  font-size: 32px;
  margin: 0 0 16px;
  line-height: 1.3;
}

.question-detail__meta {
  display: flex;
  gap: 24px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 14px;
}

.question-detail__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.question-detail__stats {
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-color);
}

.question-detail__stats .stat-item.active {
  color: var(--primary-start);
}

.question-detail__actions {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.question-detail__content {
  margin-top: 32px;
}

.content-section {
  margin-bottom: 40px;
}

.content-section:last-child {
  margin-bottom: 0;
}

.content-section h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  color: var(--primary-start);
  margin: 0 0 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--primary-start);
}

.content-text {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-secondary);
}

.content-text :deep(pre) {
  background: var(--bg-secondary);
  padding: 16px;
  border-radius: var(--radius-md);
  overflow-x: auto;
}

.content-text :deep(code) {
  font-family: 'Fira Code', monospace;
  font-size: 14px;
}
</style>
