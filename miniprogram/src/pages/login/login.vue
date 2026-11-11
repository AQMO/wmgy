<template>
  <view class="page">
    <view class="logo"><view class="fruit"></view></view>
    <view class="title">{{ storeName }}</view>
    <view class="sub">一园一码 · 扫码进园</view>

    <view class="card form">
      <input v-model="form.username" placeholder="用户名" class="ipt" />
      <input v-if="mode === 'register'" v-model="form.nickname" placeholder="昵称（选填）" class="ipt" />
      <input v-model="form.password" placeholder="密码" password class="ipt" />
      <button class="btn-primary submit" @click="submit">{{ mode === 'register' ? '注 册' : '登 录' }}</button>
      <button class="wx-btn" @click="wxLogin">微信一键登录</button>
      <view class="reg-link" @click="toggleMode">{{ mode === 'register' ? '已有账号？去登录 ›' : '注册新账号 ›' }}</view>
    </view>

    <view class="current">
      <text class="cdot"></text>
      <text>当前果园：{{ storeName }}{{ store.address ? '（' + store.address + '）' : '' }}</text>
    </view>
    <view class="agree">登录即代表同意《用户协议》与《隐私政策》</view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { apiLogin, apiWxLogin, apiRegister } from '../../utils/api'
import { getCachedStore, storeNameOf } from '../../utils/store'

const form = reactive({ username: '', password: '', nickname: '' })
const mode = ref('login')
const store = ref(getCachedStore() || {})
const storeName = computed(() => storeNameOf(store.value))

function toggleMode() { mode.value = mode.value === 'register' ? 'login' : 'register' }

function done(r) {
  uni.setStorageSync('token', r.token)
  uni.setStorageSync('user', r)
  uni.showToast({ title: '登录成功' })
  setTimeout(() => uni.switchTab({ url: '/pages/home/home' }), 600)
}
async function submit() {
  if (!form.username || !form.password) {
    return uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
  }
  if (mode.value === 'register') {
    if (form.password.length < 6) return uni.showToast({ title: '密码至少 6 位', icon: 'none' })
    done(await apiRegister(form))
  } else {
    done(await apiLogin(form))
  }
}
function wxLogin() {
  uni.login({
    provider: 'weixin',
    success: async res => {
      // code 换 Token；后端未配置微信 AppID 时为开发模式
      const r = await apiWxLogin({ code: res.code, nickname: '微信用户' })
      done(r)
    },
    fail: () => uni.showToast({ title: 'wx.login 失败', icon: 'none' })
  })
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; align-items: center; padding-top: 110rpx; }
.logo { width: 140rpx; height: 140rpx; border-radius: 36rpx; background: #eaf3de; display: flex; align-items: center; justify-content: center; }
.fruit { width: 76rpx; height: 76rpx; border-radius: 50%; background: #97c459; }
.title { font-size: 40rpx; font-weight: 700; color: #2c2c2a; margin-top: 22rpx; }
.sub { color: #888780; font-size: 24rpx; margin: 10rpx 0 36rpx; }
.form { width: 86%; display: flex; flex-direction: column; gap: 20rpx; }
.ipt { background: #f7f7f2; border-radius: 40rpx; padding: 22rpx 32rpx; font-size: 26rpx; }
.submit { margin-top: 8rpx; }
.wx-btn { background: #fff; border: 1rpx solid #639922; color: #3b6d11; border-radius: 40rpx; font-size: 28rpx; }
.reg-link { text-align: center; color: #3b6d11; font-size: 24rpx; padding: 8rpx 0; }
.current { display: flex; align-items: center; gap: 10rpx; margin-top: 28rpx; background: #eaf3de; border-radius: 12rpx; padding: 12rpx 20rpx; font-size: 22rpx; color: #3b6d11; }
.cdot { width: 12rpx; height: 12rpx; border-radius: 4rpx; background: #639922; }
.agree { color: #b4b2a9; font-size: 20rpx; margin-top: 20rpx; }
</style>
