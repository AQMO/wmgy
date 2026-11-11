<template>
  <div class="card" v-loading="loading">
    <h3>交易抽成规则配置</h3>
    <p class="tip">规则修改仅对新订单生效，不回溯历史订单数据（PRD 5.2）</p>

    <el-form :model="form" label-width="130px" style="max-width:560px;margin-top:20px">
      <el-form-item label="抽成模式">
        <el-radio-group v-model="form.mode">
          <el-radio value="RATIO">比例抽成（按实付百分比）</el-radio>
          <el-radio value="FIXED">固定金额抽成</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.mode === 'RATIO'" label="平台抽成比例(%)">
        <el-input-number v-model="form.ratio" :min="0" :max="100" :precision="2" />
      </el-form-item>
      <el-form-item v-else label="每单固定佣金(¥)">
        <el-input-number v-model="form.fixedAmount" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="运费计入抽成基数">
        <el-switch v-model="form.includeFreight" />
      </el-form-item>
      <el-form-item>
        <el-tag type="info" size="small">二期预留：采摘门票/电子面单独立抽成、营销分摊比例（bizType 已预留）</el-tag>
      </el-form-item>
      <el-button type="primary" @click="save">保存并生成新规则版本</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { currentRule, saveRule } from '../../api'

const form = reactive({ mode: 'RATIO', ratio: 5, fixedAmount: 2, includeFreight: false })
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const r = await currentRule()
    if (r) {
      form.mode = r.mode
      form.ratio = r.ratio ?? 5
      form.fixedAmount = r.fixedAmount ?? 2
      form.includeFreight = !!r.includeFreight
    }
  } finally { loading.value = false }
})
async function save() {
  await saveRule({ ...form })
  ElMessage.success('规则已更新，仅对新订单生效')
}
</script>

<style scoped>
.card { background: #fff; border-radius: 10px; padding: 24px; }
h3 { color: #e8611d; }
.tip { font-size: 12px; color: #999; margin-top: 6px; }
</style>
