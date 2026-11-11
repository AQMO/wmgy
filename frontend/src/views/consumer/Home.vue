<template>
  <div>
    <el-input v-model="keyword" placeholder="搜索果园 / 商品" size="large" clearable @keyup.enter="load">
      <template #append>
        <el-button @click="load">搜索</el-button>
      </template>
    </el-input>

    <div class="banner">🌳 一果园一店铺 · 产地直发 · 新鲜到家</div>

    <div v-loading="loading" class="store-list">
      <div v-for="s in stores" :key="s.id" class="store-card" @click="$router.push(`/shop/store/${s.id}`)">
        <div class="logo">{{ s.name?.slice(0, 1) }}</div>
        <div class="info">
          <div class="name">{{ s.name }}</div>
          <div class="intro">{{ s.intro || '产地直发，新鲜到家' }}</div>
        </div>
        <el-button type="primary" size="small" round>进店</el-button>
      </div>
      <el-empty v-if="!loading && stores.length === 0" description="暂无果园店铺" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { listStores } from '../../api'

const keyword = ref('')
const stores = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const page = await listStores({ keyword: keyword.value, page: 0, size: 20 })
    stores.value = page.content || []
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped>
.banner { margin: 12px 0; padding: 18px; border-radius: 10px; background: linear-gradient(135deg, #ffb56b, #e8611d); color: #fff; text-align: center; font-weight: 600; }
.store-card { display: flex; align-items: center; gap: 12px; background: #fff; border-radius: 10px; padding: 14px; margin-bottom: 10px; cursor: pointer; }
.store-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,.08); }
.logo { width: 52px; height: 52px; border-radius: 10px; background: #ffe3cc; color: #e8611d; font-size: 24px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.info { flex: 1; min-width: 0; }
.name { font-weight: 600; }
.intro { font-size: 12px; color: #999; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 4px; }
</style>
