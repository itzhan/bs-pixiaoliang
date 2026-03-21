import axios from 'axios'
import { createDiscreteApi } from 'naive-ui'

const { message } = createDiscreteApi(['message'])

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// Request interceptor – attach token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// Response interceptor – unwrap { code, data, message }
request.interceptors.response.use(
  (response) => {
    const res = response.data as { code: number; data: unknown; message: string }

    if (res.code === 200) {
      return res as any
    }

    if (res.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      message.error('登录已过期，请重新登录')
      window.location.href = '/login'
      return Promise.reject(new Error(res.message || '未授权'))
    }

    message.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      message.error('登录已过期，请重新登录')
      window.location.href = '/login'
    } else if (status === 403) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      message.warning('请先登录后再操作')
      window.location.href = '/login'
    } else if (status === 404) {
      message.error('请求的资源不存在')
    } else if (status === 500) {
      message.error('服务器内部错误')
    } else {
      message.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  },
)

export default request
