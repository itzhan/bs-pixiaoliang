import request from '@/utils/request'

export function getPublishedAnnouncements() {
  return request.get('/announcements/published')
}
