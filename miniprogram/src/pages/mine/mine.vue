<template>
  <view class="page">
    <view class="hero">
      <view class="avatar">{{ (user?.nickname || '客').slice(0, 1) }}</view>
      <view class="uinfo">
        <view class="nick">{{ user?.nickname || '未登录' }}</view>
        <view class="sub">{{ user ? '本园老客' : '登录后可下单、查物流' }}</view>
      </view>
      <view class="setting" @click="go('/pages/store/store')">关于果园 ›</view>
    </view>

    <view class="card order-card">
      <view class="oline">
        <text class="otitle">我的订单</text>
        <text class="omore" @click="goOrders('ALL')">全部订单 ›</text>
      </view>
      <view class="grid">
        <view class="g" v-for="g in grid" :key="g.v" @click="goOrders(g.v)">
          <view class="gic" :class="g.cls"></view>
          <text>{{ g.label }}</text>
        </view>
      </view>
    </view>

    <view class="card list">
      <view class="row" @click="go('/pages/notifications/notifications')"><text>消息中心</text><text class="arw">›</text></view>
      <view class="row" @click="goChat"><text>联系园主</text><text class="arw">›</text></view>
      <view class="row" @click="goTab('/pages/cart/cart')"><text>购物车</text><text class="arw">›</text></view>
    </view>

    <view class="card list">
      <view class="row" @click="go('/pages/store/store')"><text>果园溯源 · 关于本园</text><text class="arw">›</text></view>
      <view class="row" @click="go('/pages/login/login')" v-if="!user"><text>登录 / 注册</text><text class="arw">›</text></view>
      <view class="row" v-else @click="logout"><text class="danger">退出登录</text><text class="arw">›</text></view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { currentUser, isLoggedIn } from '../../utils/request'
import { getStoreId } from '../../utils/store'

const user = ref(null)
const grid = [
  { v: 'UNPAID', label: '待付款', cls: 'y' },
  { v: 'UNSHIPPED', label: '待发货', cls: 'y' },
  { v: 'SHIPPED', label: '待收货', cls: 'g' },
  { v: 'COMPLETED', label: '已完成', cls: 'g' }
]

function go(url) { uni.navigateTo({ url }) }
function goTab(url) { uni.switchTab({ url }) }
function goChat() { uni.navigateTo({ url: '/pages/chat/chat?storeId=' + getStoreId() }) }
function goOrders(status) {
  uni.setStorageSync('orderTab', status)
  uni.switchTab({ url: '/pages/orders/orders' })
}
function logout() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('user')
  user.value = null
  uni.showToast({ title: '已退出' })
}
onShow(() => { user.value = isLoggedIn() ? currentUser() : null })
</script>

<style scoped>
.hero { background: #3b6d11; color: #fff; padding: 40rpx 24rpx 70rpx; display: flex; align-items: center; gap: 20rpx; }
.avatar { width: 110rpx; height: 110rpx; border-radius: 50%; background: #c0dd97; color: #27500a; font-size: 44rpx; display: flex; align-items: center; justify-content: center; }
.uinfo { flex: 1; }
.nick { font-size: 32rpx; font-weight: 700; }
.sub { font-size: 22rpx; color: #c0dd97; margin-top: 8rpx; }
.setting { font-size: 22rpx; color: #c0dd97; }
.order-card { margin-top: -50rpx; }
.oline { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18rpx; }
.otitle { font-size: 26rpx; font-weight: 600; color: #2c2c2a; }
.omore { font-size: 22rpx; color: #888780; }
.grid { display: flex; justify-content: space-between; }
.g { display: flex; flex-direction: column; align-items: center; gap: 8rpx; font-size: 22rpx; color: #5f5e5a; }
.gic { width: 48rpx; height: 48rpx; border-radius: 12rpx; background: #eaf3de; }
.gic.y { background: #faeeda; }
.gic.g { background: #eaf3de; }
.list { padding: 0 24rpx; }
.row { display: flex; justify-content: space-between; align-items: center; padding: 28rpx 0; border-bottom: 1rpx solid #f3f1ea; font-size: 27rpx; color: #2c2c2a; }
.row:last-child { border-bottom: none; }
.arw { color: #c0bfb8; font-size: 24rpx; }
.danger { color: #d85a30; }
</style>
