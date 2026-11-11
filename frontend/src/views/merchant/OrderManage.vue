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
      <span class="tip">仅「待发货」订单展示发货入口；未签收前可修改物流，修改留痕</span>
    </div>

    <el-table :data="orders" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="orderNo" label="订单号" width="190" />
      <el-table-column prop="receiverName" label="买家" width="90" />
      <el-table-column prop="receiverPhone" label="电话" width="120" />
      <el-table-column label="金额" width="100">
        <template #default="{ row }"><span class="price">¥{{ row.payAmount }}</span></template>
      </el-table-column>
      <el-table-column label="物流" min-width="150">
        <template #default="{ row }">
          <span v-if="row.expressNo">{{ row.expressCompany }} {{ row.expressNo }}</span>
          <span v-else style="color:#999">未发货</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="{ UNPAID: 'danger', UNSHIPPED: 'warning', SHIPPED: 'primary', COMPLETED: 'success' }[row.status]">
            {{ { UNPAID: '待付款', UNSHIPPED: '待发货', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170">
        <template #default="{ row }">
          <el-button v-if="row.status === 'UNSHIPPED'" type="primary" size="small" @click="openShip(row)">自助发货</el-button>
          <el-button v-if="row.status === 'UNSHIPPED'" type="success" size="small" @click="ewb(row)">电子面单</el-button>
          <el-button v-if="row.status === 'SHIPPED'" size="small" @click="openModify(row)">修改物流</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 发货 / 修改物流 弹窗 -->
    <el-dialog v-model="dialog" :title="isModify ? '修改物流信息' : '自助发货'" width="440px">
      <el-form :model="shipForm" label-width="90px">
        <el-form-item label="快递公司" required>
          <el-select v-model="shipForm.expressCompany" placeholder="选择快递公司" style="width:100%">
            <el-option v-for="c in companies" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" required>
          <el-input v-model="shipForm.expressNo" placeholder="手动输入单号" />
        </el-form-item>
        <el-form-item v-if="!isModify" label="发货备注"><el-input v-model="shipForm.remark" placeholder="易碎、加急等（选填）" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submitShip">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { merchantOrders, shipOrder, updateExpress, ewbShip } from '../../api'

const companies = ['顺丰速运', '京东物流', '中通快递', '圆通速递', '韵达快递', '申通快递', '邮政 EMS']
const orders = ref([])
const status = ref('ALL')
const loading = ref(false)
const dialog = ref(false)
const isModify = ref(false)
const curOrder = ref(null)
const shipForm = reactive({ expressCompany: '', expressNo: '', remark: '' })

async function load() {
  loading.value = true
  try {
    const page = await merchantOrders({ status: status.value, page: 0, size: 50 })
    orders.value = page.content || []
  } finally { loading.value = false }
}
function openShip(row) {
  isModify.value = false
  curOrder.value = row
  Object.assign(shipForm, { expressCompany: '', expressNo: '', remark: '' })
  dialog.value = true
}
function openModify(row) {
  isModify.value = true
  curOrder.value = row
  Object.assign(shipForm, { expressCompany: row.expressCompany, expressNo: row.expressNo, remark: '' })
  dialog.value = true
}
async function submitShip() {
  if (!shipForm.expressCompany || !shipForm.expressNo) {
    return ElMessage.warning('快递公司与快递单号为必填项')
  }
  if (isModify.value) {
    await updateExpress(curOrder.value.id, shipForm)
    ElMessage.success('物流信息已修改（修改记录已留存日志）')
  } else {
    await shipOrder(curOrder.value.id, shipForm)
    ElMessage.success('发货成功，已同步推送买家')
  }
  dialog.value = false
  load()
}
async function ewb(row) {
  await ElMessageBox.confirm(`将自动生成电子运单号并完成发货（订单 ${row.orderNo}），确认？`, '电子面单发货', { type: 'info' })
  await ewbShip(row.id, '')
  ElMessage.success('电子面单发货成功，运单号已生成并推送买家')
  load()
}
onMounted(load)
</script>

<style scoped>
.card { background: #fff; border-radius: 10px; padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.tip { font-size: 12px; color: #999; }
.price { color: #e8611d; font-weight: 600; }
</style>
