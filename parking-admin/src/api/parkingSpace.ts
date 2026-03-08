import request from '@/utils/request'

export function getAvailableSpaces(params?: any) {
  return request.get('/parking-spaces/available', { params })
}

export function getSpaces(params: { page: number; size: number; areaId?: number; status?: number; spaceType?: string }) {
  return request.get('/parking-spaces', { params })
}

export function createSpace(data: any) {
  return request.post('/parking-spaces', data)
}

export function updateSpace(id: number, data: any) {
  return request.put(`/parking-spaces/${id}`, data)
}

export function updateSpaceStatus(id: number, status: number) {
  return request.put(`/parking-spaces/${id}/status`, null, { params: { status } })
}

export function deleteSpace(id: number) {
  return request.delete(`/parking-spaces/${id}`)
}
