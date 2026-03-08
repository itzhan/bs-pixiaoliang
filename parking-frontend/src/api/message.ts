import request from '@/utils/request'

/** 获取消息列表 */
export function getMessages(params?: {
  page?: number
  size?: number
  isRead?: boolean
}) {
  return request.get('/messages', { params })
}

/** 获取未读消息数量 */
export function getUnreadCount() {
  return request.get('/messages/unread-count')
}

/** 标记单条消息为已读 */
export function markAsRead(id: number) {
  return request.put(`/messages/${id}/read`)
}

/** 标记所有消息为已读 */
export function markAllAsRead() {
  return request.put('/messages/read-all')
}
