<template>
  <div class="admin-shell dashboard">
    <aside class="sidebar">
      <div class="brand">校园百事通后台</div>
      <button v-for="item in tabs" :key="item.key" :class="{ active: tab === item.key }" @click="tab = item.key">
        {{ item.label }}
      </button>
      <button @click="logout">退出登录</button>
    </aside>

    <main class="content">
      <section v-if="tab === 'overview'" class="panel-grid">
        <div class="panel stat">
          <span>平台用户数</span>
          <strong>{{ users.length }}</strong>
        </div>
        <div class="panel stat">
          <span>实名待审数</span>
          <strong>{{ verifications.length }}</strong>
        </div>
        <div class="panel stat">
          <span>举报工单数</span>
          <strong>{{ reports.length }}</strong>
        </div>
        <div class="panel stat">
          <span>跑腿订单数</span>
          <strong>{{ errandOrders.length }}</strong>
        </div>
      </section>

      <section v-if="tab === 'users'" class="panel">
        <div class="section-head">
          <h2>用户列表</h2>
        </div>
        <el-table :data="users" stripe @row-click="openUserDetail">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="phone" label="手机号" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="role" label="角色" width="100" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="openUserDetail(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'verification'" class="panel">
        <div class="section-head">
          <h2>实名审核</h2>
        </div>
        <el-table :data="verifications" stripe @row-click="openVerificationDetail">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="realName" label="姓名" />
          <el-table-column prop="idCardNo" label="身份证号" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="reviewVerification(row.id, 'approve')">通过</el-button>
              <el-button type="danger" link @click.stop="reviewVerification(row.id, 'reject')">驳回</el-button>
              <el-button type="info" link @click.stop="openVerificationDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'reports'" class="panel">
        <div class="section-head">
          <h2>举报工单</h2>
        </div>
        <el-table :data="reports" stripe @row-click="openReportDetail">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="module" label="模块" width="120" />
          <el-table-column prop="reportType" label="类型" width="120" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column prop="description" label="说明" show-overflow-tooltip />
          <el-table-column label="操作" width="260">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="handleReport(row.id, 'PROCESSING')">处理中</el-button>
              <el-button type="success" link @click.stop="handleReport(row.id, 'RESOLVED')">已处理</el-button>
              <el-button type="danger" link @click.stop="handleReport(row.id, 'REJECTED')">驳回</el-button>
              <el-button type="info" link @click.stop="openReportDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'errand'" class="panel">
        <div class="section-head">
          <h2>跑腿订单</h2>
          <div class="toolbar">
            <el-input v-model="errandKeyword" placeholder="搜索订单号、地址、发布人、接单人" clearable class="toolbar-input" />
            <el-select v-model="errandStatus" placeholder="状态筛选" clearable class="toolbar-select">
              <el-option v-for="item in errandStatusOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-button type="primary" @click="loadErrandOrders">查询</el-button>
          </div>
        </div>

        <div class="rules-bar">
          <el-input-number v-model="errandRules.urgentFee" :min="0" :precision="2" :step="0.5" />
          <span>加急附加费</span>
          <el-input-number v-model="errandRules.fragileFee" :min="0" :precision="2" :step="0.5" />
          <span>易碎附加费</span>
          <el-button type="primary" @click="saveErrandRules">保存配置</el-button>
        </div>

        <el-table :data="errandOrders" stripe @row-click="openErrandDetail">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="serviceType" label="服务类型" width="110" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column label="发布人" width="170">
            <template #default="{ row }">{{ row.publisher?.nickname || '-' }} / {{ row.publisher?.phone || '-' }}</template>
          </el-table-column>
          <el-table-column label="接单人" width="170">
            <template #default="{ row }">{{ row.runner?.nickname || '-' }} / {{ row.runner?.phone || '-' }}</template>
          </el-table-column>
          <el-table-column prop="pickupAddress" label="取件地址" show-overflow-tooltip />
          <el-table-column prop="deliveryAddress" label="送达地址" show-overflow-tooltip />
          <el-table-column prop="totalFee" label="金额" width="110" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button type="info" link @click.stop="openErrandDetail(row)">详情</el-button>
              <el-button type="danger" link @click.stop="handleErrandAction(row.id, 'FORCE_CANCEL')">强制取消</el-button>
              <el-button type="warning" link @click.stop="handleErrandAction(row.id, 'MARK_DISPUTED')">争议中</el-button>
              <el-button type="success" link @click.stop="handleErrandAction(row.id, 'MARK_COMPLETED')">已完成</el-button>
              <el-button type="primary" link @click.stop="handleErrandAction(row.id, 'RESTORE_PUBLISHED')">恢复公开</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'message'" class="panel">
        <div class="section-head">
          <h2>系统消息发送</h2>
        </div>
        <el-radio-group v-model="messageScope" class="message-scope">
          <el-radio-button label="all">全体用户</el-radio-button>
          <el-radio-button label="selected">指定用户</el-radio-button>
        </el-radio-group>
        <el-select
          v-if="messageScope === 'selected'"
          v-model="selectedUserIds"
          class="user-select"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="选择要接收消息的用户"
        >
          <el-option
            v-for="item in users"
            :key="item.id"
            :label="`${item.phone} / ${item.nickname}`"
            :value="item.id"
          />
        </el-select>
        <el-input v-model="systemMessage" type="textarea" :rows="5" placeholder="输入要发送给用户的消息" />
        <el-button type="primary" class="send-btn" @click="sendMessage">
          {{ messageScope === 'all' ? '发送给全部用户' : '发送给指定用户' }}
        </el-button>
      </section>
    </main>

    <el-drawer v-model="userDrawerVisible" title="用户详情" size="480px">
      <template v-if="userDetail">
        <div class="detail-grid">
          <div class="detail-item"><span>手机号</span><strong>{{ userDetail.phone }}</strong></div>
          <div class="detail-item"><span>昵称</span><strong>{{ userDetail.nickname }}</strong></div>
          <div class="detail-item"><span>角色</span><strong>{{ userDetail.role }}</strong></div>
          <div class="detail-item"><span>状态</span><strong>{{ userDetail.status }}</strong></div>
          <div class="detail-item"><span>实名状态</span><strong>{{ userDetail.realNameStatus }}</strong></div>
          <div class="detail-item"><span>性别</span><strong>{{ userDetail.gender || '-' }}</strong></div>
          <div class="detail-item full"><span>联系方式</span><strong>{{ userDetail.contactPhone || '-' }}</strong></div>
          <div class="detail-item full"><span>个人简介</span><strong>{{ userDetail.bio || '-' }}</strong></div>
          <div class="detail-item"><span>举报次数</span><strong>{{ userDetail.reportCount }}</strong></div>
          <div class="detail-item"><span>注册时间</span><strong>{{ formatDate(userDetail.createdAt) }}</strong></div>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="verificationDrawerVisible" title="实名审核详情" size="560px">
      <template #header>
        <div class="drawer-head">
          <span>实名审核详情</span>
          <div class="drawer-actions" v-if="verificationDetail">
            <el-button type="primary" @click="reviewVerification(verificationDetail.id, 'approve')">通过</el-button>
            <el-button type="danger" @click="reviewVerification(verificationDetail.id, 'reject')">驳回</el-button>
          </div>
        </div>
      </template>
      <template v-if="verificationDetail">
        <div class="detail-grid">
          <div class="detail-item"><span>姓名</span><strong>{{ verificationDetail.realName }}</strong></div>
          <div class="detail-item"><span>状态</span><strong>{{ verificationDetail.status }}</strong></div>
          <div class="detail-item full"><span>身份证号</span><strong>{{ verificationDetail.idCardNo }}</strong></div>
          <div class="detail-item full"><span>驳回原因</span><strong>{{ verificationDetail.rejectReason || '-' }}</strong></div>
          <div class="detail-item"><span>审核人</span><strong>{{ verificationDetail.reviewedBy || '-' }}</strong></div>
          <div class="detail-item"><span>审核时间</span><strong>{{ formatDate(verificationDetail.reviewedAt) }}</strong></div>
        </div>
        <div class="preview-section">
          <div class="preview-card" v-if="verificationDetail.frontFileUrl">
            <div class="preview-title">身份证正面</div>
            <img :src="verificationDetail.frontFileUrl" alt="front" />
          </div>
          <div class="preview-card" v-if="verificationDetail.backFileUrl">
            <div class="preview-title">身份证反面</div>
            <img :src="verificationDetail.backFileUrl" alt="back" />
          </div>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="reportDrawerVisible" title="举报工单详情" size="620px">
      <template v-if="reportDetail">
        <div class="drawer-head space-between">
          <span>举报工单详情</span>
          <div class="drawer-actions">
            <el-button type="primary" @click="handleReport(reportDetail.id, 'PROCESSING')">处理中</el-button>
            <el-button type="success" @click="handleReport(reportDetail.id, 'RESOLVED')">已处理</el-button>
            <el-button type="danger" @click="handleReport(reportDetail.id, 'REJECTED')">驳回</el-button>
          </div>
        </div>
        <div class="detail-grid">
          <div class="detail-item"><span>模块</span><strong>{{ reportDetail.module }}</strong></div>
          <div class="detail-item"><span>状态</span><strong>{{ reportDetail.status }}</strong></div>
          <div class="detail-item"><span>目标类型</span><strong>{{ reportDetail.targetType }}</strong></div>
          <div class="detail-item"><span>目标 ID</span><strong>{{ reportDetail.targetId ?? '-' }}</strong></div>
          <div class="detail-item"><span>举报类型</span><strong>{{ reportDetail.reportType }}</strong></div>
          <div class="detail-item"><span>联系方式</span><strong>{{ reportDetail.contactPhone || '-' }}</strong></div>
          <div class="detail-item full"><span>举报描述</span><strong>{{ reportDetail.description }}</strong></div>
          <div class="detail-item full"><span>处理备注</span><strong>{{ reportDetail.handleRemark || '-' }}</strong></div>
          <div class="detail-item"><span>处理人</span><strong>{{ reportDetail.handledBy || '-' }}</strong></div>
          <div class="detail-item"><span>处理时间</span><strong>{{ formatDate(reportDetail.handledAt) }}</strong></div>
        </div>
        <div v-if="reportDetail.attachments?.length" class="preview-section">
          <div v-for="item in reportDetail.attachments" :key="item.fileId" class="preview-card">
            <div class="preview-title">{{ item.originalName }}</div>
            <img :src="item.url" :alt="item.originalName" />
          </div>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="errandDrawerVisible" title="跑腿订单详情" size="720px">
      <template v-if="errandDetail">
        <div class="drawer-head space-between">
          <span>跑腿订单详情</span>
          <div class="drawer-actions">
            <el-button type="danger" @click="handleErrandAction(errandDetail.id, 'FORCE_CANCEL')">强制取消</el-button>
            <el-button type="warning" @click="handleErrandAction(errandDetail.id, 'MARK_DISPUTED')">标记争议中</el-button>
            <el-button type="success" @click="handleErrandAction(errandDetail.id, 'MARK_COMPLETED')">标记已完成</el-button>
            <el-button type="primary" @click="handleErrandAction(errandDetail.id, 'RESTORE_PUBLISHED')">恢复公开</el-button>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-item"><span>订单号</span><strong>{{ errandDetail.orderNo }}</strong></div>
          <div class="detail-item"><span>状态</span><strong>{{ errandDetail.status }}</strong></div>
          <div class="detail-item"><span>服务类型</span><strong>{{ errandDetail.serviceType }}</strong></div>
          <div class="detail-item"><span>金额</span><strong>￥{{ errandDetail.totalFee }}</strong></div>
          <div class="detail-item full"><span>取件地址</span><strong>{{ errandDetail.pickupAddress }}</strong></div>
          <div class="detail-item full"><span>送达地址</span><strong>{{ errandDetail.deliveryAddress }}</strong></div>
          <div class="detail-item full"><span>预约时间</span><strong>{{ errandDetail.pickupTimeText || '-' }}</strong></div>
          <div class="detail-item full"><span>需求明细</span><strong>{{ errandDetail.detailContent }}</strong></div>
          <div class="detail-item full"><span>补充备注</span><strong>{{ errandDetail.remark || '-' }}</strong></div>
          <div class="detail-item"><span>发布人</span><strong>{{ formatCounterparty(errandDetail.publisher) }}</strong></div>
          <div class="detail-item"><span>接单人</span><strong>{{ formatCounterparty(errandDetail.runner) }}</strong></div>
          <div class="detail-item"><span>会话 ID</span><strong>{{ errandDetail.conversationId || '-' }}</strong></div>
          <div class="detail-item"><span>接单截止</span><strong>{{ formatDate(errandDetail.acceptDeadline) }}</strong></div>
          <div class="detail-item"><span>接单时间</span><strong>{{ formatDate(errandDetail.acceptedAt) }}</strong></div>
          <div class="detail-item"><span>开始/取件</span><strong>{{ formatDate(errandDetail.pickedUpAt) }}</strong></div>
          <div class="detail-item"><span>处理中</span><strong>{{ formatDate(errandDetail.deliveredAt) }}</strong></div>
          <div class="detail-item"><span>完成时间</span><strong>{{ formatDate(errandDetail.completedAt) }}</strong></div>
          <div class="detail-item"><span>取消时间</span><strong>{{ formatDate(errandDetail.cancelledAt) }}</strong></div>
          <div class="detail-item full"><span>取消原因</span><strong>{{ errandDetail.cancelReason || '-' }}</strong></div>
        </div>

        <div v-if="errandDetail.attachments?.length" class="preview-section">
          <div v-for="item in errandDetail.attachments" :key="item.fileId" class="preview-card">
            <div class="preview-title">{{ item.originalName }}</div>
            <img :src="item.url" :alt="item.originalName" />
          </div>
        </div>

        <div v-if="errandDetail.relatedReports?.length" class="related-panel">
          <div class="related-title">关联举报工单</div>
          <div v-for="item in errandDetail.relatedReports" :key="item.id" class="related-item">
            <strong>#{{ item.id }} {{ item.status }}</strong>
            <span>{{ item.reportType }} / {{ item.description }}</span>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

type UserRow = {
  id: number
  phone: string
  nickname: string
  role: string
  status: string
}

type AdminUserDetail = {
  userId: number
  phone: string
  nickname: string
  avatarUrl?: string
  role: string
  status: string
  realNameStatus: string
  gender?: string
  contactPhone?: string
  bio?: string
  reportCount: number
  createdAt?: string
}

type VerificationDetail = {
  id: number
  realName: string
  idCardNo: string
  status: string
  rejectReason?: string
  reviewedBy?: number
  reviewedAt?: string
  frontFileUrl?: string
  backFileUrl?: string
}

type FileAttachment = {
  fileId: number
  url: string
  originalName: string
}

type ReportDetail = {
  id: number
  module: string
  targetType: string
  targetId?: number
  reportType: string
  description: string
  contactPhone?: string
  status: string
  handleRemark?: string
  handledBy?: number
  handledAt?: string
  attachments?: FileAttachment[]
}

type ErrandCounterparty = {
  userId: number
  nickname: string
  phone: string
}

type RelatedReport = {
  id: number
  status: string
  reportType: string
  description: string
}

type ErrandOrder = {
  id: number
  orderNo: string
  serviceType: string
  pickupAddress: string
  deliveryAddress: string
  pickupTimeText?: string
  detailContent: string
  remark?: string
  totalFee: number
  status: string
  acceptDeadline?: string
  acceptedAt?: string
  pickedUpAt?: string
  deliveredAt?: string
  completedAt?: string
  cancelledAt?: string
  cancelReason?: string
  conversationId?: number
  publisher?: ErrandCounterparty
  runner?: ErrandCounterparty
  attachments?: FileAttachment[]
  relatedReports?: RelatedReport[]
}

const router = useRouter()
const tabs = [
  { key: 'overview', label: '概览' },
  { key: 'users', label: '用户管理' },
  { key: 'verification', label: '实名审核' },
  { key: 'reports', label: '举报工单' },
  { key: 'errand', label: '跑腿订单' },
  { key: 'message', label: '系统消息' },
]
const errandStatusOptions = ['PUBLISHED', 'ACCEPTED', 'IN_PROGRESS', 'DELIVERING', 'COMPLETED', 'CANCELLED', 'DISPUTED', 'EXPIRED']

const tab = ref('overview')
const users = ref<UserRow[]>([])
const verifications = ref<any[]>([])
const reports = ref<any[]>([])
const errandOrders = ref<ErrandOrder[]>([])
const systemMessage = ref('系统通知：欢迎体验校园跑腿模块。')
const messageScope = ref<'all' | 'selected'>('all')
const selectedUserIds = ref<number[]>([])
const errandKeyword = ref('')
const errandStatus = ref('')
const errandRules = ref({
  urgentFee: 2,
  fragileFee: 3,
})

const userDrawerVisible = ref(false)
const verificationDrawerVisible = ref(false)
const reportDrawerVisible = ref(false)
const errandDrawerVisible = ref(false)

const userDetail = ref<AdminUserDetail | null>(null)
const verificationDetail = ref<VerificationDetail | null>(null)
const reportDetail = ref<ReportDetail | null>(null)
const errandDetail = ref<ErrandOrder | null>(null)

const loadAll = async () => {
  users.value = (await api.get('/admin/users')) as UserRow[]
  verifications.value = ((await api.get('/admin/verifications')) as any).records || []
  reports.value = ((await api.get('/admin/reports')) as any).records || []
  await Promise.all([loadErrandOrders(), loadErrandRules()])
}

const loadErrandOrders = async () => {
  const data = (await api.get('/admin/errand/orders', {
    params: {
      keyword: errandKeyword.value,
      status: errandStatus.value,
      current: 1,
      size: 50,
    },
  })) as any
  errandOrders.value = data.records || []
}

const loadErrandRules = async () => {
  errandRules.value = (await api.get('/admin/errand/rules')) as typeof errandRules.value
}

const saveErrandRules = async () => {
  await api.post('/admin/errand/rules', errandRules.value)
  ElMessage.success('配置已保存')
}

const openUserDetail = async (row: UserRow) => {
  userDetail.value = (await api.get(`/admin/users/${row.id}`)) as AdminUserDetail
  userDrawerVisible.value = true
}

const openVerificationDetail = async (row: { id: number }) => {
  verificationDetail.value = (await api.get(`/admin/verifications/${row.id}`)) as VerificationDetail
  verificationDrawerVisible.value = true
}

const openReportDetail = async (row: { id: number }) => {
  reportDetail.value = (await api.get(`/admin/reports/${row.id}`)) as ReportDetail
  reportDrawerVisible.value = true
}

const openErrandDetail = async (row: { id: number }) => {
  errandDetail.value = (await api.get(`/admin/errand/orders/${row.id}`)) as ErrandOrder
  errandDrawerVisible.value = true
}

const reviewVerification = async (id: number, action: 'approve' | 'reject') => {
  let rejectReason = ''
  if (action === 'reject') {
    rejectReason = await ElMessageBox.prompt('请输入驳回原因', '驳回实名', {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：证件照片不清晰，请重新上传',
    }).then(({ value }) => value)
  }
  await api.post(`/admin/verifications/${id}/review`, {
    action,
    rejectReason,
  })
  ElMessage.success('处理成功')
  if (verificationDrawerVisible.value && verificationDetail.value?.id === id) {
    verificationDetail.value = (await api.get(`/admin/verifications/${id}`)) as VerificationDetail
  }
  await loadAll()
}

const handleReport = async (id: number, status: 'PROCESSING' | 'RESOLVED' | 'REJECTED') => {
  const promptTitle = status === 'REJECTED' ? '请输入驳回备注' : '请输入处理备注'
  const defaultRemark =
    status === 'PROCESSING'
      ? '后台已受理，正在处理中'
      : status === 'RESOLVED'
        ? '后台已确认处理完成'
        : '后台驳回该工单，请查看备注'
  const remark = await ElMessageBox.prompt(promptTitle, '处理举报工单', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: defaultRemark,
    inputPlaceholder: '请输入处理说明',
  }).then(({ value }) => value || defaultRemark)

  await api.post(`/admin/reports/${id}/handle`, {
    status,
    remark,
  })
  ElMessage.success('处理成功')
  if (reportDrawerVisible.value && reportDetail.value?.id === id) {
    reportDetail.value = (await api.get(`/admin/reports/${id}`)) as ReportDetail
  }
  await loadAll()
}

const handleErrandAction = async (id: number, action: 'FORCE_CANCEL' | 'RESTORE_PUBLISHED' | 'MARK_DISPUTED' | 'MARK_COMPLETED') => {
  const defaultRemarkMap = {
    FORCE_CANCEL: '后台人工取消订单',
    RESTORE_PUBLISHED: '后台恢复订单公开状态',
    MARK_DISPUTED: '后台标记订单争议处理中',
    MARK_COMPLETED: '后台确认订单完成',
  }
  const remark = await ElMessageBox.prompt('请输入处理备注', '处理跑腿订单', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: defaultRemarkMap[action],
    inputPlaceholder: '请输入处理说明',
  }).then(({ value }) => value || defaultRemarkMap[action])

  await api.post(`/admin/errand/orders/${id}/action`, {
    action,
    remark,
  })
  ElMessage.success('订单状态已更新')
  if (errandDrawerVisible.value && errandDetail.value?.id === id) {
    errandDetail.value = (await api.get(`/admin/errand/orders/${id}`)) as ErrandOrder
  }
  await loadErrandOrders()
}

const sendMessage = async () => {
  if (messageScope.value === 'selected' && !selectedUserIds.value.length) {
    ElMessage.warning('请先选择接收用户')
    return
  }
  await api.post('/admin/messages/system/send', {
    content: systemMessage.value,
    userIds: messageScope.value === 'all' ? [] : selectedUserIds.value,
  })
  ElMessage.success('消息已发送')
}

const logout = () => {
  localStorage.removeItem('adminToken')
  router.push('/login')
}

const formatDate = (value?: string) => {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

const formatCounterparty = (value?: ErrandCounterparty) => {
  if (!value) {
    return '-'
  }
  return `${value.nickname} / ${value.phone}`
}

onMounted(loadAll)
</script>

<style scoped>
.dashboard {
  display: grid;
  grid-template-columns: 240px 1fr;
}

.sidebar {
  background: #0f172a;
  color: #fff;
  min-height: 100vh;
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.brand {
  font-size: 22px;
  font-weight: 800;
  margin-bottom: 18px;
}

.sidebar button {
  border: 0;
  border-radius: 14px;
  padding: 12px 14px;
  text-align: left;
  background: transparent;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
}

.sidebar button.active,
.sidebar button:hover {
  background: rgba(59, 130, 246, 0.25);
  color: #fff;
}

.content {
  padding: 24px;
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.panel {
  background: #fff;
  border-radius: 20px;
  padding: 22px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.stat span {
  color: #64748b;
  display: block;
  margin-bottom: 10px;
}

.stat strong {
  font-size: 40px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-input {
  width: 320px;
}

.toolbar-select {
  width: 180px;
}

.rules-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  padding: 16px;
  border-radius: 16px;
  background: #f8fafc;
}

h2 {
  margin: 0;
}

.message-scope {
  margin-bottom: 16px;
}

.user-select {
  width: 100%;
  margin-bottom: 16px;
}

.send-btn {
  margin-top: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.detail-item {
  border-radius: 16px;
  background: #f8fafc;
  padding: 14px 16px;
}

.detail-item.full {
  grid-column: 1 / -1;
}

.detail-item span {
  display: block;
  color: #64748b;
  margin-bottom: 8px;
  font-size: 13px;
}

.detail-item strong {
  color: #0f172a;
  font-size: 14px;
  word-break: break-all;
}

.drawer-head,
.drawer-head.space-between {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.drawer-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.preview-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.preview-card {
  border-radius: 18px;
  overflow: hidden;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.preview-title {
  padding: 12px 14px;
  color: #475569;
  font-size: 13px;
  border-bottom: 1px solid #e2e8f0;
}

.preview-card img {
  width: 100%;
  height: 220px;
  object-fit: cover;
  display: block;
}

.related-panel {
  margin-top: 22px;
  padding: 18px;
  border-radius: 18px;
  background: #f8fafc;
}

.related-title {
  font-weight: 700;
  margin-bottom: 12px;
}

.related-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
}

.related-item:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}
</style>
