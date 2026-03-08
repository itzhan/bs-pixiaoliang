import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'
import router from '@/router'

interface LoginResult {
  token: string
  userId: number
  username: string
  realName: string
  role: string
  avatar: string
}

interface UserInfo {
  userId: number
  username: string
  realName: string
  role: string
  avatar: string
}

export const useUserStore = defineStore('user', () => {
  /* ---------- state ---------- */
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref<number>(0)
  const username = ref('')
  const realName = ref('')
  const role = ref('')
  const avatar = ref('')

  /* ---------- getters ---------- */
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN')

  /* ---------- helpers ---------- */
  function _setUserInfo(info: UserInfo) {
    userId.value = info.userId
    username.value = info.username
    realName.value = info.realName
    role.value = info.role
    avatar.value = info.avatar
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function _loadFromStorage() {
    const raw = localStorage.getItem('userInfo')
    if (raw) {
      try {
        const info: UserInfo = JSON.parse(raw)
        _setUserInfo(info)
      } catch {
        // ignore
      }
    }
  }

  // Hydrate from localStorage on creation
  _loadFromStorage()

  /* ---------- actions ---------- */
  async function login(usernameVal: string, password: string) {
    const res = (await request.post('/auth/login', {
      username: usernameVal,
      password,
    })) as { code: number; data: LoginResult; message: string }

    const data = res.data
    token.value = data.token
    localStorage.setItem('token', data.token)
    _setUserInfo({
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      role: data.role,
      avatar: data.avatar,
    })
  }

  async function getUserInfo() {
    const res = (await request.get('/auth/info')) as {
      code: number
      data: UserInfo
      message: string
    }
    _setUserInfo(res.data)
  }

  async function logout() {
    try {
      await request.post('/auth/logout')
    } catch {
      // Even if the request fails, proceed with local cleanup
    }
    token.value = ''
    userId.value = 0
    username.value = ''
    realName.value = ''
    role.value = ''
    avatar.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  return {
    token,
    userId,
    username,
    realName,
    role,
    avatar,
    isLoggedIn,
    isAdmin,
    login,
    getUserInfo,
    logout,
  }
})
