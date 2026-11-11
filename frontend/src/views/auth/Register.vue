<template>
  <div class="auth-page">
    <div class="card">
      <h2>注册账号</h2>
      <p class="sub">注册即成为消费者，商家可后续申请入驻果园</p>
      <el-form :model="form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名（3-20位）" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.nickname" placeholder="昵称（选填）" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.phone" placeholder="手机号（选填）" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码（6-32位）" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="onRegister">注 册</el-button>
      </el-form>
      <div class="links"><router-link to="/login">已有账号？去登录</router-link></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '', nickname: '', phone: '' })

async function onRegister() {
  if (!form.username || !form.password) return ElMessage.warning('请填写用户名和密码')
  if (form.password.length < 6) return ElMessage.warning('密码至少 6 位')
  loading.value = true
  try {
    const r = await register(form)
    store.setUser(r.token, r)
    ElMessage.success('注册成功')
    router.push('/shop/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #ffe8d6, #f5f6f8); }
.card { width: 380px; background: #fff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 20px rgba(0,0,0,.08); }
h2 { text-align: center; color: #e8611d; }
.sub { text-align: center; color: #999; font-size: 12px; margin: 6px 0 20px; }
.links { text-align: right; margin-top: 12px; font-size: 13px; }
</style>
