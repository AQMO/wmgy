<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-select v-model="productId" placeholder="选择商品查看溯源" style="width: 280px" @change="loadRecords">
        <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
      <el-button type="primary" :disabled="!productId" @click="dialog = true">+ 新增溯源记录</el-button>
    </div>

    <el-timeline v-if="records.length" style="margin-top: 20px; padding-left: 6px">
      <el-timeline-item v-for="r in records" :key="r.id" :timestamp="fmt(r.eventTime || r.createdAt)" placement="top">
        <div class="t-card">
          <div class="row">
            <el-tag size="small" type="warning">{{ r.stage }}</el-tag>
            <b style="margin-left: 8px">{{ r.title }}</b>
            <el-button link type="danger" size="small" style="margin-left: auto" @click="del(r.id)">删除</el-button>
          </div>
          <div class="content" v-if="r.content">{{ r.content }}</div>
          <div class="imgs" v-if="imgList(r.images).length">
            <img v-for="(im, i) in imgList(r.images)" :key="i" :src="im" />
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else description="选择商品后查看/维护溯源链路" />

    <el-dialog v-model="dialog" title="新增溯源记录" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="阶段">
          <el-select v-model="form.stage">
            <el-option v-for="s in ['种植', '施肥', '疏果', '生长', '采摘', '质检', '包装', '入库']" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="如：人工采摘装箱" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="现场图片">
          <input type="file" accept="image/*" multiple @change="onFiles" />
          <div class="imgs">
            <img v-for="(im, i) in form.imageUrls" :key="i" :src="im" />
          </div>
        </el-form-item>
        <el-form-item label="发生时间">
          <el-date-picker v-model="form.eventTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save" :disabled="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { myProducts, traceAdd, traceByProduct, traceDelete, uploadFile } from '../../api'

const products = ref([])
const productId = ref(null)
const records = ref([])
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const form = reactive({ stage: '采摘', title: '', content: '', imageUrls: [], eventTime: '' })

onMounted(async () => {
  const r = await myProducts({ size: 100 })
  products.value = r.content || []
})

async function loadRecords() {
  if (!productId.value) return
  loading.value = true
  try { records.value = await traceByProduct(productId.value) } finally { loading.value = false }
}

async function onFiles(e) {
  for (const f of e.target.files) {
    const r = await uploadFile(f, 'TRACE')
    form.imageUrls.push(r.url)
  }
  e.target.value = ''
}

async function save() {
  if (!form.title) return ElMessage.warning('请填写标题')
  saving.value = true
  try {
    await traceAdd({ ...form, productId: productId.value })
    ElMessage.success('已录入')
    dialog.value = false
    form.title = ''
    form.content = ''
    form.imageUrls = []
    loadRecords()
  } finally {
    saving.value = false
  }
}

async function del(id) {
  await traceDelete(id)
  loadRecords()
}

function imgList(images) {
  return images ? String(images).split(',').filter(Boolean) : []
}
function fmt(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; }
.t-card { background: #fff; border-radius: 10px; padding: 12px 14px; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.row { display: flex; align-items: center; }
.content { font-size: 13px; color: #555; margin-top: 8px; white-space: pre-wrap; }
.imgs img { width: 72px; height: 72px; object-fit: cover; border-radius: 6px; margin: 8px 6px 0 0; }
</style>
