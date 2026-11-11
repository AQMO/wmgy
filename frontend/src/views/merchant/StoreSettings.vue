<template>
  <div class="card" v-loading="loading">
    <h3>店铺装修设置</h3>
    <p class="tip">保存后实时同步至消费者端果园店铺首页</p>
    <el-form :model="form" label-width="90px" style="max-width:560px;margin-top:16px">
      <el-form-item label="店铺名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="店铺简介"><el-input v-model="form.intro" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="店铺公告"><el-input v-model="form.notice" placeholder="如：满99减5，坏果包赔" /></el-form-item>
      <el-form-item label="店铺地址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="LOGO链接"><el-input v-model="form.logo" /></el-form-item>
      <el-form-item label="封面链接"><el-input v-model="form.cover" /></el-form-item>
      <el-form-item label="线下自提">
        <el-switch v-model="form.selfPickup" active-text="开启（订单跳过发货填单）" />
      </el-form-item>
      <el-button type="primary" @click="save">保存设置</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { myStore, updateStore } from '../../api'

const form = reactive({ name: '', intro: '', notice: '', address: '', logo: '', cover: '', selfPickup: false })
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const s = await myStore()
    Object.assign(form, s)
  } finally { loading.value = false }
})
async function save() {
  await updateStore({ ...form })
  ElMessage.success('保存成功，已同步至店铺首页')
}
</script>

<style scoped>
.card { background: #fff; border-radius: 10px; padding: 24px; }
h3 { color: #e8611d; }
.tip { font-size: 12px; color: #999; margin-top: 6px; }
</style>
