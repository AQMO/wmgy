<template>
  <view class="page">
    <view class="hero">
      <view class="logo">{{ storeName.slice(0, 1) }}</view>
      <view class="hname">{{ storeName }}</view>
      <view class="hsub">{{ opening ? '营业中 · 今日可发货' : '休息中 · 可留言预订' }}</view>
    </view>

    <view class="card">
      <view class="sec-title">果园简介</view>
      <view class="text">{{ store.intro || '自家果园，产地直发，当天采摘当天打包。' }}</view>
    </view>

    <view class="card" v-if="store.notice">
      <view class="sec-title">园主公告</view>
      <view class="text">{{ store.notice }}</view>
    </view>

    <view class="card">
      <view class="kv"><text class="k">所在地</text><text class="v">{{ store.address || '山东烟台' }}</text></view>
      <view class="kv"><text class="k">营业状态</text><text class="v">{{ opening ? '营业中' : '休息中' }}</text></view>
      <view class="kv"><text class="k">线下自提</text><text class="v">{{ store.selfPickup ? '支持到园自提' : '暂不支持' }}</text></view>
      <view class="kv" style="border: none"><text class="k">售后承诺</text><text class="v">坏果包赔，园主直办</text></view>
    </view>

    <view class="acts">
      <button class="btn-ghost" @click="goChat">联系园主</button>
      <button class="btn-primary" @click="goHome">逛本园商品</button>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import { apiStoreDetail } from '../../utils/api'
import { getStoreId, cacheStore, getCachedStore, storeNameOf } from '../../utils/store'

const storeId = ref(getStoreId())
const store = ref(getCachedStore() || {})
const storeName = computed(() => storeNameOf(store.value))
const opening = computed(() => (store.value.status || 'ACTIVE') === 'ACTIVE')

function goChat() { uni.navigateTo({ url: '/pages/chat/chat?storeId=' + storeId.value }) }
function goHome() { uni.switchTab({ url: '/pages/home/home' }) }

onShow(async () => {
  try {
    const d = await apiStoreDetail(storeId.value)
    store.value = d.store || {}
    cacheStore(store.value)
  } catch (e) { }
})
</script>

<style scoped>
.hero { background: #3b6d11; color: #fff; padding: 48rpx 24rpx 56rpx; display: flex; flex-direction: column; align-items: center; }
.logo { width: 120rpx; height: 120rpx; border-radius: 28rpx; background: #c0dd97; color: #27500a; font-size: 52rpx; font-weight: 700; display: flex; align-items: center; justify-content: center; }
.hname { font-size: 34rpx; font-weight: 700; margin-top: 18rpx; }
.hsub { font-size: 22rpx; color: #c0dd97; margin-top: 8rpx; }
.card { margin-top: -30rpx; }
.card + .card { margin-top: 16rpx; }
.sec-title { font-size: 26rpx; font-weight: 600; color: #2c2c2a; margin-bottom: 12rpx; }
.text { font-size: 26rpx; color: #5f5e5a; line-height: 1.8; }
.kv { display: flex; justify-content: space-between; padding: 16rpx 0; border-bottom: 1rpx solid #f3f1ea; font-size: 26rpx; }
.k { color: #888780; }
.v { color: #2c2c2a; }
.acts { display: flex; gap: 16rpx; padding: 24rpx; }
.btn-ghost { flex: 1; background: #fff; border: 1rpx solid #639922; color: #3b6d11; border-radius: 40rpx; font-size: 28rpx; }
.btn-primary { flex: 1; font-size: 28rpx; }
</style>
