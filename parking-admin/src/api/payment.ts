import request from '@/utils/request'

export function getAllPayments(params: { page: number; size: number; status?: number; keyword?: string }) {
  return request.get('/payments', { params })
}

export function deletePayment(id: number) {
  return request.delete(`/payments/${id}`)
}
