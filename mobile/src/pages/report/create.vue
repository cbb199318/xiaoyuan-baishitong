<template>
  <view class="page-shell">
    <view class="card panel">
      <view class="section-title">发起举报</view>
      <view class="field-label">所属模块</view>
      <input v-model="form.module" class="input" placeholder="如 errand / partner / beauty / job" />
      <view class="field-label">目标类型</view>
      <input v-model="form.targetType" class="input" placeholder="如 user / order / message" />
      <view class="field-label">目标 ID</view>
      <input v-model="form.targetId" class="input" placeholder="没有可留空" />
      <view class="field-label">举报类型</view>
      <input v-model="form.reportType" class="input" placeholder="如 虚假信息 / 骚扰 / 违规内容" />
      <view class="field-label">详细描述</view>
      <textarea v-model="form.description" class="input textarea" placeholder="请输入举报说明"></textarea>
      <view class="field-label">联系方式</view>
      <input v-model="form.contactPhone" class="input" placeholder="方便平台联系你" />
      <view class="field-label">举报截图</view>
      <ImageUploader v-model="attachmentFile" biz-type="report_attachment" title="上传举报截图" tip="可上传聊天截图、违规页面截图等" />
      <view class="primary-btn" @click="submit">提交举报</view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import ImageUploader from '../../components/ImageUploader.vue'
import request from '../../utils/request'

const form = reactive({
  module: '',
  targetType: '',
  targetId: '',
  reportType: '',
  description: '',
  contactPhone: '',
})
const attachmentFile = ref(null)

onLoad((options) => {
  form.module = options?.module || ''
  form.targetType = options?.targetType || ''
  form.targetId = options?.targetId || ''
})

const submit = async () => {
  await request.post('/reports', {
    ...form,
    targetId: form.targetId ? Number(form.targetId) : null,
    attachmentFileIds: attachmentFile.value?.fileId ? [attachmentFile.value.fileId] : [],
  })
  uni.showToast({ title: '提交成功', icon: 'success' })
  setTimeout(() => {
    uni.navigateTo({ url: '/pages/report/list' })
  }, 400)
}
</script>

<style scoped lang="scss">
.panel {
  padding: 34rpx 28rpx;
}

.textarea {
  height: 220rpx;
  padding-top: 20rpx;
}
</style>
