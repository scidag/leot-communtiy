<template>
  <GlassCard 
    class="question-card" 
    :class="{ 'question-card--expanded': expanded }"
    :glow="expanded"
    :hover-scale="!expanded"
    :clickable="!expanded"
    @click="handleClick"
  >
    <div class="question-card__header">
      <h3 class="question-card__title">{{ question.title }}</h3>
      <div v-if="showActions" class="question-card__actions">
        <el-button 
          :type="question.hasThumb ? 'primary' : 'default'"
          :icon="question.hasThumb ? StarFilled : Star"
          circle
          size="small"
          @click.stop="handleThumb"
        />
        <el-button 
          :type="question.hasFavour ? 'warning' : 'default'"
          :icon="question.hasFavour ? FolderChecked : Collection"
          circle
          size="small"
          @click.stop="handleFavour"
        />
      </div>
    </div>
    
    <div class="question-card__tags">
      <span 
        v-for="tag in question.tags" 
        :key="tag" 
        class="tech-tag"
      >
        {{ tag }}
      </span>
    </div>
    
    <div class="question-card__stats">
      <span class="stat-item">
        <el-icon><View /></el-icon>
        {{ question.viewNum }}
      </span>
      <span class="stat-item">
        <el-icon><Star /></el-icon>
        {{ question.thumbNum }}
      </span>
      <span class="stat-item">
        <el-icon><Collection /></el-icon>
        {{ question.favourNum }}
      </span>
    </div>
    
    <transition name="expand">
      <div v-if="expanded" class="question-card__content">
        <div class="question-card__section">
          <h4>题目内容</h4>
          <div class="question-card__text" v-html="question.content"></div>
        </div>
        <div v-if="question.answer" class="question-card__section">
          <h4>参考答案</h4>
          <div class="question-card__text" v-html="question.answer"></div>
        </div>
      </div>
    </transition>
    
    <div v-if="expanded" class="question-card__footer">
      <span class="question-card__author">
        <el-icon><User /></el-icon>
        {{ question.userName || '匿名' }}
      </span>
      <span class="question-card__time">
        {{ formatDate(question.createTime) }}
      </span>
    </div>
  </GlassCard>
</template>

<script setup lang="ts">
import { View, Star, StarFilled, Collection, FolderChecked, User } from '@element-plus/icons-vue'
import GlassCard from './GlassCard.vue'
import type { Question } from '@/types/bagu'

interface Props {
  question: Question
  expanded?: boolean
  showActions?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  expanded: false,
  showActions: true
})

const emit = defineEmits<{
  (e: 'click', id: number): void
  (e: 'thumb', id: number): void
  (e: 'favour', id: number): void
}>()

const handleClick = () => {
  if (!props.expanded) {
    emit('click', props.question.id)
  }
}

const handleThumb = () => {
  emit('thumb', props.question.id)
}

const handleFavour = () => {
  emit('favour', props.question.id)
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.question-card {
  padding: 20px;
}

.question-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 12px;
}

.question-card__title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
  flex: 1;
  line-height: 1.4;
}

.question-card__actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.question-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.question-card__stats {
  display: flex;
  gap: 20px;
}

.question-card__content {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

.question-card__section {
  margin-bottom: 20px;
}

.question-card__section:last-child {
  margin-bottom: 0;
}

.question-card__section h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-start);
  margin: 0 0 12px;
}

.question-card__text {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-secondary);
}

.question-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
  font-size: 13px;
  color: var(--text-muted);
}

.question-card__author {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 展开动画 */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 1000px;
}
</style>
