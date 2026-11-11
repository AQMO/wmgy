<template>
  <div class="card">
    <div class="toolbar">
      <h4>🔔 消息中心</h4>
      <el-button size="small" @click="readAll">全部已读</el-button>
    </div>
    <div v-loading="loading">
      <div v-for="n in list" :key="n.id" class="item" :class="{ unread: !n.readFlag }" @click="read(n)">
        <div class="t">{{ n.title }} <el-tag v-if="!n.readFlag" size="small" type="danger">未读</el-tag></div>
        <div class="c">{{ n.content }}</div>
        <div class="time">{{ fmt(n.createdAt) }}</div>
      </div>
      <el-empty v-if="!loading && list.length === 0" description="暂无消息" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { notificationList, notificationRead, notificationReadAll } from '../../api'

const list = ref([])
const loading = ref(false)

function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '' }
async function load() {
  loading.value = true
  try {
    const page = await notificationList({ page: 0, size: 30 })
    list.value = page.content || []
  } finally { loading.value = false }
}
async function read(n) {
  if (!n.readFlag) { await notificationRead(n.id); n.readFlag = true }
}
async function readAll() {
  await notificationReadAll()
  load()
}
onMounted(load)
</script>

<style scoped>
.card { background: #fff; border-radius: 10px; padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.item { padding: 12px; border-bottom: 1px solid #f2f2f2; cursor: pointer; border-radius: 6px; }
.item:hover { background: #f9f9f9; }
.item.unread { background: #fff8f2; }
.t { font-weight: 600; font-size: 14px; display: flex; gap: 8px; align-items: center; }
.c { font-size: 13px; color: #666; margin-top: 4px; }
.time { font-size: 11px; color: #bbb; margin-top: 4px; }
</style>
