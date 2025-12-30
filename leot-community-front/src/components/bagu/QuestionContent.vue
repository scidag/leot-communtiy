<template>
  <div class="question-content" ref="contentRef">
    <div v-if="loading" class="question-content__skeleton">
      <div class="skeleton" style="height: 40px; width: 80%; margin-bottom: 20px;"></div>
      <div class="skeleton" style="height: 24px; width: 40%; margin-bottom: 30px;"></div>
      <div class="skeleton" style="height: 200px; margin-bottom: 30px;"></div>
      <div class="skeleton" style="height: 200px;"></div>
    </div>
    
    <template v-else-if="question">
      <div class="question-content__header">
        <h1 class="gradient-text">{{ question.title }}</h1>
        <div class="question-content__meta">
          <span class="meta-item">
            <el-icon><User /></el-icon>
            {{ question.userName || '匿名' }}
          </span>
          <span class="meta-item">
            <el-icon><Calendar /></el-icon>
            {{ formatDate(question.createTime) }}
          </span>
        </div>
      </div>
      
      <div class="question-content__tags">
        <span 
          v-for="tag in question.tags" 
          :key="tag" 
          class="tech-tag"
        >
          {{ tag }}
        </span>
      </div>
      
      <div class="question-content__stats">
        <span class="stat-item">
          <el-icon><View /></el-icon>
          {{ question.viewNum }} 浏览
        </span>
        <span class="stat-item" :class="{ active: question.hasThumb }">
          <el-icon><Star /></el-icon>
          {{ question.thumbNum }} 点赞
        </span>
        <span class="stat-item" :class="{ active: question.hasFavour }">
          <el-icon><Collection /></el-icon>
          {{ question.favourNum }} 收藏
        </span>
      </div>
      
      <div class="question-content__actions">
        <el-button 
          :type="question.hasThumb ? 'primary' : 'default'"
          :icon="question.hasThumb ? StarFilled : Star"
          @click="emit('thumb')"
        >
          {{ question.hasThumb ? '已点赞' : '点赞' }}
        </el-button>
        <el-button 
          :type="question.hasFavour ? 'warning' : 'default'"
          :icon="question.hasFavour ? FolderChecked : Collection"
          @click="emit('favour')"
        >
          {{ question.hasFavour ? '已收藏' : '收藏' }}
        </el-button>
      </div>
      
      <div class="question-content__body">
        <div class="content-section">
          <h2>
            <el-icon><Document /></el-icon>
            答案
          </h2>
          <MarkdownRenderer :content="question.answer" />
        </div>
      </div>
      
      <!-- 上下题导航 -->
      <div class="question-content__nav">
        <el-button 
          v-if="hasPrev"
          :icon="ArrowLeft"
          @click="emit('prev')"
        >
          上一题
        </el-button>
        <span v-else></span>
        
        <span class="question-content__nav-progress">
          {{ currentIndex + 1 }} / {{ totalCount }}
        </span>
        
        <el-button 
          v-if="hasNext"
          @click="emit('next')"
        >
          下一题
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
        <span v-else></span>
      </div>
    </template>
    
    <div v-else class="question-content__empty">
      <el-icon :size="64"><Document /></el-icon>
      <p>题目不存在或已删除</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { 
  User, Calendar, View, Star, StarFilled, 
  Collection, FolderChecked, Document, Checked,
  ArrowLeft, ArrowRight
} from '@element-plus/icons-vue'
import MarkdownRenderer from '@/components/bagu/MarkdownRenderer.vue'
import type { Question } from '@/types/bagu'

interface Props {
  question: Question | null
  loading?: boolean
  currentIndex: number
  totalCount: number
  hasPrev: boolean
  hasNext: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<{
  (e: 'prev'): void
  (e: 'next'): void
  (e: 'thumb'): void
  (e: 'favour'): void
}>()

const contentRef = ref<HTMLElement | null>(null)

// 切换题目时滚动到顶部
watch(() => props.question?.id, () => {
  if (contentRef.value) {
    contentRef.value.scrollTop = 0
  }
})

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.question-content {
  height: 100%;
  overflow-y: auto;
  padding: 32px;
}

.question-content__skeleton {
  padding: 20px 0;
}

.question-content__header {
  margin-bottom: 20px;
}

.question-content__header h1 {
  font-size: 28px;
  margin: 0 0 16px;
  line-height: 1.3;
}

.question-content__meta {
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

.question-content__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.question-content__stats {
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-color);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 14px;
}

.stat-item.active {
  color: var(--primary-start);
}

.question-content__actions {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.question-content__body {
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

.question-content__nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.question-content__nav-progress {
  font-size: 14px;
  color: var(--text-secondary);
}

.question-content__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted);
  gap: 16px;
}

.question-content__empty p {
  margin: 0;
  font-size: 16px;
}
</style>
