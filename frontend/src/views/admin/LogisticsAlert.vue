<template>
  <div v-loading="loading">
    <el-alert type="warning" :closable="false" style="margin-bottom: 14px"
      :title="`已支付超过 24 小时未发货订单 ${list.length} 笔，请尽快处理（每小时自动扫描并提醒）`" />

    <el-table :data="list" stripe>
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="payAmount" label="实付" width="100">
        <template #default="{ row }">¥{{ row.payAmount }}</template>
      </el-table-column>
      <el-table-column label="支付时间" width="160">
        <template #default="{ row }">{{ fmt(row.paidAt) }}</template>
      </el-table-column>
      <el-table-column label="超时" width="100">
        <template #default="{ row }">{{ hours(row.paidAt) }} 小时</template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && !list.length" description="暂无超时未发货订单 🎉" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminOverdue } from '../../api'

const list = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try { list.value = await adminOverdue(24) } finally { loading.value = false }
})
function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '-' }
function hours(t) {
  if (!t) return '-'
  return Math.floor((Date.now() - new Date(t).getTime()) / 3600000)
}
</script>
