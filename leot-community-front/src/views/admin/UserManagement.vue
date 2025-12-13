<template>
  <el-container class="admin-container">
    <el-header class="admin-header">
      <div class="header-left">
        <el-button type="text" @click="goBack" style="color: white; margin-right: 20px">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>用户管理</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </el-header>
    <el-main class="admin-main">
      <el-card>
        <!-- 搜索栏 -->
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="用户名">
            <el-input
              v-model="searchForm.userName"
              placeholder="请输入用户名"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="账号">
            <el-input
              v-model="searchForm.userAccount"
              placeholder="请输入账号"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 用户表格 -->
        <el-table
          v-loading="loading"
          :data="tableData"
          border
          style="width: 100%"
        >
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
              <el-button type="primary" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </el-main>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
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
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="密码" prop="userPassword" v-if="!isEdit">
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { ArrowLeft, Plus, Search } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUserInfo, deleteUser } from '@/api/user'
import type { User, UserListRequest } from '@/types/user'

const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新增用户')
const userFormRef = ref<FormInstance>()

const searchForm = reactive({
  userName: '',
  userAccount: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<User[]>([])

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

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params: UserListRequest = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      userName: searchForm.userName || undefined,
      userAccount: searchForm.userAccount || undefined
    }
    const res = await getUserList(params)
    if (res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchUserList()
}

// 重置搜索
const handleReset = () => {
  searchForm.userName = ''
  searchForm.userAccount = ''
  handleSearch()
}

// 分页大小改变
const handleSizeChange = () => {
  fetchUserList()
}

// 当前页改变
const handleCurrentChange = () => {
  fetchUserList()
}

// 新增用户
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  resetUserForm()
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row: User) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  Object.assign(userForm, {
    id: row.id,
    userName: row.userName,
    userAccount: row.userAccount,
    profile: row.profile || '',
    userRole: row.userRole,
    userPassword: '' // 编辑时不显示密码
  })
  dialogVisible.value = true
}

// 删除用户
const handleDelete = async (row: User) => {
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
      fetchUserList()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          // 编辑用户
          const { userPassword, ...updateData } = userForm
          await updateUserInfo(updateData)
          ElMessage.success('更新成功')
        } else {
          // 新增用户
          await addUser(userForm)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        fetchUserList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetUserForm = () => {
  Object.assign(userForm, {
    id: undefined,
    userName: '',
    userAccount: '',
    userPassword: '',
    profile: '',
    userRole: 'user'
  })
  userFormRef.value?.clearValidate()
}

// 对话框关闭
const handleDialogClose = () => {
  resetUserForm()
}

// 返回
const goBack = () => {
  router.push('/dashboard')
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.admin-container {
  height: 100vh;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #409eff;
  color: white;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left h2 {
  margin: 0;
  color: white;
}

.admin-main {
  background-color: #f5f5f5;
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

