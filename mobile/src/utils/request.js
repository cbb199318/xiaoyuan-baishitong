import axios from 'axios'

const baseURL = 'http://localhost:9001/api'

const service = axios.create({
  baseURL,
  timeout: 10000,
})

service.interceptors.request.use((config) => {
  const token = uni.getStorageSync('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload.code !== 0) {
      uni.showToast({ title: payload.message || '请求失败', icon: 'none' })
      if (payload.code === 401) {
        uni.removeStorageSync('token')
        uni.reLaunch({ url: '/pages/login/index' })
      }
      return Promise.reject(payload)
    }
    return payload.data
  },
  (error) => {
    uni.showToast({ title: '网络异常，请稍后再试', icon: 'none' })
    return Promise.reject(error)
  }
)

export default service
