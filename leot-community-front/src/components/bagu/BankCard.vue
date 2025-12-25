<template>
  <GlassCard 
    class="bank-card" 
    :glow="true" 
    :hover-scale="true" 
    :clickable="true"
    padding="0"
    @click="handleClick"
  >
    <div class="bank-card__cover">
      <img 
        v-if="bank.picture" 
        :src="bank.picture" 
        :alt="bank.title"
        class="bank-card__image"
      />
      <div v-else class="bank-card__placeholder">
        <el-icon :size="48"><Folder /></el-icon>
      </div>
      <div class="bank-card__overlay"></div>
    </div>
    <div class="bank-card__content">
      <h3 class="bank-card__title">{{ bank.title }}</h3>
      <p v-if="bank.description" class="bank-card__desc">{{ bank.description }}</p>
      <div class="bank-card__footer">
        <span class="bank-card__count">
          <el-icon><Document /></el-icon>
          {{ bank.questionCount || 0 }} 题
        </span>
        <span class="bank-card__time">
          {{ formatDate(bank.createTime) }}
        </span>
      </div>
    </div>
  </GlassCard>
</template>

<script setup lang="ts">
import { Folder, Document } from '@element-plus/icons-vue'
import GlassCard from './GlassCard.vue'
import type { QuestionBank } from '@/types/bagu'

interface Props {
  bank: QuestionBank
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'click', id: number): void
}>()

const handleClick = () => {
  emit('click', props.bank.id)
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.bank-card {
  overflow: hidden;
}

.bank-card__cover {
  position: relative;
  height: 140px;
  overflow: hidden;
}

.bank-card__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: var(--transition-base);
}

.bank-card:hover .bank-card__image {
  transform: scale(1.1);
}

.bank-card__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-tertiary) 100%);
  color: var(--text-muted);
}

.bank-card__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6) 0%, transparent 50%);
}

.bank-card__content {
  padding: 16px;
}

.bank-card__title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bank-card__desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.bank-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--text-muted);
}

.bank-card__count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--primary-start);
}
</style>
