import request from '@/utils/request'

export function getAllRules() {
  return request.get('/billing-rules')
}

export function getRules(params: { page: number; size: number }) {
  return request.get('/billing-rules/page', { params })
}

export function createRule(data: any) {
  return request.post('/billing-rules', data)
}

export function updateRule(id: number, data: any) {
  return request.put(`/billing-rules/${id}`, data)
}

export function deleteRule(id: number) {
  return request.delete(`/billing-rules/${id}`)
}
