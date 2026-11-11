<template>
  <div v-loading="loading">
    <div class="store-head" v-if="storeData.store">
      <div class="cover">{{ storeData.store.name?.slice(0, 1) }}</div>
      <div class="info">
        <h2>{{ storeData.store.name }}</h2>
        <p class="intro">{{ storeData.store.intro }}</p>
        <el-tag v-if="storeData.store.notice" type="warning" size="small">📢 {{ storeData.store.notice }}</el-tag>
      </div>
    </div>

    <div class="coupon-strip" v-if="coupons.length">
      <div class="c-c" v-for="c in coupons" :key="c.id" @click="claim(c)">
        <div class="amt">¥{{ c.amount }}</div>
        <div class="cond">满{{ c.threshold }}可用 · 领取</div>
      </div>
    </div>

    <div class="cat-tabs">
      <div class="cat" :class="{ active: !curCat }" @click="curCat = null">全部</div>
      <div v-for="c in storeData.categories" :key="c.id" class="cat" :class="{ active: curCat === c.id }" @click="curCat = c.id">
        {{ c.name }}
      </div>
      <div class="cat service" @click="contact">💬 联系客服</div>
    </div>

    <div class="product-grid" v-loading="loadingProducts">
      <div v-for="p in products" :key="p.id" class="p-card" @click="$router.push(`/shop/product/${p.id}`)">
        <div class="p-img">🍊</div>
        <div class="p-name">{{ p.name }}</div>
        <div class="p-bottom">
          <span class="price">¥{{ p.price }}</span>
          <span class="sales">已售 {{ p.sales }}</span>
        </div>
      </div>
      <el-empty v-if="!loadingProducts && products.length === 0" description="该分类暂无商品" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeDetail, listProducts, visitLog, couponStoreList, couponClaim } from '../../api'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const coupons = ref([])

const route = useRoute()
const router = useRouter()
const storeData = ref({})
const products = ref([])
const curCat = ref(null)
const loading = ref(false)
const loadingProducts = ref(false)

async function loadDetail() {
  loading.value = true
  try {
    storeData.value = await storeDetail(route.params.id)
  } finally {
    loading.value = false
  }
}
async function loadProducts() {
  loadingProducts.value = true
  try {
    const page = await listProducts({ storeId: route.params.id, categoryId: curCat.value || undefined, page: 0, size: 24 })
    products.value = page.content || []
  } finally {
    loadingProducts.value = false
  }
}
watch(curCat, loadProducts)
function contact() {
  router.push({ path: '/shop/chat', query: { storeId: route.params.id } })
}
// 店铺访问埋点（流量监控数据源）
onMounted(() => { visitLog({ storeId: route.params.id, page: 'store' }).catch(() => {}) })
onMounted(() => { loadDetail(); loadProducts(); loadCoupons() })

async function loadCoupons() {
  try {
    coupons.value = await couponStoreList(route.params.id)
  } catch (e) { coupons.value = [] }
}
async function claim(c) {
  if (!userStore.isLogin) return router.push('/login')
  try {
    await couponClaim(c.id)
    ElMessage.success('领取成功，下单时可选')
    loadCoupons()
  } catch (e) { /* 失败提示由拦截器统一处理 */ }
}
</script>

<style scoped>
.store-head { display: flex; gap: 14px; background: #fff; border-radius: 10px; padding: 16px; }
.cover { width: 72px; height: 72px; border-radius: 12px; background: linear-gradient(135deg, #ffb56b, #e8611d); color: #fff; font-size: 32px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.intro { font-size: 12px; color: #888; margin: 4px 0 8px; }
.cat-tabs { display: flex; gap: 8px; margin: 12px 0; overflow-x: auto; }
.cat { background: #fff; border-radius: 16px; padding: 6px 16px; font-size: 13px; cursor: pointer; white-space: nowrap; color: #666; }
.cat.active { background: #e8611d; color: #fff; }
.product-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.p-card { background: #fff; border-radius: 10px; padding: 10px; cursor: pointer; }
.p-img { height: 110px; border-radius: 8px; background: #fff3e6; display: flex; align-items: center; justify-content: center; font-size: 44px; }
.p-name { font-size: 14px; margin: 8px 0 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.p-bottom { display: flex; justify-content: space-between; align-items: center; }
.price { color: #e8611d; font-weight: 700; }
.sales { font-size: 11px; color: #999; }
.coupon-strip { display: flex; gap: 8px; overflow-x: auto; margin-bottom: 4px; }
.c-c { background: #fff1ec; border: 1px solid #ffd4c2; border-radius: 8px; padding: 6px 12px; cursor: pointer; color: #e8611d; white-space: nowrap; }
.c-c .amt { font-size: 16px; font-weight: 700; }
.c-c .cond { font-size: 10px; }
</style>
