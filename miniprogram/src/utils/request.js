// 统一请求封装：uni.request + JWT Token
const BASE_URL = 'http://localhost:8080/api'   // 开发者工具需勾选「不校验合法域名」；生产替换为 https 域名

export function request(url, method = 'GET', data = null) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: { Authorization: 'Bearer ' + (uni.getStorageSync('token') || '') },
      success: res => {
        const r = res.data
        if (r.code === 0) {
          resolve(r.data)
        } else if (r.code === 40002) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('user')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(r)
        } else {
          uni.showToast({ title: r.msg || '请求失败', icon: 'none' })
          reject(r)
        }
      },
      fail: err => {
        uni.showToast({ title: '网络异常，请检查后端服务', icon: 'none' })
        reject(err)
      }
    })
  })
}

export function isLoggedIn() {
  return !!uni.getStorageSync('token')
}

export function currentUser() {
  return uni.getStorageSync('user') || null
}

/** 果园访问埋点（流量监控，静默失败） */
export function visit(storeId, page) {
  request('/stats/visit', 'POST', { storeId, page }).catch(() => {})
}
