<template>
  <div class="card" v-loading="loading">
    <h3>全局配置</h3>
    <p class="tip">平台级参数：默认保证金、退保工作日等（PRD 5.3）</p>
    <el-table :data="rows" style="margin-top:16px;max-width:720px">
      <el-table-column prop="key" label="配置项" width="220" />
      <el-table-column label="说明" min-width="200">
        <template #default="{ row }">{{ { DEFAULT_DEPOSIT: '全局默认保证金(¥)', RETENTION_DAYS: '退保处理工作日' }[row.key] || '-' }}</template>
      </el-table-column>
      <el-table-column label="值" width="180">
        <template #default="{ row }">
          <el-input v-model="row.value" size="small" style="width:140px" />
        </template>
      </el-table-column>
    </el-table>
    <el-button type="primary" style="margin-top:16px" @click="save">保存配置</el-button>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminConfigs, setConfig } from '../../api'

const rows = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const cfg = await adminConfigs()
    rows.value = Object.entries(cfg || {}).map(([key, value]) => ({ key, value }))
  } finally { loading.value = false }
})
async function save() {
  const payload = {}
  rows.value.forEach(r => { payload[r.key] = r.value })
  await setConfig(payload)
  ElMessage.success('配置已保存')
}
</script>

<style scoped>
.card { background: #fff; border-radius: 10px; padding: 24px; }
h3 { color: #e8611d; }
.tip { font-size: 12px; color: #999; margin-top: 6px; }
</style>
