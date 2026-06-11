import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const client = axios.create({
  baseURL: 'http://localhost:8088/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

client.interceptors.response.use(
  (response) => {
    const { code, msg, data } = response.data
    if (code === 0) {
      response.data = data
      return response
    }
    return Promise.reject(new Error(msg))
  },
  (error) => {
    if (error.response?.status === 401) {
      const auth = useAuthStore()
      auth.user = null
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    }
    return Promise.reject(error)
  },
)

export default client
