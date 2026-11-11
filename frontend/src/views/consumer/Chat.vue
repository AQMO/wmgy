<template>
  <div class="chat-page">
    <div class="head">
      <span class="title">💬 {{ storeName }}</span>
      <span class="tip">实时消息（WebSocket）</span>
    </div>
    <div class="msgs" ref="box">
      <div v-for="m in messages" :key="m.id" class="row" :class="{ mine: m.senderRole === 'USER' }">
        <div class="bubble">{{ m.content }}</div>
        <div class="time">{{ fmt(m.createdAt) }}</div>
      </div>
      <el-empty v-if="messages.length === 0" description="暂无消息，打个招呼吧~" />
    </div>
    <div class="input-bar">
      <el-input v-model="text" placeholder="输入消息…" @keyup.enter="send" />
      <el-button type="primary" :disabled="!text.trim()" @click="send">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { chatMessages, chatSend, storeDetail } from '../../api'
import { useUserStore } from '../../stores/user'
import { onChat, startWs, stopWs } from '../../utils/ws'

const route = useRoute()
const userStore = useUserStore()
const storeId = route.query.storeId
const conversationKey = `u${userStore.user?.id}_s${storeId}`
const storeName = ref('果园客服')
const messages = ref([])
const text = ref('')
const box = ref(null)
let timer = null
let offChat = null

function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '' }
function scrollBottom() {
  nextTick(() => { if (box.value) box.value.scrollTop = box.value.scrollHeight })
}
async function load() {
  try {
    const d = await chatMessages({ conversationKey })
    messages.value = d.messages || []
    scrollBottom()
  } catch (e) { /* 轮询容错 */ }
}
async function send() {
  if (!text.value.trim()) return
  const content = text.value
  text.value = ''
  await chatSend({ storeId, content })
  load()
}
onMounted(async () => {
  storeDetail(storeId).then(d => { storeName.value = d.store.name }).catch(() => {})
  load()
  // WebSocket 实时接收（含本人/对方消息），轮询仅兜底
  startWs()
  offChat = onChat(msg => {
    const m = msg.data
    if (m && m.conversationKey === conversationKey) {
      if (!messages.value.some(x => x.id === m.id)) {
        messages.value.push(m)
        scrollBottom()
      }
    }
  })
  timer = setInterval(load, 30000)
})
onBeforeUnmount(() => { clearInterval(timer); if (offChat) offChat(); stopWs() })
</script>

<style scoped>
.chat-page { max-width: 760px; margin: 0 auto; background: #fff; border-radius: 10px; display: flex; flex-direction: column; height: calc(100vh - 130px); }
.head { padding: 12px 16px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.title { font-weight: 600; }
.tip { font-size: 11px; color: #bbb; }
.msgs { flex: 1; overflow-y: auto; padding: 16px; }
.row { margin-bottom: 14px; }
.row.mine { text-align: right; }
.bubble { display: inline-block; background: #f2f3f5; padding: 8px 12px; border-radius: 10px; max-width: 70%; text-align: left; font-size: 14px; word-break: break-all; }
.mine .bubble { background: #e8611d; color: #fff; }
.time { font-size: 10px; color: #ccc; margin-top: 4px; }
.input-bar { display: flex; gap: 10px; padding: 12px; border-top: 1px solid #eee; }
</style>
