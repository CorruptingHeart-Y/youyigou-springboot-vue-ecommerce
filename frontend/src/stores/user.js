import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '../api/endpoints'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')
  const avatar = ref(localStorage.getItem('avatar') || '')

  const isLoggedIn = () => !!token.value
  const isAdmin = () => role.value === 'ADMIN' || role.value === 'SUPER_ADMIN'

  const login = async (data) => {
    const res = await authApi.login(data)
    const d = res.data
    token.value = d.token
    userId.value = d.userId
    username.value = d.username
    role.value = d.role
    nickname.value = d.nickname || d.username
    avatar.value = d.avatar || ''
    localStorage.setItem('token', d.token)
    localStorage.setItem('userId', d.userId)
    localStorage.setItem('username', d.username)
    localStorage.setItem('role', d.role)
    localStorage.setItem('nickname', d.nickname || d.username)
    localStorage.setItem('avatar', d.avatar || '')
  }

  const logout = async () => {
    try {
      await authApi.logout()
    } catch (e) {
    }
    token.value = ''
    userId.value = ''
    username.value = ''
    role.value = ''
    nickname.value = ''
    avatar.value = ''
    localStorage.clear()
  }

  const updateAvatar = (url) => {
    avatar.value = url
    localStorage.setItem('avatar', url)
  }

  const clearState = () => {
    token.value = ''
    userId.value = ''
    username.value = ''
    role.value = ''
    nickname.value = ''
    avatar.value = ''
    localStorage.clear()
  }

  return { token, userId, username, role, nickname, avatar, isLoggedIn, isAdmin, login, logout, updateAvatar, clearState }
})
