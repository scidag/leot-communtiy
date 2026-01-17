<template>
  <div class="tech-page bagu-home">
    <TechHeader 
      title="八股文题库" 
      subtitle="系统化学习，高效备战面试"
      :show-search="true"
      @search="handleSearch"
    >
      <template #extra>
        <div v-if="userStore.isAdmin()" class="tech-header__actions">
          <el-button 
            type="primary"
            :icon="Plus"
            @click="showCreateBankDialog = true"
          >
            新建题库
          </el-button>
        </div>
      </template>
    </TechHeader>
    
    <div class="bagu-home__content">
      <div v-if="loading" class="card-grid">
        <div v-for="i in 8" :key="i" class="skeleton-card">
          <div class="skeleton" style="height: 140px;"></div>
          <div style="padding: 16px;">
            <div class="skeleton" style="height: 24px; margin-bottom: 8px;"></div>
            <div class="skeleton" style="height: 40px; margin-bottom: 12px;"></div>
            <div class="skeleton" style="height: 20px; width: 60%;"></div>
          </div>
        </div>
      </div>
      
      <div v-else-if="banks.length === 0" class="empty-state">
        <el-icon :size="64"><Folder /></el-icon>
        <p>暂无题库</p>
      </div>
      
      <div v-else class="card-grid">
        <BankCard 
          v-for="(bank, index) in banks" 
          :key="bank.id" 
          :bank="bank"
          class="animate-fade-in-up"
          :style="{ animationDelay: `${index * 0.1}s` }"
          @click="goToBank"
        />
      </div>
      
      <div v-if="total > pageSize" class="bagu-home__pagination">
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

    <!-- 新建题库对话框 -->
    <el-dialog
      v-model="showCreateBankDialog"
      title="新建题库"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="bankForm"
        :rules="bankRules"
        label-width="80px"
      >
        <el-form-item label="题库名称" prop="title">
          <el-input
            v-model="bankForm.title"
            placeholder="请输入题库名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="题库描述" prop="description">
          <el-input
            v-model="bankForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入题库描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateBankDialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="creating"
          @click="handleCreateBank"
        >
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Folder, Plus } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useBaguStore } from '@/stores/bagu'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import TechHeader from '@/components/bagu/TechHeader.vue'
import BankCard from '@/components/bagu/BankCard.vue'
import { questionBankApi } from '@/api/questionBank'

const router = useRouter()
const baguStore = useBaguStore()
const userStore = useUserStore()
const { banks, loading, total } = storeToRefs(baguStore)

const currentPage = ref(1)
const pageSize = ref(12)

// 新建题库相关
const showCreateBankDialog = ref(false)
const creating = ref(false)
const formRef = ref<FormInstance>()
const bankForm = reactive({
  title: '',
  description: ''
})

const bankRules: FormRules = {
  title: [
    { required: true, message: '请输入题库名称', trigger: 'blur' },
    { min: 2, max: 50, message: '题库名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

onMounted(() => {
  fetchData()
})

const fetchData = () => {
  baguStore.fetchBanks({
    current: currentPage.value,
    pageSize: pageSize.value
  })
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

const handleSearch = (keyword: string) => {
  router.push({ path: '/bagu/search', query: { keyword } })
}

const goToBank = (id: number) => {
  // 检查是否登录
  if (!userStore.token) {
    ElMessage.warning('请先登录后查看题库详情')
    router.push('/login')
    return
  }
  router.push(`/bagu/bank/${id}`)
}

// 创建题库
const handleCreateBank = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    creating.value = true
    try {
      const res = await questionBankApi.add({
        title: bankForm.title,
        description: bankForm.description
      })
      
      if (res.code === 0) {
        ElMessage.success('题库创建成功')
        showCreateBankDialog.value = false
        bankForm.title = ''
        bankForm.description = ''
        fetchData() // 刷新列表
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '创建失败')
    } finally {
      creating.value = false
    }
  })
}
</script>

<style scoped>
.bagu-home__content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.skeleton-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.bagu-home__pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.bagu-home__pagination :deep(.el-pagination) {
  --el-pagination-bg-color: var(--bg-card);
  --el-pagination-text-color: var(--text-secondary);
  --el-pagination-button-disabled-bg-color: var(--bg-secondary);
}

.bagu-home__pagination :deep(.el-pager li) {
  background: var(--bg-card);
  color: var(--text-secondary);
}

.bagu-home__pagination :deep(.el-pager li.is-active) {
  background: var(--primary-gradient);
  color: white;
}

.tech-header__actions {
  margin-top: 24px;
}
</style>
