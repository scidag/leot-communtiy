<template>
  <div class="question-nav-list">
    <div class="question-nav-list__header">
      <span class="question-nav-list__title">题目列表</span>
      <span class="question-nav-list__progress">{{ currentIndex + 1 }}/{{ questions.length }}</span>
    </div>
    
    <div v-if="loading" class="question-nav-list__loading">
      <div v-for="i in 5" :key="i" class="skeleton-item">
        <div class="skeleton" style="height: 20px; width: 80%;"></div>
      </div>
    </div>
    
    <div v-else-if="questions.length === 0" class="question-nav-list__empty">
      <el-icon :size="32"><Document /></el-icon>
      <span>暂无题目</span>
    </div>
    
    <ul v-else class="question-nav-list__items" ref="listRef">
      <li 
        v-for="(question, index) in questions" 
        :key="question.id"
        :class="['question-nav-list__item', { active: question.id === activeId }]"
        :ref="el => { if (question.id === activeId) activeItemRef = el as HTMLElement }"
        @click="handleSelect(question.id)"
      >
        <span class="question-nav-list__index">{{ index + 1 }}</span>
        <span class="question-nav-list__item-title">{{ question.title }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { Document } from '@element-plus/icons-vue'
import type { Question } from '@/types/bagu'

interface Props {
  questions: Question[]
  activeId: number
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<{
  (e: 'select', id: number): void
}>()

const listRef = ref<HTMLElement | null>(null)
const activeItemRef = ref<HTMLElement | null>(null)

const currentIndex = computed(() => {
  return props.questions.findIndex(q => q.id === props.activeId)
})

const handleSelect = (id: number) => {
  emit('select', id)
}

// 当 activeId 变化时，滚动到当前选中项
watch(() => props.activeId, async () => {
  await nextTick()
  if (activeItemRef.value && listRef.value) {
    activeItemRef.value.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
  }
})
</script>

<style scoped>
.question-nav-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.question-nav-list__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.question-nav-list__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.question-nav-list__progress {
  font-size: 14px;
  color: var(--primary-start);
  font-weight: 500;
}

.question-nav-list__loading,
.question-nav-list__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: var(--text-muted);
  gap: 12px;
}

.skeleton-item {
  padding: 12px 20px;
}

.question-nav-list__items {
  flex: 1;
  overflow-y: auto;
  list-style: none;
  margin: 0;
  padding: 8px 0;
}

.question-nav-list__item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
}

.question-nav-list__item:hover {
  background: var(--bg-secondary);
}

.question-nav-list__item.active {
  background: rgba(var(--primary-rgb), 0.1);
  border-left-color: var(--primary-start);
}

.question-nav-list__item.active .question-nav-list__index {
  background: var(--primary-start);
  color: white;
}

.question-nav-list__index {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 500;
  background: var(--bg-secondary);
  color: var(--text-secondary);
  border-radius: 50%;
  transition: all 0.2s ease;
}

.question-nav-list__item-title {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.question-nav-list__item.active .question-nav-list__item-title {
  color: var(--primary-start);
  font-weight: 500;
}
</style>
