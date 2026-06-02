<template>
  <view class="page-shell errand-rules-page">
    <view class="card rules-hero">
      <text class="hero-title">平台规则中心</text>
      <text class="hero-desc">跑腿模块统一查看计费规则、订单履约规则、售后须知与免责声明。</text>
    </view>

    <view class="rules-list">
      <view v-for="section in errandRuleSections" :key="section.title" class="card rule-card">
        <text class="rule-title">{{ section.title }}</text>
        <text class="rule-content">{{ section.content }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { errandRuleSections } from '../../utils/errandRules'
</script>

<style scoped lang="scss">
.rules-hero,
.rule-card {
  padding: 28rpx 26rpx;
}

.hero-title,
.rule-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}

.hero-desc,
.rule-content {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.8;
  color: #64748b;
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 22rpx;
}
</style>
