import axios from 'axios'
import { ElMessage } from 'element-plus'

export const API_BASE_URL = window.libraryDesktop?.apiBaseUrl
  || import.meta.env.VITE_API_BASE_URL
  || 'http://localhost:8080/api'

const isLocalApi = /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?\//.test(API_BASE_URL)

const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: isLocalApi ? 10000 : 45000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('library_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && typeof payload.success === 'boolean') {
      if (!payload.success) {
        const message = payload.message || '请求失败'
        ElMessage.error(message)
        throw new Error(message)
      }
      return payload.data
    }
    return payload
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
