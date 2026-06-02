<template>
  <view class="page-shell errand-support-page">
    <view class="card support-hero">
      <text class="hero-title">帮助与售后</text>
      <text class="hero-desc">集中处理跑腿过程中的异常咨询、售后说明和规则查阅，方便你快速找到对应入口。</text>
    </view>

    <view class="support-section">
      <view class="card support-card" @click="go('/pages/report/list')">
        <text class="support-title">售后与举报记录</text>
        <text class="support-desc">查看全部工单、核查进度和处理结果，适合追踪历史问题。</text>
      </view>

      <view class="card support-card" @click="go('/pages/errand/rules')">
        <text class="support-title">平台规则中心</text>
        <text class="support-desc">查看计费规则、履约规范、售后须知和平台免责声明。</text>
      </view>
    </view>

    <view class="card support-faq">
      <text class="faq-title">常见问题</text>
      <view class="faq-item">
        <text class="faq-question">订单长时间无人接单怎么办？</text>
        <text class="faq-answer">可以返回广场重新发布更清晰的需求说明，或适当调整酬劳和预约时间，提高被承接概率。</text>
      </view>
      <view class="faq-item">
        <text class="faq-question">履约过程中发生争议怎么办？</text>
        <text class="faq-answer">请优先保留聊天记录、图片凭证和通话沟通结果，再通过举报记录入口提交工单，后台会进行人工核查。</text>
      </view>
      <view class="faq-item">
        <text class="faq-question">如何查看平台处理进度？</text>
        <text class="faq-answer">进入售后与举报记录页，可以看到全部工单状态、处理备注和最终结果。</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const go = (url) => {
  uni.navigateTo({ url })
}
</script>

<style scoped lang="scss">
.support-hero,
.support-faq,
.support-card {
  padding: 28rpx 26rpx;
}

.hero-title,
.faq-title,
.support-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}

.hero-desc,
.support-desc,
.faq-answer {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.8;
  color: #64748b;
}

.support-section {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 22rpx;
}

.faq-title {
  margin-bottom: 10rpx;
}

.support-faq {
  margin-top: 22rpx;
}

.faq-item + .faq-item {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1px solid #e2e8f0;
}

.faq-question {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}
</style>
