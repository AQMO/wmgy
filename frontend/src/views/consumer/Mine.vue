<template>
  <div class="mine">
    <div class="card head">
      <div class="avatar">{{ (store.user?.nickname || '游')[0] }}</div>
      <div class="info">
        <div class="name">{{ store.user?.nickname || '游客' }}</div>
        <div class="pts">🍊 积分 {{ points }}</div>
      </div>
    </div>
    <div class="grid">
      <div class="cell" @click="$router.push('/shop/coupons')">🎟️<span>我的优惠券</span></div>
      <div class="cell" @click="$router.push('/shop/appointments')">🌿<span>采摘预约</span></div>
      <div class="cell" @click="$router.push('/shop/points')">✨<span>积分明细</span></div>
      <div class="cell" @click="$router.push('/shop/orders')">📦<span>我的订单</span></div>
      <div class="cell" @click="$router.push('/shop/notifications')">🔔<span>消息中心</span></div>
      <div class="cell" @click="$router.push('/shop/chat')">💬<span>我的消息</span></div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useUserStore } from '../../stores/user'
import { pointsMine } from '../../api'

const store = useUserStore()
const points = ref(0)
onMounted(async () => {
  try {
    const r = await pointsMine()
    points.value = r.points ?? 0
  } catch (e) { /* 未登录忽略 */ }
})
</script>

<style scoped>
.mine { padding-bottom: 20px; }
.card.head { display: flex; align-items: center; gap: 14px; background: linear-gradient(135deg, #ff9a56, #e8611d); color: #fff; border-radius: 14px; padding: 20px; }
.avatar { width: 54px; height: 54px; border-radius: 50%; background: rgba(255,255,255,.25); display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: 700; }
.name { font-size: 17px; font-weight: 600; }
.pts { font-size: 13px; opacity: .9; margin-top: 4px; }
.grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-top: 14px; }
.cell { background: #fff; border-radius: 12px; text-align: center; padding: 18px 4px; font-size: 22px; cursor: pointer; }
.cell span { display: block; font-size: 12px; color: #555; margin-top: 6px; }
</style>
