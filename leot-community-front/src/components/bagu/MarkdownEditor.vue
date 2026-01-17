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
  border: 1px solid var(--border-color);
  overflow: hidden;
  box-shadow: var(--shadow-card);
  transition: all 0.3s ease;
}

.markdown-editor :deep(.md-editor:hover) {
  border-color: var(--primary-start);
  box-shadow: 0 0 0 1px var(--primary-start), var(--shadow-card);
}

/* 工具栏美化 */
.markdown-editor :deep(.md-editor-toolbar) {
  background: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-color);
  padding: 8px 12px;
}

.markdown-editor :deep(.md-editor-toolbar-item) {
  color: var(--text-secondary);
  border-radius: 6px;
  transition: all 0.2s ease;
  margin: 0 2px;
}

.markdown-editor :deep(.md-editor-toolbar-item:hover) {
  color: var(--primary-start);
  background: rgba(102, 126, 234, 0.1);
}

.markdown-editor :deep(.md-editor-toolbar-item.active) {
  color: var(--primary-start);
  background: rgba(102, 126, 234, 0.15);
}

/* 编辑区域美化 */
.markdown-editor :deep(.md-editor-input-wrapper) {
  background: var(--bg-secondary);
}

.markdown-editor :deep(.md-editor-input-wrapper textarea) {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.8;
  padding: 16px;
}

.markdown-editor :deep(.md-editor-input-wrapper textarea::placeholder) {
  color: var(--text-muted);
}

/* 预览区域美化 */
.markdown-editor :deep(.md-editor-preview-wrapper) {
  background: var(--bg-secondary);
  padding: 16px;
}

.markdown-editor :deep(.md-editor-preview) {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.8;
}

/* Markdown 内容样式 */
.markdown-editor :deep(.md-editor-preview h1),
.markdown-editor :deep(.md-editor-preview h2),
.markdown-editor :deep(.md-editor-preview h3),
.markdown-editor :deep(.md-editor-preview h4),
.markdown-editor :deep(.md-editor-preview h5),
.markdown-editor :deep(.md-editor-preview h6) {
  color: var(--text-primary);
  font-weight: 600;
  margin-top: 24px;
  margin-bottom: 16px;
  line-height: 1.4;
}

.markdown-editor :deep(.md-editor-preview h1) {
  font-size: 28px;
  border-bottom: 2px solid var(--border-color);
  padding-bottom: 12px;
}

.markdown-editor :deep(.md-editor-preview h2) {
  font-size: 24px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 10px;
}

.markdown-editor :deep(.md-editor-preview h3) {
  font-size: 20px;
}

.markdown-editor :deep(.md-editor-preview p) {
  margin: 12px 0;
}

.markdown-editor :deep(.md-editor-preview a) {
  color: var(--primary-start);
  text-decoration: none;
  transition: opacity 0.2s;
}

.markdown-editor :deep(.md-editor-preview a:hover) {
  opacity: 0.8;
  text-decoration: underline;
}

.markdown-editor :deep(.md-editor-preview code) {
  background: var(--bg-tertiary);
  color: var(--primary-start);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.markdown-editor :deep(.md-editor-preview pre) {
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  padding: 16px;
  overflow-x: auto;
  margin: 16px 0;
}

.markdown-editor :deep(.md-editor-preview pre code) {
  background: transparent;
  padding: 0;
  color: var(--text-primary);
}

.markdown-editor :deep(.md-editor-preview blockquote) {
  border-left: 4px solid var(--primary-start);
  background: rgba(102, 126, 234, 0.05);
  padding: 12px 16px;
  margin: 16px 0;
  color: var(--text-secondary);
}

.markdown-editor :deep(.md-editor-preview ul),
.markdown-editor :deep(.md-editor-preview ol) {
  padding-left: 24px;
  margin: 12px 0;
}

.markdown-editor :deep(.md-editor-preview li) {
  margin: 6px 0;
}

.markdown-editor :deep(.md-editor-preview table) {
  border-collapse: collapse;
  width: 100%;
  margin: 16px 0;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.markdown-editor :deep(.md-editor-preview table th) {
  background: var(--bg-tertiary);
  color: var(--text-primary);
  font-weight: 600;
  padding: 12px;
  text-align: left;
  border-bottom: 2px solid var(--border-color);
}

.markdown-editor :deep(.md-editor-preview table td) {
  padding: 12px;
  border-bottom: 1px solid var(--border-color);
}

.markdown-editor :deep(.md-editor-preview table tr:last-child td) {
  border-bottom: none;
}

.markdown-editor :deep(.md-editor-preview table tr:hover) {
  background: var(--bg-card-hover);
}

.markdown-editor :deep(.md-editor-preview img) {
  max-width: 100%;
  border-radius: var(--radius-sm);
  margin: 16px 0;
  box-shadow: var(--shadow-card);
}

/* 滚动条美化 */
.markdown-editor :deep(.md-editor-input-wrapper::-webkit-scrollbar),
.markdown-editor :deep(.md-editor-preview-wrapper::-webkit-scrollbar) {
  width: 8px;
  height: 8px;
}

.markdown-editor :deep(.md-editor-input-wrapper::-webkit-scrollbar-track),
.markdown-editor :deep(.md-editor-preview-wrapper::-webkit-scrollbar-track) {
  background: var(--bg-tertiary);
  border-radius: 4px;
}

.markdown-editor :deep(.md-editor-input-wrapper::-webkit-scrollbar-thumb),
.markdown-editor :deep(.md-editor-preview-wrapper::-webkit-scrollbar-thumb) {
  background: var(--primary-start);
  border-radius: 4px;
}

.markdown-editor :deep(.md-editor-input-wrapper::-webkit-scrollbar-thumb:hover),
.markdown-editor :deep(.md-editor-preview-wrapper::-webkit-scrollbar-thumb:hover) {
  background: var(--primary-end);
}

/* 分割线美化 */
.markdown-editor :deep(.md-editor-resize-operate) {
  background: var(--border-color);
  width: 2px;
  transition: background 0.2s;
}

.markdown-editor :deep(.md-editor-resize-operate:hover) {
  background: var(--primary-start);
}
</style>
