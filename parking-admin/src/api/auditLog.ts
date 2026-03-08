import request from '@/utils/request'

export function getLogs(params: { page: number; size: number; module?: string; action?: string }) {
  return request.get('/audit-logs', { params })
}
