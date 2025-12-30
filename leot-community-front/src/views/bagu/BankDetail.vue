<template>
  <div class="tech-page bank-detail">
    <div class="bank-detail__header">
      <el-button 
        class="bank-detail__back" 
        :icon="ArrowLeft" 
        circle 
        @click="goBack"
      />
      <GlassCard v-if="currentBank" class="bank-detail__info" :glow="true">
        <div class="bank-detail__cover">
          <img 
            v-if="currentBank.picture" 
            :src="currentBank.picture" 
            :alt="currentBank.title"
          />
          <div v-else class="bank-detail__placeholder">
            <el-icon :size="64"><Folder /></el-icon>
          </div>
          <!-- 管理员可见的修改封面按钮 -->
          <el-button 
            v-if="userStore.isAdmin()"
            class="bank-detail__edit-cover"
            type="primary"
            size="small"
            :icon="Camera"
            @click="showEditCoverDialog = true"
          >
            修改封面
          </el-button>
        </div>
        <div class="bank-detail__meta">
          <h1 class="gradient-text">{{ currentBank.title }}</h1>
          <p v-if="currentBank.description">{{ currentBank.description }}</p>
          <div class="bank-detail__stats">
            <span class="stat-item">
              <el-icon><Document /></el-icon>
              {{ currentBank.questionCount || 0 }} 题
            </span>
            <span class="stat-item">
              <el-icon><Calendar /></el-icon>
              {{ formatDate(currentBank.createTime) }}
            </span>
          </div>
        </div>
        <!-- 管理员可见的添加题目按钮 -->
        <div v-if="userStore.isAdmin()" class="bank-detail__actions">
          <el-button 
            type="primary"
            :icon="Plus"
            @click="goToAddQuestion"
          >
            添加题目
          </el-button>
        </div>
      </GlassCard>
    </div>
    
    <div class="bank-detail__content">
      <h2 class="bank-detail__section-title">
        <span class="gradient-text">题目列表</span>
      </h2>
      
      <div v-if="loading" class="question-list">
        <div v-for="i in 5" :key="i" class="skeleton-question">
          <div class="skeleton" style="height: 24px; width: 70%; margin-bottom: 12px;"></div>
          <div class="skeleton" style="height: 20px; width: 40%; margin-bottom: 16px;"></div>
          <div class="skeleton" style="height: 20px; width: 30%;"></div>
        </div>
      </div>
      
      <div v-else-if="questions.length === 0" class="empty-state">
        <el-icon :size="64"><Document /></el-icon>
        <p>该题库暂无题目</p>
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
      
      <div v-if="total > pageSize" class="bank-detail__pagination">
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

    <!-- 修改封面弹窗 -->
    <el-dialog
      v-model="showEditCoverDialog"
      title="修改题库封面"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="edit-cover-dialog">
        <ImageUpload 
          v-model="newCoverUrl" 
          biz-type="questionbank"
          @success="handleCoverUploadSuccess"
        />
      </div>
      <template #footer>
        <el-button @click="showEditCoverDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="savingCover"
          :disabled="!newCoverUrl"
          @click="saveCover"
        >
          保存
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Folder, Document, Calendar, Camera, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useBaguStore } from '@/stores/bagu'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import GlassCard from '@/components/bagu/GlassCard.vue'
import QuestionCard from '@/components/bagu/QuestionCard.vue'
import ImageUpload from '@/components/bagu/ImageUpload.vue'
import { questionBankApi } from '@/api/questionBank'

const route = useRoute()
const router = useRouter()
const baguStore = useBaguStore()
const userStore = useUserStore()
const { currentBank, questions, loading, total } = storeToRefs(baguStore)

const currentPage = ref(1)
const pageSize = ref(10)
const expandedId = ref<number | null>(null)

// 修改封面相关
const showEditCoverDialog = ref(false)
const newCoverUrl = ref('')
const savingCover = ref(false)

// 添加题目 - 改为路由跳转
const goToAddQuestion = () => {
  router.push(`/admin/question/add?bankId=${bankId}`)
}

const bankId = Number(route.params.id)

onMounted(() => {
  baguStore.fetchBankDetail(bankId)
  fetchQuestions()
})

const fetchQuestions = () => {
  baguStore.fetchQuestions({
    current: currentPage.value,
    pageSize: pageSize.value,
    questionBankId: bankId
  })
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  expandedId.value = null
  fetchQuestions()
}

const toggleExpand = (id: number) => {
  // 跳转到带导航的题目详情页
  router.push(`/bagu/bank/${bankId}/question/${id}`)
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

const goBack = () => {
  router.push('/bagu')
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 封面上传成功回调
const handleCoverUploadSuccess = (url: string) => {
  newCoverUrl.value = url
}

// 保存封面
const saveCover = async () => {
  if (!newCoverUrl.value || !currentBank.value) return
  
  savingCover.value = true
  try {
    const res = await questionBankApi.update({
      id: currentBank.value.id,
      picture: newCoverUrl.value
    })
    
    if (res.code === 0) {
      ElMessage.success('封面修改成功')
      currentBank.value.picture = newCoverUrl.value
      showEditCoverDialog.value = false
      newCoverUrl.value = ''
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '修改失败')
  } finally {
    savingCover.value = false
  }
}
</script>

<style scoped>
.bank-detail__header {
  position: relative;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.bank-detail__back {
  position: absolute;
  top: 24px;
  left: 24px;
  z-index: 10;
  background: var(--bg-card);
  border-color: var(--border-color);
  color: var(--text-primary);
}

.bank-detail__info {
  display: flex;
  gap: 32px;
  padding: 32px;
}

.bank-detail__cover {
  flex-shrink: 0;
  width: 200px;
  height: 150px;
  border-radius: var(--radius-md);
  overflow: hidden;
  position: relative;
}

.bank-detail__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bank-detail__edit-cover {
  position: absolute;
  bottom: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.bank-detail__cover:hover .bank-detail__edit-cover {
  opacity: 1;
}

.bank-detail__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  color: var(--text-muted);
}

.bank-detail__meta {
  flex: 1;
}

.bank-detail__meta h1 {
  font-size: 32px;
  margin: 0 0 12px;
}

.bank-detail__meta p {
  color: var(--text-secondary);
  margin: 0 0 20px;
  line-height: 1.6;
}

.bank-detail__stats {
  display: flex;
  gap: 24px;
}

.bank-detail__content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.bank-detail__section-title {
  font-size: 24px;
  margin: 0 0 24px;
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

.bank-detail__pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

@media (max-width: 768px) {
  .bank-detail__info {
    flex-direction: column;
  }
  
  .bank-detail__cover {
    width: 100%;
    height: 200px;
  }
  
  .bank-detail__edit-cover {
    opacity: 1;
  }
}

/* 修改封面弹窗样式 */
.edit-cover-dialog {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 添加题目按钮区域 */
.bank-detail__actions {
  display: flex;
  align-items: flex-start;
  flex-shrink: 0;
}
</style>
