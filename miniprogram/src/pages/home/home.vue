<template>
  <view class="page">
    <!-- 果园身份卡：小程序即本园店铺，不做多店聚合 -->
    <view class="card head">
      <view class="logo">{{ storeName.slice(0, 1) }}</view>
      <view class="hinfo">
        <view class="hline">
          <text class="hname">{{ storeName }}</text>
          <text class="badge" :class="{ off: !opening }">{{ opening ? '营业中' : '休息中' }}</text>
        </view>
        <view class="hsub">{{ subline }}</view>
      </view>
    </view>

    <view class="notice" v-if="store.notice">园主说：{{ store.notice }}</view>

    <view class="search-bar">
      <input class="search-input" v-model="keyword" placeholder="搜本园 苹果 / 梨 / 桃" confirm-type="search" />
      <view class="ico" @click="goChat">问园主</view>
    </view>

    <scroll-view scroll-x class="cats">
      <view class="cat" :class="{ active: !curCat }" @click="curCat = null">全部</view>
      <view v-for="c in categories" :key="c.id" class="cat" :class="{ active: curCat === c.id }" @click="curCat = c.id">
        {{ c.name }}
      </view>
    </scroll-view>

    <view class="grid">
      <view v-for="p in products" :key="p.id" class="p-card" @click="goProduct(p.id)">
        <view class="p-img"><view class="fruit"></view></view>
        <view class="p-name">{{ p.name }}</view>
        <view class="p-bottom">
          <text class="price">¥{{ p.price }}</text>
          <text class="sales">已售{{ p.sales || 0 }}</text>
        </view>
      </view>
    </view>
    <view v-if="products.length === 0" class="empty">{{ keyword ? '没有匹配的果子' : '本园暂未上架商品' }}</view>
  </view>
</template>

<script setup>
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import { apiStoreDetail, apiProducts } from '../../utils/api'
import { getStoreId, setStoreId, cacheStore, getCachedStore, storeNameOf } from '../../utils/store'
import { visit } from '../../utils/request'

const storeId = ref(getStoreId())
const store = ref(getCachedStore() || {})
const categories = ref([])
const all = ref([])
const curCat = ref(null)
const keyword = ref('')

const storeName = computed(() => storeNameOf(store.value))
const opening = computed(() => (store.value.status || 'ACTIVE') === 'ACTIVE')
const subline = computed(() => {
  const parts = [store.value.address, store.value.intro].filter(Boolean)
  return parts.length ? parts.join(' · ') : '产地直发 · 新鲜到家'
})
const products = computed(() => {
  const k = keyword.value.trim()
  return all.value.filter(p =>
    (!curCat.value || p.categoryId === curCat.value) && (!k || (p.name || '').includes(k))
  )
})

async function load() {
  try {
    const d = await apiStoreDetail(storeId.value)
    store.value = d.store || {}
    categories.value = d.categories || []
    cacheStore(store.value)
    const page = await apiProducts({ storeId: storeId.value })
    all.value = page.content || []
  } catch (e) { }
}
function goProduct(id) { uni.navigateTo({ url: '/pages/product/product?id=' + id }) }
function goChat() { uni.navigateTo({ url: '/pages/chat/chat?storeId=' + storeId.value }) }

onLoad(q => { if (q.storeId) { setStoreId(q.storeId); storeId.value = getStoreId() } })
onShow(() => { load(); visit(storeId.value, 'home') })
onPullDownRefresh(async () => { await load(); uni.stopPullDownRefresh() })
</script>

<style scoped>
.head { display: flex; align-items: center; gap: 20rpx; margin-top: 24rpx; }
.logo { width: 100rpx; height: 100rpx; border-radius: 24rpx; background: #eaf3de; color: #3b6d11; font-size: 44rpx; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.hinfo { flex: 1; min-width: 0; }
.hline { display: flex; align-items: center; gap: 12rpx; }
.hname { font-size: 32rpx; font-weight: 700; color: #2c2c2a; }
.badge { font-size: 20rpx; color: #3b6d11; background: #eaf3de; border-radius: 20rpx; padding: 4rpx 12rpx; }
.badge.off { color: #888780; background: #f1efe8; }
.hsub { font-size: 22rpx; color: #888780; margin-top: 8rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.notice { margin: 0 24rpx 8rpx; padding: 12rpx 20rpx; background: #faeeda; color: #854f0b; font-size: 22rpx; border-radius: 12rpx; }
.search-bar { display: flex; gap: 12rpx; padding: 16rpx 24rpx; align-items: center; }
.search-input { flex: 1; background: #fff; border-radius: 40rpx; padding: 14rpx 28rpx; font-size: 26rpx; }
.ico { color: #3b6d11; font-size: 24rpx; background: #eaf3de; border-radius: 40rpx; padding: 12rpx 22rpx; }
.cats { white-space: nowrap; padding: 4rpx 24rpx 8rpx; }
.cat { display: inline-block; background: #fff; border-radius: 32rpx; padding: 10rpx 28rpx; margin-right: 14rpx; color: #666; font-size: 26rpx; }
.cat.active { background: #3b6d11; color: #fff; }
.grid { display: flex; flex-wrap: wrap; padding: 8rpx 16rpx 24rpx; gap: 16rpx; }
.p-card { width: calc(50% - 24rpx); background: #fff; border-radius: 16rpx; padding: 16rpx; box-sizing: border-box; }
.p-img { height: 200rpx; border-radius: 12rpx; background: #eaf3de; display: flex; align-items: center; justify-content: center; }
.fruit { width: 76rpx; height: 76rpx; border-radius: 50%; background: #97c459; }
.p-name { font-size: 26rpx; color: #2c2c2a; margin: 12rpx 0 8rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.p-bottom { display: flex; justify-content: space-between; align-items: center; }
.price { font-size: 30rpx; }
.sales { font-size: 20rpx; color: #b4b2a9; }
.empty { text-align: center; color: #999; padding: 80rpx 0; }
</style>
