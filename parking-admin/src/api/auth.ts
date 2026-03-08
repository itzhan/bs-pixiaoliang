import request from '@/utils/request'

export function login(data: { username: string; password: string }) {
  return request.post('/auth/login', data)
}

export function register(data: any) {
  return request.post('/auth/register', data)
}

export function getUserInfo() {
  return request.get('/auth/info')
}

export function logout() {
  return request.post('/auth/logout')
}
