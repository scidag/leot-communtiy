<template>
  <div class="tech-page bagu-manage">
    <TechHeader 
      title="八股文管理" 
      subtitle="管理题库和题目内容"
      :animated="false"
    />
    
    <div class="bagu-manage__content">
      <el-tabs v-model="activeTab" class="manage-tabs">
        <!-- 题库管理 -->
        <el-tab-pane label="题库管理" name="banks">
          <GlassCard padding="24px">
            <div class="manage-header">
              <h3>题库列表</h3>
              <el-button type="primary" :icon="Plus" @click="openBankDialog()">
                新增题库
              </el-button>
            </div>
            
            <el-table :data="banks" style="width: 100%" v-loading="loading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" min-width="150" />
              <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
              <el-table-column prop="questionCount" label="题目数" width="100" />
              <el-table-column prop="createTime" label="创建时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="openBankDialog(row)">编辑</el-button>
                  <el-button type="danger" link @click="handleDeleteBank(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </GlassCard>
        </el-tab-pane>
        
        <!-- 题目管理 -->
        <el-tab-pane label="题目管理" name="questions">
          <GlassCard padding="24px">
            <div class="manage-header">
              <h3>题目列表</h3>
              <el-button type="primary" :icon="Plus" @click="goToAddQuestion">
                新增题目
              </el-button>
            </div>
            
            <el-table :data="questions" style="width: 100%" v-loading="loading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
              <el-table-column prop="tags" label="标签" width="200">
                <template #default="{ row }">
                  <el-tag v-for="tag in row.tags?.slice(0, 3)" :key="tag" size="small" class="tag-item">
                    {{ tag }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="viewNum" label="浏览" width="80" />
              <el-table-column prop="thumbNum" label="点赞" width="80" />
              <el-table-column prop="favourNum" label="收藏" width="80" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="goToEditQuestion(row)">编辑</el-button>
                  <el-button type="danger" link @click="handleDeleteQuestion(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="questionPage"
                :page-size="10"
                :total="total"
                layout="prev, pager, next"
                @current-change="fetchQuestions"
              />
            </div>
          </GlassCard>
        </el-tab-pane>
        
        <!-- 用户管理 -->
        <el-tab-pane label="用户管理" name="users">
          <GlassCard padding="24px">
            <div class="manage-header">
              <h3>用户列表</h3>
              <el-button type="primary" :icon="Plus" @click="openUserDialog()">
                新增用户
              </el-button>
            </div>
            
            <!-- 用户搜索栏 -->
            <el-form :inline="true" :model="userSearchForm" class="search-form">
              <el-form-item label="用户名">
                <el-input
                  v-model="userSearchForm.userName"
                  placeholder="请输入用户名"
                  clearable
                  @clear="handleUserSearch"
                  @keyup.enter="handleUserSearch"
                />
              </el-form-item>
              <el-form-item label="账号">
                <el-input
                  v-model="userSearchForm.userAccount"
                  placeholder="请输入账号"
                  clearable
                  @clear="handleUserSearch"
                  @keyup.enter="handleUserSearch"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :icon="Search" @click="handleUserSearch">
                  搜索
                </el-button>
                <el-button @click="resetUserSearch">重置</el-button>
              </el-form-item>
            </el-form>
            
            <!-- 用户表格 -->
            <el-table :data="users" style="width: 100%" v-loading="userLoading">
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column prop="id" label="用户ID" width="80" align="center" />
              <el-table-column prop="userName" label="用户名" width="120" />
              <el-table-column prop="userAccount" label="账号" width="150" />
              <el-table-column prop="profile" label="简介" show-overflow-tooltip />
              <el-table-column prop="userRole" label="角色" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.userRole === 'admin' ? 'danger' : 'primary'">
                    {{ row.userRole === 'admin' ? '管理员' : '普通用户' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column prop="updateTime" label="更新时间" width="180" />
              <el-table-column label="操作" width="200" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="openUserDialog(row)">编辑</el-button>
                  <el-button type="danger" link @click="handleDeleteUser(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 用户分页 -->
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="userPagination.current"
                v-model:page-size="userPagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="userPagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleUserPageSizeChange"
                @current-change="handleUserPageChange"
              />
            </div>
          </GlassCard>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 题库编辑弹窗 -->
    <el-dialog v-model="bankDialogVisible" :title="editingBank ? '编辑题库' : '新增题库'" width="500px">
      <el-form :model="bankForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="bankForm.title" placeholder="请输入题库标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="bankForm.description" type="textarea" :rows="3" placeholder="请输入题库描述" />
        </el-form-item>
        <el-form-item label="封面图">
          <ImageUpload v-model="bankForm.picture" biz-type="questionbank" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bankDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveBank">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 用户编辑弹窗 -->
    <el-dialog v-model="userDialogVisible" :title="editingUser ? '编辑用户' : '新增用户'" width="500px">
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="userForm.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="账号" prop="userAccount">
          <el-input
            v-model="userForm.userAccount"
            placeholder="请输入账号"
            :disabled="!!editingUser"
          />
        </el-form-item>
        <el-form-item label="密码" prop="userPassword" v-if="!editingUser">
          <el-input
            v-model="userForm.userPassword"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="简介" prop="profile">
          <el-input
            v-model="userForm.profile"
            type="textarea"
            :rows="3"
            placeholder="请输入简介"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="角色" prop="userRole">
          <el-select v-model="userForm.userRole" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="userSubmitLoading" @click="handleSaveUser">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { useBaguStore } from '@/stores/bagu'
import { storeToRefs } from 'pinia'
import { questionApi } from '@/api/question'
import { questionBankApi } from '@/api/questionBank'
import { getUserList, addUser, updateUserInfo, deleteUser } from '@/api/user'
import TechHeader from '@/components/bagu/TechHeader.vue'
import GlassCard from '@/components/bagu/GlassCard.vue'
import ImageUpload from '@/components/bagu/ImageUpload.vue'
import type { Question, QuestionBank, AddQuestionBankDTO, UpdateQuestionBankDTO } from '@/types/bagu'
import type { User, UserListRequest } from '@/types/user'

const router = useRouter()

const baguStore = useBaguStore()
const { banks, questions, loading, total } = storeToRefs(baguStore)

const activeTab = ref('banks')
const questionPage = ref(1)

// 题库表单
const bankDialogVisible = ref(false)
const editingBank = ref<QuestionBank | null>(null)
const bankForm = reactive<AddQuestionBankDTO & { id?: number }>({
  title: '',
  description: '',
  picture: ''
})

// 题目表单 - 改为路由跳转
const goToAddQuestion = () => {
  router.push('/admin/question/add')
}

const goToEditQuestion = (question: Question) => {
  router.push(`/admin/question/edit/${question.id}`)
}

// 用户管理相关
const userLoading = ref(false)
const userSubmitLoading = ref(false)
const userDialogVisible = ref(false)
const editingUser = ref<User | null>(null)
const userFormRef = ref<FormInstance>()

const userSearchForm = reactive({
  userName: '',
  userAccount: ''
})

const userPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const users = ref<User[]>([])

const userForm = reactive<User>({
  id: undefined,
  userName: '',
  userAccount: '',
  userPassword: '',
  profile: '',
  userRole: 'user'
})

const userFormRules: FormRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  userRole: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

onMounted(() => {
  baguStore.fetchBanks({ current: 1, pageSize: 100 })
  fetchQuestions()
  fetchUsers()
})

const fetchQuestions = () => {
  baguStore.fetchQuestions({ current: questionPage.value, pageSize: 10 })
}

// 用户管理相关函数
const fetchUsers = async () => {
  userLoading.value = true
  try {
    const params: UserListRequest = {
      current: userPagination.current,
      pageSize: userPagination.pageSize,
      userName: userSearchForm.userName || undefined,
      userAccount: userSearchForm.userAccount || undefined
    }
    const res = await getUserList(params)
    if (res.data) {
      users.value = res.data.records || []
      userPagination.total = res.data.total || 0
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取用户列表失败')
  } finally {
    userLoading.value = false
  }
}

const handleUserSearch = () => {
  userPagination.current = 1
  fetchUsers()
}

const resetUserSearch = () => {
  userSearchForm.userName = ''
  userSearchForm.userAccount = ''
  handleUserSearch()
}

const handleUserPageSizeChange = () => {
  fetchUsers()
}

const handleUserPageChange = () => {
  fetchUsers()
}

const openUserDialog = (user?: User) => {
  editingUser.value = user || null
  if (user) {
    Object.assign(userForm, {
      id: user.id,
      userName: user.userName,
      userAccount: user.userAccount,
      profile: user.profile || '',
      userRole: user.userRole,
      userPassword: '' // 编辑时不显示密码
    })
  } else {
    Object.assign(userForm, {
      id: undefined,
      userName: '',
      userAccount: '',
      userPassword: '',
      profile: '',
      userRole: 'user'
    })
  }
  userDialogVisible.value = true
}

const handleSaveUser = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      userSubmitLoading.value = true
      try {
        if (editingUser.value) {
          // 编辑用户
          const { userPassword, ...updateData } = userForm
          await updateUserInfo(updateData)
          ElMessage.success('更新成功')
        } else {
          // 新增用户
          await addUser(userForm)
          ElMessage.success('新增成功')
        }
        userDialogVisible.value = false
        fetchUsers()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        userSubmitLoading.value = false
      }
    }
  })
}

const handleDeleteUser = async (row: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.userName}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (row.id) {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchUsers()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 题库操作
const openBankDialog = (bank?: QuestionBank) => {
  editingBank.value = bank || null
  if (bank) {
    bankForm.id = bank.id
    bankForm.title = bank.title
    bankForm.description = bank.description || ''
    bankForm.picture = bank.picture || ''
  } else {
    bankForm.id = undefined
    bankForm.title = ''
    bankForm.description = ''
    bankForm.picture = ''
  }
  bankDialogVisible.value = true
}

const handleSaveBank = async () => {
  if (!bankForm.title) {
    ElMessage.warning('请输入题库标题')
    return
  }
  try {
    if (editingBank.value) {
      await questionBankApi.update(bankForm as UpdateQuestionBankDTO)
      ElMessage.success('更新成功')
    } else {
      await questionBankApi.add(bankForm)
      ElMessage.success('创建成功')
    }
    bankDialogVisible.value = false
    baguStore.fetchBanks({ current: 1, pageSize: 100 })
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDeleteBank = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该题库吗？', '提示', { type: 'warning' })
    await questionBankApi.delete(id)
    ElMessage.success('删除成功')
    baguStore.fetchBanks({ current: 1, pageSize: 100 })
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDeleteQuestion = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该题目吗？', '提示', { type: 'warning' })
    await questionApi.delete(id)
    ElMessage.success('删除成功')
    fetchQuestions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>
<style scoped>
.bagu-manage__content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.manage-tabs :deep(.el-tabs__item) {
  color: var(--text-secondary);
}

.manage-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary-start);
}

.manage-tabs :deep(.el-tabs__active-bar) {
  background: var(--primary-gradient);
}

.manage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.manage-header h3 {
  margin: 0;
  color: var(--text-primary);
}

.tag-item {
  margin-right: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: var(--bg-secondary);
  --el-table-text-color: var(--text-secondary);
  --el-table-header-text-color: var(--text-primary);
  --el-table-border-color: var(--border-color);
}

:deep(.el-dialog) {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
}

:deep(.el-dialog__title) {
  color: var(--text-primary);
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
}
</style>
