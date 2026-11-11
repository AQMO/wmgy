<template>
  <div class="points">
    <div class="card">
      <div class="label">当前积分</div>
      <div class="num">{{ overview.points ?? 0 }}</div>
      <div class="tip">订单确认收货后自动累计（1 元 = 1 分）</div>
    </div>
    <el-empty v-if="!logs.length" description="暂无积分记录" />
    <div class="log" v-for="l in logs" :key="l.id">
      <div>
        <div class="remark">{{ l.remark }}</div>
        <div class="time">{{ fmt(l.createdAt) }}</div>
      </div>
      <div class="delta" :class="{ plus: l.delta > 0 }">{{ l.delta > 0 ? '+' : '' }}{{ l.delta }}</div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { pointsMine } from '../../api'

const overview = ref({})
const logs = ref([])

function fmt(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}
onMounted(async () => {
  const r = await pointsMine({ size: 50 })
  overview.value = r
  logs.value = (r.logs && r.logs.content) || []
})
</script>

<style scoped>
.card { background: linear-gradient(135deg, #ff9a56, #e8611d); color: #fff; border-radius: 14px; text-align: center; padding: 26px; }
.label { font-size: 13px; opacity: .9; }
.num { font-size: 40px; font-weight: 800; margin: 6px 0; }
.tip { font-size: 11px; opacity: .85; }
.log { display: flex; justify-content: space-between; align-items: center; background: #fff; border-radius: 10px; padding: 12px 14px; margin-top: 10px; }
.remark { font-size: 13px; }
.time { font-size: 11px; color: #999; margin-top: 4px; }
.delta { font-weight: 700; color: #999; }
.delta.plus { color: #e8611d; }
</style>
