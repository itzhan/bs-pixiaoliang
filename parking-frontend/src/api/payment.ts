import request from '@/utils/request'

/** 创建支付 */
export function createPayment(data: {
  orderId: number
  paymentMethod: string
}) {
  return request.post('/payments', data)
}

/** 获取我的支付记录 */
export function getMyPayments(params?: {
  page?: number
  size?: number
}) {
  return request.get('/payments/my', { params })
}
