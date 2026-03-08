import request from '@/utils/request'

export function getVehicles(params: { page: number; size: number; keyword?: string; userId?: number }) {
  return request.get('/vehicles', { params })
}

export function addVehicle(data: any) {
  return request.post('/vehicles', data)
}

export function updateVehicle(id: number, data: any) {
  return request.put(`/vehicles/${id}`, data)
}

export function deleteVehicle(id: number) {
  return request.delete(`/vehicles/${id}`)
}
