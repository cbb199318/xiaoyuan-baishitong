<template>
  <view class="page-shell login-page">
    <view class="hero">
      <text class="brand">校园百事通</text>
      <text class="sub">校园一站式服务平台</text>
    </view>
    <view class="card panel">
      <view class="section-title">账号登录</view>
      <view class="field-label">手机号</view>
      <input v-model="form.phone" class="input" maxlength="11" placeholder="请输入手机号" />
      <view class="field-label">密码</view>
      <input v-model="form.password" class="input" password placeholder="请输入密码" />
      <view :class="['primary-btn', submitting ? 'is-disabled' : '']" @click="submit">
        {{ submitting ? '登录中...' : '登录' }}
      </view>
      <view class="link-row">
        <text @click="goRegister">没有账号？去注册</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const submitting = ref(false)
const form = reactive({
  phone: '18800000001',
  password: 'user123',
})

const submit = async () => {
  if (submitting.value) {
    return
  }
  submitting.value = true
  try {
    const data = await request.post('/auth/login', form)
    store.setToken(data.token)
    store.setProfile(data.user)
    await store.fetchUnread()
    await store.syncAnnouncement()
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/home/index' })
    }, 200)
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const goRegister = () => {
  uni.navigateTo({ url: '/pages/register/index' })
}
</script>

<style scoped lang="scss">
.login-page {
  background: radial-gradient(circle at top, #dbeafe 0, #f4f7fb 45%, #f4f7fb 100%);
}

.hero {
  padding: 80rpx 6rpx 50rpx;
}

.brand {
  display: block;
  font-size: 58rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 16rpx;
}

.sub {
  display: block;
  color: #64748b;
}

.panel {
  padding: 34rpx 28rpx;
}

.link-row {
  padding-top: 26rpx;
  text-align: center;
  color: #2d7ff9;
}

.is-disabled {
  opacity: 0.7;
}
</style>
