<template>
  <div class="card">
    <div class="toolbar">
      <el-radio-group v-model="status" @change="load">
        <el-radio-button value="ALL">全部</el-radio-button>
        <el-radio-button value="PENDING">待审核</el-radio-button>
        <el-radio-button value="ACTIVE">营业中</el-radio-button>
        <el-radio-button value="FROZEN">已冻结</el-radio-button>
        <el-radio-button value="CLOSED">已关停</el-radio-button>
      </el-radio-group>
      <el-input v-model="keyword" placeholder="搜索果园名称" style="width:220px" clearable @keyup.enter="load" />
    </div>

    <el-table :data="stores" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="果园名称" min-width="140" />
      <el-table-column prop="address" label="地址" min-width="160" />
      <el-table-column prop="deposit" label="保证金" width="100" />
      <el-table-column prop="balance" label="可提现余额" width="110" />
      <el-table-column prop="createdAt" label="入驻时间" width="160" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="{ PENDING: 'warning', ACTIVE: 'success', FROZEN: 'danger', CLOSED: 'info' }[row.status]">
            {{ { PENDING: '待审核', ACTIVE: '营业中', FROZEN: '已冻结', CLOSED: '已关停' }[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="audit(row, 'PASS')">审核通过</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="audit(row, 'REJECT')">驳回</el-button>
          <el-button v-if="row.status === 'ACTIVE'" size="small" type="warning" @click="audit(row, 'FREEZE')">冻结</el-button>
          <el-button v-if="row.status === 'FROZEN'" size="small" type="success" @click="audit(row, 'UNFREEZE')">解冻</el-button>
          <el-button v-if="row.status !== 'CLOSED' && row.status !== 'PENDING'" size="small" type="danger" @click="audit(row, 'CLOSE')">关停</el-button>
          <el-button size="small" @click="openDeposit(row)">保证金</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="depositDialog" title="修改单店保证金" width="360px">
      <el-input-number v-model="deposit" :min="0" :precision="2" style="width:100%" />
      <template #footer>
        <el-button @click="depositDialog = false">取消</el-button>
        <el-button type="primary" @click="saveDeposit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminStores, auditStore, setDeposit } from '../../api'

const stores = ref([])
const status = ref('ALL')
const keyword = ref('')
const loading = ref(false)
const depositDialog = ref(false)
const deposit = ref(5000)
const curStore = ref(null)

async function load() {
  loading.value = true
  try {
    const page = await adminStores({ status: status.value, keyword: keyword.value, page: 0, size: 50 })
    stores.value = page.content || []
  } finally { loading.value = false }
}
async function audit(row, action) {
  const names = { PASS: '审核通过并开通店铺（需缴纳保证金）', REJECT: '驳回该入驻申请', FREEZE: '冻结该店铺', UNFREEZE: '解冻该店铺', CLOSE: '关停该店铺' }
  await ElMessageBox.confirm(`确认${names[action]}？`, '提示', { type: 'warning' })
  await auditStore(row.id, action)
  ElMessage.success('操作成功')
  load()
}
function openDeposit(row) {
  curStore.value = row
  deposit.value = row.deposit
  depositDialog.value = true
}
async function saveDeposit() {
  await setDeposit(curStore.value.id, deposit.value)
  ElMessage.success('保证金已更新')
  depositDialog.value = false
  load()
}
onMounted(load)
</script>

<style scoped>
.card { background: #fff; border-radius: 10px; padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
</style>
