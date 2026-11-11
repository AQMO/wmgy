<template>
  <div class="merchant-chat">
    <div class="conv-list">
      <div class="conv-head">💬 买家消息</div>
      <div v-for="c in conversations" :key="c.conversationKey" class="conv"
           :class="{ active: curKey === c.conversationKey }" @click="open(c)">
        <div class="name">{{ c.peerName }} <el-badge v-if="c.unread" :value="c.unread" class="badge" /></div>
        <div class="last">{{ c.lastContent }}</div>
      </div>
      <el-empty v-if="conversations.length === 0" description="暂无买家消息" :image-size="60" />
    </div>

    <div class="window" v-if="curKey">
      <div class="win-head">与 {{ peerName }} 的会话</div>
      <div class="msgs" ref="box">
        <div v-for="m in messages" :key="m.id" class="row" :class="{ mine: m.senderRole === 'MERCHANT' }">
          <div class="bubble">{{ m.content }}</div>
          <div class="time">{{ fmt(m.createdAt) }}</div>
        </div>
      </div>
      <div class="input-bar">
        <el-input v-model="text" placeholder="输入消息…" @keyup.enter="send" />
        <el-button type="primary" :disabled="!text.trim()" @click="send">发送</el-button>
      </div>
    </div>
    <div class="window empty" v-else>
      <el-empty description="选择左侧会话开始沟通" />
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { chatConversations, chatMessages, chatSend } from '../../api'
import { onChat, startWs, stopWs } from '../../utils/ws'

const route = useRoute()
const conversations = ref([])
const curKey = ref('')
const peerName = ref('')
const curUserId = ref(null)
const messages = ref([])
const text = ref('')
const box = ref(null)
let timer = null
let offChat = null

function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '' }
function scrollBottom() { nextTick(() => { if (box.value) box.value.scrollTop = box.value.scrollHeight }) }
async function loadConvs() {
  conversations.value = await chatConversations()
}
async function open(c) {
  curKey.value = c.conversationKey
  peerName.value = c.peerName
  curUserId.value = c.userId
  await load()
}
async function load() {
  if (!curKey.value) return
  const d = await chatMessages({ conversationKey: curKey.value })
  messages.value = d.messages || []
  scrollBottom()
}
async function send() {
  if (!text.value.trim()) return
  const content = text.value
  text.value = ''
  await chatSend({ userId: curUserId.value, content })
  load()
  loadConvs()
}
onMounted(async () => {
  await loadConvs()
  // 支持从订单页带 ?userId= 直接打开会话
  if (route.query.userId) {
    const c = conversations.value.find(x => String(x.userId) === String(route.query.userId))
    if (c) open(c)
  }
  // WebSocket 实时消息（聊天 + 会话列表刷新），轮询兜底
  startWs()
  offChat = onChat(msg => {
    const m = msg.data
    if (!m) return
    if (m.conversationKey === curKey.value) {
      if (!messages.value.some(x => x.id === m.id)) {
        messages.value.push(m)
        scrollBottom()
      }
    }
    loadConvs()
  })
  timer = setInterval(loadConvs, 30000)
})
onBeforeUnmount(() => { clearInterval(timer); if (offChat) offChat(); stopWs() })
</script>

<style scoped>
.merchant-chat { display: flex; gap: 12px; height: calc(100vh - 140px); }
.conv-list { width: 280px; background: #fff; border-radius: 10px; overflow-y: auto; padding: 8px; flex-shrink: 0; }
.conv-head { font-weight: 600; padding: 8px 10px; }
.conv { padding: 10px; border-radius: 8px; cursor: pointer; }
.conv:hover, .conv.active { background: #f5f7fa; }
.name { font-size: 14px; font-weight: 600; display: flex; align-items: center; gap: 6px; }
.last { font-size: 12px; color: #999; margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.window { flex: 1; background: #fff; border-radius: 10px; display: flex; flex-direction: column; }
.window.empty { align-items: center; justify-content: center; }
.win-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: 600; }
.msgs { flex: 1; overflow-y: auto; padding: 16px; }
.row { margin-bottom: 14px; }
.row.mine { text-align: right; }
.bubble { display: inline-block; background: #f2f3f5; padding: 8px 12px; border-radius: 10px; max-width: 70%; text-align: left; font-size: 14px; word-break: break-all; }
.mine .bubble { background: #409eff; color: #fff; }
.time { font-size: 10px; color: #ccc; margin-top: 4px; }
.input-bar { display: flex; gap: 10px; padding: 12px; border-top: 1px solid #eee; }
</style>
