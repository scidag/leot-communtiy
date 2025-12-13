// 用户相关类型定义
export interface User {
  id?: number
  userName: string
  userAccount: string
  userPassword?: string
  profile?: string
  userRole: 'user' | 'admin'
  createTime?: string
  updateTime?: string
}

export interface LoginRequest {
  userName: string
  userPassword: string
}

export interface RegisterRequest {
  userName: string
  userPassword: string
  profile?: string
}

export interface UserListRequest {
  current: number
  pageSize: number
  userName?: string
  userAccount?: string
}

export interface UserListResponse {
  records: User[]
  total: number
  current: number
  pageSize: number
}

export interface ApiResponse<T = any> {
  code: number
  data: T
  message: string
}

