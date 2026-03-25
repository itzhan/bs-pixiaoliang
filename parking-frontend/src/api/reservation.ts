import request from '@/utils/request'

/** 创建预约 */
export function createReservation(data: {
  vehicleId: number
  spaceId: number
  startTime: string
  endTime: string
  payNow?: boolean
}) {
  return request.post('/reservations', data)
}

/** 获取我的预约列表 */
export function getMyReservations(params?: {
  page?: number
  size?: number
  status?: number | string
  paymentStatus?: number | string
}) {
  return request.get('/reservations/my', { params })
}

/** 取消预约 */
export function cancelReservation(id: number, reason?: string) {
  return request.put(`/reservations/${id}/cancel`, null, { params: { reason } })
}

/** 查询某车位某日已被预约的时间段 */
export function getBookedSlots(spaceId: number, date: string) {
  return request.get('/reservations/available-slots', { params: { spaceId, date } })
}

/** 支付预约 */
export function payReservation(id: number) {
  return request.put(`/reservations/${id}/pay`)
}
