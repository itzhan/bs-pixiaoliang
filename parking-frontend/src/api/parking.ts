import request from '@/utils/request'

export function getAllAreas() {
  return request.get('/parking-areas')
}

export function getAreaStats() {
  return request.get('/parking-areas/stats')
}

export function getAreaById(id: number) {
  return request.get(`/parking-areas/${id}`)
}

export function getAvailableSpaces(params: { areaId?: number; spaceType?: string }) {
  return request.get('/parking-spaces/available', { params })
}
