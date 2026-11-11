<template>
  <view class="page">
    <view class="toolbar">
      <text class="title">消息</text>
      <text class="live"><text class="dot"></text>实时接收中</text>
      <button size="mini" class="readall" @click="readAll">全部已读</button>
    </view>

    <view v-for="n in list" :key="n.id" class="card item" :class="{ unread: !n.readFlag }" @click="markRead(n)">
      <view class="ic" :class="typeOf(n).cls"></view>
      <view class="main">
        <view class="tline">
          <text class="t">{{ n.title }}</text>
          <text v-if="!n.readFlag" class="badge">未读</text>
          <text class="time">{{ fmt(n.createdAt) }}</text>
        </view>
        <view class="c">{{ n.content }}</view>
      </view>
    </view>
    <view v-if="list.length === 0" class="empty">暂无消息</view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { apiNotifications, apiReadAll } from '../../utils/api'
import { request } from '../../utils/request'
import { onNotify, startWs } from '../../utils/ws'

const list = ref([])
let wsReady = false
const fmt = t => (t ? String(t).replace('T', ' ').slice(0, 16) : '')

// 按标题关键字给消息分色，避免依赖后端 type 字段
function typeOf(n) {
  const t = n.title || ''
  if (t.includes('发货') || t.includes('物流') || t.includes('收货')) return { cls: 'good' }
  if (t.includes('优惠') || t.includes('券')) return { cls: 'warn' }
  if (t.includes('私信') || t.includes('聊天')) return { cls: 'chat' }
  return { cls: 'sys' }
}

async function load() {
  if (!uni.getStorageSync('token')) return
  const page = await apiNotifications()
  list.value = page.content || []
}
async function markRead(n) {
  if (n.readFlag) return
  await request(`/notifications/${n.id}/read`, 'PUT')
  n.readFlag = true
}
async function readAll() {
  await apiReadAll()
  load()
}
onShow(() => {
  load()
  if (!wsReady) {
    wsReady = true
    startWs()
    onNotify(() => load())
  }
})
</script>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 16rpx; padding: 20rpx 24rpx 8rpx; }
.title { font-weight: 700; font-size: 32rpx; color: #2c2c2a; }
.live { display: flex; align-items: center; gap: 8rpx; font-size: 20rpx; color: #639922; margin-right: auto; }
.dot { width: 10rpx; height: 10rpx; border-radius: 50%; background: #639922; }
.readall { border-radius: 30rpx; font-size: 22rpx; color: #5f5e5a; background: #fff; }
.item { display: flex; gap: 16rpx; align-items: flex-start; }
.item.unread { border-left: 6rpx solid #639922; }
.ic { width: 60rpx; height: 60rpx; border-radius: 16rpx; flex-shrink: 0; background: #f1efe8; display: flex; align-items: center; justify-content: center; }
.ic::after { content: ''; width: 20rpx; height: 20rpx; border-radius: 50%; background: #888780; }
.ic.good { background: #eaf3de; }
.ic.good::after { background: #639922; }
.ic.warn { background: #faeeda; }
.ic.warn::after { background: #ef9f27; border-radius: 6rpx; }
.ic.chat { background: #fbeaf0; }
.ic.chat::after { background: #ed93b1; }
.main { flex: 1; min-width: 0; }
.tline { display: flex; align-items: center; gap: 10rpx; }
.t { font-weight: 600; font-size: 26rpx; color: #2c2c2a; }
.badge { font-size: 18rpx; background: #d85a30; color: #fff; border-radius: 8rpx; padding: 2rpx 8rpx; }
.time { margin-left: auto; font-size: 20rpx; color: #c0bfb8; }
.c { font-size: 24rpx; color: #888780; margin-top: 8rpx; line-height: 1.6; }
.empty { text-align: center; color: #999; padding: 120rpx 0; font-size: 26rpx; }
</style>
