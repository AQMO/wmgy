<template>
  <view class="page">
    <view class="card">
      <view class="sec-title">收货信息</view>
      <view class="form">
        <input v-model="form.receiverName" placeholder="收货人姓名" class="ipt" />
        <input v-model="form.receiverPhone" placeholder="手机号" type="number" class="ipt" />
        <input v-model="form.receiverAddress" placeholder="收货地址" class="ipt" />
        <input v-model="form.remark" placeholder="备注（选填）" class="ipt" />
      </view>
    </view>

    <view class="card">
      <view class="sec-title">商品清单</view>
      <view v-for="it in items" :key="it.productId" class="item">
        <text class="n">{{ it.name }}</text>
        <text class="q">x{{ it.quantity }}</text>
        <text class="p">¥{{ (it.price * it.quantity).toFixed(2) }}</text>
      </view>
      <view class="kv"><text class="k">配送</text><text>本园直发 · 顺丰冷链</text></view>
      <view class="kv"><text class="k">发货</text><text>下单后现摘，明早出园</text></view>
      <view class="sum">应付总额：<text class="total">¥{{ total.toFixed(2) }}</text></view>
    </view>

    <view class="promise">
      <text class="pico"></text>
      <text>本单由{{ storeName }}直接采摘发货，坏果联系园主秒赔</text>
    </view>

    <view class="submit-wrap">
      <button class="btn-primary submit" :disabled="submitting" @click="submit">提交订单并模拟支付</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { apiProductDetail, apiCartList, apiCreateOrder, apiPayOrder } from '../../utils/api'
import { getCachedStore, storeNameOf } from '../../utils/store'

const form = ref({ receiverName: '', receiverPhone: '', receiverAddress: '', remark: '' })
const items = ref([])
const submitting = ref(false)
const storeName = ref(storeNameOf(getCachedStore()))
const total = computed(() => items.value.reduce((s, it) => s + (it.price || 0) * it.quantity, 0))

onLoad(async q => {
  if (q.productId) {
    const p = await apiProductDetail(q.productId)
    items.value = [{ productId: +q.productId, quantity: +(q.quantity || 1), name: p.name, price: p.price }]
  } else if (q.items) {
    const sel = JSON.parse(decodeURIComponent(q.items))
    items.value = []
    for (const it of sel) {
      const p = await apiProductDetail(it.productId)
      items.value.push({ productId: it.productId, quantity: it.quantity, name: p.name, price: p.price })
    }
  } else {
    const cart = (await apiCartList()) || []
    items.value = cart.map(c => ({ productId: c.productId, quantity: c.quantity, name: c.name, price: c.price }))
  }
})

async function submit() {
  const f = form.value
  if (!f.receiverName || !f.receiverPhone || !f.receiverAddress) {
    return uni.showToast({ title: '请填写完整收货信息', icon: 'none' })
  }
  submitting.value = true
  try {
    const orders = await apiCreateOrder({
      ...f,
      items: items.value.map(i => ({ productId: i.productId, quantity: i.quantity }))
    })
    // 单果园场景下后端固定返回 1 单，仍按数组遍历以兼容平台侧拆单逻辑
    for (const o of orders) await apiPayOrder(o.id)
    uni.showToast({ title: '支付成功' })
    setTimeout(() => uni.switchTab({ url: '/pages/orders/orders' }), 800)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.sec-title { font-weight: 600; color: #2c2c2a; margin-bottom: 16rpx; }
.form { display: flex; flex-direction: column; gap: 16rpx; }
.ipt { background: #f7f7f2; border-radius: 12rpx; padding: 18rpx 24rpx; font-size: 26rpx; }
.item { display: flex; gap: 16rpx; padding: 10rpx 0; font-size: 26rpx; }
.n { flex: 1; color: #2c2c2a; }
.q { color: #999; }
.p { color: #d85a30; font-weight: 600; }
.kv { display: flex; justify-content: space-between; font-size: 24rpx; color: #5f5e5a; padding: 8rpx 0; border-top: 1rpx solid #f3f1ea; }
.k { color: #888780; }
.sum { text-align: right; margin-top: 14rpx; color: #5f5e5a; font-size: 26rpx; }
.total { color: #d85a30; font-size: 34rpx; font-weight: 700; }
.promise { display: flex; gap: 12rpx; align-items: flex-start; margin: 0 24rpx; padding: 16rpx 20rpx; background: #eaf3de; border-radius: 12rpx; font-size: 22rpx; color: #3b6d11; line-height: 1.6; }
.pico { width: 20rpx; height: 20rpx; border-radius: 4rpx; background: #639922; flex-shrink: 0; margin-top: 6rpx; }
.submit-wrap { padding: 24rpx; }
.submit { width: 100%; }
</style>
