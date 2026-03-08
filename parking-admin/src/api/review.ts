import request from '@/utils/request'

export function getAllReviews(params: { page: number; size: number; status?: number }) {
  return request.get('/reviews', { params })
}

export function replyReview(id: number, data: any) {
  return request.put(`/reviews/${id}/reply`, data)
}

export function updateReviewStatus(id: number, status: number) {
  return request.put(`/reviews/${id}/status`, null, { params: { status } })
}
