<template>
  <div>
    <div class="status-tabs">
      <div v-for="t in tabs" :key="t.v" class="tab" :class="{ active: cur === t.v }" @click="switchTab(t.v)">{{ t.label }}</div>
    </div>
    <div v-loading="loading">
      <div v-for="o in orders" :key="o.id" class="order-card" @click="$router.push(`/shop/order/${o.id}`)">
        <div class="row1">
          <span class="store">🏪 {{ storeNames[o.storeId] || ('果园 #' + o.storeId) }}</span>
          <el-tag size="small" :type="statusType(o.status)">{{ statusText(o.status) }}</el-tag>
        </div>
        <div class="row2">
          <span>{{ (o.items && o.items[0]?.productName) || '商品' }}</span>
          <b class="price">¥{{ o.payAmount }}</b>
        </div>
        <div class="row3">
          <span class="no">订单号 {{ o.orderNo }}</span>
          <div class="btns" @click.stop>
            <el-button v-if="o.status === 'UNPAID'" size="small" type="danger" @click="doPay(o)">去支付</el-button>
            <el-button v-if="o.status === 'UNPAID'" size="small" @click="doCancel(o)">取消</el-button>
            <el-button v-if="o.status === 'SHIPPED'" size="small" type="primary" @click="doConfirm(o)">确认收货</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myOrders, payOrder, cancelOrder, confirmOrder, storeDetail } from '../../api'

const route = useRoute()
const tabs = [
  { v: 'ALL', label: '全部' },
  { v: 'UNPAID', label: '待付款' },
  { v: 'UNSHIPPED', label: '待发货' },
  { v: 'SHIPPED', label: '待收货' },
  { v: 'COMPLETED', label: '已完成' },
  { v: 'CANCELLED', label: '已取消' }
]
const cur = ref(route.query.status || 'ALL')
const orders = ref([])
const loading = ref(false)
const storeNames = ref({})

async function load() {
  loading.value = true
  try {
    const page = await myOrders({ status: cur.value, page: 0, size: 20 })
    orders.value = page.content || []
    // 取店铺名
    for (const o of orders.value) {
      if (!storeNames.value[o.storeId]) {
        storeDetail(o.storeId).then(d => { storeNames.value[o.storeId] = d.store.name }).catch(() => {})
      }
    }
  } finally {
    loading.value = false
  }
}
function switchTab(v) { cur.value = v; load() }

const statusText = s => ({ UNPAID: '待付款', UNSHIPPED: '待发货', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消', REFUNDED: '已退款' }[s] || s)
const statusType = s => ({ UNPAID: 'danger', UNSHIPPED: 'warning', SHIPPED: 'primary', COMPLETED: 'success' }[s] || 'info')

async function doPay(o) {
  await payOrder(o.id)
  ElMessage.success('支付成功')
  load()
}
async function doCancel(o) {
  await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
  await cancelOrder(o.id)
  ElMessage.success('已取消')
  load()
}
async function doConfirm(o) {
  await ElMessageBox.confirm('确认已收到货？', '提示', { type: 'warning' })
  await confirmOrder(o.id)
  ElMessage.success('确认收货成功')
  load()
}
onMounted(load)
</script>

<style scoped>
.status-tabs { display: flex; gap: 6px; overflow-x: auto; margin-bottom: 10px; }
.tab { background: #fff; border-radius: 14px; padding: 6px 14px; font-size: 13px; cursor: pointer; color: #666; white-space: nowrap; }
.tab.active { background: #e8611d; color: #fff; }
.order-card { background: #fff; border-radius: 10px; padding: 12px 14px; margin-bottom: 10px; cursor: pointer; }
.row1, .row2, .row3 { display: flex; justify-content: space-between; align-items: center; }
.store { font-weight: 600; font-size: 14px; }
.row2 { margin: 8px 0; font-size: 13px; color: #555; }
.price { color: #e8611d; }
.no { font-size: 11px; color: #999; }
.btns { display: flex; gap: 6px; }
</style>
