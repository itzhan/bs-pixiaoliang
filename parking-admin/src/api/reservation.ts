import request from '@/utils/request'

export function getAllReservations(params: { page: number; size: number; status?: number; keyword?: string }) {
  return request.get('/reservations', { params })
}

export function getReservationById(id: number) {
  return request.get(`/reservations/${id}`)
}
