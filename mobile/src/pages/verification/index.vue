<template>
  <view class="page-shell">
    <view class="card panel">
      <view class="section-title">实名认证</view>
      <view class="status-box">
        当前状态：<text class="status">{{ current?.status || '未提交' }}</text>
      </view>
      <view class="field-label">真实姓名</view>
      <input v-model="form.realName" class="input" placeholder="请输入真实姓名" />
      <view class="field-label">身份证号</view>
      <input v-model="form.idCardNo" class="input" placeholder="请输入身份证号" />
      <view class="field-label">身份证正面</view>
      <ImageUploader v-model="frontFile" biz-type="verification_front" title="上传身份证正面" tip="拍照或从相册选择清晰图片" />
      <view class="field-label">身份证反面</view>
      <ImageUploader v-model="backFile" biz-type="verification_back" title="上传身份证反面" tip="拍照或从相册选择清晰图片" />
      <view class="primary-btn" @click="submit">提交审核</view>
      <view v-if="current?.rejectReason" class="reject">驳回原因：{{ current.rejectReason }}</view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import ImageUploader from '../../components/ImageUploader.vue'
import request from '../../utils/request'

const current = ref(null)
const form = reactive({
  realName: '',
  idCardNo: '',
  frontFileId: null,
  backFileId: null,
})
const frontFile = ref(null)
const backFile = ref(null)

const loadCurrent = async () => {
  current.value = await request.get('/verification/current')
  if (current.value) {
    form.realName = current.value.realName || ''
    form.idCardNo = current.value.idCardNo || ''
    form.frontFileId = current.value.frontFileId || null
    form.backFileId = current.value.backFileId || null
    frontFile.value = current.value.frontFileId
      ? {
          fileId: current.value.frontFileId,
          url: current.value.frontFileUrl,
          originalName: '身份证正面',
        }
      : null
    backFile.value = current.value.backFileId
      ? {
          fileId: current.value.backFileId,
          url: current.value.backFileUrl,
          originalName: '身份证反面',
        }
      : null
  }
}

onMounted(loadCurrent)

const submit = async () => {
  form.frontFileId = frontFile.value?.fileId || null
  form.backFileId = backFile.value?.fileId || null
  await request.post('/verification/submit', {
    ...form,
  })
  uni.showToast({ title: '已提交审核', icon: 'success' })
  await loadCurrent()
}
</script>

<style scoped lang="scss">
.panel {
  padding: 34rpx 28rpx;
}

.status-box {
  padding: 22rpx;
  margin-bottom: 24rpx;
  border-radius: 18rpx;
  background: #f8fafc;
}

.status {
  color: #2563eb;
  font-weight: 700;
}

.reject {
  margin-top: 20rpx;
  color: #dc2626;
}
</style>
