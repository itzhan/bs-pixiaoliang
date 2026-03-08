# 智能停车场管理系统 - API 文档

## 概览

- **Base URL**: `http://localhost:8088/api`
- **认证方式**: JWT Bearer Token
- **Content-Type**: `application/json`
- **分页参数**: `page`(页码,从1开始) `size`(每页条数,默认10)

## 统一返回格式

```json
{ "code": 200, "message": "success", "data": {} }
```

## 分页返回格式

```json
{ "code": 200, "data": { "records": [], "total": 100, "page": 1, "size": 10 } }
```

## 错误码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误/业务校验失败 |
| 401 | 未认证 |
| 403 | 无权限 |
| 500 | 服务器内部错误 |

---

## 1. 认证 /api/auth

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/auth/login | 公开 | 登录,返回token |
| POST | /api/auth/register | 公开 | 注册 |
| GET | /api/auth/info | 需登录 | 当前用户信息 |

**登录请求**: `{ "username": "admin", "password": "admin123" }`
**登录响应**: `{ token, userId, username, realName, role, avatar }`

## 2. 用户 /api/users

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/users?page&size&keyword&role | ADMIN | 用户列表 |
| GET | /api/users/{id} | 需登录 | 用户详情 |
| PUT | /api/users/{id} | 需登录 | 更新用户 |
| PUT | /api/users/profile | 需登录 | 更新个人信息 |
| DELETE | /api/users/{id} | ADMIN | 删除用户 |

## 3. 车辆 /api/vehicles

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/vehicles/my | 需登录 | 我的车辆 |
| GET | /api/vehicles?page&size&keyword&userId | ADMIN | 车辆列表 |
| POST | /api/vehicles | 需登录 | 绑定车辆 |
| PUT | /api/vehicles/{id} | 需登录 | 更新车辆 |
| DELETE | /api/vehicles/{id} | 需登录 | 删除车辆 |

## 4. 停车区域 /api/parking-areas

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/parking-areas | 公开 | 所有启用区域 |
| GET | /api/parking-areas/page?page&size&keyword | 需登录 | 分页查询 |
| GET | /api/parking-areas/stats | 需登录 | 区域车位统计 |
| GET | /api/parking-areas/{id} | 需登录 | 区域详情 |
| POST | /api/parking-areas | ADMIN | 新增区域 |
| PUT | /api/parking-areas/{id} | ADMIN | 更新区域 |
| DELETE | /api/parking-areas/{id} | ADMIN | 删除区域 |

## 5. 车位 /api/parking-spaces

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/parking-spaces/available?areaId&spaceType | 公开 | 空闲车位 |
| GET | /api/parking-spaces?page&size&areaId&status&spaceType | 需登录 | 分页查询 |
| POST | /api/parking-spaces | ADMIN | 新增车位 |
| PUT | /api/parking-spaces/{id} | ADMIN | 更新车位 |
| PUT | /api/parking-spaces/{id}/status?status= | ADMIN | 更新状态 |
| DELETE | /api/parking-spaces/{id} | ADMIN | 删除车位 |

## 6. 预约 /api/reservations

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/reservations | 需登录 | 创建预约 |
| PUT | /api/reservations/{id}/cancel?reason= | 需登录 | 取消预约 |
| GET | /api/reservations/my?page&size&status | 需登录 | 我的预约 |
| GET | /api/reservations?page&size&status&keyword | ADMIN | 所有预约 |
| GET | /api/reservations/{id} | 需登录 | 预约详情 |

## 7. 停车订单 /api/orders

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/orders/entry | ADMIN | 车辆入场 |
| POST | /api/orders/exit | ADMIN | 车辆出场 |
| GET | /api/orders/my?page&size&status | 需登录 | 我的订单 |
| GET | /api/orders?page&size&status&paymentStatus&keyword | ADMIN | 所有订单 |
| GET | /api/orders/{id} | 需登录 | 订单详情 |
| PUT | /api/orders/{id}/abnormal | ADMIN | 标记异常 |

## 8. 计费规则 /api/billing-rules

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/billing-rules | 需登录 | 所有规则 |
| GET | /api/billing-rules/page?page&size | ADMIN | 分页查询 |
| POST | /api/billing-rules | ADMIN | 新增规则 |
| PUT | /api/billing-rules/{id} | ADMIN | 更新规则 |
| DELETE | /api/billing-rules/{id} | ADMIN | 删除规则 |

## 9. 黑名单 /api/blacklist

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/blacklist?page&size&status&keyword | ADMIN | 分页查询 |
| POST | /api/blacklist | ADMIN | 添加黑名单 |
| PUT | /api/blacklist/{id}/remove | ADMIN | 移除黑名单 |

## 10. 支付 /api/payments

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/payments | 需登录 | 支付订单 |
| GET | /api/payments/my?page&size | 需登录 | 我的支付 |
| GET | /api/payments?page&size&status&keyword | ADMIN | 所有支付 |

## 11. 公告 /api/announcements

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/announcements/published | 公开 | 已发布公告 |
| GET | /api/announcements?page&size&status&keyword | ADMIN | 分页查询 |
| POST | /api/announcements | ADMIN | 新增公告 |
| PUT | /api/announcements/{id} | ADMIN | 更新公告 |
| PUT | /api/announcements/{id}/publish | ADMIN | 发布公告 |
| DELETE | /api/announcements/{id} | ADMIN | 删除公告 |

## 12. 评价 /api/reviews

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/reviews | 需登录 | 提交评价 |
| GET | /api/reviews/my?page&size | 需登录 | 我的评价 |
| GET | /api/reviews?page&size&status | ADMIN | 所有评价 |
| PUT | /api/reviews/{id}/reply | ADMIN | 回复评价 |
| PUT | /api/reviews/{id}/status?status= | ADMIN | 更新状态 |

## 13. 消息 /api/messages

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/messages?page&size&isRead | 需登录 | 我的消息 |
| GET | /api/messages/unread-count | 需登录 | 未读数 |
| PUT | /api/messages/{id}/read | 需登录 | 标记已读 |
| PUT | /api/messages/read-all | 需登录 | 全部已读 |

## 14. 出入日志 /api/entry-exit-logs

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/entry-exit-logs?page&size&plateNumber&logType | ADMIN | 分页查询 |

## 15. 系统设置 /api/settings

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/settings | ADMIN | 所有设置 |
| GET | /api/settings/{key} | ADMIN | 获取设置值 |
| PUT | /api/settings | ADMIN | 批量更新 |

## 16. 审计日志 /api/audit-logs

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/audit-logs?page&size&module&action | ADMIN | 分页查询 |

## 17. 统计 /api/statistics

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/statistics/dashboard | ADMIN | 仪表盘概览 |
| GET | /api/statistics/revenue-trend?days=7 | ADMIN | 营收趋势 |
| GET | /api/statistics/hourly | ADMIN | 时段统计 |

---

## 数据字典

### 车位状态
0-空闲 1-占用 2-预约 3-维护

### 车位类型
STANDARD-普通 VIP-VIP CHARGING-充电 DISABLED-无障碍

### 预约状态
0-待确认 1-已确认 2-已取消 3-已过期 4-已完成

### 订单状态
0-停车中 1-已完成 2-异常

### 支付状态
0-未支付 1-已支付 2-已退款

### 支付方式
WECHAT-微信 ALIPAY-支付宝 CASH-现金 CARD-刷卡

### 公告类型
NOTICE-通知 MAINTENANCE-维护 PROMOTION-推广

### 测试账号
管理员: admin / admin123
操作员: operator1 / admin123
用户: user1~user10 / 123456
