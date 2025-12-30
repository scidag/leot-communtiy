import request from '@/utils/request'
import type { ApiResponse } from '@/types/user'
import type { Question } from '@/types/bagu'

/**
 * 添加题目到题库 DTO
 */
export interface AddQuestionBankQuestionDTO {
  questionBankId: number
  questionId: number
}

/**
 * 批量添加题目到题库 DTO
 */
export interface BatchAddQuestionDTO {
  questionBankId: number
  questionIds: number[]
}

/**
 * 题库题目关联 API
 */
export const questionBankQuestionApi = {
  /**
   * 添加题目到题库（管理员）
   */
  add: (data: AddQuestionBankQuestionDTO): Promise<ApiResponse<boolean>> => {
    return request.post('/bagu/questionBankQuestion/add', data)
  },

  /**
   * 批量添加题目到题库（管理员）
   */
  batchAdd: (data: BatchAddQuestionDTO): Promise<ApiResponse<number>> => {
    return request.post('/bagu/questionBankQuestion/add/batch', data)
  },

  /**
   * 从题库移除题目（管理员）
   */
  remove: (questionBankId: number, questionId: number): Promise<ApiResponse<boolean>> => {
    return request.delete(`/bagu/questionBankQuestion/remove?questionBankId=${questionBankId}&questionId=${questionId}`)
  },

  /**
   * 查询题库下的题目列表
   */
  listByBankId: (questionBankId: number): Promise<ApiResponse<Question[]>> => {
    return request.get(`/bagu/questionBankQuestion/list?questionBankId=${questionBankId}`)
  }
}
