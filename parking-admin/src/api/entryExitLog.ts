import request from '@/utils/request'

export function getLogs(params: { page: number; size: number; plateNumber?: string; logType?: string }) {
  return request.get('/entry-exit-logs', { params })
}

export function deleteLog(id: number) {
  return request.delete(`/entry-exit-logs/${id}`)
}
