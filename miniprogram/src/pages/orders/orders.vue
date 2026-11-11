<template>
  <view class="page">
    <scroll-view scroll-x class="tabs">
      <view v-for="t in tabs" :key="t.v" class="tab" :class="{ active: cur === t.v }" @click="switchTab(t.v)">{{ t.label }}</view>
    </scroll-view>

    <view v-for="o in orders" :key="o.id" class="card" @click="goDetail(o.id)">
      <view class="row1">
        <text class="no">订单 {{ o.orderNo }}</text>
        <text class="status" :class="o.status">{{ statusText(o.status) }}</text>
      </view>
      <view class="row2">
        <text class="amount">¥{{ o.payAmount }}</text>
        <text class="time">{{ fmt(o.createdAt) }}</text>
      </view>
      <view class="btns">
        <button v-if="o.status === 'UNPAID'" class="mini danger" size="mini" @click.stop="doPay(o)">去支付</button>
        <button v-if="o.status === 'SHIPPED'" class="mini primary" size="mini" @click.stop="doConfirm(o)">确认收货</button>
        <button v-if="o.status === 'COMPLETED'" class="mini ghost" size="mini" @click.stop="goDetail(o.id)">再来一单</button>
      </view>
    </view>
    <view v-if="orders.length === 0" class="empty">还没有订单，去本园逛逛吧</view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { apiMyOrders, apiPayOrder, apiConfirmOrder } from '../../utils/api'

const tabs = [
  { v: 'ALL', label: '全部' }, { v: 'UNPAID', label: '待付款' }, { v: 'UNSHIPPED', label: '待发货' },
  { v: 'SHIPPED', label: '待收货' }, { v: 'COMPLETED', label: '已完成' }, { v: 'CANCELLED', label: '已取消' }
]
const cur = ref('ALL')
const orders = ref([])

const statusText = s => ({ UNPAID: '待付款', UNSHIPPED: '待发货', SHIPPED: '待收货', COMPLETED: '已完成', CANCELLED: '已取消', REFUNDED: '已退款' }[s] || s)
const fmt = t => (t ? String(t).replace('T', ' ').slice(0, 16) : '')

async function load() {
  if (!uni.getStorageSync('token')) return
  const page = await apiMyOrders({ status: cur.value })
  orders.value = page.content || []
}
function switchTab(v) { cur.value = v; load() }
function goDetail(id) { uni.navigateTo({ url: '/pages/order-detail/order-detail?id=' + id }) }
async function doPay(o) { await apiPayOrder(o.id); uni.showToast({ title: '支付成功' }); load() }
function doConfirm(o) {
  uni.showModal({
    title: '提示',
    content: '确认已收到货？',
    success: async r => {
      if (!r.confirm) return
      await apiConfirmOrder(o.id)
      uni.showToast({ title: '已确认收货' })
      load()
    }
  })
}
onShow(() => {
  const preset = uni.getStorageSync('orderTab')
  if (preset) { cur.value = preset; uni.removeStorageSync('orderTab') }
  load()
})
</script>

<style scoped>
.tabs { white-space: nowrap; padding: 8rpx 24rpx; }
.tab { display: inline-block; background: #fff; border-radius: 30rpx; padding: 10rpx 26rpx; margin-right: 12rpx; color: #666; font-size: 26rpx; }
.tab.active { background: #3b6d11; color: #fff; }
.row1 { display: flex; justify-content: space-between; align-items: center; }
.no { font-size: 22rpx; color: #b4b2a9; }
.status { font-size: 24rpx; color: #5f5e5a; }
.status.UNPAID { color: #d85a30; }
.status.SHIPPED { color: #639922; }
.row2 { display: flex; justify-content: space-between; align-items: baseline; margin: 14rpx 0 12rpx; }
.amount { font-size: 32rpx; font-weight: 700; color: #2c2c2a; }
.time { font-size: 22rpx; color: #b4b2a9; }
.btns { display: flex; justify-content: flex-end; gap: 12rpx; }
.mini { border-radius: 30rpx; font-size: 22rpx; }
.danger { background: #d85a30; color: #fff; }
.primary { border: 1rpx solid #639922; color: #3b6d11; background: #fff; }
.ghost { border: 1rpx solid #e0ded6; color: #5f5e5a; background: #fff; }
.empty { text-align: center; color: #999; padding: 120rpx 0; font-size: 26rpx; }
</style>
