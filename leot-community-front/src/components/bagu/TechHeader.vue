<template>
  <header class="tech-header" :class="{ 'tech-header--animated': animated }">
    <div class="tech-header__bg"></div>
    <div class="tech-header__content">
      <h1 class="tech-header__title gradient-text">{{ title }}</h1>
      <p v-if="subtitle" class="tech-header__subtitle">{{ subtitle }}</p>
      <div v-if="showSearch" class="tech-header__search">
        <el-input
          v-model="searchValue"
          :placeholder="searchPlaceholder"
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      <slot name="extra" />
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'

interface Props {
  title: string
  subtitle?: string
  showSearch?: boolean
  searchPlaceholder?: string
  animated?: boolean
}

withDefaults(defineProps<Props>(), {
  showSearch: false,
  searchPlaceholder: '搜索题目...',
  animated: true
})

const emit = defineEmits<{
  (e: 'search', keyword: string): void
}>()

const searchValue = ref('')

const handleSearch = () => {
  emit('search', searchValue.value)
}
</script>

<style scoped>
.tech-header {
  position: relative;
  padding: 48px 24px;
  text-align: center;
  overflow: hidden;
}

.tech-header__bg {
  position: absolute;
  inset: 0;
  background: 
    radial-gradient(ellipse at 50% 0%, rgba(102, 126, 234, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 50%, rgba(118, 75, 162, 0.1) 0%, transparent 40%),
    radial-gradient(ellipse at 20% 80%, rgba(102, 126, 234, 0.1) 0%, transparent 40%);
  z-index: 0;
}

.tech-header--animated .tech-header__bg {
  animation: float 6s ease-in-out infinite;
}

.tech-header__content {
  position: relative;
  z-index: 1;
  max-width: 600px;
  margin: 0 auto;
}

.tech-header__title {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 12px;
  letter-spacing: 2px;
}

.tech-header__subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0 0 32px;
}

.tech-header__search {
  max-width: 500px;
  margin: 0 auto;
}

.tech-header__search :deep(.el-input__wrapper) {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: none;
}

.tech-header__search :deep(.el-input__wrapper:hover),
.tech-header__search :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-start);
  box-shadow: var(--glow-subtle);
}

.tech-header__search :deep(.el-input__inner) {
  color: var(--text-primary);
}

.tech-header__search :deep(.el-input__inner::placeholder) {
  color: var(--text-muted);
}

.tech-header__search :deep(.el-input-group__append) {
  background: var(--primary-gradient);
  border: none;
  color: white;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-10px) scale(1.02); }
}
</style>
