import request from '@/utils/request'
import type { ApiResponse } from '@/types/user'

/**
 * 文件上传 API
 */
export const fileApi = {
  /**
   * 上传图片到 COS
   * @param file 图片文件
   * @param bizType 业务类型 (默认: questionbank)
   * @returns 图片访问 URL
   */
  uploadImage: (file: File, bizType: string = 'questionbank'): Promise<ApiResponse<string>> => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('bizType', bizType)
    
    return request.post('/bagu/file/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

/**
 * 上传图片（便捷函数）
 * @param file 图片文件
 * @param bizType 业务类型 (默认: questionbank)
 * @returns 图片访问 URL
 */
export const uploadImage = (file: File, bizType: string = 'questionbank'): Promise<ApiResponse<string>> => {
  return fileApi.uploadImage(file, bizType)
}
