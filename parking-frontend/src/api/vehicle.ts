import request from '@/utils/request'

/** 获取我的车辆列表 */
export function getMyVehicles() {
  return request.get('/vehicles/my')
}

/** 添加车辆 */
export function addVehicle(data: {
  plateNumber: string
  vehicleType?: string
  brand?: string
  color?: string
}) {
  return request.post('/vehicles', data)
}

/** 更新车辆信息 */
export function updateVehicle(id: number, data: {
  plateNumber?: string
  vehicleType?: string
  brand?: string
  color?: string
}) {
  return request.put(`/vehicles/${id}`, data)
}

/** 删除车辆 */
export function deleteVehicle(id: number) {
  return request.delete(`/vehicles/${id}`)
}
