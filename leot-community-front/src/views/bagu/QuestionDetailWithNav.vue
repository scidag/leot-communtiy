<template>
  <div class="tech-page question-detail-nav">
    <!-- 返回按钮 -->
    <el-button 
      class="question-detail-nav__back" 
      :icon="ArrowLeft" 
      circle 
      @click="goBack"
    />
    
    <!-- 移动端导航切换按钮 -->
    <el-button 
      class="question-detail-nav__toggle"
      :icon="Menu"
      circle
      @click="toggleNav"
    />
    
    <div class="question-detail-nav__container">
      <!-- 左侧导航列表 -->
      <aside 
        :class="['question-detail-nav__sidebar', { visible: isNavVisible }]"
        @click.self="closeNav"
      >
        <div class="question-detail-nav__sidebar-content">
          <QuestionNavList 
            :questions="bankQuestionList"
            :active-id="questionId"
            :loading="navLoading"
            @select="handleSelectQuestion"
          />
        </div>
      </aside>
      
      <!-- 右侧详情内容 -->
      <main class="question-detail-nav__main">
        <GlassCard class="question-detail-nav__card" :glow="true">
          <QuestionContent 
            :question="currentQuestion"
            :loading="loading"
            :current-index="currentIndex"
            :total-count="bankQuestionList.length"
            :has-prev="hasPrev"
            :has-next="hasNext"
            @prev="handlePrev"
            @next="handleNext"
            @thumb="handleThumb"
            @favour="handleFavour"
          />
        </GlassCard>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Menu } from '@element-plus/icons-vue'
import { useBaguStore } from '@/stores/bagu'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import GlassCard from '@/components/bagu/GlassCard.vue'
import QuestionNavList from '@/components/bagu/QuestionNavList.vue'
import QuestionContent from '@/components/bagu/QuestionContent.vue'

const route = useRoute()
const router = useRouter()
const baguStore = useBaguStore()
const userStore = useUserStore()
const { currentQuestion, loading, bankQuestionList } = storeToRefs(baguStore)

const bankId = computed(() => Number(route.params.bankId))
const questionId = computed(() => Number(route.params.questionId))

const navLoading = ref(false)
const isNavVisible = ref(false)

// 计算当前索引
const currentIndex = computed(() => {
  return baguStore.getCurrentQuestionIndex(questionId.value)
})

// 是否有上一题/下一题
const hasPrev = computed(() => {
  return baguStore.getPrevQuestionId(questionId.value) !== null
})

const hasNext = computed(() => {
  return baguStore.getNextQuestionId(questionId.value) !== null
})

onMounted(async () => {
  // 加载题库题目列表
  navLoading.value = true
  await baguStore.fetchBankQuestions(bankId.value)
  navLoading.value = false
  
  // 加载当前题目详情
  await baguStore.fetchQuestionDetail(questionId.value)
})

// 监听路由参数变化，切换题目
watch(() => route.params.questionId, async (newId) => {
  if (newId) {
    await baguStore.fetchQuestionDetail(Number(newId))
  }
})

// 选择题目
const handleSelectQuestion = (id: number) => {
  router.push(`/bagu/bank/${bankId.value}/question/${id}`)
  closeNav()
}

// 上一题
const handlePrev = () => {
  const prevId = baguStore.getPrevQuestionId(questionId.value)
  if (prevId) {
    router.push(`/bagu/bank/${bankId.value}/question/${prevId}`)
  }
}

// 下一题
const handleNext = () => {
  const nextId = baguStore.getNextQuestionId(questionId.value)
  if (nextId) {
    router.push(`/bagu/bank/${bankId.value}/question/${nextId}`)
  }
}

// 点赞
const handleThumb = () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  baguStore.toggleThumb(questionId.value)
}

// 收藏
const handleFavour = () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  baguStore.toggleFavour(questionId.value)
}

// 返回题库详情
const goBack = () => {
  router.push(`/bagu/bank/${bankId.value}`)
}

// 切换移动端导航
const toggleNav = () => {
  isNavVisible.value = !isNavVisible.value
}

// 关闭移动端导航
const closeNav = () => {
  isNavVisible.value = false
}
</script>

<style scoped>
.question-detail-nav {
  position: relative;
  min-height: 100vh;
}

.question-detail-nav__back {
  position: fixed;
  top: 80px;
  left: 24px;
  z-index: 100;
  background: var(--bg-card);
  border-color: var(--border-color);
  color: var(--text-primary);
}

.question-detail-nav__toggle {
  display: none;
  position: fixed;
  top: 80px;
  right: 24px;
  z-index: 100;
  background: var(--bg-card);
  border-color: var(--border-color);
  color: var(--text-primary);
}

.question-detail-nav__container {
  display: flex;
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  gap: 24px;
}

.question-detail-nav__sidebar {
  flex-shrink: 0;
  width: 300px;
  position: sticky;
  top: 80px;
  height: calc(100vh - 104px);
}

.question-detail-nav__sidebar-content {
  height: 100%;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.question-detail-nav__main {
  flex: 1;
  min-width: 0;
}

.question-detail-nav__card {
  min-height: calc(100vh - 152px);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .question-detail-nav__toggle {
    display: flex;
  }
  
  .question-detail-nav__container {
    padding: 16px;
  }
  
  .question-detail-nav__sidebar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    height: 100%;
    z-index: 200;
    background: rgba(0, 0, 0, 0.5);
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s ease;
  }
  
  .question-detail-nav__sidebar.visible {
    opacity: 1;
    visibility: visible;
  }
  
  .question-detail-nav__sidebar-content {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 280px;
    border-radius: 0;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .question-detail-nav__sidebar.visible .question-detail-nav__sidebar-content {
    transform: translateX(0);
  }
}
</style>
