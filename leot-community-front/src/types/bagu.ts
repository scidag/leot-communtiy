// 八股文模块类型定义

/**
 * 题目实体
 */
export interface Question {
  id: number
  title: string
  content: string
  answer?: string
  tags: string[]
  userId: number
  userName?: string
  viewNum: number
  thumbNum: number
  favourNum: number
  hasThumb?: boolean
  hasFavour?: boolean
  createTime: string
  updateTime?: string
}

/**
 * 题库实体
 */
export interface QuestionBank {
  id: number
  title: string
  description?: string
  picture?: string
  userId: number
  userName?: string
  questionCount?: number
  createTime: string
  updateTime?: string
}

/**
 * 搜索参数
 */
export interface SearchParams {
  keyword?: string
  tags?: string[]
  questionBankId?: number
  sortField?: string
  sortOrder?: 'asc' | 'desc'
  current: number
  pageSize: number
}

/**
 * 分页参数
 */
export interface PageParams {
  current: number
  pageSize: number
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

/**
 * 新增题目 DTO
 */
export interface AddQuestionDTO {
  title: string
  content: string
  answer?: string
  tags?: string[]
  questionBankId?: number
}

/**
 * 更新题目 DTO
 */
export interface UpdateQuestionDTO {
  id: number
  title?: string
  content?: string
  answer?: string
  tags?: string[]
}

/**
 * 新增题库 DTO
 */
export interface AddQuestionBankDTO {
  title: string
  description?: string
  picture?: string
}

/**
 * 更新题库 DTO
 */
export interface UpdateQuestionBankDTO {
  id: number
  title?: string
  description?: string
  picture?: string
}

/**
 * 查询题目 DTO
 */
export interface QueryQuestionDTO extends PageParams {
  questionBankId?: number
  title?: string
  tags?: string[]
}

/**
 * 查询题库 DTO
 */
export interface QueryQuestionBankDTO extends PageParams {
  title?: string
}
