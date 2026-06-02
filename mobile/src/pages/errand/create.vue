<template>
  <view :class="[embedded ? 'embedded-page' : 'create-page']">
    <view class="form-scroll">
      <view class="section-card">
        <view class="section-head">
          <text class="section-title">服务类型</text>
          <text class="section-tip">必选，决定订单展示标签与需求场景</text>
        </view>
        <view class="type-grid">
          <view
            v-for="item in serviceTypes"
            :key="item.value"
            :class="['type-item', form.serviceType === item.value ? 'active' : '']"
            @click="form.serviceType = item.value"
          >
            <text class="type-label">{{ item.label }}</text>
            <text class="type-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>

      <view class="section-card">
        <view class="section-head">
          <text class="section-title">增值服务</text>
          <text class="section-tip">多选后自动叠加固定增值费用</text>
        </view>
        <view class="service-option-list">
          <view class="service-option" @click="form.urgentFlag = !form.urgentFlag">
            <view>
              <text class="option-title">加急配送</text>
              <text class="option-desc">优先处理，附加费用 ￥{{ urgentFeeAmount.toFixed(2) }}</text>
            </view>
            <view class="switch-wrap" @click.stop>
              <switch :checked="form.urgentFlag" color="#2d7ff9" @change="toggleUrgent" />
            </view>
          </view>
          <view class="service-option" @click="form.fragileFlag = !form.fragileFlag">
            <view>
              <text class="option-title">易碎品专属保护</text>
              <text class="option-desc">谨慎搬运，附加费用 ￥{{ fragileFeeAmount.toFixed(2) }}</text>
            </view>
            <view class="switch-wrap" @click.stop>
              <switch :checked="form.fragileFlag" color="#2d7ff9" @change="toggleFragile" />
            </view>
          </view>
        </view>
      </view>

      <view class="section-card">
        <view class="section-head">
          <text class="section-title">取件与送达</text>
          <text class="section-tip">请尽量填写清晰的楼栋、门口、柜台或窗口信息</text>
        </view>

        <view class="field-block">
          <text class="field-label">取件地址</text>
          <input
            v-model.trim="form.pickupAddress"
            class="field-input"
            placeholder="如 南苑菜鸟驿站 3 号取件柜"
          />
          <view class="quick-addresses">
            <text
              v-for="item in pickupSuggestions"
              :key="item"
              class="quick-chip"
              @click="form.pickupAddress = item"
            >
              {{ item }}
            </text>
          </view>
        </view>

        <view class="field-block">
          <text class="field-label">派送地址</text>
          <input
            v-model.trim="form.deliveryAddress"
            class="field-input"
            placeholder="如 北苑 8 号楼宿舍门口 / 图书馆东侧"
          />
        </view>

        <view class="field-block">
          <text class="field-label">预期取件时间</text>
          <picker mode="selector" :range="timeOptions" @change="changeTimeOption">
            <view class="picker-input">{{ selectedTimeText }}</view>
          </picker>
        </view>
      </view>

      <view class="section-card">
        <view class="section-head">
          <text class="section-title">需求说明</text>
          <text class="section-tip">写清代购清单、打印要求、物品说明等关键信息</text>
        </view>

        <view class="field-block">
          <text class="field-label">需求明细</text>
          <textarea
            v-model.trim="form.detailContent"
            class="field-textarea detail-textarea"
            placeholder="如 代购两瓶矿泉水、一份面包；或打印论文 36 页双面黑白装订"
          ></textarea>
        </view>

        <view class="field-block">
          <text class="field-label">补充备注</text>
          <textarea
            v-model.trim="form.remark"
            class="field-textarea remark-textarea"
            placeholder="可填写电话联系要求、交付位置、特殊注意事项"
          ></textarea>
        </view>

        <view class="field-block">
          <text class="field-label">附件说明</text>
          <ImageUploader
            v-model="attachmentFile"
            biz-type="errand_order_attachment"
            title="上传补充图片"
            tip="可上传代购清单、打印要求、位置截图等辅助信息"
          />
        </view>
      </view>

      <view class="section-card">
        <view class="section-head">
          <text class="section-title">酬劳设置</text>
          <text class="section-tip">用户自主填写基础酬劳，平台不做规格限制</text>
        </view>

        <view class="field-block">
          <text class="field-label">基础酬劳</text>
          <view class="money-input-wrap">
            <text class="money-prefix">￥</text>
            <input
              v-model.trim="form.baseFee"
              class="money-input"
              type="digit"
              placeholder="请输入基础酬劳"
            />
          </view>
        </view>
      </view>

      <view class="bottom-spacer"></view>
    </view>

    <view class="bottom-actions">
      <view class="fee-panel">
        <view class="fee-row">
          <text class="fee-label">基础酬劳</text>
          <text class="fee-value">￥{{ baseFeeAmount.toFixed(2) }}</text>
        </view>
        <view class="fee-row">
          <text class="fee-label">增值服务附加</text>
          <text class="fee-value">￥{{ extraFeeAmount.toFixed(2) }}</text>
        </view>
        <view class="fee-row total">
          <text class="fee-label">订单应付总价</text>
          <text class="fee-value">￥{{ totalFee.toFixed(2) }}</text>
        </view>
        <text class="fee-tip">{{ feeTip }}</text>
      </view>

      <view :class="['submit-btn', !canSubmit || submitting ? 'disabled' : '']" @click="submit">
        {{ submitting ? '发布中...' : '发布订单' }}
      </view>
    </view>

  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import ImageUploader from '../../components/ImageUploader.vue'
import request from '../../utils/request'

defineProps({
  embedded: {
    type: Boolean,
    default: false,
  },
})

const serviceTypes = [
  { label: '取件', value: 'PICKUP', desc: '适合快递、外卖、文件代取' },
  { label: '送件', value: 'DELIVERY', desc: '适合资料、雨伞、物品代送' },
  { label: '代购', value: 'PURCHASE', desc: '适合超市、商店、食物代买' },
  { label: '代打印', value: 'PRINT', desc: '适合文档、资料、作业打印' },
]

const timeOptions = [
  '立即取件',
  '30 分钟内',
  '1 小时内',
  '今天中午前',
  '今天下午前',
  '今天晚上前',
  '明天上午前',
  '明天下午前',
]

const pickupSuggestions = [
  '南苑菜鸟驿站',
  '图书馆一楼服务台',
  '西门外卖柜',
  '创新楼打印店',
]

const form = reactive({
  serviceType: 'PICKUP',
  pickupAddress: '',
  deliveryAddress: '',
  pickupTimeText: '',
  detailContent: '',
  remark: '',
  baseFee: '',
  urgentFlag: false,
  fragileFlag: false,
})

const attachmentFile = ref(null)
const submitting = ref(false)
const rules = ref({
  urgentFee: 2,
  fragileFee: 3,
})

request.get('/errand/rules').then((data) => {
  rules.value = data
}).catch(() => {})

const baseFeeAmount = computed(() => {
  const amount = Number(form.baseFee || 0)
  return Number.isNaN(amount) ? 0 : amount
})

const urgentFeeAmount = computed(() => Number(rules.value.urgentFee || 0))
const fragileFeeAmount = computed(() => Number(rules.value.fragileFee || 0))
const extraFeeAmount = computed(() =>
  (form.urgentFlag ? urgentFeeAmount.value : 0) + (form.fragileFlag ? fragileFeeAmount.value : 0)
)
const totalFee = computed(() => baseFeeAmount.value + extraFeeAmount.value)

const selectedTimeText = computed(() => form.pickupTimeText || '请选择预约取件时间')
const missingFields = computed(() => {
  const list = []
  if (!form.pickupAddress) {
    list.push('取件地址')
  }
  if (!form.deliveryAddress) {
    list.push('派送地址')
  }
  if (!form.pickupTimeText) {
    list.push('预期取件时间')
  }
  if (!form.detailContent) {
    list.push('需求明细')
  }
  if (!(baseFeeAmount.value > 0)) {
    list.push('基础酬劳')
  }
  return list
})

const canSubmit = computed(() => (
  Boolean(form.serviceType) &&
  Boolean(form.pickupAddress) &&
  Boolean(form.deliveryAddress) &&
  Boolean(form.pickupTimeText) &&
  Boolean(form.detailContent) &&
  baseFeeAmount.value > 0
))

const feeTip = computed(() => (
  canSubmit.value
    ? '信息完整，可以发布订单。'
    : `请完善订单信息：${missingFields.value.join('、')}`
))

const toggleUrgent = (event) => {
  form.urgentFlag = event.detail.value
}

const toggleFragile = (event) => {
  form.fragileFlag = event.detail.value
}

const changeTimeOption = (event) => {
  form.pickupTimeText = timeOptions[event.detail.value] || ''
}

const submit = async () => {
  if (!canSubmit.value || submitting.value) {
    uni.showToast({ title: feeTip.value, icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await request.post('/errand/orders', {
      ...form,
      baseFee: baseFeeAmount.value,
      attachmentFileIds: attachmentFile.value?.fileId ? [attachmentFile.value.fileId] : [],
    })
    uni.showToast({ title: '订单已发布', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/errand/index?tab=square' })
    }, 350)
  } finally {
    submitting.value = false
  }
}

</script>

<style lang="scss">
.create-page,
.embedded-page {
  min-height: 100vh;
  background: #f4f7fb;
}

.embedded-page {
  padding: 24rpx 24rpx 0;
  box-sizing: border-box;
}

.form-scroll {
  padding-bottom: calc(420rpx + env(safe-area-inset-bottom));
}

.section-card {
  margin-bottom: 20rpx;
  padding: 26rpx 24rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(15, 23, 42, 0.06);
}

.section-head {
  margin-bottom: 20rpx;
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.section-tip {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #94a3b8;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
}

.type-item {
  padding: 24rpx 20rpx;
  border-radius: 22rpx;
  background: #f8fafc;
  border: 2rpx solid transparent;
}

.type-item.active {
  background: rgba(45, 127, 249, 0.08);
  border-color: #2d7ff9;
}

.type-label {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.type-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #64748b;
}

.service-option-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.service-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 22rpx 20rpx;
  border-radius: 22rpx;
  background: #f8fafc;
}

.switch-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.option-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.option-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #64748b;
}

.field-block + .field-block {
  margin-top: 24rpx;
}

.field-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 25rpx;
  color: #475569;
}

.field-input,
.picker-input,
.field-textarea,
.money-input-wrap {
  width: 100%;
  border-radius: 20rpx;
  background: #f8fafc;
  box-sizing: border-box;
}

.field-input,
.picker-input {
  min-height: 88rpx;
  padding: 0 24rpx;
  line-height: 88rpx;
  font-size: 26rpx;
  color: #0f172a;
}

.field-textarea {
  padding: 20rpx 24rpx;
  font-size: 26rpx;
  color: #0f172a;
}

.detail-textarea {
  min-height: 220rpx;
}

.remark-textarea {
  min-height: 160rpx;
}

.quick-addresses {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 14rpx;
}

.quick-chip {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #e8eef8;
  color: #315172;
  font-size: 22rpx;
}

.money-input-wrap {
  display: flex;
  align-items: center;
  min-height: 88rpx;
  padding: 0 24rpx;
}

.money-prefix {
  font-size: 32rpx;
  font-weight: 700;
  color: #dc2626;
  margin-right: 12rpx;
}

.money-input {
  flex: 1;
  font-size: 28rpx;
  color: #0f172a;
}

.bottom-spacer {
  height: 12rpx;
}

.bottom-actions {
  position: fixed;
  left: 0;
  right: 0;
  bottom: calc(108rpx + env(safe-area-inset-bottom));
  z-index: 25;
  padding: 18rpx 24rpx 16rpx;
  background: rgba(255, 255, 255, 0.98);
  border-top: 1px solid #e2e8f0;
  backdrop-filter: blur(18rpx);
}

.fee-panel {
  padding: 20rpx 22rpx;
  border-radius: 24rpx;
  background: #f8fafc;
}

.fee-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 24rpx;
  color: #475569;
}

.fee-row + .fee-row {
  margin-top: 10rpx;
}

.fee-row.total {
  margin-top: 14rpx;
  padding-top: 14rpx;
  border-top: 1px solid #e2e8f0;
  color: #0f172a;
  font-weight: 700;
}

.fee-tip {
  display: block;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: #ef4444;
}

.submit-btn {
  margin-top: 16rpx;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #1677ff, #38bdf8);
  color: #ffffff;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
}

.submit-btn.disabled {
  background: #cbd5e1;
  color: #ffffff;
}

</style>
