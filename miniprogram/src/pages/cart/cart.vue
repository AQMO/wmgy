<template>
  <view class="page">
    <view class="tip" v-if="items.length">
      <text v-if="total >= FREE_LIMIT">已满 ¥{{ FREE_LIMIT }}，本单包顺丰</text>
      <text v-else>满 ¥{{ FREE_LIMIT }} 包顺丰，还差 ¥{{ (FREE_LIMIT - total).toFixed(2) }}</text>
    </view>

    <view class="card" v-for="c in items" :key="c.id">
      <view class="item">
        <view class="img"><view class="fruit"></view></view>
        <view class="info">
          <view class="name">{{ c.name }}</view>
          <view class="meta">{{ c.spec || '本园现摘' }}</view>
          <view class="price">¥{{ c.price }}</view>
        </view>
        <view class="ops">
          <text class="minus" @click="change(c, c.quantity - 1)">-</text>
          <text class="qty">{{ c.quantity }}</text>
          <text class="plus" @click="change(c, c.quantity + 1)">+</text>
        </view>
      </view>
      <view class="del" @click="remove(c)">移除</view>
    </view>

    <view v-if="items.length === 0" class="empty">
      <view class="fruit big"></view>
      <view>购物车空空如也，去逛逛本园吧</view>
    </view>

    <view class="checkout" v-if="items.length">
      <view class="sum">合计：<text class="total">¥{{ total.toFixed(2) }}</text></view>
      <button class="btn-primary" size="mini" @click="checkout">去结算({{ count }})</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { apiCartList, apiCartUpdate, apiCartRemove } from '../../utils/api'

const FREE_LIMIT = 99
const items = ref([])
const total = computed(() => items.value.reduce((s, c) => s + (c.price || 0) * c.quantity, 0))
const count = computed(() => items.value.reduce((s, c) => s + c.quantity, 0))

async function load() {
  if (!uni.getStorageSync('token')) return
  items.value = (await apiCartList()) || []
}
async function change(c, q) {
  if (q < 1) return remove(c)
  await apiCartUpdate(c.id, Math.min(q, c.stock || 99))
  load()
}
async function remove(c) {
  await apiCartRemove(c.id)
  load()
}
function checkout() {
  const sel = items.value.map(c => ({ productId: c.productId, quantity: c.quantity }))
  uni.navigateTo({ url: '/pages/confirm/confirm?items=' + encodeURIComponent(JSON.stringify(sel)) })
}
onShow(load)
</script>

<style scoped>
.tip { margin: 20rpx 24rpx 0; padding: 12rpx 20rpx; background: #eaf3de; color: #3b6d11; font-size: 22rpx; border-radius: 12rpx; }
.item { display: flex; align-items: center; gap: 20rpx; }
.img { width: 110rpx; height: 110rpx; border-radius: 14rpx; background: #eaf3de; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.fruit { width: 56rpx; height: 56rpx; border-radius: 50%; background: #97c459; }
.fruit.big { width: 100rpx; height: 100rpx; margin-bottom: 20rpx; }
.info { flex: 1; min-width: 0; }
.name { font-size: 26rpx; color: #2c2c2a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.meta { font-size: 22rpx; color: #b4b2a9; margin-top: 6rpx; }
.price { color: #d85a30; font-weight: 600; margin-top: 6rpx; }
.ops { display: flex; align-items: center; gap: 12rpx; }
.minus, .plus { width: 44rpx; height: 44rpx; line-height: 42rpx; text-align: center; border: 1rpx solid #e0ded6; border-radius: 8rpx; font-size: 26rpx; }
.qty { min-width: 44rpx; text-align: center; font-size: 26rpx; }
.del { text-align: right; font-size: 22rpx; color: #b4b2a9; margin-top: 12rpx; }
.checkout { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; border-top: 1rpx solid #f0eee7; display: flex; justify-content: space-between; align-items: center; padding: 20rpx 30rpx; }
.sum { font-size: 26rpx; color: #5f5e5a; }
.total { color: #d85a30; font-size: 34rpx; font-weight: 700; }
.empty { text-align: center; color: #999; padding: 140rpx 0; display: flex; flex-direction: column; align-items: center; font-size: 26rpx; }
</style>
