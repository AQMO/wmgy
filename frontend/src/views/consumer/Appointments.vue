<template>
  <div class="appointments">
    <el-empty v-if="!list.length" description="暂无采摘预约，去果园商品页预约" />
    <div class="a-item" v-for="a in list" :key="a.id">
      <div class="row1">
        <span class="pname">{{ a.productName }}</span>
        <el-tag size="small" :type="tagType(a.status)">{{ statusText(a.status) }}</el-tag>
      </div>
      <div class="row2">📅 {{ a.pickDate }} {{ a.timeSlot }} · {{ a.quantity }} 人/份</div>
      <div class="row2" v-if="a.storeReply">商家留言：{{ a.storeReply }}</div>
      <el-button v-if="['PENDING', 'CONFIRMED'].includes(a.status)" size="small" @click="cancel(a.id)">取消预约</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pickupMine, pickupCancel } from '../../api'

const list = ref([])

async function load() {
  try {
    const r = await pickupMine({ size: 50 })
    list.value = r.content || []
  } catch (e) { list.value = [] }
}
onMounted(load)

async function cancel(id) {
  await pickupCancel(id)
  ElMessage.success('已取消')
  load()
}
function statusText(s) {
  return { PENDING: '待确认', CONFIRMED: '已确认', REJECTED: '已拒绝', CANCELLED: '已取消', DONE: '已完成' }[s] || s
}
function tagType(s) {
  return { PENDING: 'warning', CONFIRMED: 'success', REJECTED: 'danger', CANCELLED: 'info', DONE: 'success' }[s] || 'info'
}
</script>

<style scoped>
.a-item { background: #fff; border-radius: 12px; padding: 14px; margin-bottom: 10px; }
.row1 { display: flex; justify-content: space-between; align-items: center; }
.pname { font-weight: 600; }
.row2 { font-size: 13px; color: #555; margin: 6px 0; }
</style>
