import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  { path: '/', redirect: '/shop/home' },
  { path: '/login', component: () => import('../views/auth/Login.vue') },
  { path: '/register', component: () => import('../views/auth/Register.vue') },
  // 消费者端
  {
    path: '/shop',
    component: () => import('../layouts/ConsumerLayout.vue'),
    children: [
      { path: 'home', component: () => import('../views/consumer/Home.vue') },
      { path: 'store/:id', component: () => import('../views/consumer/StoreDetail.vue') },
      { path: 'product/:id', component: () => import('../views/consumer/ProductDetail.vue') },
      { path: 'cart', component: () => import('../views/consumer/Cart.vue'), meta: { auth: true } },
      { path: 'confirm', component: () => import('../views/consumer/ConfirmOrder.vue'), meta: { auth: true } },
      { path: 'orders', component: () => import('../views/consumer/OrderList.vue'), meta: { auth: true } },
      { path: 'order/:id', component: () => import('../views/consumer/OrderDetail.vue'), meta: { auth: true } },
      { path: 'chat', component: () => import('../views/consumer/Chat.vue'), meta: { auth: true } },
      { path: 'notifications', component: () => import('../views/common/Notifications.vue'), meta: { auth: true } },
      { path: 'mine', component: () => import('../views/consumer/Mine.vue'), meta: { auth: true } },
      { path: 'coupons', component: () => import('../views/consumer/Coupons.vue'), meta: { auth: true } },
      { path: 'points', component: () => import('../views/consumer/Points.vue'), meta: { auth: true } },
      { path: 'appointments', component: () => import('../views/consumer/Appointments.vue'), meta: { auth: true } },
      { path: 'trace/:id', component: () => import('../views/consumer/Trace.vue') }
    ]
  },
  // 商家端
  {
    path: '/merchant',
    component: () => import('../layouts/MerchantLayout.vue'),
    meta: { role: 'MERCHANT' },
    children: [
      { path: 'apply', component: () => import('../views/merchant/ApplyStore.vue') },
      { path: 'workbench', component: () => import('../views/merchant/Workbench.vue') },
      { path: 'settings', component: () => import('../views/merchant/StoreSettings.vue') },
      { path: 'products', component: () => import('../views/merchant/ProductManage.vue') },
      { path: 'orders', component: () => import('../views/merchant/OrderManage.vue') },
      { path: 'settlement', component: () => import('../views/merchant/Settlement.vue') },
      { path: 'chat', component: () => import('../views/merchant/Chat.vue') },
      { path: 'notifications', component: () => import('../views/common/Notifications.vue') },
      { path: 'coupons', component: () => import('../views/merchant/CouponManage.vue') },
      { path: 'traces', component: () => import('../views/merchant/TraceManage.vue') },
      { path: 'pickups', component: () => import('../views/merchant/PickupManage.vue') }
    ]
  },
  // 平台后台
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { role: 'ADMIN' },
    children: [
      { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'merchants', component: () => import('../views/admin/MerchantManage.vue') },
      { path: 'commission', component: () => import('../views/admin/CommissionRule.vue') },
      { path: 'config', component: () => import('../views/admin/ConfigManage.vue') },
      { path: 'orders', component: () => import('../views/admin/OrderMonitor.vue') },
      { path: 'chats', component: () => import('../views/admin/ChatMonitor.vue') },
      { path: 'traffic', component: () => import('../views/admin/TrafficMonitor.vue') },
      { path: 'logistics', component: () => import('../views/admin/LogisticsAlert.vue') },
      { path: 'notifications', component: () => import('../views/common/Notifications.vue') }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(to => {
  const store = useUserStore()
  if (to.meta.auth && !store.isLogin) {
    ElMessage.warning('请先登录')
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path.startsWith('/merchant') && store.isLogin && store.role !== 'MERCHANT') {
    // 未开店的普通用户进商家端走入驻页；非商家角色拦截
    if (store.role === 'USER') {
      return to.path === '/merchant/apply' ? true : '/merchant/apply'
    }
    ElMessage.warning('无权限访问')
    return '/shop/home'
  }
  if (to.path.startsWith('/admin') && store.role !== 'ADMIN') {
    ElMessage.warning('请使用管理员账号登录')
    return '/login'
  }
  return true
})

export default router
