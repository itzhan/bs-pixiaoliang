import request from '@/utils/request'

export function getAllPayments(params: { page: number; size: number; status?: number; keyword?: string }) {
  return request.get('/payments', { params })
}
