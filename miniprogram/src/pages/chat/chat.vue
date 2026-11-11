<template>
  <view class="chat-page">
    <view class="online">
      <text class="dot"></text>
      <text>园主在线 · 通常 1 分钟内回复</text>
    </view>
    <scroll-view scroll-y class="msgs" :scroll-top="scrollTop" scroll-with-animation>
      <view v-for="m in messages" :key="m.id" class="row" :class="{ mine: m.senderRole === 'USER' }">
        <view class="avatar" :class="{ me: m.senderRole === 'USER' }">{{ m.senderRole === 'USER' ? me : owner }}</view>
        <view class="body">
          <view class="bubble">{{ m.content }}</view>
          <view class="time">{{ fmt(m.createdAt) }}</view>
        </view>
      </view>
      <view v-if="messages.length === 0" class="empty">还没有聊过，问问园主今天的果子吧</view>
    </scroll-view>
    <view class="quick">
      <text class="chip" v-for="q in quick" :key="q" @click="sendText(q)">{{ q }}</text>
    </view>
    <view class="input-bar">
      <input v-model="text" placeholder="问问园主…" class="ipt" confirm-type="send" @confirm="send" />
      <button class="send-btn" size="mini" :disabled="!text.trim()" @click="send">发送</button>
    </view>
  </view>
</template>

<script setup>
import { onLoad, onShow, onUnload } from '@dcloudio/uni-app'
import { nextTick, ref } from 'vue'
import { apiChatHistory, apiChatSend } from '../../utils/api'
import { currentUser } from '../../utils/request'
import { getStoreId, getCachedStore, storeNameOf } from '../../utils/store'
import { onChat, startWs } from '../../utils/ws'

const storeId = ref(getStoreId())
const messages = ref([])
const text = ref('')
const scrollTop = ref(0)
const owner = ref(storeNameOf(getCachedStore()).slice(0, 1))
const me = ref((currentUser()?.nickname || '我').slice(0, 1))
const quick = ['今天摘的吗', '什么时候发货', '可以发顺丰吗']
let timer = null
let wsReady = false

const conversationKey = () => `u${currentUser()?.id}_s${storeId.value}`
const fmt = t => (t ? String(t).replace('T', ' ').slice(0, 16) : '')

async function load() {
  try {
    const d = await apiChatHistory(conversationKey())
    messages.value = d.messages || []
    nextTick(() => { scrollTop.value = messages.value.length * 999 })
  } catch (e) { }
}
async function sendText(content) {
  if (!content || !content.trim()) return
  await apiChatSend({ storeId: storeId.value, content })
  load()
}
function send() {
  const c = text.value
  text.value = ''
  sendText(c)
}

onLoad(q => { if (q.storeId) storeId.value = q.storeId })
onShow(() => {
  uni.setNavigationBarTitle({ title: storeNameOf(getCachedStore()) })
  load()
  if (!wsReady) {
    startWs()
    wsReady = true
    onChat(msg => {
      const m = msg.data
      if (m && m.conversationKey === conversationKey()) {
        if (!messages.value.some(x => x.id === m.id)) {
          messages.value.push(m)
          nextTick(() => { scrollTop.value = messages.value.length * 999 })
        }
      }
    })
  }
  timer = setInterval(load, 30000)
})
onUnload(() => { clearInterval(timer) })
</script>

<style scoped>
.chat-page { display: flex; flex-direction: column; height: 100vh; background: #fafaf6; }
.online { display: flex; align-items: center; gap: 8rpx; padding: 14rpx 24rpx; font-size: 22rpx; color: #639922; background: #eaf3de; }
.dot { width: 10rpx; height: 10rpx; border-radius: 50%; background: #639922; }
.msgs { flex: 1; padding: 20rpx; box-sizing: border-box; }
.row { display: flex; gap: 12rpx; margin-bottom: 22rpx; align-items: flex-start; }
.row.mine { flex-direction: row-reverse; }
.avatar { width: 60rpx; height: 60rpx; border-radius: 50%; background: #c0dd97; color: #27500a; font-size: 24rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.avatar.me { background: #3b6d11; color: #fff; }
.body { max-width: 70%; }
.row.mine .body { text-align: right; }
.bubble { display: inline-block; background: #fff; padding: 16rpx 22rpx; border-radius: 4rpx 16rpx 16rpx 16rpx; font-size: 26rpx; color: #2c2c2a; text-align: left; line-height: 1.5; }
.mine .bubble { background: #eaf3de; color: #3b6d11; border-radius: 16rpx 4rpx 16rpx 16rpx; }
.time { font-size: 20rpx; color: #c0bfb8; margin-top: 8rpx; }
.empty { text-align: center; color: #b4b2a9; padding: 120rpx 0; font-size: 24rpx; }
.quick { display: flex; gap: 12rpx; padding: 0 20rpx 12rpx; flex-wrap: wrap; }
.chip { font-size: 22rpx; color: #5f5e5a; background: #fff; border: 1rpx solid #e8e6de; border-radius: 24rpx; padding: 6rpx 18rpx; }
.input-bar { display: flex; gap: 16rpx; padding: 16rpx 20rpx; background: #fff; border-top: 1rpx solid #f0eee7; }
.ipt { flex: 1; background: #f5f4ef; border-radius: 40rpx; padding: 16rpx 28rpx; font-size: 26rpx; }
.send-btn { background: #3b6d11; color: #fff; border-radius: 40rpx; font-size: 26rpx; }
</style>
