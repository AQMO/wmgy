<template>
  <div v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="4">
        <div class="stat-card">
          <div class="label">待发货订单</div>
          <div class="num warn">{{ data.unshippedCount ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="label">可提现余额</div>
          <div class="num">¥{{ data.balance ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="label">冻结金额</div>
          <div class="num">¥{{ data.frozenAmount ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="label">店铺保证金</div>
          <div class="num">¥{{ data.store?.deposit ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="label">今日访客 PV</div>
          <div class="num green">{{ traffic.todayPv ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="label">今日登录 UV</div>
          <div class="num green">{{ traffic.todayUv ?? '-' }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="card">
      <h4>🏪 {{ data.store?.name }}</h4>
      <p class="sub">{{ data.store?.intro }}</p>
      <el-space wrap style="margin-top:12px">
        <el-button type="primary" @click="$router.push('/merchant/products')">商品管理</el-button>
        <el-button type="success" @click="$router.push('/merchant/orders')">订单发货</el-button>
        <el-button @click="$router.push('/merchant/settings')">店铺装修</el-button>
        <el-button @click="$router.push('/merchant/settlement')">资金结算</el-button>
      </el-space>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { workbench, merchantTraffic } from '../../api'

const data = ref({})
const traffic = ref({})
const loading = ref(false)
onMounted(async () => {
  loading.value = true
  try {
    data.value = await workbench()
    traffic.value = await merchantTraffic().catch(() => ({}))
  } finally { loading.value = false }
})
</script>

<style scoped>
.stat-card { background: #fff; border-radius: 10px; padding: 16px; }
.label { font-size: 13px; color: #888; }
.num { font-size: 22px; font-weight: 700; margin-top: 8px; }
.num.warn { color: #e6a23c; }
.num.green { color: #67c23a; }
.card { background: #fff; border-radius: 10px; padding: 20px; margin-top: 16px; }
h4 { margin-bottom: 6px; }
.sub { font-size: 13px; color: #999; }
</style>
