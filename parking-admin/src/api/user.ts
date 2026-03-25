import request from '@/utils/request'

export function getUsers(params: { page: number; size: number; keyword?: string; role?: string }) {
  return request.get('/users', { params })
}

export function createUser(data: any) {
  return request.post('/auth/register', data)
}

export function getUserById(id: number) {
  return request.get(`/users/${id}`)
}

export function updateUser(id: number, data: any) {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/users/${id}`)
}
