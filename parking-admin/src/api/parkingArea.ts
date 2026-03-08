import request from '@/utils/request'

export function getAllAreas() {
  return request.get('/parking-areas')
}

export function getAreas(params: { page: number; size: number; keyword?: string }) {
  return request.get('/parking-areas/page', { params })
}

export function getAreaStats() {
  return request.get('/parking-areas/stats')
}

export function getAreaById(id: number) {
  return request.get(`/parking-areas/${id}`)
}

export function createArea(data: any) {
  return request.post('/parking-areas', data)
}

export function updateArea(id: number, data: any) {
  return request.put(`/parking-areas/${id}`, data)
}

export function deleteArea(id: number) {
  return request.delete(`/parking-areas/${id}`)
}
