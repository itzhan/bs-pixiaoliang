import request from '@/utils/request'

export function getBlacklist(params: { page: number; size: number; status?: number; keyword?: string }) {
  return request.get('/blacklist', { params })
}

export function addToBlacklist(data: any) {
  return request.post('/blacklist', data)
}

export function removeFromBlacklist(id: number) {
  return request.put(`/blacklist/${id}/remove`)
}
