import { request, isLoggedIn } from './request'

// 认证
export const apiLogin = d => request('/auth/login', 'POST', d)
export const apiWxLogin = d => request('/auth/wxlogin', 'POST', d)
export const apiRegister = d => request('/auth/register', 'POST', d)

// 果园/商品
export const apiStores = p => request('/stores?keyword=' + encodeURIComponent(p?.keyword || ''))
export const apiStoreDetail = id => request('/stores/' + id)
export const apiProducts = p => request(`/products?storeId=${p.storeId}${p.categoryId ? '&categoryId=' + p.categoryId : ''}`)
export const apiProductDetail = id => request('/products/' + id)

// 购物车
export const apiCartList = () => request('/cart')
export const apiCartAdd = d => request('/cart', 'POST', d)
export const apiCartUpdate = (id, quantity) => request('/cart/' + id, 'PUT', { quantity })
export const apiCartRemove = id => request('/cart/' + id, 'DELETE')

// 订单
export const apiCreateOrder = d => request('/orders', 'POST', d)
export const apiMyOrders = p => request('/orders?status=' + (p?.status || 'ALL'))
export const apiOrderDetail = id => request('/orders/' + id)
export const apiPayOrder = id => request('/orders/' + id + '/pay', 'PUT')
export const apiCancelOrder = id => request('/orders/' + id + '/cancel', 'PUT')
export const apiConfirmOrder = id => request('/orders/' + id + '/confirm', 'PUT')

// 聊天
export const apiChatHistory = key => request('/chat/messages?conversationKey=' + key)
export const apiChatSend = d => request('/chat/send', 'POST', d)

// 消息通知
export const apiNotifications = () => request('/notifications?page=0&size=30')
export const apiUnreadCount = () => request('/notifications/unread-count')
export const apiReadAll = () => request('/notifications/read-all', 'PUT')
