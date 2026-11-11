<template>
  <div>
    <div class="card" v-loading="loading">
      <div v-for="(c, i) in items" :key="c.id" class="cart-item">
        <div class="p-img">🍊</div>
        <div class="info">
          <div class="name">{{ c.name }}</div>
          <div class="price">¥{{ c.price }}</div>
        </div>
        <div class="ops">
          <el-input-number v-model="c.quantity" :min="1" :max="c.stock" size="small" @change="q => change(c, q)" />
          <el-button link type="danger" size="small" @click="remove(c)">删除</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && items.length === 0" description="购物车空空如也，去逛逛果园吧~">
        <el-button type="primary" @click="$router.push('/shop/home')">去逛逛</el-button>
      </el-empty>
    </div>

    <div class="checkout card" v-if="items.length">
      <span>合计：<b class="total">¥{{ total.toFixed(2) }}</b></span>
      <el-button type="primary" @click="checkout">去结算（{{ items.length }}）</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { cartList, cartUpdate, cartRemove } from '../../api'

const router = useRouter()
const items = ref([])
const loading = ref(false)

const total = computed(() => items.value.reduce((s, c) => s + (c.price || 0) * c.quantity, 0))

async function load() {
  loading.value = true
  try { items.value = await cartList() } finally { loading.value = false }
}
async function change(c, q) {
  await cartUpdate(c.id, q)
}
async function remove(c) {
  await cartRemove(c.id)
  load()
}
function checkout() {
  const sel = items.value.map(c => ({ productId: c.productId, quantity: c.quantity }))
  router.push({ path: '/shop/confirm', query: { items: JSON.stringify(sel) } })
}
onMounted(load)
</script>

<style scoped>
.card { background: #fff; border-radius: 10px; padding: 8px 14px; }
.cart-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid #f2f2f2; }
.p-img { width: 60px; height: 60px; border-radius: 8px; background: #fff3e6; display: flex; align-items: center; justify-content: center; font-size: 28px; }
.info { flex: 1; }
.price { color: #e8611d; font-weight: 600; margin-top: 4px; }
.ops { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; }
.checkout { margin-top: 12px; display: flex; justify-content: space-between; align-items: center; padding: 14px; }
.total { color: #e8611d; font-size: 18px; }
</style>
