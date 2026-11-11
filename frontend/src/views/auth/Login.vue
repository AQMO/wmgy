<template>
  <div class="auth-page">
    <div class="card">
      <h2>🍊 万亩果园</h2>
      <p class="sub">多商户果园小店平台</p>
      <el-form :model="form" @keyup.enter="onLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="onLogin">登 录</el-button>
      </el-form>
      <div class="links">
        <router-link to="/register">注册新账号</router-link>
      </div>
      <el-divider />
      <p class="tips">演示账号：admin/admin123（平台）· demo/merchant123（商家）· guohr/user123（消费者）</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const store = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function onLogin() {
  if (!form.username || !form.password) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try {
    const r = await login(form)
    store.setUser(r.token, r)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    if (redirect) return router.push(redirect)
    if (r.role === 'ADMIN') router.push('/admin/dashboard')
    else if (r.role === 'MERCHANT') router.push('/merchant/workbench')
    else router.push('/shop/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #ffe8d6, #f5f6f8); }
.card { width: 380px; background: #fff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 20px rgba(0,0,0,.08); }
h2 { text-align: center; color: #e8611d; }
.sub { text-align: center; color: #999; font-size: 13px; margin: 6px 0 20px; }
.links { text-align: right; margin-top: 12px; font-size: 13px; }
.tips { font-size: 12px; color: #999; line-height: 1.8; }
</style>
