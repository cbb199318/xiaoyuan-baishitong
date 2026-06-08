<template>
  <view :class="[embedded ? 'embedded-page' : 'page-shell']">
    <view class="publish-navbar">
      <view class="nav-side nav-left" hover-class="nav-hover" @click="goBack">‹</view>
      <view class="nav-center">
        <text class="nav-title">{{ pageTitle }}</text>
      </view>
      <view class="nav-side nav-placeholder"></view>
    </view>

    <scroll-view :class="['publish-scroll', embedded ? 'with-tabbar' : '']" scroll-y>
      <view v-if="isRejectedEdit" class="reject-banner">
        <text class="reject-banner-title">当前岗位曾被驳回，请修改后重新提交</text>
        <text class="reject-banner-desc">{{ rejectReasonText }}</text>
      </view>

      <view :class="['publish-card', embedded ? 'main-card' : '']">
        <text class="section-title">通用表单</text>

        <view class="field-block">
          <text class="field-label"><text class="required">*</text>需求 / 岗位类型</text>
          <view class="chip-group">
            <view
              v-for="item in jobsPublishTypeOptions"
              :key="item.value"
              :class="['chip', form.jobType === item.value ? 'active' : '']"
              @click="form.jobType = item.value"
            >
              {{ item.label }}
            </view>
          </view>
          <text v-if="showErrors && !form.jobType" class="field-error">请选择需求 / 岗位类型</text>
        </view>

        <view class="field-block">
          <text class="field-label"><text class="required">*</text>需求标题</text>
          <input v-model.trim="form.title" class="field-input" maxlength="40" placeholder="请输入本次发布的核心标题" />
          <text v-if="showErrors && !form.title" class="field-error">请填写需求标题</text>
        </view>

        <view class="field-block">
          <text class="field-label"><text class="required">*</text>服务形式</text>
          <view class="chip-group">
            <view
              v-for="item in serviceModeOptions"
              :key="item.value"
              :class="['chip', form.serviceMode === item.value ? 'active' : '']"
              @click="form.serviceMode = item.value"
            >
              {{ item.label }}
            </view>
          </view>
          <text v-if="showErrors && !form.serviceMode" class="field-error">请选择服务形式</text>
        </view>

        <view class="field-block">
          <text class="field-label"><text class="required">*</text>服务 / 工作地点</text>
          <input
            v-model.trim="form.location"
            class="field-input"
            maxlength="40"
            :placeholder="form.serviceMode === 'ONLINE' ? '例如 线上沟通 / 云端交付' : '请输入校园内具体地点'"
          />
          <text v-if="showErrors && !form.location" class="field-error">请填写服务 / 工作地点</text>
        </view>

        <view class="field-block">
          <text class="field-label"><text class="required">*</text>预期服务 / 工作时间</text>
          <view class="picker-row">
            <picker mode="date" :value="form.serviceDate" @change="handleDateChange">
              <view :class="['picker-field', form.serviceDate ? 'filled' : '']">
                {{ form.serviceDate || '选择日期' }}
              </view>
            </picker>
            <picker mode="time" :value="form.serviceTime" @change="handleTimeChange">
              <view :class="['picker-field', form.serviceTime ? 'filled' : '']">
                {{ form.serviceTime || '选择时间' }}
              </view>
            </picker>
          </view>
          <text v-if="showErrors && (!form.serviceDate || !form.serviceTime)" class="field-error">请选择完整时间</text>
        </view>

        <view class="field-block">
          <text class="field-label"><text class="required">*</text>酬劳 / 薪资预算</text>
          <view class="money-row">
            <text class="money-prefix">￥</text>
            <input v-model.trim="form.salaryText" type="digit" class="money-input" maxlength="8" placeholder="请输入金额" />
          </view>
          <text v-if="showErrors && !form.salaryText" class="field-error">请填写酬劳 / 薪资预算</text>
        </view>

        <view class="field-block">
          <text class="field-label">详细需求描述</text>
          <textarea
            v-model.trim="form.summary"
            class="field-textarea"
            maxlength="300"
            placeholder="可补充工作细节、交付标准、特殊要求、注意事项"
          />
          <view class="field-foot">
            <text class="field-helper">选填，补充越完整越方便匹配。</text>
            <text class="field-count">{{ form.summary.length }}/300</text>
          </view>
        </view>
      </view>

      <view v-if="isBusiness" class="publish-card">
        <text class="section-title">商家资质</text>

        <view class="field-block">
          <text class="field-label"><text class="required">*</text>商家资质照片上传</text>
          <ImageUploader
            v-model="form.credentialFile"
            biz-type="jobs_credential"
            title="上传营业执照、门店资质证明照片"
            tip="支持预览、重新上传和替换图片，未上传资质无法提交审核"
          />
          <text v-if="showErrors && !form.credentialFile?.url" class="field-error">请上传商家资质照片</text>
        </view>
      </view>
    </scroll-view>

    <view :class="['publish-footer', embedded ? 'with-tabbar' : '']">
      <view class="footer-actions">
        <view class="footer-btn ghost" @click="resetForm">重置</view>
        <view :class="['footer-btn', canSubmit ? '' : 'disabled']" @click="canSubmit && submit()">
          {{ isBusiness ? '提交审核' : '立即发布' }}
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import ImageUploader from '../../components/ImageUploader.vue'
import { useUserStore } from '../../stores/user'
import {
  createJobPost,
  getJobDetail,
  updateJobPost,
} from '../../utils/jobsApi'
import { jobsPublishTypeOptions } from '../../utils/jobsMeta'

const props = defineProps({
  embedded: {
    type: Boolean,
    default: false,
  },
})

const store = useUserStore()
const showErrors = ref(false)

const serviceModeOptions = [
  { label: '线上完成', value: 'ONLINE' },
  { label: '线下校园完成', value: 'OFFLINE' },
]

const createDefaultForm = () => ({
  jobType: '',
  title: '',
  serviceMode: '',
  location: '',
  serviceDate: '',
  serviceTime: '',
  salaryText: '',
  summary: '',
  credentialFile: null,
})

const form = reactive(createDefaultForm())
const editId = ref('')
const rejectReason = ref('')
const isEditMode = computed(() => !!editId.value)
const isRejectedEdit = computed(() => isEditMode.value && isBusiness.value && !!rejectReason.value)
const rejectReasonText = computed(() => rejectReason.value || '请根据驳回原因补充资料后再次提交。')

const currentRoleType = computed(() => {
  const raw = String(store.profile?.publishRole || store.profile?.roleType || '').toUpperCase()
  return raw === 'BUSINESS' || raw === 'MERCHANT' || raw === 'ADMIN' ? 'BUSINESS' : 'STUDENT'
})

const isBusiness = computed(() => currentRoleType.value === 'BUSINESS')

const pageTitle = computed(() => {
  if (isEditMode.value) {
    return isRejectedEdit.value ? '修改并重新提交岗位' : '编辑已发布内容'
  }
  return isBusiness.value ? '发布校园兼职岗位' : '发布勤工互助需求'
})

const submitSuccessText = computed(() => {
  if (isRejectedEdit.value) {
    return '已重新提交审核'
  }
  if (isEditMode.value) {
    return '已保存修改'
  }
  return isBusiness.value ? '已提交审核' : '已发布成功'
})

const canSubmit = computed(() => {
  if (
    !form.jobType ||
    !form.title ||
    !form.serviceMode ||
    !form.location ||
    !form.serviceDate ||
    !form.serviceTime ||
    !form.salaryText
  ) {
    return false
  }
  if (isBusiness.value && !form.credentialFile?.url) {
    return false
  }
  return true
})

const resetForm = () => {
  editId.value = ''
  rejectReason.value = ''
  Object.assign(form, createDefaultForm())
  showErrors.value = false
}

const handleDateChange = (event) => {
  form.serviceDate = event.detail.value
}

const handleTimeChange = (event) => {
  form.serviceTime = event.detail.value
}

const goBack = () => {
  if (props.embedded) {
    uni.switchTab({ url: '/pages/home/index' })
    return
  }
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: '/pages/jobs/index?tab=mine' })
    },
  })
}

const submit = async () => {
  showErrors.value = true
  if (!canSubmit.value) {
    return
  }
  const payload = {
    roleType: currentRoleType.value,
    jobType: form.jobType,
    title: form.title,
    serviceMode: form.serviceMode,
    location: form.location,
    serviceDate: form.serviceDate,
    serviceTime: form.serviceTime,
    salary: Number(form.salaryText || 0),
    summary: form.summary,
    credentialFileId: form.credentialFile?.fileId || null,
  }
  if (editId.value) {
    await updateJobPost(editId.value, payload)
  } else {
    await createJobPost(payload)
  }
  uni.showToast({
    title: submitSuccessText.value,
    icon: 'success',
  })
  if (props.embedded) {
    resetForm()
    uni.$emit('jobs-home-refresh')
    uni.$emit('jobs-switch-tab', 'home')
    return
  }
  setTimeout(() => {
    uni.navigateBack({
      fail: () => {
        uni.redirectTo({ url: '/pages/jobs/index?tab=mine' })
      },
    })
  }, 280)
}

const splitTimeText = (timeText = '') => {
  const normalized = String(timeText || '').trim()
  const match = normalized.match(/^(\d{4}-\d{2}-\d{2})\s+(\d{2}:\d{2})$/)
  if (!match) {
    return {
      serviceDate: '',
      serviceTime: '',
    }
  }
  return {
    serviceDate: match[1],
    serviceTime: match[2],
  }
}

const fillFormByJob = (job) => {
  if (!job) {
    return
  }
  const time = splitTimeText(job.timeText)
  rejectReason.value = job.qualificationRejectReason || ''
  Object.assign(form, {
    jobType: job.jobType || '',
    title: job.title || '',
    serviceMode: job.serviceMode || '',
    location: job.location || '',
    serviceDate: time.serviceDate,
    serviceTime: time.serviceTime,
    salaryText: String(job.salaryText || '').replace(/\s*元.*$/, ''),
    summary: job.summary || '',
    credentialFile: job.credentialFile || null,
    id: job.id,
    headCount: job.headCount,
    filledCount: job.filledCount,
    expired: job.expired,
    publisherId: job.publisherId,
    publisherName: job.publisherName,
    publisherPhone: job.publisherPhone,
    publisherAvatarColor: job.publisherAvatarColor,
    createdAt: job.createdAt,
    status: job.status,
    publicVisible: job.publicVisible,
    qualificationRejectReason: job.qualificationRejectReason || '',
  })
}

onLoad(async (options) => {
  if (!options?.editId) {
    return
  }
  editId.value = String(options.editId)
  const job = await getJobDetail(options.editId)
  if (!job) {
    return
  }
  fillFormByJob(job)
})
</script>

<style scoped lang="scss">
.embedded-page {
  min-height: 100%;
  padding: 0 24rpx 0;
  box-sizing: border-box;
}

.publish-navbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: grid;
  grid-template-columns: 88rpx 1fr 88rpx;
  align-items: center;
  min-height: 96rpx;
  padding: 16rpx 0 12rpx;
  background: #f4f7fb;
}

.nav-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  color: #1f2937;
}

.nav-left {
  font-size: 42rpx;
}

.nav-center {
  text-align: center;
  min-width: 0;
}

.nav-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-placeholder {
  min-height: 72rpx;
  pointer-events: none;
  visibility: hidden;
}

.nav-hover {
  background: #e8eef8;
}

.publish-scroll {
  height: calc(100vh - 116rpx - 162rpx - env(safe-area-inset-bottom));
}

.publish-scroll.with-tabbar {
  height: calc(100vh - 116rpx - env(safe-area-inset-bottom));
  min-height: calc(100vh - 116rpx - env(safe-area-inset-bottom));
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.publish-card {
  margin-bottom: 20rpx;
  padding: 26rpx 24rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.reject-banner {
  margin-bottom: 20rpx;
  padding: 22rpx 24rpx;
  border-radius: 24rpx;
  background: #fff1f2;
  border: 2rpx solid #fecdd3;
}

.reject-banner-title,
.reject-banner-desc {
  display: block;
}

.reject-banner-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #be123c;
}

.reject-banner-desc {
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.7;
  color: #9f1239;
}

.publish-card.main-card {
  min-height: calc(100vh - 116rpx - 120rpx - env(safe-area-inset-bottom));
  margin-bottom: 0;
  padding-bottom: calc(260rpx + env(safe-area-inset-bottom));
  background: transparent;
  box-shadow: none;
  box-sizing: border-box;
}

.section-title,
.field-label {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.required {
  color: #ef4444;
  margin-right: 6rpx;
}

.field-block + .field-block {
  margin-top: 24rpx;
}

.chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.chip {
  padding: 14rpx 22rpx;
  border-radius: 999rpx;
  background: #edf2f7;
  color: #475569;
  font-size: 23rpx;
}

.chip.active {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.field-input,
.field-textarea,
.picker-field,
.money-row {
  width: 100%;
  box-sizing: border-box;
  margin-top: 12rpx;
  border-radius: 22rpx;
  background: #f8fafc;
  border: 2rpx solid #e2e8f0;
}

.field-input,
.picker-field {
  min-height: 88rpx;
  padding: 0 22rpx;
  display: flex;
  align-items: center;
}

.field-textarea {
  min-height: 200rpx;
  padding: 18rpx 22rpx;
  line-height: 1.7;
}

.picker-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12rpx;
}

.picker-field {
  color: #94a3b8;
}

.picker-field.filled {
  color: #0f172a;
}

.money-row {
  min-height: 88rpx;
  padding: 0 22rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.money-prefix {
  font-size: 28rpx;
  font-weight: 700;
  color: #ef4444;
}

.money-input {
  flex: 1;
  min-width: 0;
}

.field-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
  margin-top: 10rpx;
}

.field-error,
.field-helper,
.field-count {
  font-size: 22rpx;
  line-height: 1.6;
}

.field-error {
  display: block;
  margin-top: 10rpx;
  color: #ef4444;
}

.field-helper,
.field-count {
  color: #64748b;
}

.publish-footer {
  position: fixed;
  left: 50%;
  bottom: calc(20rpx + env(safe-area-inset-bottom));
  transform: translateX(-50%);
  display: flex;
  justify-content: center;
  pointer-events: none;
  z-index: 30;
}

.publish-footer.with-tabbar {
  bottom: calc(112rpx + env(safe-area-inset-bottom));
}

.footer-actions {
  display: inline-flex;
  gap: 14rpx;
  width: auto;
  max-width: 100%;
  pointer-events: auto;
}

.footer-btn {
  min-width: 220rpx;
  min-height: 84rpx;
  padding: 0 34rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #0f172a, #2563eb);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 800;
  box-shadow: 0 10rpx 20rpx rgba(37, 99, 235, 0.12);
}

.footer-btn.ghost {
  background: #ffffff;
  color: #334155;
  border: 2rpx solid #d9e2ef;
  box-shadow: 0 8rpx 18rpx rgba(148, 163, 184, 0.1);
}

.footer-btn.disabled {
  background: #cbd5e1;
  color: #f8fafc;
  pointer-events: none;
}
</style>
