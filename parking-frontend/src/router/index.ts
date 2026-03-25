import { createRouter, createWebHistory } from 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    hideNav?: boolean
    requiresAuth?: boolean
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginPage.vue'),
      meta: { hideNav: true },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterPage.vue'),
      meta: { hideNav: true },
    },
    {
      path: '/parking',
      name: 'ParkingSearch',
      component: () => import('@/views/ParkingSearch.vue'),
    },
    {
      path: '/announcements',
      name: 'Announcements',
      component: () => import('@/views/Announcements.vue'),
    },
    {
      path: '/orders',
      name: 'MyOrders',
      component: () => import('@/views/MyOrders.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/reservations',
      name: 'MyReservations',
      component: () => import('@/views/MyReservations.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/vehicles',
      name: 'MyVehicles',
      component: () => import('@/views/MyVehicles.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/reviews',
      name: 'MyReviews',
      component: () => import('@/views/MyReviews.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/messages',
      name: 'MyMessages',
      component: () => import('@/views/MyMessages.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/profile',
      name: 'UserProfile',
      component: () => import('@/views/UserProfile.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue'),
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

// Navigation guard — redirect unauthenticated users
router.beforeEach((to) => {
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      return { name: 'Login', query: { redirect: to.fullPath } }
    }
  }
})

export default router
