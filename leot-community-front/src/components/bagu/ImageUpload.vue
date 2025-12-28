<template>
  <div class="image-upload">
    <!-- 图片预览区域 -->
    <div 
      class="image-upload__preview"
      :class="{ 'image-upload__preview--empty': !modelValue }"
      @click="triggerFileSelect"
    >
      <!-- 已上传图片 -->
      <template v-if="modelValue">
        <img 
          :src="modelValue" 
          alt="预览图片" 
          class="image-upload__image"
          @error="handleImageError"
        />
        <div class="image-upload__overlay">
          <el-icon class="image-upload__icon" @click.stop="triggerFileSelect">
            <Upload />
          </el-icon>
          <el-icon class="image-upload__icon image-upload__icon--delete" @click.stop="handleRemove">
            <Delete />
          </el-icon>
        </div>
      </template>
      
      <!-- 空状态 -->
      <template v-else>
        <div class="image-upload__placeholder">
          <el-icon class="image-upload__placeholder-icon" :size="32">
            <Plus />
          </el-icon>
          <span class="image-upload__placeholder-text">点击上传图片</span>
        </div>
      </template>
      
      <!-- 加载状态 -->
      <div v-if="uploading" class="image-upload__loading">
        <el-icon class="is-loading" :size="32">
          <Loading />
        </el-icon>
        <span>上传中...</span>
      </div>
    </div>
    
    <!-- 隐藏的文件输入 -->
    <input
      ref="fileInputRef"
      type="file"
      :accept="accept"
      class="image-upload__input"
      @change="handleFileChange"
    />
    
    <!-- 提示信息 -->
    <div class="image-upload__tip">
      支持 jpg、png、gif、webp 格式，大小不超过 {{ maxSize }}MB
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Upload, Delete, Loading } from '@element-plus/icons-vue'
import { uploadImage } from '@/api/file'

interface Props {
  modelValue?: string      // 当前图片 URL (v-model)
  bizType?: string         // 业务类型
  maxSize?: number         // 最大文件大小 (MB)
  accept?: string          // 接受的文件类型
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  bizType: 'questionbank',
  maxSize: 5,
  accept: 'image/jpeg,image/png,image/gif,image/webp'
})

const emit = defineEmits<{
  (e: 'update:modelValue', url: string): void
  (e: 'success', url: string): void
  (e: 'error', error: Error): void
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

// 允许的文件类型
const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']

// 错误信息映射
const errorMessages: Record<number, string> = {
  40001: '请选择要上传的文件',
  40002: '仅支持 jpg、png、gif、webp 格式的图片',
  40003: '图片大小不能超过 5MB',
  50001: '上传失败，请稍后重试',
  50002: '文件服务暂不可用'
}

// 触发文件选择
const triggerFileSelect = () => {
  if (uploading.value) return
  fileInputRef.value?.click()
}

// 处理文件选择
const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // 重置 input，允许重复选择同一文件
  target.value = ''
  
  // 客户端验证文件类型
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 jpg、png、gif、webp 格式的图片')
    return
  }
  
  // 客户端验证文件大小
  const maxSizeBytes = props.maxSize * 1024 * 1024
  if (file.size > maxSizeBytes) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return
  }
  
  // 上传文件
  await uploadFile(file)
}

// 上传文件
const uploadFile = async (file: File) => {
  uploading.value = true
  
  try {
    const res = await uploadImage(file, props.bizType)
    
    if (res.code === 0 && res.data) {
      emit('update:modelValue', res.data)
      emit('success', res.data)
      ElMessage.success('上传成功')
    } else {
      const errorMsg = errorMessages[res.code] || res.message || '上传失败'
      ElMessage.error(errorMsg)
      emit('error', new Error(errorMsg))
    }
  } catch (error: any) {
    const errorCode = error.response?.data?.code
    const errorMsg = errorMessages[errorCode] || error.message || '上传失败'
    ElMessage.error(errorMsg)
    emit('error', error)
  } finally {
    uploading.value = false
  }
}

// 移除图片
const handleRemove = () => {
  emit('update:modelValue', '')
}

// 处理图片加载错误
const handleImageError = () => {
  // 图片加载失败时不做特殊处理，保持当前 URL
  // 用户可以选择重新上传
}
</script>

<style scoped>
.image-upload {
  display: inline-block;
}

.image-upload__preview {
  position: relative;
  width: 148px;
  height: 148px;
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-md, 8px);
  cursor: pointer;
  overflow: hidden;
  transition: var(--transition-base, all 0.3s ease);
  background: var(--bg-secondary, #1a1a2e);
}

.image-upload__preview:hover {
  border-color: var(--primary-start, #00d4ff);
}

.image-upload__preview--empty:hover {
  background: var(--bg-card-hover, rgba(255, 255, 255, 0.05));
}

.image-upload__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-upload__overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-upload__preview:hover .image-upload__overlay {
  opacity: 1;
}

.image-upload__icon {
  font-size: 24px;
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.image-upload__icon:hover {
  transform: scale(1.2);
}

.image-upload__icon--delete:hover {
  color: #f56c6c;
}

.image-upload__placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: var(--text-secondary, #8b8b9a);
}

.image-upload__placeholder-icon {
  margin-bottom: 8px;
  color: var(--text-secondary, #8b8b9a);
}

.image-upload__placeholder-text {
  font-size: 12px;
}

.image-upload__loading {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 14px;
}

.image-upload__input {
  display: none;
}

.image-upload__tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-secondary, #8b8b9a);
}
</style>
