<template>
  <div class="chat-monitor">
    <div class="conv-list">
      <div class="conv-head">🕵️ 全平台会话监控（纠纷取证）</div>
      <div v-for="c in conversations" :key="c.conversationKey" class="conv"
           :class="{ active: curKey === c.conversationKey }" @click="open(c)">
        <div class="name">{{ c.buyerName }} ↔ {{ c.storeName }}</div>
        <div class="last">{{ c.lastContent }}</div>
        <div class="time">{{ fmt(c.lastTime) }}</div>
      </div>
      <el-empty v-if="conversations.length === 0" description="暂无会话记录" :image-size="60" />
    </div>
    <div class="window" v-if="curKey">
      <div class="win-head">{{ peerDesc }} 会话记录（只读）</div>
      <div class="msgs">
        <div v-for="m in messages" :key="m.id" class="row" :class="{ mine: m.senderRole === 'MERCHANT' }">
          <div class="bubble"><span class="who">{{ m.senderRole === 'MERCHANT' ? '商家' : '买家' }}：</span>{{ m.content }}</div>
          <div class="time">{{ fmt(m.createdAt) }}</div>
        </div>
      </div>
    </div>
    <div class="window empty" v-else><el-empty description="选择左侧会话查看记录" /></div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminChats, adminChatMessages } from '../../api'

const conversations = ref([])
const curKey = ref('')
const peerDesc = ref('')
const messages = ref([])

function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '' }
async function open(c) {
  curKey.value = c.conversationKey
  peerDesc.value = `${c.buyerName} ↔ ${c.storeName}`
  const d = await adminChatMessages({ conversationKey: c.conversationKey })
  messages.value = d.messages || []
}
onMounted(async () => { conversations.value = await adminChats() })
</script>

<style scoped>
.chat-monitor { display: flex; gap: 12px; height: calc(100vh - 140px); }
.conv-list { width: 320px; background: #fff; border-radius: 10px; overflow-y: auto; padding: 8px; flex-shrink: 0; }
.conv-head { font-weight: 600; padding: 8px 10px; }
.conv { padding: 10px; border-radius: 8px; cursor: pointer; }
.conv:hover, .conv.active { background: #f5f7fa; }
.name { font-size: 14px; font-weight: 600; }
.last { font-size: 12px; color: #999; margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.time { font-size: 11px; color: #ccc; margin-top: 2px; }
.window { flex: 1; background: #fff; border-radius: 10px; display: flex; flex-direction: column; }
.window.empty { align-items: center; justify-content: center; }
.win-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: 600; }
.msgs { flex: 1; overflow-y: auto; padding: 16px; }
.row { margin-bottom: 14px; }
.row.mine { text-align: right; }
.bubble { display: inline-block; background: #f2f3f5; padding: 8px 12px; border-radius: 10px; max-width: 75%; text-align: left; font-size: 14px; word-break: break-all; }
.who { color: #999; font-size: 12px; }
.time { font-size: 10px; color: #ccc; margin-top: 4px; }
</style>
