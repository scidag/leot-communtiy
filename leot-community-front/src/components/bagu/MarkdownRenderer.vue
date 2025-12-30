<template>
  <div class="markdown-renderer">
    <MdPreview
      :model-value="content"
      :theme="theme"
      :preview-theme="previewTheme"
      :code-theme="codeTheme"
      :language="language"
    />
  </div>
</template>

<script setup lang="ts">
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'

interface Props {
  content: string
}

const props = withDefaults(defineProps<Props>(), {
  content: ''
})

// 渲染器配置
const theme = 'dark'
const previewTheme = 'github'
const codeTheme = 'atom'
const language = 'zh-CN'
</script>

<style scoped>
.markdown-renderer {
  width: 100%;
}

.markdown-renderer :deep(.md-editor-preview-wrapper) {
  padding: 0;
}

.markdown-renderer :deep(.md-editor-preview) {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

.markdown-renderer :deep(.md-editor-preview h1),
.markdown-renderer :deep(.md-editor-preview h2),
.markdown-renderer :deep(.md-editor-preview h3),
.markdown-renderer :deep(.md-editor-preview h4),
.markdown-renderer :deep(.md-editor-preview h5),
.markdown-renderer :deep(.md-editor-preview h6) {
  color: var(--text-primary, #ffffff);
  margin-top: 24px;
  margin-bottom: 16px;
}

.markdown-renderer :deep(.md-editor-preview h1) {
  font-size: 28px;
}

.markdown-renderer :deep(.md-editor-preview h2) {
  font-size: 24px;
}

.markdown-renderer :deep(.md-editor-preview h3) {
  font-size: 20px;
}

.markdown-renderer :deep(.md-editor-preview p) {
  margin-bottom: 16px;
}

.markdown-renderer :deep(.md-editor-preview img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 16px 0;
}

.markdown-renderer :deep(.md-editor-preview pre) {
  background: var(--bg-tertiary, #16213e) !important;
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
  margin: 16px 0;
}

.markdown-renderer :deep(.md-editor-preview code) {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
}

.markdown-renderer :deep(.md-editor-preview blockquote) {
  border-left: 4px solid var(--primary-start, #667eea);
  background: rgba(102, 126, 234, 0.1);
  padding: 12px 16px;
  margin: 16px 0;
  border-radius: 0 8px 8px 0;
}

.markdown-renderer :deep(.md-editor-preview ul),
.markdown-renderer :deep(.md-editor-preview ol) {
  padding-left: 24px;
  margin-bottom: 16px;
}

.markdown-renderer :deep(.md-editor-preview li) {
  margin-bottom: 8px;
}

.markdown-renderer :deep(.md-editor-preview table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.markdown-renderer :deep(.md-editor-preview table th),
.markdown-renderer :deep(.md-editor-preview table td) {
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
  padding: 12px;
  text-align: left;
}

.markdown-renderer :deep(.md-editor-preview table th) {
  background: var(--bg-tertiary, #16213e);
  font-weight: 600;
}

.markdown-renderer :deep(.md-editor-preview a) {
  color: var(--primary-start, #667eea);
  text-decoration: none;
}

.markdown-renderer :deep(.md-editor-preview a:hover) {
  text-decoration: underline;
}

.markdown-renderer :deep(.md-editor-preview hr) {
  border: none;
  border-top: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
  margin: 24px 0;
}
</style>
