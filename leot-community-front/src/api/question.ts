import request from '@/utils/request'
import type { 
  Question, 
  SearchParams, 
  PageParams, 
  PageResponse, 
  AddQuestionDTO, 
  UpdateQuestionDTO,
  QueryQuestionDTO
} from '@/types/bagu'
import type { ApiResponse } from '@/types/user'

/**
 * 题目 API
 */
export const questionApi = {
  /**
   * 获取题目详情
   */
  getById: (id: number): Promise<ApiResponse<Question>> => {
    return request.get(`/bagu/question/get?id=${id}`)
  },

  /**
   * 分页查询题目列表
   */
  listByPage: (params: QueryQuestionDTO): Promise<ApiResponse<PageResponse<Question>>> => {
    return request.post('/bagu/question/list/page', params)
  },

  /**
   * 搜索题目
   */
  search: (params: SearchParams): Promise<ApiResponse<PageResponse<Question>>> => {
    return request.post('/bagu/question/search', params)
  },

  /**
   * 点赞/取消点赞题目
   */
  thumb: (questionId: number): Promise<ApiResponse<boolean>> => {
    return request.post(`/bagu/question/thumb?questionId=${questionId}`)
  },

  /**
   * 收藏/取消收藏题目
   */
  favour: (questionId: number): Promise<ApiResponse<boolean>> => {
    return request.post(`/bagu/question/favour?questionId=${questionId}`)
  },

  /**
   * 获取用户收藏的题目列表
   */
  listFavour: (params: PageParams): Promise<ApiResponse<PageResponse<Question>>> => {
    return request.post('/bagu/question/favour/list', params)
  },

  // ========== 管理员接口 ==========

  /**
   * 新增题目
   */
  add: (data: AddQuestionDTO): Promise<ApiResponse<number>> => {
    return request.post('/bagu/question/add', data)
  },

  /**
   * 更新题目
   */
  update: (data: UpdateQuestionDTO): Promise<ApiResponse<boolean>> => {
    return request.put('/bagu/question/update', data)
  },

  /**
   * 删除题目
   */
  delete: (id: number): Promise<ApiResponse<boolean>> => {
    return request.delete('/bagu/question/delete', { data: { id } })
  }
}
