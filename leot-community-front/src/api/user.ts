import request from '@/utils/request'
import type {
  User,
  LoginRequest,
  RegisterRequest,
  UserListRequest,
  UserListResponse,
  ApiResponse
} from '@/types/user'

// 用户注册
export const register = (data: RegisterRequest): Promise<ApiResponse<User>> => {
  return request.post('/user/register', data)
}

// 用户登录
export const login = (data: LoginRequest): Promise<ApiResponse<User>> => {
  return request.post('/user/login', data)
}

// 获取当前登录用户
export const getLoginUser = (): Promise<ApiResponse<User>> => {
  return request.get('/user/get/loginuser')
}

// 用户登出
export const logout = (): Promise<ApiResponse> => {
  return request.post('/user/logout')
}

// 管理员创建用户
export const addUser = (data: RegisterRequest): Promise<ApiResponse<User>> => {
  return request.post('/user/add', data)
}

// 管理员删除用户
export const deleteUser = (id: number): Promise<ApiResponse> => {
  return request.delete(`/user/delete?id=${id}`)
}

// 管理员更新用户信息
export const updateUserInfo = (data: User): Promise<ApiResponse<User>> => {
  return request.put('/user/updateuserinfo', data)
}

// 管理员分页查询用户列表
export const getUserList = (data: UserListRequest): Promise<ApiResponse<UserListResponse>> => {
  return request.post('/user/list', data)
}

// 管理员按 ID 获取用户详情
export const getUserById = (id: number): Promise<ApiResponse<User>> => {
  return request.get(`/user/getone/${id}`)
}

// 普通用户按 ID 安全获取脱敏用户信息
export const getSafeUserById = (id: number): Promise<ApiResponse<User>> => {
  return request.post('/user/getone/safe', { id })
}

