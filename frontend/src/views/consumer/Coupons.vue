<template>
  <div class="coupons">
    <el-tabs v-model="tab">
      <el-tab-pane label="可用优惠券" name="UNUSED" />
      <el-tab-pane label="已使用" name="USED" />
      <el-tab-pane label="已过期" name="EXPIRED" />
    </el-tabs>
    <el-empty v-if="!list.length" description="暂无优惠券，去店铺页领取" />
    <div class="c-item" v-for="c in list" :key="c.userCouponId" :class="{ used: tab !== 'UNUSED' }">
      <div class="left">
        <div class="amt">¥<b>{{ c.amount }}</b></div>
        <div class="cond">满 {{ c.threshold }} 可用</div>
      </div>
      <div class="right">
        <div class="name">{{ c.name }}</div>
        <div class="sname">{{ c.storeName }}</div>
        <div class="time">{{ fmt(c.startTime) }} ~ {{ fmt(c.endTime) || '长期' }}</div>
      </div>
      <el-button v-if="tab === 'UNUSED'" type="danger" size="small" round @click="goUse(c)">去使用</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { couponMine } from '../../api'

const router = useRouter()
const tab = ref('UNUSED')
const list = ref([])

async function load() {
  try {
    list.value = await couponMine(tab.value)
  } catch (e) {
    list.value = []
  }
}
watch(tab, load)
onMounted(load)

function fmt(t) {
  return t ? String(t).replace('T', ' ').slice(0, 10) : ''
}
function goUse(c) {
  ElMessage.success('结算时选择该券即可抵扣')
  router.push('/shop/home')
}
</script>

<style scoped>
.c-item { display: flex; align-items: center; gap: 14px; background: #fff; border-radius: 12px; padding: 14px; margin-bottom: 10px; }
.c-item.used { opacity: .55; }
.left { min-width: 88px; text-align: center; color: #e8611d; border-right: 1px dashed #eee; padding-right: 12px; }
.amt b { font-size: 24px; }
.cond { font-size: 11px; }
.right { flex: 1; }
.name { font-weight: 600; }
.sname { font-size: 12px; color: #666; margin-top: 2px; }
.time { font-size: 11px; color: #999; margin-top: 4px; }
</style>
