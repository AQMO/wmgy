<template>
  <div v-loading="loading">
    <div class="card" v-if="d.order">
      <div class="head">
        <el-tag :type="statusType(d.order.status)">{{ statusText(d.order.status) }}</el-tag>
        <span class="no">订单号 {{ d.order.orderNo }}</span>
      </div>
      <el-descriptions :column="1" border style="margin-top:12px">
        <el-descriptions-item label="果园店铺">{{ d.storeName }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ d.order.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="收货信息">{{ d.order.receiverName }} {{ d.order.receiverPhone }}<br />{{ d.order.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="物流信息">
          <template v-if="d.order.expressNo">
            {{ d.order.expressCompany }} <b>{{ d.order.expressNo }}</b>
            <el-button link type="primary" size="small" @click="copyNo(d.order.expressNo)">复制单号</el-button>
          </template>
          <span v-else>暂未发货</span>
        </el-descriptions-item>
        <el-descriptions-item label="商品明细">
          <div v-for="it in d.items" :key="it.id" class="item">
            {{ it.productName }} x{{ it.quantity }} = ¥{{ (it.price * it.quantity).toFixed(2) }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="金额明细">
          商品总额 ¥{{ d.order.totalAmount }} · 运费 ¥{{ d.order.freight }} · 实付 <b class="total">¥{{ d.order.payAmount }}</b>
          <div v-if="d.order.commissionAmount" class="commission">平台抽成 ¥{{ d.order.commissionAmount }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="card" v-if="d.logs?.length">
      <h4>订单日志</h4>
      <el-timeline style="margin-top:12px">
        <el-timeline-item v-for="l in d.logs" :key="l.id" :timestamp="l.createdAt">
          {{ l.action }} - {{ l.detail }}
        </el-timeline-item>
      </el-timeline>
    </div>

    <div class="card btns" v-if="d.order">
      <el-button @click="contact">💬 联系商家</el-button>
      <el-button v-if="d.order.status === 'UNPAID'" type="danger" @click="doPay">去支付</el-button>
      <el-button v-if="d.order.status === 'UNPAID'" @click="doCancel">取消订单</el-button>
      <el-button v-if="d.order.status === 'SHIPPED'" type="primary" @click="doConfirm">确认收货</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderDetail, payOrder, cancelOrder, confirmOrder } from '../../api'

const route = useRoute()
const router = useRouter()
const d = ref({})
const loading = ref(false)

async function load() {
  loading.value = true
  try { d.value = await orderDetail(route.params.id) } finally { loading.value = false }
}
const statusText = s => ({ UNPAID: '待付款', UNSHIPPED: '待发货', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消', REFUNDED: '已退款' }[s] || s)
const statusType = s => ({ UNPAID: 'danger', UNSHIPPED: 'warning', SHIPPED: 'primary', COMPLETED: 'success' }[s] || 'info')

function copyNo(no) {
  navigator.clipboard?.writeText(no)
  ElMessage.success('快递单号已复制')
}
function contact() {
  router.push({ path: '/shop/chat', query: { storeId: d.value.order.storeId } })
}
async function doPay() { await payOrder(d.value.order.id); ElMessage.success('支付成功'); load() }
async function doCancel() {
  await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
  await cancelOrder(d.value.order.id); ElMessage.success('已取消'); load()
}
async function doConfirm() {
  await ElMessageBox.confirm('确认已收到货？', '提示', { type: 'warning' })
  await confirmOrder(d.value.order.id); ElMessage.success('确认收货成功'); load()
}
onMounted(load)
</script>

<style scoped>
.card { background: #fff; border-radius: 10px; padding: 14px; margin-bottom: 12px; }
.head { display: flex; justify-content: space-between; align-items: center; }
.no { font-size: 12px; color: #999; }
.item { font-size: 13px; padding: 2px 0; }
.total { color: #e8611d; }
.commission { font-size: 12px; color: #999; margin-top: 4px; }
.btns { display: flex; justify-content: flex-end; }
</style>
