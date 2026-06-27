import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

const PROTECTED_ROUTES = ['/admin', '/cart', '/orders', '/profile', '/favorites', '/checkout']

function isProtectedRoute(path) {
  return PROTECTED_ROUTES.some(prefix => path.startsWith(prefix))
}

function silentClearAuth() {
  localStorage.clear()
  const { useUserStore } = require('@/stores/user')
  const { useCartStore } = require('@/stores/cart')
  try {
    useUserStore().clearState()
    useCartStore().clearCount()
  } catch (e) {}
}

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

api.interceptors.response.use(
  response => {
    if (response.config.responseType === 'blob') {
      return response.data
    }
    const res = response.data
    if (res.code !== 200) {
      const msg = res.message || '请求失败'
      if (res.code === 401 || res.code === 403) {
        return handleAuthError(res.code, msg)
      }
      ElMessage.error(msg)
      return Promise.reject(new Error(msg))
    }
    return res
  },
  error => {
    if (!error.response) {
      if (error.code === 'ECONNABORTED') {
        ElMessage.error('请求超时，请检查网络后重试')
      } else {
        ElMessage.error('网络连接异常，请检查网络设置')
      }
      return Promise.reject(error)
    }

    const status = error.response.status
    const msg = error.response?.data?.message

    switch (status) {
      case 400:
        ElMessage.error(msg || '请求参数有误，请检查后重试')
        break
      case 401:
      case 403:
        return handleAuthError(status, msg)
      case 404:
        ElMessage.error(msg || '请求的资源不存在')
        break
      case 409:
        ElMessage.error(msg || '数据冲突，请刷新后重试')
        break
      case 429:
        ElMessage.warning('操作过于频繁，请稍后再试')
        break
      case 500:
        ElMessage.error(msg || '服务器内部错误，请稍后重试')
        break
      case 502:
      case 503:
        ElMessage.error('服务暂时不可用，请稍后重试')
        break
      default:
        ElMessage.error(msg || `请求失败 (${status})`)
    }
    return Promise.reject(error)
  }
)

function handleAuthError(code, msg) {
  const currentPath = router.currentRoute.value.path
  const isProtected = isProtectedRoute(currentPath)

  silentClearAuth()

  if (isProtected) {
    if (code === 401) {
      ElMessage.warning('登录已过期，请重新登录')
    } else {
      ElMessage.error(msg || '权限不足，无法访问该资源')
    }
    if (currentPath !== '/login' && currentPath !== '/register' && currentPath !== '/forgot-password') {
      router.push({ path: '/login', query: { redirect: currentPath } })
    }
  }

  return Promise.reject(new Error(msg || 'auth_error'))
}

export default api