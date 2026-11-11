<template>
  <div v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="12"><div class="stat"><div class="label">今日全站 PV</div><div class="num">{{ data.todayPv ?? '-' }}</div></div></el-col>
      <el-col :span="12"><div class="stat"><div class="label">累计全站 PV</div><div class="num">{{ data.totalPv ?? '-' }}</div></div></el-col>
    </el-row>

    <div class="card">
      <h4>🔥 店铺流量排行 Top10</h4>
      <el-table :data="data.topStores || []" style="margin-top:12px">
        <el-table-column type="index" label="排名" width="70" />
        <el-table-column prop="storeName" label="果园店铺" min-width="180" />
        <el-table-column prop="pv" label="访问量 PV" width="120" />
        <el-table-column label="占比" min-width="200">
          <template #default="{ row }">
            <el-progress :percentage="pct(row.pv)" :color="'#e8611d'" />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminTraffic } from '../../api'

const data = ref({})
const loading = ref(false)
function pct(pv) {
  const top = data.value.topStores || []
  const max = top.length ? top[0].pv : 1
  return Math.round((pv / (max || 1)) * 100)
}
onMounted(async () => {
  loading.value = true
  try { data.value = await adminTraffic() } finally { loading.value = false }
})
</script>

<style scoped>
.stat { background: #fff; border-radius: 10px; padding: 20px; }
.label { font-size: 13px; color: #888; }
.num { font-size: 24px; font-weight: 700; margin-top: 8px; }
.card { background: #fff; border-radius: 10px; padding: 20px; margin-top: 16px; }
h4 { margin-bottom: 4px; }
</style>
