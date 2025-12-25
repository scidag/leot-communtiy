<template>
  <div class="tech-page question-search">
    <TechHeader 
      title="搜索题目" 
      subtitle="快速找到你想学习的内容"
      :show-search="true"
      :search-placeholder="'输入关键词搜索...'"
      @search="handleSearch"
    />
    
    <div class="question-search__content">
      <!-- 筛选区域 -->
      <GlassCard class="question-search__filters" padding="20px">
        <div class="filter-row">
          <div class="filter-item">
            <label>题库筛选</label>
            <el-select 
              v-model="searchParams.questionBankId" 
              placeholder="选择题库"
              clearable
              @change="handleFilter"
            >
              <el-option 
                v-for="bank in allBanks" 
                :key="bank.id" 
                :label="bank.title" 
                :value="bank.id" 
              />
            </el-select>
          </div>
          <div class="filter-item">
            <label>标签筛选</label>
            <el-select 
              v-model="searchParams.tags" 
              placeholder="选择标签"
              multiple
              clearable
              @change="handleFilter"
            >
              <el-option 
                v-for="tag in allTags" 
                :key="tag" 
                :label="tag" 
                :value="tag" 
              />
            </el-select>
          </div>
          <div class="filter-item">
            <label>排序方式</label>
            <el-select v-model="searchParams.sortField" @change="handleFilter">
              <el-option label="最新发布" value="createTime" />
              <el-option label="最多浏览" value="viewNum" />
              <el-option label="最多点赞" value="thumbNum" />
              <el-option label="最多收藏" value="favourNum" />
            </el-select>
          </div>
        </div>
      </GlassCard>
      
      <!-- 搜索结果 -->
      <div class="question-search__results">
        <div class="results-header">
          <span v-if="searchParams.keyword" class="results-keyword">
            搜索 "<span class="gradient-text">{{ searchParams.keyword }}</span>" 的结果
          </span>
          <span class="results-count">共 {{ total }} 条结果</span>
        </div>
        
        <div v-if="loading" class="question-list">
          <div v-for="i in 5" :key="i" class="skeleton-question">
            <div class="skeleton" style="height: 24px; width: 70%; margin-bottom: 12px;"></div>
            <div class="skeleton" style="height: 20px; width: 40%; margin-bottom: 16px;"></div>
            <div class="skeleton" style="height: 20px; width: 30%;"></div>
          </div>
        </div>
        
        <div v-else-if="questions.length === 0" class="empty-state">
          <el-icon :size="64"><Search /></el-icon>
          <p>未找到相关题目</p>
          <p class="empty-hint">试试其他关键词或筛选条件</p>
        </div>
        
        <div v-else class="question-list">
          <QuestionCard 
            v-for="(question, index) in questions" 
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
        
        <div v-if="total > searchParams.pageSize" class="question-search__pagination">
          <el-pagination
            v-model:current-page="searchParams.current"
            :page-size="searchParams.pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useBaguStore } from '@/stores/bagu'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import TechHeader from '@/components/bagu/TechHeader.vue'
import GlassCard from '@/components/bagu/GlassCard.vue'
import QuestionCard from '@/components/bagu/QuestionCard.vue'
import type { SearchParams } from '@/types/bagu'

const route = useRoute()
const router = useRouter()
const baguStore = useBaguStore()
const userStore = useUserStore()
const { questions, banks: allBanks, loading, total } = storeToRefs(baguStore)

const searchParams = reactive<SearchParams>({
  keyword: '',
  tags: [],
  questionBankId: undefined,
  sortField: 'createTime',
  sortOrder: 'desc',
  current: 1,
  pageSize: 10
})

const expandedId = ref<number | null>(null)

// 常用标签列表（可从后端获取）
const allTags = ref(['Java', 'Spring', 'MySQL', 'Redis', 'JVM', '并发', '网络', '算法', '设计模式', '微服务'])

onMounted(() => {
  // 从 URL 获取搜索关键词
  if (route.query.keyword) {
    searchParams.keyword = route.query.keyword as string
  }
  baguStore.fetchBanks({ current: 1, pageSize: 100 })
  doSearch()
})

watch(() => route.query.keyword, (newKeyword) => {
  if (newKeyword) {
    searchParams.keyword = newKeyword as string
    searchParams.current = 1
    doSearch()
  }
})

const doSearch = () => {
  baguStore.searchQuestions(searchParams)
}

const handleSearch = (keyword: string) => {
  searchParams.keyword = keyword
  searchParams.current = 1
  doSearch()
}

const handleFilter = () => {
  searchParams.current = 1
  doSearch()
}

const handlePageChange = (page: number) => {
  searchParams.current = page
  expandedId.value = null
  doSearch()
}

const toggleExpand = (id: number) => {
  expandedId.value = expandedId.value === id ? null : id
}

const handleThumb = (id: number) => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  baguStore.toggleThumb(id)
}

const handleFavour = (id: number) => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  baguStore.toggleFavour(id)
}
</script>

<style scoped>
.question-search__content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.question-search__filters {
  margin-bottom: 24px;
}

.filter-row {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 200px;
}

.filter-item label {
  font-size: 14px;
  color: var(--text-secondary);
}

.filter-item :deep(.el-select) {
  width: 100%;
}

.filter-item :deep(.el-input__wrapper) {
  background: var(--bg-secondary);
  border-color: var(--border-color);
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.results-keyword {
  font-size: 16px;
  color: var(--text-secondary);
}

.results-count {
  font-size: 14px;
  color: var(--text-muted);
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
  margin-top: 8px;
}

.question-search__pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>
