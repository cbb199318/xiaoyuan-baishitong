<template>
  <view class="page-shell jobs-help-page">
    <view class="card help-hero">
      <text class="hero-title">勤工助学帮助中心</text>
      <text class="hero-desc">
        这里集中说明勤工助学模块里的发布、报名、沟通、审核与售后规则，方便学生和商家快速理解页面怎么用、遇到问题怎么处理。
      </text>
    </view>

    <view class="help-section">
      <view class="card entry-card" @click="go('/pages/jobs/index?tab=publish')">
        <text class="entry-title">去发布需求</text>
        <text class="entry-desc">学生可直接发布线上互助需求，商家可发布线下兼职岗位。</text>
      </view>

      <view class="card entry-card" @click="go('/pages/report/list?module=jobs&title=勤工助学举报记录')">
        <text class="entry-title">查看举报记录</text>
        <text class="entry-desc">遇到虚假岗位、薪资不符、恶意沟通等情况，可在这里查看处理进度。</text>
      </view>

      <view class="card entry-card" @click="go('/pages/jobs/resume?from=jobs-mine')">
        <text class="entry-title">完善我的简历</text>
        <text class="entry-desc">补充擅长技能、可服务时间和联系方式，方便对方快速判断是否匹配。</text>
      </view>
    </view>

    <view class="card guide-card">
      <text class="section-title">发布说明</text>
      <view class="guide-item">
        <text class="guide-question">学生用户可以发布什么？</text>
        <text class="guide-answer">
          学生身份可直接发布 PPT 制作、文档修改、文案代写、家教辅导、校园互助等线上或校内协助需求，填写完整后可立即展示到岗位首页。
        </text>
      </view>
      <view class="guide-item">
        <text class="guide-question">商家用户发布有什么区别？</text>
        <text class="guide-answer">
          商家发布线下兼职岗位时，需要额外上传营业执照或门店资质照片。提交后岗位会进入审核中，审核通过后才会在首页公开展示。
        </text>
      </view>
      <view class="guide-item">
        <text class="guide-question">怎样提高发布效果？</text>
        <text class="guide-answer">
          建议标题写清楚核心内容，地点和时间尽量具体，酬劳预算明确，补充完整工作细节与交付要求，能明显减少来回沟通成本。
        </text>
      </view>
    </view>

    <view class="card guide-card">
      <text class="section-title">报名与沟通</text>
      <view class="guide-item">
        <text class="guide-question">如何报名或接单？</text>
        <text class="guide-answer">
          在岗位详情页点击“立即报名/接单”即可提交意向，系统会自动创建对应会话，方便双方继续沟通服务时间、地点和交付细节。
        </text>
      </view>
      <view class="guide-item">
        <text class="guide-question">消息中心里能做什么？</text>
        <text class="guide-answer">
          消息中心会汇总所有兼职咨询、报名、接单产生的沟通会话，支持查看最新消息、清空未读、发送文字和图片。
        </text>
      </view>
      <view class="guide-item">
        <text class="guide-question">收藏岗位有什么用？</text>
        <text class="guide-answer">
          收藏后岗位会进入“我的收藏”，方便后续对比多个岗位或需求，再决定是否联系对方或继续报名。
        </text>
      </view>
    </view>

    <view class="card guide-card">
      <text class="section-title">审核与售后</text>
      <view class="guide-item">
        <text class="guide-question">商家怎么查看审核结果？</text>
        <text class="guide-answer">
          商家可以在个人中心进入“审核进度”，查看资质审核状态、最近提交时间、岗位审核情况以及驳回原因。
        </text>
      </view>
      <view class="guide-item">
        <text class="guide-question">遇到问题该如何反馈？</text>
        <text class="guide-answer">
          如遇虚假岗位、薪资不符、恶意沟通、违规广告等情况，请保留聊天截图或页面截图，通过举报反馈入口提交，后台会人工处理。
        </text>
      </view>
      <view class="guide-item">
        <text class="guide-question">什么时候按钮会不可操作？</text>
        <text class="guide-answer">
          如果岗位已招满、需求已过期、内容已完结，或当前内容是你自己发布的，详情页底部部分操作按钮会自动置灰或限制点击。
        </text>
      </view>
    </view>

    <view class="card faq-card">
      <text class="section-title">常见问题</text>
      <view class="faq-item">
        <text class="faq-question">发布后首页没有看到我的岗位怎么办？</text>
        <text class="faq-answer">
          如果你是学生身份，提交成功后通常会立即展示；如果你是商家身份，需等待审核通过后才会公开显示。也可以进入“我的发布”查看当前状态。
        </text>
      </view>
      <view class="faq-item">
        <text class="faq-question">报名后为什么还没有回复？</text>
        <text class="faq-answer">
          对方可能暂未查看消息，建议进入消息中心继续补充自己的时间安排、经验或可服务内容，让沟通更明确。
        </text>
      </view>
      <view class="faq-item">
        <text class="faq-question">简历一定要填吗？</text>
        <text class="faq-answer">
          不是强制项，但建议尽量完善。尤其是技能互助、文案、家教等需求，简历信息越完整，越容易获得对方信任。
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onBackPress, onLoad } from '@dcloudio/uni-app'
import { handleJobsMineBackPress } from '../../utils/jobsNavigation'

let pageOptions = {}

const go = (url) => {
  uni.navigateTo({ url })
}

onLoad((options) => {
  pageOptions = options || {}
})

onBackPress(() => handleJobsMineBackPress(pageOptions))
</script>

<style scoped lang="scss">
.jobs-help-page {
  padding-top: 20rpx;
}

.help-hero,
.entry-card,
.guide-card,
.faq-card {
  padding: 30rpx 26rpx;
}

.hero-title,
.section-title,
.entry-title,
.guide-question,
.faq-question {
  display: block;
  color: #0f172a;
}

.hero-title {
  font-size: 34rpx;
  font-weight: 800;
}

.hero-desc,
.entry-desc,
.guide-answer,
.faq-answer {
  display: block;
  margin-top: 12rpx;
  color: #64748b;
  font-size: 24rpx;
  line-height: 1.8;
}

.help-section {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 22rpx;
}

.entry-title {
  font-size: 28rpx;
  font-weight: 800;
}

.entry-card {
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.section-title {
  font-size: 30rpx;
  font-weight: 800;
  margin-bottom: 8rpx;
}

.guide-card,
.faq-card {
  margin-top: 22rpx;
}

.guide-item + .guide-item,
.faq-item + .faq-item {
  margin-top: 22rpx;
  padding-top: 22rpx;
  border-top: 1px solid #e2e8f0;
}

.guide-question,
.faq-question {
  font-size: 27rpx;
  font-weight: 700;
}
</style>
