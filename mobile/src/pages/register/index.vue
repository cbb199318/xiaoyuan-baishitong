<template>
  <view class="page-shell">
    <view class="card panel">
      <view class="section-title">新用户注册</view>
      <view class="field-label">手机号</view>
      <input v-model="form.phone" class="input" maxlength="11" placeholder="请输入手机号" />
      <view class="field-label">密码</view>
      <input v-model="form.password" class="input" password placeholder="6-16 位密码" />
      <view class="field-label">确认密码</view>
      <input v-model="form.confirmPassword" class="input" password placeholder="再次输入密码" />
      <view class="primary-btn" @click="submit">注册并登录</view>
    </view>
  </view>
</template>

<script setup>
import { reactive } from 'vue'
import request from '../../utils/request'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const form = reactive({
  phone: '',
  password: '',
  confirmPassword: '',
})

const submit = async () => {
  const data = await request.post('/auth/register', form)
  store.setToken(data.token)
  store.profile = data.user
  await store.fetchUnread()
  await store.syncAnnouncement()
  uni.switchTab({ url: '/pages/home/index' })
}
</script>

<style scoped lang="scss">
.panel {
  padding: 34rpx 28rpx;
  margin-top: 50rpx;
}
</style>
