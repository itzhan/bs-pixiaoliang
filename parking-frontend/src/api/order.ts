import request from '@/utils/request'

/** 获取我的订单列表 */
export function getMyOrders(params?: {
  status?: string
  page?: number
  size?: number
}) {
  return request.get('/orders/my', { params })
}

/** 获取订单详情 */
export function getOrderById(id: number) {
  return request.get(`/orders/${id}`)
}
