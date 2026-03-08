import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo as getUserInfoApi } from '@/api/auth'

interface UserInfo {
  userId: number
  username: string
  realName: string
  phone: string
  email: string
  role: string
  avatar: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(
    JSON.parse(localStorage.getItem('userInfo') || 'null'),
  )

  const isLoggedIn = computed(() => !!token.value)

  /* Convenience accessors used by views (e.g. UserProfile) */
  const username = computed(() => userInfo.value?.username ?? '')
  const realName = computed(() => userInfo.value?.realName ?? '')
  const phone = computed(() => userInfo.value?.phone ?? '')
  const email = computed(() => userInfo.value?.email ?? '')
  const role = computed(() => userInfo.value?.role ?? '')
  const avatar = computed(() => userInfo.value?.avatar ?? '')

  function setAuth(data: {
    token: string
    userId: number
    username: string
    realName: string
    phone?: string
    email?: string
    role: string
    avatar: string
  }) {
    token.value = data.token
    const info: UserInfo = {
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      phone: data.phone ?? '',
      email: data.email ?? '',
      role: data.role,
      avatar: data.avatar,
    }
    userInfo.value = info
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function updateUserInfo(data: Partial<UserInfo>) {
    if (userInfo.value) {
      Object.assign(userInfo.value, data)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  }

  async function login(usernameVal: string, password: string) {
    const res: any = await loginApi({ username: usernameVal, password })
    setAuth(res.data)
    return res.data
  }

  async function register(formData: {
    username: string
    password: string
    realName?: string
    phone?: string
    email?: string
  }) {
    const res: any = await registerApi(formData)
    setAuth(res.data)
    return res.data
  }

  /** Fetch latest user info from backend and update local state */
  async function getUserInfo() {
    const res: any = await getUserInfoApi()
    const u = res.data
    if (u) {
      const info: UserInfo = {
        userId: u.id ?? u.userId ?? userInfo.value?.userId ?? 0,
        username: u.username ?? '',
        realName: u.realName ?? '',
        phone: u.phone ?? '',
        email: u.email ?? '',
        role: u.role ?? '',
        avatar: u.avatar ?? '',
      }
      userInfo.value = info
      localStorage.setItem('userInfo', JSON.stringify(info))
    }
    return u
  }

  function logout() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    realName,
    phone,
    email,
    role,
    avatar,
    login,
    register,
    logout,
    getUserInfo,
    updateUserInfo,
  }
})
