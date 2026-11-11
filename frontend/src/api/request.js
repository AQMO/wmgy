import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({ baseURL: '/api', timeout: 15000 })

// 请求拦截：自动携带 Token
request.interceptors.request.use(cfg => {
  const token = localStorage.getItem('token')
  if (token) cfg.headers.Authorization = 'Bearer ' + token
  return cfg
})

// 响应拦截：统一错误处理
request.interceptors.response.use(
  resp => {
    const r = resp.data
    if (r.code !== 0) {
      if (r.code === 40002) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        router.push('/login')
      }
      ElMessage.error(r.msg || '请求失败')
      return Promise.reject(new Error(r.msg))
    }
    return r.data
  },
  err => {
    ElMessage.error('网络异常，请稍后重试')
    return Promise.reject(err)
  }
)

export default request
