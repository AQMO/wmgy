<template>
  <view class="page">
    <view class="card" v-if="d.order">
      <view class="head">
        <text class="status">{{ statusText(d.order.status) }}</text>
        <text class="no">{{ d.order.orderNo }}</text>
      </view>
      <view class="kv"><text class="k">果园</text><text>{{ d.storeName }}</text></view>
      <view class="kv"><text class="k">收货信息</text><text>{{ d.order.receiverName }} {{ d.order.receiverPhone }}\n{{ d.order.receiverAddress }}</text></view>
      <view class="kv">
        <text class="k">物流信息</text>
        <text v-if="d.order.expressNo" @click="copyNo(d.order.expressNo)">{{ d.order.expressCompany }} {{ d.order.expressNo }}（点击复制）</text>
        <text v-else class="muted">暂未发货</text>
      </view>
      <view class="kv"><text class="k">实付金额</text><text class="price">¥{{ d.order.payAmount }}</text></view>
      <view class="items">
        <view v-for="it in d.items" :key="it.id" class="item">{{ it.productName }} x{{ it.quantity }} = ¥{{ (it.price * it.quantity).toFixed(2) }}</view>
      </view>
      <view class="btns">
        <button class="mini" @click="goChat">联系园主</button>
        <button v-if="d.order.status === 'UNPAID'" class="mini danger" @click="doPay">去支付</button>
        <button v-if="d.order.status === 'SHIPPED'" class="mini danger" @click="doConfirm">确认收货</button>
      </view>
    </view>
    <view class="card" v-if="d.logs && d.logs.length">
      <view class="sec-title">订单日志</view>
      <view v-for="l in d.logs" :key="l.id" class="log">{{ fmt(l.createdAt) }} · {{ l.action }}：{{ l.detail }}</view>
    </view>
  </view>
</template>

<script setup>
import { onLoad, onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { apiOrderDetail, apiPayOrder, apiConfirmOrder } from '../../utils/api'

const d = ref({})
const id = ref(null)

const statusText = s => ({ UNPAID: '待付款', UNSHIPPED: '待发货', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s)
const fmt = t => (t ? String(t).replace('T', ' ').slice(0, 16) : '')

function copyNo(no) {
  uni.setClipboardData({ data: no, success: () => uni.showToast({ title: '单号已复制' }) })
}
function goChat() { uni.navigateTo({ url: '/pages/chat/chat?storeId=' + d.value.order.storeId }) }
async function doPay() { await apiPayOrder(id.value); uni.showToast({ title: '支付成功' }); load() }
async function doConfirm() { await apiConfirmOrder(id.value); uni.showToast({ title: '已确认收货' }); load() }
async function load() { d.value = await apiOrderDetail(id.value) }
onLoad(q => { id.value = q.id })
onShow(() => { if (id.value) load() })
</script>

<style scoped>
.head { display: flex; justify-content: space-between; margin-bottom: 16rpx; }
.status { color: #3b6d11; font-weight: 700; }
.no { color: #bbb; font-size: 22rpx; }
.kv { display: flex; gap: 16rpx; padding: 10rpx 0; font-size: 26rpx; line-height: 1.6; }
.k { color: #999; width: 130rpx; flex-shrink: 0; }
.muted { color: #bbb; }
.items { border-top: 1rpx dashed #eee; margin-top: 12rpx; padding-top: 12rpx; }
.item { font-size: 26rpx; color: #555; padding: 6rpx 0; }
.btns { display: flex; justify-content: flex-end; gap: 14rpx; margin-top: 20rpx; }
.mini { border-radius: 30rpx; font-size: 24rpx; background: #fff; color: #5f5e5a; border: 1rpx solid #e0ded6; }
.danger { background: #3b6d11; color: #fff; }
.log { font-size: 24rpx; color: #888; padding: 8rpx 0; border-bottom: 1rpx dashed #f0f0f0; }
</style>
