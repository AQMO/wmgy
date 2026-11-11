<template>
  <div v-loading="loading">
    <el-row :gutter="16" class="stats">
      <el-col :span="8"><div class="stat-card"><div class="label">待确认预约</div><div class="num warn">{{ stats.pending ?? 0 }}</div></div></el-col>
      <el-col :span="8"><div class="stat-card"><div class="label">已确认待接待</div><div class="num">{{ stats.confirmed ?? 0 }}</div></div></el-col>
      <el-col :span="8"><div class="stat-card"><div class="label">累计预约</div><div class="num">{{ stats.total ?? 0 }}</div></div></el-col>
    </el-row>

    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane label="全部" name="ALL" />
      <el-tab-pane label="待确认" name="PENDING" />
      <el-tab-pane label="已确认" name="CONFIRMED" />
      <el-tab-pane label="已完成" name="DONE" />
    </el-tabs>

    <el-table :data="list" stripe>
      <el-table-column prop="productName" label="商品" min-width="140" />
      <el-table-column prop="pickDate" label="到园日期" width="110" />
      <el-table-column prop="timeSlot" label="时段" width="80" />
      <el-table-column prop="quantity" label="数量" width="70" />
      <el-table-column prop="phone" label="联系电话" width="120" />
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="tagType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button link type="success" size="small" @click="handle(row, 'CONFIRM')">确认</el-button>
            <el-button link type="danger" size="small" @click="handle(row, 'REJECT')">拒绝</el-button>
          </template>
          <el-button v-if="row.status === 'CONFIRMED'" link type="primary" size="small" @click="handle(row, 'DONE')">完成接待</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pickupStore, pickupStats, pickupHandle } from '../../api'

const list = ref([])
const stats = ref({})
const tab = ref('ALL')
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const r = await pickupStore({ status: tab.value, size: 100 })
    list.value = r.content || []
  } finally { loading.value = false }
}
onMounted(async () => {
  load()
  try { stats.value = await pickupStats() } catch (e) { /* 忽略 */ }
})

async function handle(row, action) {
  await pickupHandle(row.id, { action })
  ElMessage.success('已处理')
  load()
  try { stats.value = await pickupStats() } catch (e) { /* 忽略 */ }
}
function statusText(s) {
  return { PENDING: '待确认', CONFIRMED: '已确认', REJECTED: '已拒绝', CANCELLED: '已取消', DONE: '已完成' }[s] || s
}
function tagType(s) {
  return { PENDING: 'warning', CONFIRMED: 'success', REJECTED: 'danger', CANCELLED: 'info', DONE: 'success' }[s] || 'info'
}
</script>

<style scoped>
.stats { margin-bottom: 14px; }
.stat-card { background: #fff; border-radius: 10px; padding: 16px; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.label { font-size: 12px; color: #888; }
.num { font-size: 26px; font-weight: 700; margin-top: 4px; }
.num.warn { color: #e6a23c; }
</style>
