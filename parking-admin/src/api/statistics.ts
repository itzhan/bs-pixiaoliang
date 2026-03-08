import request from '@/utils/request'

export function getDashboard() {
  return request.get('/statistics/dashboard')
}

export function getRevenueTrend(days?: number) {
  return request.get('/statistics/revenue-trend', { params: { days } })
}

export function getHourlyStats() {
  return request.get('/statistics/hourly')
}
