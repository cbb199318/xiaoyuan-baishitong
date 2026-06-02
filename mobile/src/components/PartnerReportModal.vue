<template>
  <view v-if="visible" class="partner-report-mask" @click="handleClose">
    <view class="partner-report-panel" @click.stop>
      <view class="partner-report-header">
        <text class="partner-report-title">发起举报</text>
        <text class="partner-report-close" @click="handleClose">关闭</text>
      </view>

      <scroll-view scroll-y class="partner-report-scroll">
        <view class="partner-report-section">
          <text class="partner-report-label">违规类型</text>
          <view class="partner-report-chip-group">
            <view
              v-for="item in reportTypeOptions"
              :key="item"
              :class="['partner-report-chip', form.reportType === item ? 'active' : '']"
              @click="form.reportType = item"
            >
              {{ item }}
            </view>
          </view>
        </view>

        <view class="partner-report-section">
          <text class="partner-report-label">违规描述</text>
          <textarea
            v-model.trim="form.description"
            class="partner-report-textarea"
            maxlength="200"
            placeholder="请补充说明具体违规行为，便于平台核查"
          />
        </view>

        <view class="partner-report-section">
          <text class="partner-report-label">截图凭证</text>
          <ImageUploader
            v-model="attachmentFile"
            biz-type="report_attachment"
            title="上传举报截图"
            tip="可上传聊天截图、页面截图、违规凭证"
          />
        </view>

        <view class="partner-report-section">
          <text class="partner-report-label">联系方式</text>
          <input
            v-model.trim="form.contactPhone"
            class="partner-report-input"
            maxlength="20"
            placeholder="选填，便于平台联系你"
          />
        </view>
      </scroll-view>

      <view class="partner-report-actions">
        <view class="partner-report-btn partner-report-btn--ghost" @click="handleClose">取消</view>
        <view class="partner-report-btn" @click="submit">提交举报</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import ImageUploader from './ImageUploader.vue'
import request from '../utils/request'

const reportTypeOptions = [
  '言语辱骂',
  '言语骚扰',
  '恶意爽约',
  '虚假需求',
  '态度恶劣',
  '恶意调侃',
  '违规广告',
  '其他违规',
]

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  module: {
    type: String,
    default: 'partner',
  },
  targetType: {
    type: String,
    default: 'demand',
  },
  targetId: {
    type: [String, Number],
    default: '',
  },
})

const emit = defineEmits(['update:visible', 'submitted'])

const attachmentFile = ref(null)
const form = reactive({
  reportType: '',
  description: '',
  contactPhone: '',
})

const resetForm = () => {
  form.reportType = ''
  form.description = ''
  form.contactPhone = ''
  attachmentFile.value = null
}

watch(
  () => props.visible,
  (value) => {
    if (value) {
      resetForm()
    }
  }
)

const handleClose = () => {
  emit('update:visible', false)
}

const submit = async () => {
  if (!form.reportType || !form.description) {
    uni.showToast({ title: '请补充违规类型和描述', icon: 'none' })
    return
  }
  await request.post('/reports', {
    module: props.module,
    targetType: props.targetType,
    targetId: props.targetId ? Number(props.targetId) || props.targetId : null,
    reportType: form.reportType,
    description: form.description,
    contactPhone: form.contactPhone,
    attachmentFileIds: attachmentFile.value?.fileId ? [attachmentFile.value.fileId] : [],
  })
  uni.showToast({ title: '举报已提交', icon: 'success' })
  emit('submitted')
  emit('update:visible', false)
}
</script>

<style scoped lang="scss">
.partner-report-mask {
  position: fixed;
  inset: 0;
  z-index: 80;
  background: rgba(15, 23, 42, 0.38);
  display: flex;
  align-items: flex-end;
}

.partner-report-panel {
  width: 100%;
  max-height: 82vh;
  border-radius: 28rpx 28rpx 0 0;
  background: #ffffff;
  overflow: hidden;
}

.partner-report-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  border-bottom: 1px solid #eef2f7;
}

.partner-report-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.partner-report-close {
  font-size: 24rpx;
  color: #64748b;
}

.partner-report-scroll {
  max-height: calc(82vh - 164rpx);
}

.partner-report-section {
  padding: 22rpx 28rpx 0;
}

.partner-report-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 24rpx;
  font-weight: 700;
  color: #1e293b;
}

.partner-report-chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.partner-report-chip {
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  background: #edf2f7;
  color: #475569;
  font-size: 22rpx;
}

.partner-report-chip.active {
  background: linear-gradient(135deg, #ef4444, #fb7185);
  color: #ffffff;
}

.partner-report-input,
.partner-report-textarea {
  width: 100%;
  box-sizing: border-box;
  border-radius: 20rpx;
  background: #f8fafc;
  border: 2rpx solid #e2e8f0;
  padding: 18rpx 20rpx;
}

.partner-report-textarea {
  min-height: 180rpx;
  font-size: 24rpx;
  line-height: 1.7;
}

.partner-report-actions {
  display: flex;
  gap: 14rpx;
  padding: 22rpx 28rpx calc(22rpx + env(safe-area-inset-bottom));
}

.partner-report-btn {
  flex: 1;
  min-height: 84rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #ef4444, #fb7185);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

.partner-report-btn--ghost {
  background: #e2e8f0;
  color: #334155;
}
</style>
