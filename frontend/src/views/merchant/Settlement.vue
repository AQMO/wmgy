<template>
  <div v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="8">
        <div class="stat-card"><div class="label">💰 可提现余额</div><div class="num">¥{{ data.balance ?? '-' }}</div></div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card"><div class="label">❄️ 冻结金额</div><div class="num">¥{{ data.frozenAmount ?? '-' }}</div></div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card"><div class="label">🛡️ 保证金</div><div class="num">¥{{ data.deposit ?? '-' }}</div></div>
      </el-col>
    </el-row>

    <div class="card">
      <div class="toolbar">
        <h4>资金流水明细</h4>
        <div>
          <el-button type="primary" size="small" @click="dialog = true">申请提现</el-button>
          <el-button size="small" @click="exportCsv">导出 Excel(CSV)</el-button>
        </div>
      </div>
      <el-table :data="flows" style="margin-top:12px">
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="{ ORDER_IN: 'success', COMMISSION: 'danger', WITHDRAW: 'warning', DEPOSIT: 'info', REFUND: 'info' }[row.type]">
              {{ { ORDER_IN: '货款入账', COMMISSION: '平台抽成', WITHDRAW: '提现', DEPOSIT: '保证金', REFUND: '退款' }[row.type] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="110">
          <template #default="{ row }">
            <span :style="{ color: row.type === 'ORDER_IN' ? '#67c23a' : '#e8611d' }">
              {{ row.type === 'ORDER_IN' ? '+' : '-' }}¥{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="余额" width="110" />
        <el-table-column prop="remark" label="备注" min-width="200" />
      </el-table>
    </div>

    <el-dialog v-model="dialog" title="申请提现" width="380px">
      <el-input-number v-model="amount" :min="0.01" :precision="2" style="width:100%" />
      <p class="tip">可提现余额：¥{{ data.balance }}</p>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="doWithdraw">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { settlement, withdraw } from '../../api'

const data = ref({})
const flows = ref([])
const loading = ref(false)
const dialog = ref(false)
const amount = ref(100)

async function load() {
  loading.value = true
  try {
    const r = await settlement({ page: 0, size: 50 })
    data.value = r
    flows.value = r.flows?.content || []
  } finally { loading.value = false }
}
async function doWithdraw() {
  await withdraw({ amount: amount.value })
  ElMessage.success('提现申请已提交，等待平台打款')
  dialog.value = false
  load()
}
function exportCsv() {
  const rows = [['时间', '类型', '金额', '余额', '备注']]
  flows.value.forEach(f => rows.push([f.createdAt, f.type, f.amount, f.balanceAfter, f.remark]))
  const csv = '\uFEFF' + rows.map(r => r.join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = 'settlement-flows.csv'
  a.click()
}
onMounted(load)
</script>

<style scoped>
.stat-card { background: #fff; border-radius: 10px; padding: 16px; }
.label { font-size: 13px; color: #888; }
.num { font-size: 22px; font-weight: 700; margin-top: 8px; }
.card { background: #fff; border-radius: 10px; padding: 20px; margin-top: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.tip { font-size: 12px; color: #999; margin-top: 8px; }
</style>
