<template>
  <div class="card">
    <div class="toolbar">
      <el-radio-group v-model="status" @change="load">
        <el-radio-button value="ALL">全部</el-radio-button>
        <el-radio-button value="UNPAID">待付款</el-radio-button>
        <el-radio-button value="UNSHIPPED">待发货</el-radio-button>
        <el-radio-button value="SHIPPED">已发货</el-radio-button>
        <el-radio-button value="COMPLETED">已完成</el-radio-button>
        <el-radio-button value="CANCELLED">已取消</el-radio-button>
      </el-radio-group>
      <el-button size="small" @click="exportCsv">导出订单日志(CSV)</el-button>
    </div>

    <el-table :data="orders" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="orderNo" label="订单号" width="190" />
      <el-table-column label="所属店铺" width="100">
        <template #default="{ row }">果园 #{{ row.storeId }}</template>
      </el-table-column>
      <el-table-column prop="payAmount" label="实付" width="90" />
      <el-table-column label="平台抽成" width="100">
        <template #default="{ row }">{{ row.commissionAmount ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="快递单号" min-width="150">
        <template #default="{ row }">{{ row.expressNo || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="170" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="{ UNPAID: 'danger', UNSHIPPED: 'warning', SHIPPED: 'primary', COMPLETED: 'success' }[row.status]">
            {{ { UNPAID: '待付款', UNSHIPPED: '待发货', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminOrders } from '../../api'

const orders = ref([])
const status = ref('ALL')
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const page = await adminOrders({ status: status.value, page: 0, size: 50 })
    orders.value = page.content || []
  } finally { loading.value = false }
}
function exportCsv() {
  const rows = [['订单号', '店铺ID', '实付', '抽成', '快递公司', '快递单号', '状态', '下单时间']]
  orders.value.forEach(o => rows.push([o.orderNo, o.storeId, o.payAmount, o.commissionAmount ?? '', o.expressCompany ?? '', o.expressNo ?? '', o.status, o.createdAt]))
  const csv = '\uFEFF' + rows.map(r => r.join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = 'platform-orders.csv'
  a.click()
}
onMounted(load)
</script>

<style scoped>
.card { background: #fff; border-radius: 10px; padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
</style>
