import request from '@/utils/request'

export function getMessages(params: any) {
  return request.get('/messages', { params })
}

export function getUnreadCount() {
  return request.get('/messages/unread-count')
}
