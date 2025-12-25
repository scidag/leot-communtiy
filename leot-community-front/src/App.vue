<template>
  <div class="app-container">
    <!-- 全局导航栏（登录/注册页面不显示） -->
    <NavBar v-if="showNavBar" />
    
    <!-- 主内容区域 -->
    <main :class="{ 'with-navbar': showNavBar }">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '@/components/layout/NavBar.vue'

const route = useRoute()

// 登录和注册页面不显示导航栏
const showNavBar = computed(() => {
  const hiddenPaths = ['/login', '/register']
  return !hiddenPaths.includes(route.path)
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

body {
  margin: 0;
  padding: 0;
}

.app-container {
  min-height: 100vh;
}

/* 有导航栏时，主内容区域需要顶部间距 */
main.with-navbar {
  padding-top: 60px;
}
</style>
