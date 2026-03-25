import request from '@/utils/request'

export function getAllReservations(params: { page: number; size: number; status?: number; keyword?: string }) {
  return request.get('/reservations', { params })
}

export function getReservationById(id: number) {
  return request.get(`/reservations/${id}`)
}

export function updateReservationStatus(id: number, status: number) {
  return request.put(`/reservations/${id}/status`, null, { params: { status } })
}

export function deleteReservation(id: number) {
  return request.delete(`/reservations/${id}`)
}

export function getReservationsByPlate(plateNumber: string) {
  return request.get('/reservations/by-plate', { params: { plateNumber } })
}
