<template>
  <div v-loading="loading">
    <div class="card">
      <h4>收货信息</h4>
      <el-form :model="form" label-width="70px" style="margin-top:12px">
        <el-form-item label="收货人"><el-input v-model="form.receiverName" placeholder="收货人姓名" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.receiverPhone" placeholder="手机号" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.receiverAddress" type="textarea" placeholder="收货地址" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" placeholder="选填" /></el-form-item>
      </el-form>
    </div>

    <div class="card">
      <h4>优惠券</h4>
      <div v-if="!coupons.length" class="none-tip">所选店铺暂无可用优惠券</div>
      <el-select v-else v-model="userCouponId" placeholder="选择优惠券（仅限本单店铺）" clearable style="width: 100%">
        <el-option v-for="c in coupons" :key="c.userCouponId"
          :label="`¥${c.amount} ${c.name}（${c.storeName}，满${c.threshold}可用）`" :value="c.userCouponId" />
      </el-select>
      <div class="sum" v-if="chosenCoupon">已选券抵扣：<b class="discount">-¥{{ chosenCoupon.amount }}</b></div>
    </div>

    <div class="card">
      <h4>商品清单</h4>
      <div v-for="g in groups" :key="g.storeName" class="group">
        <div class="store-name">🏪 {{ g.storeName }}</div>
        <div v-for="it in g.items" :key="it.productId" class="item">
          <span class="n">{{ it.name }}</span>
          <span class="q">x{{ it.quantity }}</span>
          <span class="p">¥{{ (it.price * it.quantity).toFixed(2) }}</span>
        </div>
      </div>
      <div class="sum">应付总额：<b class="total">¥{{ payTotal.toFixed(2) }}</b>（多店铺商品已自动拆分为独立订单）</div>
    </div>

    <div class="card submit">
      <el-button type="danger" size="large" style="width:100%" :loading="submitting" @click="submit">提交订单</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productDetail, createOrder, couponMine, payOrder } from '../../api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const form = ref({ receiverName: '', receiverPhone: '', receiverAddress: '', remark: '' })
const items = ref([])
const groups = ref([])
const coupons = ref([])
const userCouponId = ref(null)

const total = computed(() => items.value.reduce((s, it) => s + it.price * it.quantity, 0))
const chosenCoupon = computed(() => coupons.value.find(c => c.userCouponId === userCouponId.value))
const payTotal = computed(() => {
  const d = chosenCoupon.value ? Number(chosenCoupon.value.amount) : 0
  return Math.max(0, total.value - d)
})

onMounted(async () => {
  loading.value = true
  try {
    // 直接购买 or 购物车结算
    if (route.query.productId) {
      items.value = [{ productId: +route.query.productId, quantity: +route.query.quantity || 1 }]
    } else if (route.query.items) {
      items.value = JSON.parse(route.query.items)
    }
    // 填充商品信息
    for (const it of items.value) {
      const p = await productDetail(it.productId)
      it.name = p.name
      it.price = p.price
      it.storeId = p.storeId
    }
    // 按店铺分组展示
    const storeIds = [...new Set(items.value.map(i => i.storeId))]
    groups.value = storeIds.map(sid => ({ storeName: '果园店铺 #' + sid, items: items.value.filter(i => i.storeId === sid) }))
    // 可用优惠券（限本单店铺）
    try {
      const mine = await couponMine('UNUSED')
      coupons.value = mine.filter(c => storeIds.includes(c.storeId))
    } catch (e) { coupons.value = [] }
  } finally {
    loading.value = false
  }
})

async function submit() {
  if (!form.value.receiverName || !form.value.receiverPhone || !form.value.receiverAddress) {
    return ElMessage.warning('请填写完整收货信息')
  }
  submitting.value = true
  try {
    const orders = await createOrder({
      ...form.value,
      items: items.value.map(i => ({ productId: i.productId, quantity: i.quantity })),
      userCouponId: userCouponId.value || undefined
    })
    ElMessage.success(`下单成功，共 ${orders.length} 笔订单`)
    // 逐单模拟支付
    for (const o of orders) await payOrder(o.id)
    router.push('/shop/orders?status=UNSHIPPED')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.card { background: #fff; border-radius: 10px; padding: 14px; margin-bottom: 12px; }
h4 { margin-bottom: 4px; }
.group { margin-top: 8px; }
.store-name { font-size: 13px; color: #e8611d; font-weight: 600; padding: 6px 0; border-bottom: 1px dashed #eee; }
.item { display: flex; padding: 6px 0; font-size: 13px; gap: 8px; }
.n { flex: 1; }
.q { color: #999; }
.p { color: #e8611d; font-weight: 600; }
.sum { text-align: right; margin-top: 8px; font-size: 13px; color: #666; }
.total { color: #e8611d; font-size: 18px; }
</style>
