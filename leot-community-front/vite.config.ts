import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000,
    host: '0.0.0.0', // 允许外部访问
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // Gateway 网关地址
        changeOrigin: true, // 改变请求头中的 origin
        rewrite: (path) => path, // 保持路径不变，网关会处理路由
        secure: false, // 如果是 https 接口，需要配置这个参数
        ws: true, // 支持 websocket
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, _res) => {
            console.log('proxy error', err)
          })
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            console.log('Sending Request to Gateway:', req.method, req.url)
          })
          proxy.on('proxyRes', (proxyRes, req, _res) => {
            console.log('Received Response from Gateway:', proxyRes.statusCode, req.url)
          })
        }
      }
    }
  }
})

