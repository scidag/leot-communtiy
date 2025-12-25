<template>
  <div 
    class="glass-card"
    :class="{ 
      'glass-card--glow': glow, 
      'glass-card--hover-scale': hoverScale,
      'glass-card--clickable': clickable
    }"
    :style="customStyle"
    @click="handleClick"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  glow?: boolean
  hoverScale?: boolean
  clickable?: boolean
  gradient?: string
  padding?: string
}

const props = withDefaults(defineProps<Props>(), {
  glow: false,
  hoverScale: true,
  clickable: false,
  padding: '24px'
})

const emit = defineEmits<{
  (e: 'click'): void
}>()

const customStyle = computed(() => ({
  padding: props.padding,
  ...(props.gradient && { 
    background: `linear-gradient(135deg, ${props.gradient})` 
  })
}))

const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<style scoped>
.glass-card {
  background: var(--bg-card);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  transition: var(--transition-base);
}

.glass-card--hover-scale:hover {
  background: var(--bg-card-hover);
  border-color: var(--border-hover);
  transform: translateY(-4px);
}

.glass-card--glow {
  box-shadow: var(--glow-subtle);
}

.glass-card--glow:hover {
  box-shadow: var(--glow-hover);
}

.glass-card--clickable {
  cursor: pointer;
}

.glass-card--clickable:active {
  transform: translateY(-2px) scale(0.98);
}
</style>
