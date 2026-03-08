import request from '@/utils/request'

export function getAnnouncements(params: { page: number; size: number; status?: number; keyword?: string }) {
  return request.get('/announcements', { params })
}

export function createAnnouncement(data: any) {
  return request.post('/announcements', data)
}

export function updateAnnouncement(id: number, data: any) {
  return request.put(`/announcements/${id}`, data)
}

export function publishAnnouncement(id: number) {
  return request.put(`/announcements/${id}/publish`)
}

export function deleteAnnouncement(id: number) {
  return request.delete(`/announcements/${id}`)
}
