// 单果园上下文：一个小程序实例只服务一个果园
// storeId 来源优先级：扫码进入参数 > 本地缓存 > 默认配置
const STORE_KEY = 'orchardStoreId'
const CACHE_KEY = 'orchardStoreInfo'

/** 默认果园（开发/单园部署时写死；平台化后可改为扫码带入） */
export const DEFAULT_STORE_ID = 1

export function getStoreId() {
  const v = uni.getStorageSync(STORE_KEY)
  return v ? Number(v) : DEFAULT_STORE_ID
}

export function setStoreId(id) {
  const n = Number(id)
  if (n > 0) uni.setStorageSync(STORE_KEY, n)
}

/** 缓存果园信息，避免每页重复请求 */
export function cacheStore(store) {
  if (store && store.id) uni.setStorageSync(CACHE_KEY, store)
}

export function getCachedStore() {
  return uni.getStorageSync(CACHE_KEY) || null
}

/** 果园展示名，未加载完成时用兜底名 */
export function storeNameOf(store) {
  return (store && store.name) || '万亩果园'
}
