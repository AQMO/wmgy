<template>
  <div v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="6"><div class="stat"><div class="label">入驻果园数</div><div class="num">{{ data.storeCount ?? '-' }}</div></div></el-col>
      <el-col :span="6"><div class="stat"><div class="label">营业中店铺</div><div class="num">{{ data.activeStoreCount ?? '-' }}</div></div></el-col>
      <el-col :span="6"><div class="stat"><div class="label">订单总量</div><div class="num">{{ data.orderCount ?? '-' }}</div></div></el-col>
      <el-col :span="6"><div class="stat"><div class="label">待发货订单</div><div class="num warn">{{ data.unshippedCount ?? '-' }}</div></div></el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12"><div class="stat big"><div class="label">平台 GMV（有效订单）</div><div class="num money">¥{{ data.gmv ?? '0.00' }}</div></div></el-col>
      <el-col :span="12"><div class="stat big"><div class="label">抽成总收入（已完成订单）</div><div class="num money">¥{{ data.commissionIncome ?? '0.00' }}</div></div></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { dashboard } from '../../api'

const data = ref({})
const loading = ref(false)
onMounted(async () => {
  loading.value = true
  try { data.value = await dashboard() } finally { loading.value = false }
})
</script>

<style scoped>
.stat { background: #fff; border-radius: 10px; padding: 20px; }
.big .num { font-size: 28px; }
.label { font-size: 13px; color: #888; }
.num { font-size: 24px; font-weight: 700; margin-top: 8px; }
.num.warn { color: #e6a23c; }
.money { color: #e8611d; }
</style>
