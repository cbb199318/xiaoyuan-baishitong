<template>
  <view class="beauty-publish-page">
    <view class="beauty-publish-navbar">
      <view class="beauty-nav-side beauty-nav-left" hover-class="beauty-nav-hover" @click="goBack">‹</view>
      <view class="beauty-nav-center">
        <text class="beauty-nav-title">发布平价好物</text>
      </view>
      <view class="beauty-nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="beauty-publish-scroll">
      <view class="beauty-publish-card">
        <text class="beauty-section-title">商品信息填写区</text>

        <view class="beauty-field-block">
          <text class="beauty-field-label"><text class="required">*</text>商品名称</text>
          <input v-model.trim="form.title" class="beauty-input" maxlength="40" placeholder="例如 奶杏色镜面唇釉、宿舍补妆镜" />
          <text v-if="showErrors && !form.title" class="beauty-field-error">请填写商品名称</text>
        </view>

        <view class="beauty-field-block">
          <text class="beauty-field-label"><text class="required">*</text>商品售价</text>
          <view class="beauty-price-row">
            <text class="beauty-price-prefix">￥</text>
            <input v-model.trim="form.price" type="digit" class="beauty-price-input" maxlength="8" placeholder="请输入售卖价格" />
          </view>
          <text v-if="showErrors && !form.price" class="beauty-field-error">请填写商品售价</text>
        </view>

        <view class="beauty-field-block">
          <text class="beauty-field-label"><text class="required">*</text>品类选择区</text>
          <view class="beauty-chip-row">
            <view
              v-for="item in beautyCategoryOptions.filter((option) => option.value !== 'ALL')"
              :key="item.value"
              :class="['beauty-chip', form.category === item.value ? 'active' : '']"
              @click="form.category = item.value"
            >
              {{ item.label }}
            </view>
          </view>
          <text v-if="showErrors && !form.category" class="beauty-field-error">请选择商品品类</text>
        </view>

        <view class="beauty-field-block">
          <text class="beauty-field-label"><text class="required">*</text>商品简介</text>
          <textarea
            v-model.trim="form.summary"
            class="beauty-textarea"
            maxlength="160"
            placeholder="简要说明商品特点、适合人群、价格优势"
          />
          <view class="beauty-field-foot">
            <text class="beauty-field-helper">用于首页卡片和详情页基础信息展示。</text>
            <text class="beauty-field-count">{{ form.summary.length }}/160</text>
          </view>
          <text v-if="showErrors && !form.summary" class="beauty-field-error">请填写商品简介</text>
        </view>
      </view>

      <view class="beauty-publish-card">
        <text class="beauty-section-title">商品实拍图上传区</text>
        <ImageUploader
          v-model="form.productImageFile"
          biz-type="beauty_product"
          title="上传商品实拍图"
          tip="展示商品外观、细节图，保证内容真实直观"
        />
        <text v-if="showErrors && !form.productImageFile?.url" class="beauty-field-error">请上传商品实拍图</text>
      </view>

      <view class="beauty-publish-card">
        <text class="beauty-section-title">订单购买凭证必填上传区</text>
        <ImageUploader
          v-model="form.receiptFile"
          biz-type="beauty_receipt"
          title="上传订单购买凭证"
          tip="无购买凭证无法提交发布，用于源头管控商品质量"
        />
        <text v-if="showErrors && !form.receiptFile?.url" class="beauty-field-error">请上传订单购买凭证</text>
      </view>

      <view class="beauty-publish-card">
        <text class="beauty-section-title">产品使用说明</text>
        <textarea
          v-model.trim="form.usageGuide"
          class="beauty-textarea large"
          maxlength="300"
          placeholder="可补充产品使用方式、适用场景、宿舍使用感受，无需额外勾选复杂标签"
        />
        <view class="beauty-field-foot">
          <text class="beauty-field-helper">选填，用于详情页正文内容补充。</text>
          <text class="beauty-field-count">{{ form.usageGuide.length }}/300</text>
        </view>
      </view>
    </scroll-view>

    <view class="beauty-publish-footer">
      <view class="beauty-footer-bar">
        <view class="beauty-footer-info">
          <text class="beauty-footer-title">{{ submitTip }}</text>
          <text class="beauty-footer-sub">上传实拍图和购买凭证后即可发布</text>
        </view>
        <view :class="['beauty-submit-btn', canSubmit ? '' : 'disabled']" @click="canSubmit && submit()">
          发布好物
        </view>
      </view>
    </view>

    <BeautyTabBar current="publish" @change="switchTab" />
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import BeautyTabBar from '../../components/BeautyTabBar.vue'
import ImageUploader from '../../components/ImageUploader.vue'
import { beautyCategoryOptions } from '../../utils/beautyMock'
import { createBeautyGood } from '../../utils/beautyApi'

const showErrors = ref(false)

const createDefaultForm = () => ({
  title: '',
  price: '',
  category: '',
  summary: '',
  usageGuide: '',
  productImageFile: null,
  receiptFile: null,
})

const form = reactive(createDefaultForm())

const canSubmit = computed(
  () =>
    !!form.title &&
    !!form.price &&
    !!form.category &&
    !!form.summary &&
    !!form.productImageFile?.url &&
    !!form.receiptFile?.url
)

const submitTip = computed(() => (canSubmit.value ? '信息完整，可直接发布' : '请先完善必填信息'))

const resetForm = () => {
  Object.assign(form, createDefaultForm())
  showErrors.value = false
}

const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.redirectTo({ url: '/pages/beauty/index' })
    },
  })
}

const openHome = () => {
  uni.redirectTo({ url: '/pages/beauty/index' })
}

const openMine = () => {
  uni.redirectTo({ url: '/pages/beauty/mine' })
}

const switchTab = (tab) => {
  if (tab === 'home') {
    openHome()
    return
  }
  if (tab === 'mine') {
    openMine()
  }
}

const submit = () => {
  showErrors.value = true
  if (!canSubmit.value) {
    uni.showToast({
      title: '请先完善必填信息',
      icon: 'none',
    })
    return
  }
  createBeautyGood(
    {
      title: form.title,
      price: Number(form.price),
      category: form.category,
      summary: form.summary,
      usageGuide: form.usageGuide,
      productImageFileId: form.productImageFile.fileId,
      receiptFileId: form.receiptFile.fileId,
    }
  )
    .then(() => {
      uni.showToast({
        title: '发布成功',
        icon: 'success',
      })
      uni.$emit('beauty-refresh')
      resetForm()
      setTimeout(() => {
        uni.navigateBack({
          fail: () => {
            uni.redirectTo({ url: '/pages/beauty/index' })
          },
        })
      }, 250)
    })
    .catch(() => {})
}
</script>

<style scoped lang="scss">
.beauty-publish-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(244, 114, 182, 0.18), transparent 22%),
    linear-gradient(180deg, #fff7fb 0%, #f7f8fc 24%, #f4f7fb 100%);
  padding-bottom: calc(206rpx + env(safe-area-inset-bottom));
}

.beauty-publish-navbar {
  position: sticky;
  top: 0;
  z-index: 30;
  display: grid;
  grid-template-columns: 88rpx 1fr 88rpx;
  align-items: center;
  min-height: 96rpx;
  padding: calc(16rpx + env(safe-area-inset-top)) 16rpx 12rpx;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(18rpx);
  border-bottom: 1px solid rgba(244, 114, 182, 0.08);
}

.beauty-nav-side {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  color: #1f2937;
}

.beauty-nav-left {
  font-size: 42rpx;
}

.beauty-nav-hover {
  background: #fff0f7;
}

.beauty-nav-center {
  min-width: 0;
  text-align: center;
}

.beauty-nav-title {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.beauty-nav-placeholder {
  min-height: 72rpx;
}

.beauty-publish-scroll {
  height: calc(100vh - 112rpx - env(safe-area-inset-top) - 196rpx - env(safe-area-inset-bottom));
}

.beauty-publish-card {
  margin: 24rpx;
  padding: 28rpx 24rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18rpx 44rpx rgba(236, 72, 153, 0.08);
}

.beauty-section-title,
.beauty-field-label,
.beauty-footer-title,
.beauty-footer-sub,
.beauty-field-helper,
.beauty-field-count,
.beauty-field-error {
  display: block;
}

.beauty-section-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #111827;
}

.beauty-field-block {
  margin-top: 24rpx;
}

.beauty-field-label {
  font-size: 25rpx;
  font-weight: 700;
  color: #111827;
}

.required {
  color: #e11d48;
  margin-right: 8rpx;
}

.beauty-input,
.beauty-textarea,
.beauty-price-row {
  margin-top: 14rpx;
  border-radius: 22rpx;
  background: #fff4f8;
}

.beauty-input {
  height: 88rpx;
  padding: 0 24rpx;
  font-size: 26rpx;
  color: #111827;
}

.beauty-price-row {
  height: 88rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.beauty-price-prefix {
  color: #e11d48;
  font-size: 28rpx;
  font-weight: 800;
}

.beauty-price-input {
  flex: 1;
  height: 88rpx;
  font-size: 28rpx;
  color: #111827;
}

.beauty-textarea {
  width: 100%;
  min-height: 180rpx;
  padding: 22rpx 24rpx;
  box-sizing: border-box;
  font-size: 25rpx;
  line-height: 1.7;
  color: #111827;
}

.beauty-textarea.large {
  min-height: 240rpx;
}

.beauty-chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 14rpx;
}

.beauty-chip {
  padding: 14rpx 20rpx;
  border-radius: 999rpx;
  background: #fff1f6;
  color: #be185d;
  font-size: 22rpx;
  font-weight: 700;
}

.beauty-chip.active {
  background: linear-gradient(135deg, #ec4899, #fb7185);
  color: #fff;
}

.beauty-field-foot {
  margin-top: 10rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.beauty-field-helper,
.beauty-field-count {
  font-size: 22rpx;
  color: #94a3b8;
}

.beauty-field-error {
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #e11d48;
}

.beauty-publish-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: calc(126rpx + env(safe-area-inset-bottom));
  z-index: 40;
  padding: 18rpx 24rpx 0;
  background: linear-gradient(180deg, rgba(244, 247, 251, 0) 0%, rgba(244, 247, 251, 0.9) 24%, #f4f7fb 100%);
}

.beauty-footer-bar {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 18rpx 18rpx 18rpx 22rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 18rpx 44rpx rgba(236, 72, 153, 0.12);
}

.beauty-footer-info {
  flex: 1;
  min-width: 0;
}

.beauty-footer-title {
  font-size: 24rpx;
  font-weight: 800;
  color: #111827;
}

.beauty-footer-sub {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #94a3b8;
}

.beauty-submit-btn {
  min-width: 200rpx;
  height: 86rpx;
  padding: 0 30rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #ec4899, #fb7185);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 800;
}

.beauty-submit-btn.disabled {
  background: #cbd5e1;
}
</style>
