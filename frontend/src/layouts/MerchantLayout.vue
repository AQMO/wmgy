<template>
  <el-container class="merchant-layout">
    <el-aside width="200px">
      <div class="logo">🍊 商家工作台</div>
      <el-menu :default-active="$route.path" router background-color="#2b2f3a" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/merchant/workbench">📊 工作台</el-menu-item>
        <el-menu-item index="/merchant/settings">🏪 店铺设置</el-menu-item>
        <el-menu-item index="/merchant/products">🍎 商品管理</el-menu-item>
        <el-menu-item index="/merchant/orders">📦 订单管理</el-menu-item>
        <el-menu-item index="/merchant/pickups">🌿 采摘预约</el-menu-item>
        <el-menu-item index="/merchant/coupons">🎟️ 优惠券</el-menu-item>
        <el-menu-item index="/merchant/traces">🏷️ 商品溯源</el-menu-item>
        <el-menu-item index="/merchant/chat">💬 买家消息</el-menu-item>
        <el-menu-item index="/merchant/settlement">💰 资金结算</el-menu-item>
        <el-menu-item index="/merchant/notifications">🔔 消息中心</el-menu-item>
        <el-menu-item index="/shop/home">🛍️ 前台首页</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>万亩果园商家端</span>
        <div>
          <span class="user">{{ store.user?.nickname }}</span>
          <el-button link @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()
function logout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.merchant-layout { height: 100vh; }
.logo { color: #fff; font-weight: 700; padding: 18px 16px; background: #232733; }
.el-aside { background: #2b2f3a; }
.el-aside .el-menu { border-right: none; }
.header { display: flex; justify-content: space-between; align-items: center; background: #fff; border-bottom: 1px solid #eee; }
.user { margin-right: 12px; font-size: 13px; color: #666; }
</style>
