<template>
  <view class="page-shell">
    <view class="card panel">
      <view class="section-title">编辑资料</view>
      <view class="field-label">昵称</view>
      <input v-model="form.nickname" class="input" placeholder="请输入昵称" />
      <view class="field-label">头像</view>
      <ImageUploader v-model="avatarFile" biz-type="avatar" title="上传头像" tip="上传后会自动回填头像地址" />
      <view class="field-label">性别</view>
      <input v-model="form.gender" class="input" placeholder="男 / 女 / 保密" />
      <view class="field-label">联系方式</view>
      <input v-model="form.contactPhone" class="input" placeholder="请输入联系方式" />
      <view class="field-label">个人简介</view>
      <textarea v-model="form.bio" class="input textarea" placeholder="介绍一下自己"></textarea>
      <view class="primary-btn" @click="submit">保存资料</view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import ImageUploader from '../../components/ImageUploader.vue'
import request from '../../utils/request'

const form = reactive({
  nickname: '',
  avatarUrl: '',
  gender: '',
  contactPhone: '',
  bio: '',
})
const avatarFile = ref(null)

onMounted(async () => {
  const data = await request.get('/user/profile')
  Object.assign(form, data)
  avatarFile.value = data.avatarUrl
    ? {
        fileId: null,
        url: data.avatarUrl,
        originalName: '头像',
      }
    : null
})

const submit = async () => {
  form.avatarUrl = avatarFile.value?.url || ''
  await request.put('/user/profile', form)
  uni.showToast({ title: '保存成功', icon: 'success' })
  setTimeout(() => {
    uni.switchTab({ url: '/pages/profile/index' })
  }, 400)
}
</script>

<style scoped lang="scss">
.panel {
  padding: 34rpx 28rpx;
}

.textarea {
  height: 180rpx;
  padding-top: 20rpx;
}
</style>
