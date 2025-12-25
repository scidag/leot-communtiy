import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios'
import { ElMessage, ElLoading } from 'element-plus'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true
})

let loadingInstance: any = null

// 请求拦截器
service.interceptors.request.use(
  (config: any) => {
    // 显示 loading
    loadingInstance = ElLoading.service({
      lock: true,
      text: '加载中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    // 从 localStorage 读取 token（Sa-Token 使用 satoken 作为 header 名称）
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers['satoken'] = token
    }

    return config
  },
  (error: any) => {
    loadingInstance?.close()
    ElMessage.error('请求错误')
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<any>) => {
    loadingInstance?.close()

    const res: any = response.data

    // 如果返回的业务状态码不是 200/0，则视为业务错误，交给调用方处理展示，避免 interceptor 重复弹窗
    if (res && res.code !== undefined && res.code !== 200 && res.code !== 0) {
      const err: any = new Error(res.message || '请求失败')
      // 模拟 axios 错误对象的结构，便于调用方从 error.response.data 读取后端返回
      err.response = response
      return Promise.reject(err)
    }

    // 返回解析后的业务数据（可能为原始 data 或后端自定义结构）
    return res
  },
  (error: AxiosError) => {
    loadingInstance?.close()

    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求错误，未找到该资源')
          break
        case 500: {
          // 后端可能在 response.data 中携带业务异常信息（或 Spring 默认错误结构），
          // 不在 interceptor 直接弹出，避免与页面层重复提示。
          const respData: any = error.response.data
          const serverMsg = respData?.message || respData?.error || (typeof respData === 'string' ? respData : null)
          if (serverMsg) {
            // 将后端信息放到 error.message，以便调用方统一展示
            try {
              ;(error as any).message = serverMsg
            } catch (e) {}
          }
          break
        }
        default:
          ElMessage.error(`请求失败: ${error.message}`)
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

export default service

