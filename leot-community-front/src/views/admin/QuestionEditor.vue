<template>
  <div class="tech-page question-editor">
    <TechHeader 
      :title="isEdit ? '编辑题目' : '新增题目'" 
      :subtitle="isEdit ? '修改题目内容' : '创建新的八股文题目'"
      :animated="false"
    />
    
    <div class="question-editor__content">
      <el-button 
        class="question-editor__back" 
        :icon="ArrowLeft" 
        circle 
        @click="handleCancel"
      />
      
      <GlassCard padding="32px" :glow="true">
        <el-form 
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          label-position="top"
        >
          <el-form-item label="题目标题" prop="title">
            <el-input 
              v-model="form.title" 
              placeholder="请输入题目标题"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="标签" prop="tags">
            <el-select
              v-model="form.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入标签"
              style="width: 100%"
            >
              <el-option
                v-for="tag in commonTags"
                :key="tag"
                :label="tag"
                :value="tag"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="答案" prop="content">
            <MarkdownEditor 
              v-model="form.content" 
              placeholder="请输入答案，支持 Markdown 格式..."
              :height="300"
            />
          </el-form-item>
        </el-form>
        
        <div class="question-editor__actions">
          <el-button @click="handleCancel">取消</el-button>
          <el-button 
            type="primary" 
            :loading="submitting"
            @click="handleSubmit"
          >
            {{ isEdit ? '保存修改' : '创建题目' }}
          </el-button>
        </div>
      </GlassCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import TechHeader from '@/components/bagu/TechHeader.vue'
import GlassCard from '@/components/bagu/GlassCard.vue'
import MarkdownEditor from '@/components/bagu/MarkdownEditor.vue'
import { questionApi } from '@/api/question'
import { questionBankQuestionApi } from '@/api/questionBankQuestion'

const route = useRoute()
const router = useRouter()

// 判断是否为编辑模式
const isEdit = computed(() => !!route.params.id)
const questionId = computed(() => Number(route.params.id) || 0)
const bankId = computed(() => Number(route.query.bankId) || 0)

// 表单引用
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const form = reactive({
  title: '',
  content: '',
  answer: '',
  tags: [] as string[]
})

// 初始表单状态（用于脏数据检测）
const initialForm = ref({
  title: '',
  content: '',
  answer: '',
  tags: [] as string[]
})

// 常用标签
const commonTags = [
  'Java', 'Spring', 'MySQL', 'Redis', 'JVM', 
  '并发', '集合', '设计模式', '算法', '网络',
  '操作系统', '分布式', '微服务', 'MQ', 'Kafka'
]

// 表单验证规则
const rules: FormRules = {
  title: [
    { required: true, message: '请输入题目标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入答案', trigger: 'blur' }
  ]
}

// 检测表单是否有修改
const isDirty = computed(() => {
  return form.title !== initialForm.value.title ||
    form.content !== initialForm.value.content ||
    form.answer !== initialForm.value.answer ||
    JSON.stringify(form.tags) !== JSON.stringify(initialForm.value.tags)
})

// 加载题目数据（编辑模式）
const loadQuestion = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await questionApi.getById(questionId.value)
    if (res.code === 0 && res.data) {
      const question = res.data
      form.title = question.title
      form.content = question.content
      form.answer = question.answer || ''
      form.tags = question.tags || []
      
      // 保存初始状态
      initialForm.value = {
        title: question.title,
        content: question.content,
        answer: question.answer || '',
        tags: [...(question.tags || [])]
      }
    } else {
      ElMessage.error(res.message || '加载题目失败')
      router.back()
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载题目失败')
    router.back()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        // 编辑模式
        const res = await questionApi.update({
          id: questionId.value,
          title: form.title,
          content: form.content,
          answer: form.answer,
          tags: form.tags
        })
        
        if (res.code === 0) {
          ElMessage.success('修改成功')
          router.back()
        } else {
          ElMessage.error(res.message || '修改失败')
        }
      } else {
        // 新增模式
        const addRes = await questionApi.add({
          title: form.title,
          content: form.content,
          answer: form.answer,
          tags: form.tags
        })
        
        if (addRes.code !== 0) {
          ElMessage.error(addRes.message || '创建题目失败')
          return
        }
        
        const newQuestionId = addRes.data
        
        // 如果有 bankId，将题目添加到题库
        if (bankId.value) {
          const linkRes = await questionBankQuestionApi.add({
            questionBankId: bankId.value,
            questionId: newQuestionId
          })
          
          if (linkRes.code !== 0) {
            ElMessage.warning('题目创建成功，但添加到题库失败')
          }
        }
        
        ElMessage.success('创建成功')
        router.back()
      }
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 取消编辑
const handleCancel = async () => {
  if (isDirty.value) {
    try {
      await ElMessageBox.confirm(
        '您有未保存的更改，确定要离开吗？',
        '提示',
        {
          confirmButtonText: '确定离开',
          cancelButtonText: '继续编辑',
          type: 'warning'
        }
      )
      router.back()
    } catch {
      // 用户取消，继续编辑
    }
  } else {
    router.back()
  }
}

onMounted(() => {
  loadQuestion()
})
</script>

<style scoped>
.question-editor__content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px 48px;
  position: relative;
}

.question-editor__back {
  position: absolute;
  top: 0;
  left: 24px;
  z-index: 10;
  background: var(--bg-card);
  border-color: var(--border-color);
  color: var(--text-primary);
}

.question-editor__actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

:deep(.el-form-item__label) {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 15px;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  background: var(--bg-tertiary);
  border-color: var(--border-color);
  color: var(--text-primary);
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  border-color: var(--primary-start);
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  background: var(--bg-tertiary);
}

:deep(.el-tag) {
  background: rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.3);
  color: var(--primary-start);
}
</style>
