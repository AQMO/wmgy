<template>
  <el-container class="admin-layout">
    <el-aside width="200px">
      <div class="logo">🛠️ 平台管理后台</div>
      <el-menu :default-active="$route.path" router background-color="#1f2d3d" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/admin/dashboard">📈 数据大盘</el-menu-item>
        <el-menu-item index="/admin/merchants">🏪 商家管理</el-menu-item>
        <el-menu-item index="/admin/commission">💹 抽成规则</el-menu-item>
        <el-menu-item index="/admin/config">⚙️ 全局配置</el-menu-item>
        <el-menu-item index="/admin/orders">📦 订单监控</el-menu-item>
        <el-menu-item index="/admin/logistics">🚚 物流预警</el-menu-item>
        <el-menu-item index="/admin/chats">💬 聊天监控</el-menu-item>
        <el-menu-item index="/admin/traffic">📊 流量监控</el-menu-item>
        <el-menu-item index="/admin/notifications">🔔 消息中心</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>万亩果园平台总后台</span>
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
.admin-layout { height: 100vh; }
.logo { color: #fff; font-weight: 700; padding: 18px 16px; background: #16222f; }
.el-aside { background: #1f2d3d; }
.el-aside .el-menu { border-right: none; }
.header { display: flex; justify-content: space-between; align-items: center; background: #fff; border-bottom: 1px solid #eee; }
.user { margin-right: 12px; font-size: 13px; color: #666; }
</style>
