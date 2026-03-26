import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '数据概览', roles: ['ADMIN'] },
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement.vue'),
        meta: { title: '用户列表', roles: ['ADMIN'] },
      },
      {
        path: 'vehicles',
        name: 'VehicleManagement',
        component: () => import('@/views/VehicleManagement.vue'),
        meta: { title: '车辆管理', roles: ['ADMIN'] },
      },
      {
        path: 'parking-areas',
        name: 'ParkingAreaManagement',
        component: () => import('@/views/ParkingAreaManagement.vue'),
        meta: { title: '停车区域', roles: ['ADMIN'] },
      },
      {
        path: 'parking-spaces',
        name: 'ParkingSpaceManagement',
        component: () => import('@/views/ParkingSpaceManagement.vue'),
        meta: { title: '车位管理' },
      },
      {
        path: 'reservations',
        name: 'ReservationManagement',
        component: () => import('@/views/ReservationManagement.vue'),
        meta: { title: '预约管理' },
      },
      {
        path: 'orders',
        name: 'OrderManagement',
        component: () => import('@/views/OrderManagement.vue'),
        meta: { title: '停车订单' },
      },
      {
        path: 'billing-rules',
        name: 'BillingRuleManagement',
        component: () => import('@/views/BillingRuleManagement.vue'),
        meta: { title: '计费规则', roles: ['ADMIN'] },
      },
      {
        path: 'blacklist',
        name: 'BlacklistManagement',
        component: () => import('@/views/BlacklistManagement.vue'),
        meta: { title: '黑名单' },
      },
      {
        path: 'payments',
        name: 'PaymentManagement',
        component: () => import('@/views/PaymentManagement.vue'),
        meta: { title: '支付记录', roles: ['ADMIN'] },
      },
      {
        path: 'announcements',
        name: 'AnnouncementManagement',
        component: () => import('@/views/AnnouncementManagement.vue'),
        meta: { title: '公告管理', roles: ['ADMIN'] },
      },
      {
        path: 'reviews',
        name: 'ReviewManagement',
        component: () => import('@/views/ReviewManagement.vue'),
        meta: { title: '评价管理' },
      },
      {
        path: 'entry-exit-logs',
        name: 'EntryExitLogView',
        component: () => import('@/views/EntryExitLogView.vue'),
        meta: { title: '出入日志' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404', requiresAuth: false },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guard
router.beforeEach((to, _from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || '智慧停车管理系统'} - 智慧停车管理系统`

  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/login' && token) {
    next({ path: '/' })
  } else if (requiresAuth && token) {
    // 角色权限检查：如果路由配置了 roles，检查当前用户角色是否在允许列表中
    const roles = to.meta.roles as string[] | undefined
    const raw = localStorage.getItem('userInfo')
    const userRole = raw ? JSON.parse(raw).role : ''
    if (roles && roles.length > 0 && !roles.includes(userRole)) {
      // 无权限，重定向到操作员首页
      next({ path: '/orders', replace: true })
    } else {
      next()
    }
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
