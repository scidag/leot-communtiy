import request from '@/utils/request'
import type { 
  QuestionBank, 
  PageResponse, 
  AddQuestionBankDTO, 
  UpdateQuestionBankDTO,
  QueryQuestionBankDTO
} from '@/types/bagu'
import type { ApiResponse } from '@/types/user'

/**
 * 题库 API
 */
export const questionBankApi = {
  /**
   * 获取题库详情
   */
  getById: (id: number): Promise<ApiResponse<QuestionBank>> => {
    return request.get(`/bagu/questionBank/get?id=${id}`)
  },

  /**
   * 分页查询题库列表
   */
  listByPage: (params: QueryQuestionBankDTO): Promise<ApiResponse<PageResponse<QuestionBank>>> => {
    return request.post('/bagu/questionBank/list/page', params)
  },

  // ========== 管理员接口 ==========

  /**
   * 新增题库
   */
  add: (data: AddQuestionBankDTO): Promise<ApiResponse<number>> => {
    return request.post('/bagu/questionBank/add', data)
  },

  /**
   * 更新题库
   */
  update: (data: UpdateQuestionBankDTO): Promise<ApiResponse<boolean>> => {
    return request.put('/bagu/questionBank/update', data)
  },

  /**
   * 删除题库
   */
  delete: (id: number): Promise<ApiResponse<boolean>> => {
    return request.delete('/bagu/questionBank/delete', { data: { id } })
  }
}
