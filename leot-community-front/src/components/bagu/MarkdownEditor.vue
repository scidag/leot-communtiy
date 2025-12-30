<template>
  <div class="markdown-editor">
    <MdEditor
      v-model="content"
      :theme="theme"
      :preview-theme="previewTheme"
      :code-theme="codeTheme"
      :language="language"
      :placeholder="placeholder"
      :style="{ height: editorHeight }"
      :toolbars="toolbars"
      @on-upload-img="handleUploadImg"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { ElMessage } from 'element-plus'
import { uploadImage } from '@/api/file'

interface Props {
  modelValue: string
  placeholder?: string
  height?: string | number
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '请输入内容...',
  height: 400
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

// 双向绑定
const content = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value)
})

// 编辑器配置
const theme = 'dark'
const previewTheme = 'github'
const codeTheme = 'atom'
const language = 'zh-CN'

// 计算高度
const editorHeight = computed(() => {
  if (typeof props.height === 'number') {
    return `${props.height}px`
  }
  return props.height
})

// 工具栏配置
const toolbars = [
  'bold',
  'underline',
  'italic',
  'strikeThrough',
  '-',
  'title',
  'sub',
  'sup',
  'quote',
  'unorderedList',
  'orderedList',
  'task',
  '-',
  'codeRow',
  'code',
  'link',
  'image',
  'table',
  '-',
  'revoke',
  'next',
  '=',
  'preview',
  'htmlPreview',
  'catalog'
]

// 图片上传处理
const handleUploadImg = async (
  files: File[],
  callback: (urls: string[]) => void
) => {
  const urls: string[] = []
  
  for (const file of files) {
    // 验证文件类型
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    if (!allowedTypes.includes(file.type)) {
      ElMessage.error('仅支持 jpg、png、gif、webp 格式的图片')
      continue
    }
    
    // 验证文件大小 (5MB)
    const maxSize = 5 * 1024 * 1024
    if (file.size > maxSize) {
      ElMessage.error('图片大小不能超过 5MB')
      continue
    }
    
    try {
      const res = await uploadImage(file, 'question')
      if (res.code === 0 && res.data) {
        urls.push(res.data)
      } else {
        ElMessage.error(res.message || '图片上传失败')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '图片上传失败')
    }
  }
  
  callback(urls)
}
</script>

<style scoped>
.markdown-editor {
  width: 100%;
}

.markdown-editor :deep(.md-editor) {
  border-radius: var(--radius-md, 12px);
}
</style>
