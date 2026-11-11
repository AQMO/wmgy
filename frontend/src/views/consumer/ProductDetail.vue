<template>
  <div v-loading="loading" class="detail-page">
    <div class="hero">🍊</div>
    <div class="info card" v-if="product.id">
      <div class="price">¥{{ product.price }}</div>
      <div class="name">{{ product.name }}</div>
      <div class="meta">已售 {{ product.sales }} · 库存 {{ product.stock }}</div>
    </div>
    <div class="card">
      <h4>商品详情</h4>
      <p class="detail-text">{{ product.detail || '产地直发，新鲜到家' }}</p>
      <div class="extra-links">
        <el-button link type="warning" @click="$router.push(`/shop/trace/${product.id}`)">🌿 查看溯源信息</el-button>
        <el-button link type="success" @click="pickupDialog = true">🌿 预约到园采摘</el-button>
      </div>
    </div>

    <!-- 采摘预约弹窗 -->
    <el-dialog v-model="pickupDialog" title="预约到园采摘" width="90%">
      <el-form :model="pickup" label-width="80px">
        <el-form-item label="到园日期">
          <el-date-picker v-model="pickup.pickDate" type="date" value-format="YYYY-MM-DD" :disabled-date="d => d < new Date()" />
        </el-form-item>
        <el-form-item label="时间段">
          <el-radio-group v-model="pickup.timeSlot">
            <el-radio value="上午">上午</el-radio>
            <el-radio value="下午">下午</el-radio>
            <el-radio value="傍晚">傍晚</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="人数"><el-input-number v-model="pickup.quantity" :min="1" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="pickup.phone" placeholder="方便商家与您确认" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="pickup.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pickupDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPickup">提交预约</el-button>
      </template>
    </el-dialog>
    <div class="bottom-bar">
      <el-button @click="addCart">加入购物车</el-button>
      <el-button type="primary" @click="buyNow">立即购买</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productDetail, cartAdd, pickupCreate } from '../../api'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref({})
const loading = ref(false)
const pickupDialog = ref(false)
const pickup = ref({ pickDate: '', timeSlot: '上午', quantity: 1, phone: '', remark: '' })

async function submitPickup() {
  if (!userStore.isLogin) return router.push('/login')
  if (!pickup.value.pickDate) return ElMessage.warning('请选择到园日期')
  await pickupCreate({ productId: product.value.id, ...pickup.value })
  ElMessage.success('预约已提交，等待商家确认')
  pickupDialog.value = false
}

async function load() {
  loading.value = true
  try {
    product.value = await productDetail(route.params.id)
  } finally {
    loading.value = false
  }
}
async function addCart() {
  if (!userStore.isLogin) return router.push('/login')
  await cartAdd({ productId: product.value.id, quantity: 1 })
  ElMessage.success('已加入购物车')
}
function buyNow() {
  if (!userStore.isLogin) return router.push('/login')
  router.push({ path: '/shop/confirm', query: { productId: product.value.id, quantity: 1 } })
}
onMounted(load)
</script>

<style scoped>
.hero { height: 240px; border-radius: 10px; background: #fff3e6; display: flex; align-items: center; justify-content: center; font-size: 80px; }
.card { background: #fff; border-radius: 10px; padding: 14px; margin-top: 10px; }
.price { color: #e8611d; font-size: 22px; font-weight: 700; }
.name { font-size: 16px; font-weight: 600; margin-top: 4px; }
.meta { font-size: 12px; color: #999; margin-top: 6px; }
.detail-text { font-size: 13px; color: #666; line-height: 1.8; margin-top: 8px; white-space: pre-wrap; }
.bottom-bar { position: fixed; bottom: 56px; left: 50%; transform: translateX(-50%); width: 100%; max-width: 736px; background: #fff; padding: 10px 16px; display: flex; gap: 10px; border-top: 1px solid #eee; }
.bottom-bar .el-button { flex: 1; }
.extra-links { display: flex; margin-top: 8px; border-top: 1px dashed #eee; padding-top: 8px; }
</style>
