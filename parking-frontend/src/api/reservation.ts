import request from '@/utils/request'

/** 创建预约 */
export function createReservation(data: {
  vehicleId: number
  spaceId: number
  startTime: string
  endTime: string
}) {
  return request.post('/reservations', data)
}

/** 获取我的预约列表 */
export function getMyReservations(params?: {
  page?: number
  size?: number
  status?: number | string
}) {
  return request.get('/reservations/my', { params })
}

/** 取消预约 */
export function cancelReservation(id: number, reason?: string) {
  return request.put(`/reservations/${id}/cancel`, null, { params: { reason } })
}
