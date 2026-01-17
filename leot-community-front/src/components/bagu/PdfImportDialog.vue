<template>
  <el-dialog
    v-model="visible"
    title="PDF导入题目"
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 上传区域 -->
    <div v-if="step === 'upload'" class="upload-section">
      <el-upload
        ref="uploadRef"
        class="pdf-uploader"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".pdf"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">
          将 PDF 文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="upload-tip">
            仅支持 PDF 格式，文件大小不超过 100MB
          </div>
        </template>
      </el-upload>
    </div>

    <!-- 解析中 -->
    <div v-else-if="step === 'parsing'" class="parsing-section">
      <el-icon class="parsing-icon" :size="48"><Loading /></el-icon>
      <p class="parsing-text">正在解析 PDF 文件...</p>
    </div>

    <!-- 预览区域 -->
    <div v-else-if="step === 'preview'" class="preview-section">
      <div class="preview-header">
        <span class="preview-count">共解析出 {{ questions.length }} 道题目</span>
        <span class="preview-time">耗时 {{ parseTime }}ms</span>
      </div>
      
      <div class="question-list">
        <div
          v-for="(question, index) in questions"
          :key="index"
          class="question-item"
        >
          <div class="question-header">
            <span class="question-index">题目 {{ index + 1 }}</span>
            <el-button
              type="danger"
              size="small"
              text
              @click="removeQuestion(index)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
          
          <el-form label-width="60px" class="question-form">
            <el-form-item label="标题">
              <el-input
                v-model="question.title"
                placeholder="请输入题目标题"
              />
            </el-form-item>
            <el-form-item label="内容">
              <el-input
                v-model="question.content"
                type="textarea"
                :rows="3"
                placeholder="请输入题目内容"
              />
            </el-form-item>
            <el-form-item label="答案">
              <el-input
                v-model="question.answer"
                type="textarea"
                :rows="3"
                placeholder="请输入题目答案"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 导入中 -->
    <div v-else-if="step === 'importing'" class="importing-section">
      <el-icon class="importing-icon" :size="48"><Loading /></el-icon>
      <p class="importing-text">正在导入题目...</p>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          v-if="step === 'upload'"
          type="primary"
          :disabled="!selectedFile"
          @click="handleParse"
        >
          开始解析
        </el-button>
        <el-button
          v-if="step === 'preview'"
          @click="handleBack"
        >
          重新上传
        </el-button>
        <el-button
          v-if="step === 'preview'"
          type="primary"
          :disabled="questions.length === 0"
          @click="handleImport"
        >
          确认导入 ({{ questions.length }})
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Loading, Delete } from '@element-plus/icons-vue'
import type { UploadFile, UploadInstance } from 'element-plus'
import { questionApi } from '@/api/question'
import type { ParsedQuestion } from '@/types/bagu'

const props = defineProps<{
  modelValue: boolean
  questionBankId: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const visible = ref(false)
const step = ref<'upload' | 'parsing' | 'preview' | 'importing'>('upload')
const selectedFile = ref<File | null>(null)
const questions = ref<ParsedQuestion[]>([])
const parseTime = ref(0)
const uploadRef = ref<UploadInstance>()

// 同步 visible 状态
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    resetState()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 重置状态
const resetState = () => {
  step.value = 'upload'
  selectedFile.value = null
  questions.value = []
  parseTime.value = 0
}

// 文件选择变化
const handleFileChange = (file: UploadFile) => {
  if (!file.raw) return
  
  // 校验文件类型
  if (file.raw.type !== 'application/pdf') {
    ElMessage.error('只支持 PDF 格式的文件')
    uploadRef.value?.clearFiles()
    return
  }
  
  // 校验文件大小 (100MB)
  if (file.raw.size > 100 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 100MB')
    uploadRef.value?.clearFiles()
    return
  }
  
  selectedFile.value = file.raw
}

// 文件超出限制
const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

// 解析 PDF
const handleParse = async () => {
  if (!selectedFile.value) return
  
  step.value = 'parsing'
  
  try {
    const res = await questionApi.parsePdf(selectedFile.value)
    if (res.code === 0 && res.data) {
      questions.value = res.data.questions
      parseTime.value = res.data.parseTime
      step.value = 'preview'
      
      if (questions.value.length === 0) {
        ElMessage.warning('未能从 PDF 中解析出题目')
      }
    } else {
      ElMessage.error(res.message || 'PDF 解析失败')
      step.value = 'upload'
    }
  } catch (error: any) {
    ElMessage.error(error.message || 'PDF 解析失败')
    step.value = 'upload'
  }
}

// 返回上传
const handleBack = () => {
  resetState()
  uploadRef.value?.clearFiles()
}

// 删除题目
const removeQuestion = (index: number) => {
  questions.value.splice(index, 1)
}

// 确认导入
const handleImport = async () => {
  if (questions.value.length === 0) {
    ElMessage.warning('没有可导入的题目')
    return
  }
  
  step.value = 'importing'
  
  try {
    const res = await questionApi.batchImport({
      questionBankId: props.questionBankId,
      questions: questions.value
    })
    
    if (res.code === 0 && res.data) {
      const { successCount, failCount } = res.data
      if (failCount > 0) {
        ElMessage.warning(`导入完成：成功 ${successCount} 道，失败 ${failCount} 道`)
      } else {
        ElMessage.success(`成功导入 ${successCount} 道题目`)
      }
      emit('success')
      handleClose()
    } else {
      ElMessage.error(res.message || '导入失败')
      step.value = 'preview'
    }
  } catch (error: any) {
    ElMessage.error(error.message || '导入失败')
    step.value = 'preview'
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  resetState()
}
</script>


<style scoped>
.upload-section {
  padding: 20px;
}

.pdf-uploader {
  width: 100%;
}

.pdf-uploader :deep(.el-upload-dragger) {
  background: var(--bg-card);
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-lg);
  padding: 40px;
  transition: all 0.3s;
}

.pdf-uploader :deep(.el-upload-dragger:hover) {
  border-color: var(--primary-start);
  background: var(--bg-card-hover);
}

.upload-icon {
  font-size: 48px;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.upload-text {
  color: var(--text-secondary);
  font-size: 14px;
}

.upload-text em {
  color: var(--primary-start);
  font-style: normal;
}

.upload-tip {
  color: var(--text-muted);
  font-size: 12px;
  margin-top: 8px;
  text-align: center;
}

.parsing-section,
.importing-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.parsing-icon,
.importing-icon {
  color: var(--primary-start);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.parsing-text,
.importing-text {
  color: var(--text-secondary);
  margin-top: 16px;
  font-size: 14px;
}

.preview-section {
  max-height: 500px;
  overflow-y: auto;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  margin-bottom: 16px;
}

.preview-count {
  color: var(--text-primary);
  font-weight: 500;
}

.preview-time {
  color: var(--text-muted);
  font-size: 12px;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-item {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 16px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.question-index {
  color: var(--primary-start);
  font-weight: 500;
}

.question-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.question-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.question-form :deep(.el-form-item__label) {
  color: var(--text-secondary);
}

.question-form :deep(.el-input__wrapper),
.question-form :deep(.el-textarea__inner) {
  background: var(--bg-secondary);
  border-color: var(--border-color);
  color: var(--text-primary);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
