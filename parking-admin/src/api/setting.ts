import request from '@/utils/request'

export function getAllSettings() {
  return request.get('/settings')
}

export function getSettingValue(key: string) {
  return request.get(`/settings/${key}`)
}

export function updateSettings(data: any) {
  return request.put('/settings', data)
}
