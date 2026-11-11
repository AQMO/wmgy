<template>
  <view class="page">
    <view class="hero"><view class="fruit"></view></view>

    <view class="card">
      <view class="price-row">
        <text class="price-big">¥{{ product.price }}</text>
        <text class="muted">已售 {{ product.sales || 0 }} · 库存 {{ product.stock || 0 }}</text>
      </view>
      <view class="name">{{ product.name }}</view>
      <view class="tags">
        <text class="tag">本园直供</text>
        <text class="tag">当天采摘</text>
        <text class="tag">坏果包赔</text>
      </view>
    </view>

    <view class="card promise">
      <view class="sec-title">本园承诺</view>
      <view class="ptext">自家果园，不打蜡、不催熟；下单后现摘，坏一颗赔一颗，售后由园主直接处理。</view>
      <view class="plink" @click="goChat">有疑问？问问园主 ›</view>
    </view>

    <view class="card">
      <view class="sec-title">商品详情</view>
      <view class="detail">{{ product.detail || '产地直发，新鲜到家' }}</view>
    </view>

    <view class="bottom">
      <view class="bico" @click="goChat">问园主</view>
      <view class="bico" @click="goCart">购物车</view>
      <button class="btn-cart" @click="addCart">加入购物车</button>
      <button class="btn-buy" @click="buyNow">立即购买</button>
    </view>
  </view>
</template>

<script setup>
import { onLoad, onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { apiProductDetail, apiCartAdd } from '../../utils/api'
import { isLoggedIn, visit } from '../../utils/request'
import { getStoreId } from '../../utils/store'

const product = ref({})
const id = ref(null)
const storeId = ref(getStoreId())

function checkLogin() {
  if (!isLoggedIn()) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.navigateTo({ url: '/pages/login/login' })
    return false
  }
  return true
}
async function addCart() {
  if (!checkLogin()) return
  await apiCartAdd({ productId: id.value, quantity: 1 })
  uni.showToast({ title: '已加入购物车' })
}
function buyNow() {
  if (!checkLogin()) return
  uni.navigateTo({ url: `/pages/confirm/confirm?productId=${id.value}&quantity=1` })
}
function goChat() { uni.navigateTo({ url: '/pages/chat/chat?storeId=' + (product.value.storeId || storeId.value) }) }
function goCart() { uni.switchTab({ url: '/pages/cart/cart' }) }

onLoad(q => { id.value = q.id })
onShow(async () => {
  if (!id.value) return
  product.value = await apiProductDetail(id.value)
  visit(product.value.storeId, 'product')
})
</script>

<style scoped>
.hero { height: 440rpx; background: #eaf3de; display: flex; align-items: center; justify-content: center; }
.fruit { width: 160rpx; height: 160rpx; border-radius: 50%; background: #97c459; }
.price-row { display: flex; align-items: baseline; gap: 16rpx; }
.price-big { color: #d85a30; font-size: 44rpx; font-weight: 700; }
.muted { color: #b4b2a9; font-size: 22rpx; }
.name { font-size: 32rpx; font-weight: 600; color: #2c2c2a; margin-top: 10rpx; line-height: 1.45; }
.tags { display: flex; gap: 10rpx; margin-top: 14rpx; }
.tag { font-size: 20rpx; color: #3b6d11; background: #eaf3de; border-radius: 8rpx; padding: 4rpx 12rpx; }
.promise { background: #eaf3de; }
.sec-title { font-weight: 600; color: #2c2c2a; margin-bottom: 10rpx; }
.ptext { font-size: 24rpx; color: #3b6d11; line-height: 1.7; }
.plink { font-size: 24rpx; color: #3b6d11; margin-top: 12rpx; }
.detail { font-size: 26rpx; color: #5f5e5a; line-height: 1.8; }
.bottom { position: fixed; bottom: 0; left: 0; right: 0; display: flex; align-items: center; gap: 12rpx; padding: 16rpx 24rpx; background: #fff; border-top: 1rpx solid #f0eee7; }
.bico { font-size: 22rpx; color: #888780; text-align: center; padding: 0 8rpx; }
.btn-cart { flex: 1; background: #fff; border: 1rpx solid #639922; color: #3b6d11; border-radius: 40rpx; font-size: 26rpx; }
.btn-buy { flex: 1; background: #3b6d11; color: #fff; border-radius: 40rpx; font-size: 26rpx; }
</style>
