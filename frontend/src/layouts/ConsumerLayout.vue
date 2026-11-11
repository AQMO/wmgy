<template>
  <div class="consumer-layout">
    <header class="top-bar">
      <div class="brand">🍊 万亩果园</div>
      <div class="right">
        <template v-if="store.isLogin">
          <el-badge :value="unread" :hidden="!unread" class="bell">
            <span class="bell-icon" @click="$router.push('/shop/notifications')">🔔</span>
          </el-badge>
          <span class="welcome">{{ store.user?.nickname }}</span>
          <el-button link type="primary" @click="goByRole">工作台</el-button>
          <el-button link @click="logout">退出</el-button>
        </template>
        <el-button v-else link type="primary" @click="$router.push('/login')">登录</el-button>
      </div>
    </header>
    <main class="page">
      <router-view />
    </main>
    <nav class="tab-bar" v-if="$route.path.startsWith('/shop')">
      <div class="tab" :class="{ active: $route.path === '/shop/home' }" @click="$router.push('/shop/home')">🏠<span>首页</span></div>
      <div class="tab" :class="{ active: $route.path === '/shop/cart' }" @click="$router.push('/shop/cart')">🛒<span>购物车</span></div>
      <div class="tab" :class="{ active: $route.path === '/shop/orders' }" @click="$router.push('/shop/orders')">📦<span>订单</span></div>
      <div class="tab" @click="goMy">👤<span>我的</span></div>
    </nav>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { notificationUnread } from '../api'
import { onNotify, startWs, stopWs } from '../utils/ws'

const router = useRouter()
const store = useUserStore()
const unread = ref(0)
let timer = null

async function loadUnread() {
  if (store.isLogin) {
    try { unread.value = (await notificationUnread()).count } catch (e) { /* 忽略 */ }
  }
}
onMounted(() => {
  loadUnread()
  timer = setInterval(loadUnread, 60000)
  if (store.isLogin) {
    // WebSocket 实时通知推送（轮询兜底）
    startWs()
    offNotify = onNotify(n => {
      unread.value++
      if (n.data && n.data.title) {
        ElMessage({ message: `🔔 ${n.data.title}`, type: 'success', duration: 3000 })
      }
    })
  }
})
let offNotify = null
onBeforeUnmount(() => {
  clearInterval(timer)
  if (offNotify) offNotify()
  stopWs()
})

function goByRole() {
  if (store.role === 'ADMIN') router.push('/admin/dashboard')
  else if (store.role === 'MERCHANT') router.push('/merchant/workbench')
  else router.push('/shop/orders')
}
function goMy() {
  router.push('/shop/mine')
}
function logout() {
  store.logout()
  router.push('/shop/home')
}
</script>

<style scoped>
.consumer-layout { max-width: 760px; margin: 0 auto; min-height: 100vh; background: #f5f6f8; padding-bottom: 60px; }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #fff; position: sticky; top: 0; z-index: 10; }
.brand { font-weight: 700; font-size: 18px; color: #e8611d; }
.right { display: flex; align-items: center; gap: 4px; }
.welcome { font-size: 13px; color: #666; margin-right: 6px; }
.bell { margin-right: 8px; }
.bell-icon { font-size: 16px; cursor: pointer; }
.page { padding: 12px; }
.tab-bar { position: fixed; bottom: 0; left: 50%; transform: translateX(-50%); width: 100%; max-width: 760px; background: #fff; display: flex; border-top: 1px solid #eee; z-index: 10; }
.tab { flex: 1; text-align: center; padding: 8px 0; font-size: 20px; cursor: pointer; color: #999; }
.tab span { display: block; font-size: 11px; }
.tab.active { color: #e8611d; }
</style>
