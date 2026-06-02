<template>
  <view class="partner-publish-page">
    <view class="partner-navbar">
      <view class="partner-nav-side partner-nav-left" hover-class="partner-nav-hover" @click="goBack">‹</view>
      <view class="partner-nav-center">
        <text class="partner-nav-title">{{ pageTitle }}</text>
      </view>
      <view class="partner-nav-side partner-nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="partner-publish-scroll">
      <view class="partner-form">
        <view class="partner-form-card">
          <text class="partner-section-title">基础信息</text>

          <view class="partner-field">
            <text class="partner-label"><text class="partner-required">*</text>搭子类型</text>
            <view class="partner-chip-group">
              <view
                v-for="item in partnerPublishCategoryOptions"
                :key="item.value"
                :class="['partner-chip', form.type === item.value ? 'active' : '']"
                @click="form.type = item.value"
              >
                {{ item.label }}
              </view>
            </view>
            <text v-if="showErrors && !form.type" class="partner-error">请选择搭子类型</text>
          </view>

          <view class="partner-field">
            <text class="partner-label"><text class="partner-required">*</text>需求时间</text>
            <picker mode="date" :value="form.date" @change="handleDateChange">
              <view :class="['partner-picker', form.date ? 'filled' : '']">
                {{ form.date || '请选择日期' }}
              </view>
            </picker>
            <picker :range="partnerTimeSlotOptions" :value="timeSlotIndex" @change="handleTimeSlotChange">
              <view :class="['partner-picker', form.timeSlot ? 'filled' : '']">
                {{ form.timeSlot || '请选择时段' }}
              </view>
            </picker>
            <text v-if="showErrors && (!form.date || !form.timeSlot)" class="partner-error">请选择完整的需求时间</text>
          </view>

          <view class="partner-field">
            <text class="partner-label"><text class="partner-required">*</text>活动地点</text>
            <input
              v-model.trim="form.location"
              class="partner-input"
              maxlength="40"
              placeholder="请输入活动地点"
            />
            <text v-if="showErrors && !form.location" class="partner-error">请填写活动地点</text>
          </view>
        </view>

        <view class="partner-form-card">
          <text class="partner-section-title">需求内容</text>

          <view class="partner-field">
            <text class="partner-label">个性化需求标签</text>
            <view class="partner-tag-editor">
              <input
                v-model.trim="tagInput"
                class="partner-input partner-tag-input"
                maxlength="10"
                placeholder="输入标签后点击添加"
                @confirm="addTag"
              />
              <view class="partner-tag-add" @click="addTag">添加</view>
            </view>
            <view v-if="form.needTags.length" class="partner-chip-group partner-chip-group--tags">
              <view v-for="tag in form.needTags" :key="tag" class="partner-tag-chip">
                <text>{{ tag }}</text>
                <text class="partner-tag-remove" @click="removeTag(tag)">×</text>
              </view>
            </view>
            <text v-else class="partner-helper">选填，支持自定义添加标签，例如“期末冲刺”“饭搭子”。</text>
          </view>

          <view class="partner-field">
            <text class="partner-label"><text class="partner-required">*</text>需求描述</text>
            <textarea
              v-model.trim="form.description"
              class="partner-textarea"
              maxlength="200"
              placeholder="请填写详细要求，方便系统匹配更合适的搭子"
            />
            <view class="partner-field-foot">
              <text v-if="showErrors && !form.description" class="partner-error">请填写需求描述</text>
              <text class="partner-count">{{ form.description.length }}/200</text>
            </view>
          </view>

          <view class="partner-field">
            <text class="partner-label"><text class="partner-required">*</text>参与人数</text>
            <input
              v-model="form.totalSlots"
              type="number"
              class="partner-input"
              maxlength="2"
              placeholder="请输入可匹配总人数"
            />
            <text class="partner-helper">发布后系统会自动把剩余名额初始化为与你填写的人数一致。</text>
            <text v-if="showErrors && !isValidSlots" class="partner-error">参与人数请填写 1 - 9 的整数</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="partner-actions">
      <view class="partner-action partner-action--secondary" @click="resetForm">重置</view>
      <view :class="['partner-action', canSubmit ? '' : 'disabled']" @click="submitForm">
        {{ isEditMode ? '保存修改' : '发布' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import {
  getPartnerDemandById,
  partnerCategoryMap,
  partnerPublishCategoryOptions,
  partnerTimeSlotOptions,
  savePublishedPartner,
} from '../../utils/partnerMock'

const store = useUserStore()
const showErrors = ref(false)
const tagInput = ref('')
const editDemandId = ref('')

const createDefaultForm = () => ({
  type: '',
  date: '',
  timeSlot: '',
  location: '',
  needTags: [],
  description: '',
  totalSlots: '',
})

const form = reactive(createDefaultForm())
const pageTitle = computed(() => (isEditMode.value ? '编辑搭子需求' : '发布搭子需求'))
const isEditMode = computed(() => Boolean(editDemandId.value))

const timeSlotIndex = computed(() => {
  const index = partnerTimeSlotOptions.findIndex((item) => item === form.timeSlot)
  return index >= 0 ? index : 0
})

const isValidSlots = computed(() => {
  const value = Number(form.totalSlots)
  return Number.isInteger(value) && value >= 1 && value <= 9
})

const canSubmit = computed(
  () => Boolean(form.type && form.date && form.timeSlot && form.location && form.description && isValidSlots.value)
)

const handleDateChange = (event) => {
  form.date = event.detail.value
}

const handleTimeSlotChange = (event) => {
  const value = partnerTimeSlotOptions[Number(event.detail.value) || 0]
  form.timeSlot = value
}

const addTag = () => {
  const value = tagInput.value.trim()
  if (!value) {
    return
  }
  if (form.needTags.includes(value)) {
    uni.showToast({ title: '标签已存在', icon: 'none' })
    return
  }
  if (form.needTags.length >= 6) {
    uni.showToast({ title: '最多添加 6 个标签', icon: 'none' })
    return
  }
  form.needTags = [...form.needTags, value]
  tagInput.value = ''
}

const removeTag = (tag) => {
  form.needTags = form.needTags.filter((item) => item !== tag)
}

const resetForm = () => {
  Object.assign(form, createDefaultForm())
  showErrors.value = false
  tagInput.value = ''
}

const parseTimeText = (timeText) => {
  if (!timeText) {
    return { date: '', timeSlot: '' }
  }
  const matchedSlot = partnerTimeSlotOptions.find((item) => timeText.endsWith(item))
  if (matchedSlot) {
    return {
      date: timeText.slice(0, timeText.length - matchedSlot.length).trim(),
      timeSlot: matchedSlot,
    }
  }
  const parts = String(timeText).split(' ')
  return {
    date: parts[0] || '',
    timeSlot: parts.slice(1).join(' ') || '',
  }
}

const fillForm = (detail) => {
  if (!detail) {
    return
  }
  const parsedTime = parseTimeText(detail.timeText)
  form.type = detail.type || ''
  form.date = parsedTime.date
  form.timeSlot = parsedTime.timeSlot
  form.location = detail.location || ''
  form.needTags = Array.isArray(detail.needTags) ? [...detail.needTags] : []
  form.description = detail.description || ''
  form.totalSlots = String(detail.totalSlots || detail.remainingSlots || '')
  showErrors.value = false
  tagInput.value = ''
}

const buildPartnerPayload = () => {
  const now = Date.now()
  const userId = store.profile?.userId || 'guest'
  const nickname = store.profile?.nickname || '新同学'
  const totalSlots = Number(form.totalSlots)
  const currentDetail = isEditMode.value ? getPartnerDemandById(editDemandId.value) : null
  return {
    id: currentDetail?.id || now,
    type: form.type,
    timeText: `${form.date} ${form.timeSlot}`,
    location: form.location,
    needTags: [...form.needTags],
    userTags: currentDetail?.userTags || ['新发布', '待匹配'],
    description: form.description,
    remainingSlots: currentDetail?.remainingSlots || totalSlots,
    totalSlots,
    nickname,
    createdAt: currentDetail?.createdAt || new Date(now).toISOString(),
    updatedAt: new Date(now).toISOString(),
    matchScore: currentDetail?.matchScore || 100,
    publisherId: userId,
    status: currentDetail?.status || 'ACTIVE',
  }
}

const submitForm = () => {
  showErrors.value = true
  if (!canSubmit.value) {
    uni.showToast({ title: '请完善必填信息', icon: 'none' })
    return
  }
  savePublishedPartner(buildPartnerPayload())
  uni.showToast({ title: isEditMode.value ? '需求已更新' : `${partnerCategoryMap[form.type]}已发布`, icon: 'success' })
  setTimeout(() => {
    uni.$emit('partner-published')
    uni.redirectTo({ url: isEditMode.value ? '/pages/partner/mine' : '/pages/partner/index' })
  }, 220)
}

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.navigateTo({ url: '/pages/partner/index' })
    },
  })
}

onLoad((options) => {
  const demandId = options?.id || ''
  if (!demandId) {
    resetForm()
    return
  }
  editDemandId.value = demandId
  fillForm(getPartnerDemandById(demandId))
})
</script>

<style lang="scss">
.partner-publish-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fbff 0%, #f4f7fb 30%, #f4f7fb 100%);
  padding-bottom: calc(168rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.partner-navbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: grid;
  grid-template-columns: 88rpx 1fr 88rpx;
  align-items: center;
  min-height: 96rpx;
  padding: calc(16rpx + env(safe-area-inset-top)) 16rpx 12rpx;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.partner-nav-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f2937;
  font-size: 28rpx;
  border-radius: 18rpx;
}

.partner-nav-left {
  font-size: 42rpx;
}

.partner-nav-hover {
  background: #f1f5f9;
}

.partner-nav-center {
  text-align: center;
  min-width: 0;
}

.partner-nav-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.partner-nav-placeholder {
  pointer-events: none;
}

.partner-publish-scroll {
  height: calc(100vh - 112rpx - env(safe-area-inset-top) - 132rpx - env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.partner-form {
  padding: 24rpx;
  box-sizing: border-box;
}

.partner-form-card {
  margin-bottom: 20rpx;
  padding: 28rpx 24rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
  box-sizing: border-box;
}

.partner-section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 22rpx;
}

.partner-field + .partner-field {
  margin-top: 28rpx;
}

.partner-label {
  display: block;
  margin-bottom: 14rpx;
  font-size: 26rpx;
  font-weight: 700;
  color: #1e293b;
}

.partner-required {
  color: #ef4444;
  margin-right: 6rpx;
}

.partner-chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.partner-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 14rpx 22rpx;
  border-radius: 999rpx;
  background: #edf2f7;
  color: #475569;
  font-size: 23rpx;
  font-weight: 600;
}

.partner-chip.active {
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
}

.partner-input,
.partner-picker,
.partner-textarea {
  width: 100%;
  box-sizing: border-box;
  border-radius: 22rpx;
  background: #f8fafc;
  border: 2rpx solid #e2e8f0;
  color: #0f172a;
}

.partner-input,
.partner-picker {
  min-height: 88rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  font-size: 24rpx;
  line-height: 1.4;
}

.partner-picker + .partner-picker {
  margin-top: 12rpx;
}

.partner-picker {
  color: #94a3b8;
}

.partner-picker.filled {
  color: #0f172a;
}

.partner-tag-editor {
  display: flex;
  gap: 12rpx;
  align-items: stretch;
}

.partner-tag-input {
  flex: 1;
  min-width: 0;
}

.partner-tag-add {
  width: 112rpx;
  min-height: 88rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #0f172a, #2563eb);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

.partner-chip-group--tags {
  margin-top: 14rpx;
}

.partner-tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 10rpx;
  padding: 12rpx 16rpx;
  border-radius: 999rpx;
  background: #e0f2fe;
  color: #0369a1;
  font-size: 22rpx;
  font-weight: 600;
}

.partner-tag-remove {
  font-size: 24rpx;
  color: #0f172a;
}

.partner-textarea {
  min-height: 220rpx;
  padding: 20rpx 24rpx;
  font-size: 24rpx;
  line-height: 1.7;
}

.partner-field-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 10rpx;
}

.partner-helper {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: #64748b;
}

.partner-error {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #ef4444;
}

.partner-count {
  flex-shrink: 0;
  font-size: 22rpx;
  color: #94a3b8;
}

.partner-actions {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  gap: 16rpx;
  padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom));
  background: #ffffff;
  border-top: 1px solid #e2e8f0;
  box-sizing: border-box;
}

.partner-action {
  flex: 1;
  min-height: 92rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 800;
}

.partner-action--secondary {
  background: #e2e8f0;
  color: #334155;
}

.partner-action.disabled {
  background: #cbd5e1;
  color: #ffffff;
}
</style>
