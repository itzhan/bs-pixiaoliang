import request from '@/utils/request'

export function vehicleEntry(data: any) {
  return request.post('/orders/entry', data)
}

export function vehicleExit(data: any) {
  return request.post('/orders/exit', data)
}

export function getAllOrders(params: { page: number; size: number; status?: number; paymentStatus?: number; keyword?: string }) {
  return request.get('/orders', { params })
}

export function getOrderById(id: number) {
  return request.get(`/orders/${id}`)
}

export function markAbnormal(id: number) {
  return request.put(`/orders/${id}/abnormal`)
}
