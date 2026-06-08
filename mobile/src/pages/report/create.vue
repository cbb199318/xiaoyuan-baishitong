<template>
  <view class="page-shell report-create-page">
    <view class="card report-card">
      <text class="page-title">举报反馈</text>
      <text class="page-desc">提交后将由后台人工审核处理，请尽量补充清晰的问题描述和相关截图。</text>
      <view v-if="contextText" class="report-context">
        <text class="report-context-label">当前举报对象</text>
        <text class="report-context-value">{{ contextText }}</text>
      </view>

      <view class="field-block">
        <text class="field-label">违规类型</text>
        <view class="type-list">
          <view
            v-for="item in reportTypes"
            :key="item.value"
            :class="['type-chip', form.reportType === item.value ? 'active' : '']"
            @click="form.reportType = item.value"
          >
            {{ item.label }}
          </view>
        </view>
      </view>

      <view class="field-block">
        <text class="field-label">问题描述</text>
        <textarea
          v-model.trim="form.description"
          class="report-textarea"
          maxlength="300"
          placeholder="请描述你遇到的问题，例如岗位信息与实际不符、沟通中存在恶意行为等"
        />
        <view class="text-foot">
          <text class="text-tip">补充越完整，越方便后台人工核实处理。</text>
          <text class="text-count">{{ form.description.length }}/300</text>
        </view>
      </view>

      <view class="field-block">
        <text class="field-label">图片凭证上传</text>
        <ImageUploader
          v-model="attachmentFile"
          biz-type="report_attachment"
          title="上传聊天截图、岗位页面截图等凭证"
          tip="支持单张图片上传，建议上传能直观说明问题的页面截图"
        />
      </view>

      <view :class="['primary-btn', !canSubmit ? 'is-disabled' : '']" @click="canSubmit && submit()">
        提交举报
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import ImageUploader from '../../components/ImageUploader.vue'
import request from '../../utils/request'

const reportTypeMap = {
  beauty: [
    { label: '假货', value: '假货' },
    { label: '商品描述不符', value: '商品描述不符' },
    { label: '违规内容', value: '违规内容' },
    { label: '其他问题', value: '其他问题' },
  ],
  partner: [
    { label: '言语辱骂', value: '言语辱骂' },
    { label: '言语骚扰', value: '言语骚扰' },
    { label: '聊天态度恶劣', value: '聊天态度恶劣' },
    { label: '恶意爽约', value: '恶意爽约' },
    { label: '发布虚假搭子需求', value: '发布虚假搭子需求' },
    { label: '恶意调侃骚扰', value: '恶意调侃骚扰' },
    { label: '违规引流广告', value: '违规引流广告' },
    { label: '其他违规', value: '其他违规' },
  ],
  jobs: [
    { label: '虚假岗位', value: '虚假岗位' },
    { label: '薪资不符', value: '薪资不符' },
    { label: '恶意沟通', value: '恶意沟通' },
    { label: '违规广告', value: '违规广告' },
    { label: '其他问题', value: '其他问题' },
  ],
  errand: [
    { label: '恶意爽约', value: '恶意爽约' },
    { label: '虚假订单', value: '虚假订单' },
    { label: '态度恶劣', value: '态度恶劣' },
    { label: '违规广告', value: '违规广告' },
    { label: '其他问题', value: '其他问题' },
  ],
  default: [
    { label: '虚假信息', value: '虚假信息' },
    { label: '恶意沟通', value: '恶意沟通' },
    { label: '违规广告', value: '违规广告' },
    { label: '其他问题', value: '其他问题' },
  ],
}

const form = reactive({
  module: '',
  targetType: '',
  targetId: '',
  title: '',
  reportType: '',
  description: '',
  contactPhone: '',
})
const attachmentFile = ref(null)
const reportTypes = ref(reportTypeMap.default)

const canSubmit = computed(() => !!form.reportType && !!form.description)
const targetTypeLabelMap = {
  good: '商品',
  topic: '妆容专题',
  order: '订单',
  demand: '需求',
  profile: '用户主页',
  chat: '聊天记录',
}
const contextText = computed(() => {
  const moduleTextMap = {
    beauty: '美妆',
    partner: '搭子',
    errand: '跑腿',
    jobs: '勤工助学',
  }
  const moduleText = moduleTextMap[form.module] || '平台'
  const targetText = targetTypeLabelMap[form.targetType] || form.targetType || '内容'
  const titleText = form.title ? `「${form.title}」` : ''
  const idText = form.targetId ? ` #${form.targetId}` : ''
  return `${moduleText}${targetText}${titleText}${idText}`
})

const decodeParam = (value) => {
  if (!value) {
    return ''
  }
  try {
    return decodeURIComponent(value)
  } catch (error) {
    return value
  }
}

onLoad((options) => {
  form.module = options?.module || ''
  form.targetType = options?.targetType || ''
  form.targetId = options?.targetId || ''
  form.title = decodeParam(options?.title || '')
  reportTypes.value = reportTypeMap[form.module] || reportTypeMap.default
  form.reportType = reportTypes.value[0]?.value || ''
})

const submit = async () => {
  await request.post('/reports', {
    ...form,
    targetId: form.targetId ? Number(form.targetId) : null,
    attachmentFileIds: attachmentFile.value?.fileId ? [attachmentFile.value.fileId] : [],
  })
  uni.showToast({ title: '举报提交成功', icon: 'success' })
  setTimeout(() => {
    uni.navigateBack({
      fail: () => {
        uni.redirectTo({ url: '/pages/report/list' })
      },
    })
  }, 380)
}
</script>

<style scoped lang="scss">
.report-create-page {
  padding-top: 20rpx;
}

.report-card {
  padding: 32rpx 28rpx;
}

.page-title,
.field-label {
  display: block;
  color: #0f172a;
}

.page-title {
  font-size: 32rpx;
  font-weight: 800;
}

.page-desc,
.text-tip {
  display: block;
  margin-top: 10rpx;
  color: #64748b;
  font-size: 23rpx;
  line-height: 1.7;
}

.report-context {
  margin-top: 18rpx;
  padding: 18rpx 20rpx;
  border-radius: 20rpx;
  background: #eff6ff;
}

.report-context-label,
.report-context-value {
  display: block;
}

.report-context-label {
  font-size: 22rpx;
  color: #64748b;
}

.report-context-value {
  margin-top: 8rpx;
  font-size: 24rpx;
  font-weight: 700;
  color: #1e3a8a;
  line-height: 1.6;
}

.field-block {
  margin-top: 26rpx;
}

.field-label {
  font-size: 26rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
}

.type-list {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.type-chip {
  min-height: 72rpx;
  padding: 0 22rpx;
  border-radius: 999rpx;
  background: #e2e8f0;
  color: #475569;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
}

.type-chip.active {
  background: #1677ff;
  color: #ffffff;
  box-shadow: 0 12rpx 24rpx rgba(22, 119, 255, 0.14);
}

.report-textarea {
  width: 100%;
  min-height: 240rpx;
  padding: 20rpx;
  border-radius: 22rpx;
  background: #f8fafc;
  box-sizing: border-box;
  font-size: 24rpx;
}

.text-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  margin-top: 10rpx;
}

.text-count {
  flex-shrink: 0;
  color: #94a3b8;
  font-size: 22rpx;
}

.primary-btn.is-disabled {
  opacity: 0.55;
}
</style>
