<template>
  <div class="trace" v-loading="loading">
    <div class="card head" v-if="data.productName">
      <img v-if="data.cover" :src="data.cover" class="cover" />
      <div>
        <div class="pname">{{ data.productName }}</div>
        <div class="store">果园：{{ data.storeName }}</div>
        <div class="tag">✅ 溯源信息由商家录入，全程留痕不可篡改</div>
      </div>
    </div>
    <el-empty v-if="!loading && data.records && !data.records.length" description="暂无溯源记录" />
    <div class="timeline" v-if="data.records && data.records.length">
      <div class="node" v-for="r in data.records" :key="r.id">
        <div class="dot"></div>
        <div class="body">
          <div class="stage">{{ r.stage }}</div>
          <div class="title">{{ r.title }}</div>
          <div class="content" v-if="r.content">{{ r.content }}</div>
          <div class="imgs" v-if="imgList(r.images).length">
            <img v-for="(im, i) in imgList(r.images)" :key="i" :src="im" @click="preview(im)" />
          </div>
          <div class="time">{{ fmt(r.eventTime) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { tracePublic } from '../../api'

const route = useRoute()
const data = ref({})
const loading = ref(false)

function imgList(images) {
  return images ? String(images).split(',').filter(Boolean) : []
}
function fmt(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}
function preview(url) {
  window.open(url)
}

onMounted(async () => {
  loading.value = true
  try {
    data.value = await tracePublic(route.params.id)
  } catch (e) {
    ElMessage.error('溯源信息加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.card { background: #fff; border-radius: 12px; padding: 16px; margin-bottom: 12px; }
.head { display: flex; gap: 12px; align-items: center; }
.cover { width: 72px; height: 72px; border-radius: 10px; object-fit: cover; }
.pname { font-size: 16px; font-weight: 700; }
.store { font-size: 13px; color: #666; margin-top: 4px; }
.tag { font-size: 11px; color: #2e7d32; margin-top: 6px; }
.node { position: relative; padding: 0 0 20px 22px; border-left: 2px solid #ffe0c7; margin-left: 8px; }
.node:last-child { border-left-color: transparent; }
.dot { position: absolute; left: -7px; top: 0; width: 12px; height: 12px; border-radius: 50%; background: #e8611d; }
.stage { display: inline-block; background: #fff3e8; color: #e8611d; font-size: 11px; border-radius: 4px; padding: 1px 8px; }
.title { font-weight: 600; margin-top: 6px; }
.content { font-size: 13px; color: #555; margin-top: 4px; white-space: pre-wrap; }
.imgs img { width: 84px; height: 84px; object-fit: cover; border-radius: 8px; margin: 6px 6px 0 0; cursor: pointer; }
.time { font-size: 11px; color: #999; margin-top: 6px; }
</style>
