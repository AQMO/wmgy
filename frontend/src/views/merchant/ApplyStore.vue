<template>
  <div class="apply-page" v-loading="loading">
    <div class="card" v-if="store">
      <template v-if="store.status === 'PENDING'">
        <el-result icon="info" title="入驻申请审核中" sub-title="平台将尽快人工审核您的果园资质，审核通过后请缴纳保证金正式开店">
          <template #extra>
            <el-button type="primary" @click="refresh">刷新状态</el-button>
          </template>
        </el-result>
      </template>
      <template v-else-if="store.status === 'ACTIVE'">
        <el-result icon="success" title="店铺已开通" sub-title="保证金已缴纳，开始经营您的果园小店吧">
          <template #extra>
            <el-button type="primary" @click="$router.push('/merchant/workbench')">进入工作台</el-button>
          </template>
        </el-result>
      </template>
      <template v-else>
        <el-result icon="warning" :title="store.status === 'FROZEN' ? '店铺已被冻结' : '店铺已关停'" sub-title="如有疑问请联系平台管理员" />
      </template>
    </div>

    <div class="card" v-else>
      <h3>果园入驻申请</h3>
      <p class="tip">提交后等待平台人工审核，通过后缴纳保证金即可开通店铺</p>
      <el-form :model="form" label-width="90px" style="margin-top:16px">
        <el-form-item label="果园名称" required><el-input v-model="form.name" placeholder="如：万亩一号果园" /></el-form-item>
        <el-form-item label="果园地址" required><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="果园介绍"><el-input v-model="form.intro" type="textarea" :rows="3" placeholder="向消费者介绍您的果园特色" /></el-form-item>
        <el-form-item label="资质材料"><el-input v-model="form.logo" placeholder="资质/LOGO 图片链接（演示环境可留空）" /></el-form-item>
        <el-form-item label="保证金">
          <el-tag type="warning">审核通过后按平台标准（默认 ¥5000）缴纳</el-tag>
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" @click="submit">提交审核</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { applyStore, myStore } from '../../api'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const store = ref(null)
const loading = ref(false)
const form = reactive({ name: '', address: '', intro: '', logo: '', cover: '' })

async function load() {
  loading.value = true
  try {
    store.value = await myStore().catch(() => null)
    if (store.value?.status === 'ACTIVE' && userStore.role !== 'MERCHANT') await userStore.refresh?.()
  } finally { loading.value = false }
}
async function refresh() { await load(); ElMessage.success('已刷新') }
async function submit() {
  if (!form.name || !form.address) return ElMessage.warning('请填写果园名称和地址')
  await applyStore(form)
  ElMessage.success('申请已提交，等待平台审核')
  load()
}
onMounted(load)
</script>

<style scoped>
.apply-page { max-width: 560px; margin: 0 auto; }
.card { background: #fff; border-radius: 10px; padding: 24px; margin-top: 20px; }
h3 { color: #e8611d; }
.tip { font-size: 12px; color: #999; margin-top: 6px; }
</style>
