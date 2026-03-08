import request from '@/utils/request'

/** 更新个人信息 */
export function updateProfile(data: {
  realName?: string
  phone?: string
  email?: string
  avatar?: string
}) {
  return request.put('/users/profile', data)
}

/** 修改密码 */
export function updatePassword(data: {
  oldPassword: string
  newPassword: string
}) {
  return request.put('/users/password', data)
}
