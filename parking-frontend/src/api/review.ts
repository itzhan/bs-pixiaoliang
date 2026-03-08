import request from '@/utils/request'

/** 创建评价 */
export function createReview(data: {
  orderId: number
  rating: number
  content: string
}) {
  return request.post('/reviews', data)
}

/** 获取我的评价列表 */
export function getMyReviews(params?: {
  page?: number
  size?: number
}) {
  return request.get('/reviews/my', { params })
}
