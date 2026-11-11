<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-button type="primary" @click="dialog = true">+ 发放优惠券</el-button>
    </div>
    <el-table :data="list" stripe>
      <el-table-column prop="name" label="券名称" />
      <el-table-column label="面额" width="90">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column label="门槛" width="110">
        <template #default="{ row }">满 {{ row.threshold }} 可用</template>
      </el-table-column>
      <el-table-column label="领取/总量" width="100">
        <template #default="{ row }">{{ row.total - row.remain }} / {{ row.total }}</template>
      </el-table-column>
      <el-table-column label="有效期" min-width="180">
        <template #default="{ row }">{{ fmt(row.startTime) || '即日' }} ~ {{ fmt(row.endTime) || '长期' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '生效中' : '已停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link size="small" @click="toggle(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="发放优惠券" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="券名称"><el-input v-model="form.name" placeholder="如：满100减20" /></el-form-item>
        <el-form-item label="优惠金额"><el-input-number v-model="form.amount" :min="1" :precision="2" /></el-form-item>
        <el-form-item label="使用门槛"><el-input-number v-model="form.threshold" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="发放总量"><el-input-number v-model="form.total" :min="1" /></el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">发放</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { couponMerchantList, couponCreate, couponToggle } from '../../api'

const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const form = reactive({ name: '', amount: 20, threshold: 100, total: 100, endTime: '' })

async function load() {
  loading.value = true
  try { list.value = await couponMerchantList() } finally { loading.value = false }
}
onMounted(load)

async function save() {
  if (!form.name) return ElMessage.warning('请填写券名称')
  await couponCreate({ ...form, endTime: form.endTime || '' })
  ElMessage.success('已发放')
  dialog.value = false
  load()
}
async function toggle(row) {
  await couponToggle(row.id, row.status === 1 ? 0 : 1)
  load()
}
function fmt(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
</style>
