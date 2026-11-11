import request from './request'

// ---------- 认证 ----------
export const login = data => request.post('/auth/login', data)
export const register = data => request.post('/auth/register', data)
export const me = () => request.get('/auth/me')

// ---------- 店铺 ----------
export const listStores = params => request.get('/stores', { params })
export const storeDetail = id => request.get(`/stores/${id}`)
export const myStore = () => request.get('/merchant/store')
export const applyStore = data => request.post('/merchant/store/apply', data)
export const updateStore = data => request.put('/merchant/store', data)
export const myCategories = () => request.get('/merchant/categories')
export const addCategory = data => request.post('/merchant/categories', data)
export const deleteCategory = id => request.delete(`/merchant/categories/${id}`)

// ---------- 商品 ----------
export const listProducts = params => request.get('/products', { params })
export const productDetail = id => request.get(`/products/${id}`)
export const myProducts = params => request.get('/merchant/products', { params })
export const createProduct = data => request.post('/merchant/products', data)
export const updateProduct = (id, data) => request.put(`/merchant/products/${id}`, data)
export const changeProductStatus = (id, status) => request.put(`/merchant/products/${id}/status`, { status })
export const deleteProduct = id => request.delete(`/merchant/products/${id}`)

// ---------- 购物车 ----------
export const cartList = () => request.get('/cart')
export const cartAdd = data => request.post('/cart', data)
export const cartUpdate = (id, quantity) => request.put(`/cart/${id}`, { quantity })
export const cartRemove = id => request.delete(`/cart/${id}`)

// ---------- 订单 ----------
export const createOrder = data => request.post('/orders', data)
export const myOrders = params => request.get('/orders', { params })
export const orderDetail = id => request.get(`/orders/${id}`)
export const payOrder = id => request.put(`/orders/${id}/pay`)
export const cancelOrder = id => request.put(`/orders/${id}/cancel`)
export const confirmOrder = id => request.put(`/orders/${id}/confirm`)

// ---------- 商家 ----------
export const merchantOrders = params => request.get('/merchant/orders', { params })
export const shipOrder = (id, data) => request.put(`/merchant/orders/${id}/ship`, data)
export const updateExpress = (id, data) => request.put(`/merchant/orders/${id}/express`, data)
export const workbench = () => request.get('/merchant/workbench')
export const settlement = params => request.get('/merchant/settlement', { params })
export const withdraw = data => request.post('/merchant/withdraw', data)

// ---------- 平台后台 ----------
export const adminStores = params => request.get('/admin/stores', { params })
export const auditStore = (id, action) => request.put(`/admin/stores/${id}/audit`, { action })
export const setDeposit = (id, deposit) => request.put(`/admin/stores/${id}/deposit`, { deposit })
export const currentRule = () => request.get('/admin/commission-rule')
export const saveRule = data => request.put('/admin/commission-rule', data)
export const adminConfigs = () => request.get('/admin/config')
export const setConfig = data => request.put('/admin/config', data)
export const adminOrders = params => request.get('/admin/orders', { params })
export const dashboard = () => request.get('/admin/dashboard')

// ---------- 聊天 ----------
export const chatConversations = () => request.get('/chat/conversations')
export const chatMessages = params => request.get('/chat/messages', { params })
export const chatSend = data => request.post('/chat/send', data)
export const adminChats = () => request.get('/admin/chats')
export const adminChatMessages = params => request.get('/admin/chats/messages', { params })

// ---------- 消息通知 ----------
export const notificationList = params => request.get('/notifications', { params })
export const notificationUnread = () => request.get('/notifications/unread-count')
export const notificationRead = id => request.put(`/notifications/${id}/read`)
export const notificationReadAll = () => request.put('/notifications/read-all')

// ---------- 流量统计 ----------
export const visitLog = data => request.post('/stats/visit', data)
export const merchantTraffic = () => request.get('/merchant/traffic')
export const adminTraffic = () => request.get('/admin/traffic')

// ---------- 文件（MinIO/本地，storage.type 可切换） ----------
export const uploadFile = (file, biz) => {
  const fd = new FormData()
  fd.append('file', file)
  if (biz) fd.append('biz', biz)
  return request.post('/files/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export const fileList = params => request.get('/files', { params })
export const fileDelete = id => request.delete(`/files/${id}`)

// ---------- 采摘预约 ----------
export const pickupCreate = data => request.post('/pickup', data)
export const pickupMine = params => request.get('/pickup/mine', { params })
export const pickupStore = params => request.get('/pickup/store', { params })
export const pickupStats = () => request.get('/pickup/stats')
export const pickupHandle = (id, data) => request.put(`/pickup/${id}/handle`, data)
export const pickupCancel = id => request.put(`/pickup/${id}/cancel`)

// ---------- 优惠券 ----------
export const couponCreate = data => request.post('/merchant/coupons', data)
export const couponMerchantList = () => request.get('/merchant/coupons')
export const couponToggle = (id, status) => request.put(`/merchant/coupons/${id}/status`, { status })
export const couponStoreList = storeId => request.get(`/coupons/store/${storeId}`)
export const couponClaim = id => request.post(`/coupons/${id}/claim`)
export const couponMine = status => request.get('/coupons/mine', { params: { status } })

// ---------- 溯源 ----------
export const tracePublic = productId => request.get(`/trace/product/${productId}`)
export const traceAdd = data => request.post('/merchant/traces', data)
export const traceDelete = id => request.delete(`/merchant/traces/${id}`)
export const traceByProduct = productId => request.get('/merchant/traces', { params: { productId } })

// ---------- 会员积分 ----------
export const pointsMine = params => request.get('/points/mine', { params })

// ---------- 电子面单 & 物流预警 ----------
export const ewbShip = (id, company) => request.post(`/merchant/orders/${id}/ewb`, { expressCompany: company })
export const overdueOrders = hours => request.get('/merchant/orders/overdue', { params: { hours } })
export const adminOverdue = hours => request.get('/admin/orders/overdue', { params: { hours } })
