<template>
  <div class="admin-shell dashboard">
    <aside class="sidebar">
      <div class="brand">校园百事通后台</div>
      <div v-for="group in navigationGroups" :key="group.title" class="sidebar-group">
        <button class="sidebar-group-toggle" @click="toggleGroup(group.title)">
          <span class="sidebar-group-title">{{ group.title }}</span>
          <span class="sidebar-group-arrow">{{ expandedGroups[group.title] ? '▾' : '▸' }}</span>
        </button>
        <div v-show="expandedGroups[group.title]" class="sidebar-group-items">
          <button
            v-for="item in group.items"
            :key="item.key"
            :class="{ active: tab === item.key }"
            @click="tab = item.key"
          >
            {{ item.label }}
          </button>
        </div>
      </div>
      <button @click="logout">退出登录</button>
    </aside>

    <main class="content">
      <header class="content-hero">
        <div>
          <p class="content-group">{{ currentTabMeta.group }}</p>
          <h1>{{ currentTabMeta.label }}</h1>
          <p class="content-desc">{{ currentTabMeta.description }}</p>
        </div>
      </header>

      <section v-if="tab === 'overview'" class="panel-grid">
        <div class="panel stat">
          <span>平台用户数</span>
          <strong>{{ allUsers.length }}</strong>
        </div>
        <div class="panel stat">
          <span>实名待审数</span>
          <strong>{{ verifications.length }}</strong>
        </div>
        <div class="panel stat">
          <span>举报工单数</span>
          <strong>{{ allReports.length }}</strong>
        </div>
        <div class="panel stat">
          <span>跑腿订单数</span>
          <strong>{{ errandStats.totalOrders }}</strong>
        </div>
        <div class="panel stat">
          <span>美妆好物数</span>
          <strong>{{ beautyStats.totalGoods }}</strong>
        </div>
      </section>

      <section v-if="tab === 'overview'" class="panel-grid errand-overview-grid">
        <div class="panel stat">
          <span>跑腿公开订单</span>
          <strong>{{ errandStats.publicOrders }}</strong>
        </div>
        <div class="panel stat">
          <span>跑腿锁定订单</span>
          <strong>{{ errandStats.lockedOrders }}</strong>
        </div>
        <div class="panel stat">
          <span>进行中举报</span>
          <strong>{{ errandStats.processingReports }}</strong>
        </div>
        <div class="panel stat">
          <span>完成率</span>
          <strong>{{ formatPercent(errandStats.completionRate) }}</strong>
        </div>
      </section>

      <section v-if="tab === 'overview'" class="panel-grid errand-admin-overview">
        <div class="panel errand-summary-panel">
          <div class="mini-head">
            <h3>跑腿订单状态分布</h3>
          </div>
          <div v-if="errandStats.statusBreakdown?.length" class="metrics-stack">
            <div v-for="item in errandStats.statusBreakdown" :key="item.label" class="metric-row">
              <span>{{ formatErrandStatus(item.label) }}</span>
              <strong>{{ item.count }}</strong>
            </div>
          </div>
          <div v-else class="empty-inline">暂无统计数据</div>
        </div>

        <div class="panel errand-summary-panel">
          <div class="mini-head">
            <h3>跑腿举报类型</h3>
          </div>
          <div v-if="errandStats.reportTypeBreakdown?.length" class="summary-list">
            <div v-for="item in errandStats.reportTypeBreakdown" :key="item.label" class="summary-item">
              <div>
                <strong>{{ item.label }}</strong>
                <span>校园跑腿 / 代取维权工单</span>
              </div>
              <em>{{ item.count }} 单</em>
            </div>
          </div>
          <div v-else class="empty-inline">暂无统计数据</div>
        </div>
      </section>

      <section v-if="tab === 'overview'" class="panel-grid beauty-overview-grid">
        <div class="panel beauty-summary-panel">
          <div class="mini-head">
            <h3>美妆分类分布</h3>
          </div>
          <div v-if="beautyStats.categoryBreakdown?.length" class="metrics-stack">
            <div v-for="item in beautyStats.categoryBreakdown" :key="item.category" class="metric-row">
              <span>{{ item.category }}</span>
              <strong>{{ item.count }}</strong>
            </div>
          </div>
          <div v-else class="empty-inline">暂无统计数据</div>
        </div>

        <div class="panel beauty-summary-panel">
          <div class="mini-head">
            <h3>热门浏览商品</h3>
          </div>
          <div v-if="beautyStats.topViewedGoods?.length" class="summary-list">
            <div v-for="item in beautyStats.topViewedGoods" :key="`view-${item.id}`" class="summary-item">
              <div>
                <strong>{{ item.title }}</strong>
                <span>{{ item.category }} / {{ item.status }}</span>
              </div>
              <em>{{ item.viewCount }} 浏览</em>
            </div>
          </div>
          <div v-else class="empty-inline">暂无统计数据</div>
        </div>

        <div class="panel beauty-summary-panel">
          <div class="mini-head">
            <h3>热门收藏商品</h3>
          </div>
          <div v-if="beautyStats.topFavoritedGoods?.length" class="summary-list">
            <div v-for="item in beautyStats.topFavoritedGoods" :key="`favorite-${item.id}`" class="summary-item">
              <div>
                <strong>{{ item.title }}</strong>
                <span>{{ item.category }} / {{ item.status }}</span>
              </div>
              <em>{{ item.favoriteCount }} 收藏</em>
            </div>
          </div>
          <div v-else class="empty-inline">暂无统计数据</div>
        </div>
      </section>

      <section v-if="tab === 'users'" class="panel">
        <div class="section-head">
          <div>
            <h2>用户治理</h2>
            <p class="section-subtitle">围绕跑腿发单、接单、举报行为做警告、限制、封禁与恢复操作。</p>
          </div>
          <div class="toolbar">
            <el-input v-model="userKeyword" placeholder="搜索手机号、昵称" clearable class="toolbar-input" />
            <el-select v-model="userRole" placeholder="角色筛选" clearable class="toolbar-select">
              <el-option v-for="item in userRoleOptions" :key="item" :label="formatUserRole(item)" :value="item" />
            </el-select>
            <el-select v-model="userStatus" placeholder="状态筛选" clearable class="toolbar-select">
              <el-option v-for="item in userStatusOptions" :key="item" :label="formatUserStatus(item)" :value="item" />
            </el-select>
            <el-button type="primary" @click="loadUsers">查询</el-button>
          </div>
        </div>
        <el-table :data="users" stripe @row-click="openUserDetail">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="phone" label="手机号" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column label="角色" width="100">
            <template #default="{ row }">{{ formatUserRole(row.role) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="140">
            <template #default="{ row }">
              <el-tag :type="userStatusTagType(row.status)">{{ formatUserStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="举报权限" width="130">
            <template #default="{ row }">
              <el-tag :type="row.reportRestricted ? 'danger' : 'success'">
                {{ row.reportRestricted ? '已限制' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="390">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="openUserDetail(row)">查看详情</el-button>
              <el-button type="warning" link @click.stop="handleUserStatus(row.id, 'WARNED')">警告</el-button>
              <el-button type="danger" link @click.stop="handleUserStatus(row.id, 'ERRAND_RESTRICTED')">限制跑腿</el-button>
              <el-button v-if="!row.reportRestricted" type="danger" link @click.stop="handleUserStatus(row.id, 'REPORT_RESTRICTED')">限制举报</el-button>
              <el-button v-else type="success" link @click.stop="handleUserStatus(row.id, 'REPORT_ACTIVE')">解除举报限制</el-button>
              <el-button type="danger" link @click.stop="handleUserStatus(row.id, 'TEMP_BANNED')">临时封禁</el-button>
              <el-button v-if="row.status !== 'ACTIVE'" type="success" link @click.stop="handleUserStatus(row.id, 'ACTIVE')">恢复正常</el-button>
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
          <div>
            <h2>举报工单</h2>
          </div>
          <div class="toolbar">
            <el-input v-model="reportKeyword" placeholder="搜索举报内容、联系方式、目标ID" clearable class="toolbar-input" />
            <el-select v-model="reportModule" placeholder="模块筛选" clearable class="toolbar-select">
              <el-option v-for="item in reportModuleOptions" :key="item" :label="formatModuleName(item)" :value="item" />
            </el-select>
            <el-select v-model="reportStatus" placeholder="状态筛选" clearable class="toolbar-select">
              <el-option v-for="item in reportStatusOptions" :key="item" :label="formatReportStatus(item)" :value="item" />
            </el-select>
            <el-select v-model="reportType" placeholder="类型筛选" clearable class="toolbar-select">
              <el-option v-for="item in reportTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-button type="primary" @click="loadReports">查询</el-button>
          </div>
        </div>
        <el-table :data="reports" stripe @row-click="openReportDetail">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="模块" width="120">
            <template #default="{ row }">{{ formatModuleName(row.module) }}</template>
          </el-table-column>
          <el-table-column prop="reportType" label="类型" width="160" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="reportStatusTagType(row.status)">{{ formatReportStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" show-overflow-tooltip />
          <el-table-column label="操作" width="260">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="handleReport(row, 'PROCESSING')">处理中</el-button>
              <el-button type="success" link @click.stop="handleReport(row, 'RESOLVED')">已处理</el-button>
              <el-button type="danger" link @click.stop="handleReport(row, 'REJECTED')">驳回</el-button>
              <el-button type="info" link @click.stop="openReportDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'errand-stats'" class="errand-admin-layout">
        <div class="panel">
          <div class="section-head">
            <div>
              <h2>数据统计</h2>
              <p class="section-subtitle">集中查看校园跑腿的订单发布量、接单率、完成率、违规工单量与流水统计。</p>
            </div>
            <el-button type="primary" @click="refreshErrandAdmin">刷新监管数据</el-button>
          </div>

          <div class="panel-grid errand-kpi-grid">
            <div class="panel stat inset">
              <span>订单发布量</span>
              <strong>{{ errandStats.totalOrders }}</strong>
            </div>
            <div class="panel stat inset">
              <span>接单率</span>
              <strong>{{ formatPercent(errandStats.acceptanceRate) }}</strong>
            </div>
            <div class="panel stat inset">
              <span>完成率</span>
              <strong>{{ formatPercent(errandStats.completionRate) }}</strong>
            </div>
            <div class="panel stat inset">
              <span>违规工单量</span>
              <strong>{{ errandStats.reportTotal }}</strong>
            </div>
          </div>

          <div class="rules-bar errand-highlight-bar">
            <span>公开广场：<strong>{{ errandStats.publicOrders }}</strong></span>
            <span>锁定履约：<strong>{{ errandStats.lockedOrders }}</strong></span>
            <span>归档订单：<strong>{{ errandStats.archivedOrders }}</strong></span>
            <span>交易流水：<strong>￥{{ formatMoney(errandStats.grossTransactionAmount) }}</strong></span>
            <span>已完结流水：<strong>￥{{ formatMoney(errandStats.completedTransactionAmount) }}</strong></span>
            <span>取消率：<strong>{{ formatPercent(errandStats.cancelRate) }}</strong></span>
          </div>
        </div>
      </section>

      <section v-if="tab === 'errand-orders'" class="panel">
        <div class="panel">
          <div class="section-head">
            <div>
              <h2>订单监控</h2>
              <p class="section-subtitle">实时查看公共订单广场、已接单、已完成、已取消全量订单，并监控锁定、释放、归档状态。</p>
            </div>
            <div class="toolbar">
              <el-input v-model="errandKeyword" placeholder="搜索订单号、地址、发布人、接单人" clearable class="toolbar-input" />
              <el-select v-model="errandStatus" placeholder="状态筛选" clearable class="toolbar-select">
                <el-option v-for="item in errandStatusOptions" :key="item" :label="formatErrandStatus(item)" :value="item" />
              </el-select>
              <el-select v-model="errandServiceType" placeholder="服务类型" clearable class="toolbar-select">
                <el-option v-for="item in errandServiceTypeOptions" :key="item" :label="formatErrandServiceType(item)" :value="item" />
              </el-select>
            <el-select v-model="errandFlowState" placeholder="流转状态" clearable class="toolbar-select">
              <el-option v-for="item in errandFlowStateOptions" :key="item" :label="formatErrandFlowState(item)" :value="item" />
            </el-select>
            <el-select v-model="errandAlertType" placeholder="异常预警" clearable class="toolbar-select">
              <el-option v-for="item in errandAlertTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="primary" @click="searchErrandOrders">查询</el-button>
          </div>
        </div>

        <div class="rules-bar errand-risk-bar">
          <span>争议中：<strong>{{ currentErrandAlertSummary.disputed }}</strong></span>
          <span>临近接单超时：<strong>{{ currentErrandAlertSummary.expiringAccept }}</strong></span>
          <span>超时未接单：<strong>{{ currentErrandAlertSummary.expiredUnaccepted }}</strong></span>
          <span>举报待处理：<strong>{{ currentErrandAlertSummary.reportPending }}</strong></span>
          <span>举报已超时：<strong>{{ currentErrandAlertSummary.reportOverdue }}</strong></span>
        </div>

          <el-table :data="errandOrders" stripe @row-click="openErrandDetail">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column label="状态" width="130">
              <template #default="{ row }">
                <el-tag :type="errandStatusTagType(row.status)">{{ formatErrandStatus(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="流转监控" width="140">
              <template #default="{ row }">{{ formatErrandFlow(row) }}</template>
            </el-table-column>
            <el-table-column label="服务类型" width="120">
              <template #default="{ row }">{{ formatErrandServiceType(row.serviceType) }}</template>
            </el-table-column>
            <el-table-column label="异常预警" min-width="220">
              <template #default="{ row }">
                <div v-if="row.riskTags?.length" class="tag-inline">
                  <el-tag
                    v-for="tag in row.riskTags"
                    :key="`${row.id}-${tag}`"
                    size="small"
                    :type="errandRiskTagType(tag)"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
                <span v-else class="empty-inline">暂无异常</span>
              </template>
            </el-table-column>
            <el-table-column label="发布人" width="170">
              <template #default="{ row }">{{ row.publisher?.nickname || '-' }} / {{ row.publisher?.phone || '-' }}</template>
            </el-table-column>
            <el-table-column label="接单人" width="170">
              <template #default="{ row }">{{ row.runner?.nickname || '-' }} / {{ row.runner?.phone || '-' }}</template>
            </el-table-column>
            <el-table-column prop="pickupAddress" label="取件地址" show-overflow-tooltip />
            <el-table-column prop="deliveryAddress" label="送达地址" show-overflow-tooltip />
            <el-table-column prop="totalFee" label="金额" width="110">
              <template #default="{ row }">￥{{ formatMoney(row.totalFee) }}</template>
            </el-table-column>
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

          <div class="table-footer">
            <el-pagination
              v-model:current-page="errandOrderCurrent"
              v-model:page-size="errandOrderPageSize"
              background
              :page-sizes="pageSizeOptions"
              :total="errandOrderTotal"
              :layout="paginationLayout"
              @current-change="loadErrandOrders"
              @size-change="handleErrandOrderSizeChange"
            />
          </div>
        </div>
      </section>

      <section v-if="tab === 'errand-reports'" class="panel">
        <div class="panel">
          <div class="section-head">
            <div>
              <h2>举报工单处理</h2>
              <p class="section-subtitle">处理恶意下单、超时未履约、恶意加价、物品损坏、骚扰沟通、虚假信息、恶意差评、线下违规交易等工单。</p>
            </div>
            <div class="toolbar">
              <el-input v-model="errandReportKeyword" placeholder="搜索举报说明、联系方式、订单ID" clearable class="toolbar-input" />
              <el-select v-model="errandReportStatus" placeholder="状态筛选" clearable class="toolbar-select">
                <el-option v-for="item in reportStatusOptions" :key="item" :label="formatReportStatus(item)" :value="item" />
              </el-select>
              <el-select v-model="errandReportType" placeholder="举报类型" clearable class="toolbar-select">
                <el-option v-for="item in reportTypeOptions" :key="item" :label="item" :value="item" />
              </el-select>
              <el-button type="primary" @click="searchErrandReports">查询</el-button>
            </div>
          </div>

          <div class="rules-bar errand-highlight-bar">
            <span>处理中：<strong>{{ errandStats.processingReports }}</strong></span>
            <span>已处理：<strong>{{ errandStats.resolvedReports }}</strong></span>
            <span>已驳回：<strong>{{ errandStats.rejectedReports }}</strong></span>
            <span>关联争议订单：<strong>{{ errandStats.disputedOrders }}</strong></span>
          </div>

          <el-table :data="errandReportRows" stripe @row-click="openReportDetail">
            <el-table-column prop="id" label="工单ID" width="90" />
            <el-table-column prop="reportType" label="举报类型" width="160" />
            <el-table-column label="状态" width="130">
              <template #default="{ row }">
                <el-tag :type="reportStatusTagType(row.status)">{{ formatReportStatus(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="处理时限" min-width="220">
              <template #default="{ row }">
                <div class="deadline-cell">
                  <div class="deadline-line">
                    <span class="deadline-label">截止</span>
                    <strong>{{ formatReportDeadlineAt(row) }}</strong>
                  </div>
                  <div class="deadline-line">
                    <el-tag size="small" :type="reportDeadlineTagType(row)">{{ formatReportDeadlineState(row) }}</el-tag>
                    <span class="deadline-hint">{{ formatReportDeadlineHint(row) }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="targetId" label="关联订单" width="120" />
            <el-table-column prop="description" label="问题说明" show-overflow-tooltip />
            <el-table-column prop="contactPhone" label="联系方式" width="150" />
            <el-table-column label="操作" width="250">
              <template #default="{ row }">
                <el-button type="primary" link @click.stop="handleReport(row, 'PROCESSING')">受理</el-button>
                <el-button type="success" link @click.stop="handleReport(row, 'RESOLVED')">处理完成</el-button>
                <el-button type="danger" link @click.stop="handleReport(row, 'REJECTED')">驳回</el-button>
                <el-button type="warning" link @click.stop="openErrandFromRow(row)">订单详情</el-button>
                <el-button type="info" link @click.stop="openReportDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-footer">
            <el-pagination
              v-model:current-page="errandReportCurrent"
              v-model:page-size="errandReportPageSize"
              background
              :page-sizes="pageSizeOptions"
              :total="errandReportTotal"
              :layout="paginationLayout"
              @current-change="loadErrandReports"
              @size-change="handleErrandReportSizeChange"
            />
          </div>
        </div>
      </section>

      <section v-if="tab === 'errand-governance'" class="panel">
        <div class="panel">
          <div class="section-head">
            <div>
              <h2>用户权限与处罚</h2>
              <p class="section-subtitle">支持警告、跑腿功能限制、短期封禁、永久封禁、拉黑，并可恢复正常状态。</p>
            </div>
            <div class="toolbar">
              <el-input v-model="governanceKeyword" placeholder="搜索手机号、昵称" clearable class="toolbar-input" />
              <el-select v-model="governanceStatus" placeholder="状态筛选" clearable class="toolbar-select">
                <el-option v-for="item in userStatusOptions" :key="item" :label="formatUserStatus(item)" :value="item" />
              </el-select>
              <el-button type="primary" @click="searchGovernanceUsers">查询</el-button>
            </div>
          </div>

          <el-table :data="governanceUserRows" stripe @row-click="openUserDetail">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column prop="nickname" label="昵称" width="140" />
            <el-table-column label="当前状态" width="140">
              <template #default="{ row }">
                <el-tag :type="userStatusTagType(row.status)">{{ formatUserStatus(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="举报权限" width="130">
              <template #default="{ row }">
                <el-tag :type="row.reportRestricted ? 'danger' : 'success'">
                  {{ row.reportRestricted ? '已限制' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="治理动作" min-width="390">
              <template #default="{ row }">
                <el-button type="warning" link @click.stop="handleUserStatus(row.id, 'WARNED')">警告</el-button>
                <el-button type="danger" link @click.stop="handleUserStatus(row.id, 'ERRAND_RESTRICTED')">限制跑腿</el-button>
                <el-button v-if="!row.reportRestricted" type="danger" link @click.stop="handleUserStatus(row.id, 'REPORT_RESTRICTED')">限制举报</el-button>
                <el-button v-else type="success" link @click.stop="handleUserStatus(row.id, 'REPORT_ACTIVE')">解除举报限制</el-button>
                <el-button type="danger" link @click.stop="handleUserStatus(row.id, 'TEMP_BANNED')">短期封禁</el-button>
                <el-button type="danger" link @click.stop="handleUserStatus(row.id, 'PERMANENT_BANNED')">永久封禁</el-button>
                <el-button type="info" link @click.stop="handleUserStatus(row.id, 'BLACKLISTED')">拉黑</el-button>
                <el-button v-if="row.status !== 'ACTIVE'" type="success" link @click.stop="handleUserStatus(row.id, 'ACTIVE')">恢复正常</el-button>
              </template>
            </el-table-column>
            <el-table-column label="详情" width="110">
              <template #default="{ row }">
                <el-button type="primary" link @click.stop="openUserDetail(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-footer">
            <el-pagination
              v-model:current-page="governanceCurrent"
              v-model:page-size="governancePageSize"
              background
              :page-sizes="pageSizeOptions"
              :total="governanceTotal"
              :layout="paginationLayout"
              @current-change="loadGovernanceUsers"
              @size-change="handleGovernanceSizeChange"
            />
          </div>
        </div>
      </section>

      <section v-if="tab === 'errand-rules'" class="panel">
        <div class="panel">
          <div class="section-head">
            <div>
              <h2>交易规则配置</h2>
              <p class="section-subtitle">维护加急 / 易碎固定增值费，并配置发单、接单和费用核算逻辑。</p>
            </div>
            <el-button type="primary" @click="saveErrandRules">保存全部配置</el-button>
          </div>

          <div class="rules-config-grid">
            <div class="config-card">
              <span class="config-label">加急附加费</span>
              <el-input-number v-model="errandRules.urgentFee" :min="0" :precision="2" :step="0.5" />
              <p class="config-hint">用于加急订单的固定增值费。</p>
            </div>
            <div class="config-card">
              <span class="config-label">易碎附加费</span>
              <el-input-number v-model="errandRules.fragileFee" :min="0" :precision="2" :step="0.5" />
              <p class="config-hint">用于易碎物品代取 / 配送场景。</p>
            </div>
            <div class="config-card">
              <span class="config-label">单人最大发单数</span>
              <el-input-number v-model="errandRules.publishLimitPerUser" :min="1" :step="1" />
              <p class="config-hint">限制同一用户并行中的发单数量。</p>
            </div>
            <div class="config-card">
              <span class="config-label">单人最大接单数</span>
              <el-input-number v-model="errandRules.acceptLimitPerUser" :min="1" :step="1" />
              <p class="config-hint">限制同一跑腿员并行中的接单数量。</p>
            </div>
            <div class="config-card">
              <span class="config-label">订单自动过期时长（小时）</span>
              <el-input-number v-model="errandRules.autoExpireHours" :min="1" :step="1" />
              <p class="config-hint">公共订单未被接单时自动释放并归档。</p>
            </div>
            <div class="config-card">
              <span class="config-label">基础费用区间</span>
              <div class="range-inline">
                <el-input-number v-model="errandRules.minBaseFee" :min="0" :precision="2" :step="0.5" />
                <span>至</span>
                <el-input-number v-model="errandRules.maxBaseFee" :min="0" :precision="2" :step="0.5" />
              </div>
              <p class="config-hint">用于约束发单预算区间，避免恶意低价或异常高价。</p>
            </div>
          </div>
        </div>
      </section>

      <section v-if="tab === 'partner-content'" class="panel">
        <div class="section-head">
          <div>
            <h2>找搭子内容审核</h2>
            <p class="section-subtitle">审核找搭子广场发布内容，处理展示准入、人工驳回与风险下架。</p>
          </div>
          <div class="toolbar">
            <el-input v-model="partnerContentKeyword" placeholder="搜索标题、地点、发布人、内容" clearable class="toolbar-input" />
            <el-select v-model="partnerContentType" placeholder="搭子类型" clearable class="toolbar-select">
              <el-option v-for="item in partnerTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-select v-model="partnerContentStatus" placeholder="审核状态" clearable class="toolbar-select">
              <el-option v-for="item in partnerDemandStatusOptions" :key="item" :label="formatPartnerDemandStatus(item)" :value="item" />
            </el-select>
            <el-select v-model="partnerContentRisk" placeholder="风险标签" clearable class="toolbar-select">
              <el-option v-for="item in partnerContentRiskOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-button type="primary" @click="refreshPartnerAdmin">刷新</el-button>
          </div>
        </div>

        <div class="rules-bar partner-highlight-bar">
          <span>待审核：<strong>{{ partnerStats.pendingDemands }}</strong></span>
          <span>已通过：<strong>{{ partnerStats.approvedDemands }}</strong></span>
          <span>已下架：<strong>{{ partnerStats.offlineDemands }}</strong></span>
          <span>累计报名：<strong>{{ partnerStats.totalApplications }}</strong></span>
        </div>

        <el-table :data="partnerContentRows" stripe @row-click="openPartnerDemandDetail">
          <el-table-column prop="id" label="需求ID" width="90" />
          <el-table-column prop="title" label="需求标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="type" label="搭子类型" width="120" />
          <el-table-column label="审核状态" width="120">
            <template #default="{ row }">
              <el-tag :type="partnerDemandStatusTagType(row.status)">{{ formatPartnerDemandStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="风险标签" min-width="220">
            <template #default="{ row }">
              <div v-if="row.riskTags?.length" class="tag-inline">
                <el-tag v-for="tag in row.riskTags" :key="`${row.id}-${tag}`" size="small" :type="tag.includes('超时') || tag.includes('高风险') ? 'danger' : tag.includes('待处理') || tag.includes('下架') ? 'warning' : 'info'">
                  {{ tag }}
                </el-tag>
              </div>
              <span v-else class="empty-inline compact">暂无风险</span>
            </template>
          </el-table-column>
          <el-table-column prop="publisherName" label="发布人" width="120" />
          <el-table-column prop="location" label="活动地点" min-width="180" show-overflow-tooltip />
          <el-table-column label="报名 / 举报" width="120">
            <template #default="{ row }">{{ row.applyCount }} / {{ row.reportCount }}</template>
          </el-table-column>
          <el-table-column label="操作" width="300" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="handlePartnerDemandAction(row, 'APPROVE')">通过</el-button>
              <el-button type="danger" link @click.stop="handlePartnerDemandAction(row, 'REJECT')">驳回</el-button>
              <el-button type="warning" link @click.stop="handlePartnerDemandAction(row, 'OFFLINE')">下架</el-button>
              <el-button type="success" link @click.stop="handlePartnerDemandAction(row, 'RESTORE')">恢复</el-button>
              <el-button type="info" link @click.stop="openPartnerDemandDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'partner-reports'" class="panel">
        <div class="section-head">
          <div>
            <h2>找搭子举报治理</h2>
            <p class="section-subtitle">处理虚假邀约、恶意骚扰、违规广告和线下交易风险举报，并回看关联会话证据。</p>
          </div>
          <div class="toolbar">
            <el-input v-model="partnerReportKeyword" placeholder="搜索标题、举报说明、举报人、联系方式" clearable class="toolbar-input" />
            <el-select v-model="partnerReportStatus" placeholder="工单状态" clearable class="toolbar-select">
              <el-option v-for="item in reportStatusOptions" :key="item" :label="formatReportStatus(item)" :value="item" />
            </el-select>
            <el-select v-model="partnerReportType" placeholder="举报类型" clearable class="toolbar-select">
              <el-option v-for="item in partnerReportTypeOptionList" :key="item" :label="item" :value="item" />
            </el-select>
            <el-button type="primary" @click="refreshPartnerAdmin">刷新</el-button>
          </div>
        </div>

        <div class="rules-bar partner-highlight-bar">
          <span>总举报数：<strong>{{ partnerStats.totalReports }}</strong></span>
          <span>待受理：<strong>{{ partnerStats.pendingReports }}</strong></span>
          <span>处理中：<strong>{{ partnerStats.processingReports }}</strong></span>
          <span>高风险会话：<strong>{{ partnerStats.highRiskChats }}</strong></span>
        </div>

        <el-table :data="partnerReportRows" stripe @row-click="openPartnerReportDetail">
          <el-table-column prop="id" label="工单ID" width="90" />
          <el-table-column prop="reportType" label="举报类型" width="140" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="reportStatusTagType(row.status)">{{ formatReportStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理时限" min-width="220">
            <template #default="{ row }">
              <div class="deadline-cell">
                <div class="deadline-line">
                  <span class="deadline-label">截止</span>
                  <strong>{{ formatReportDeadlineAt(row) }}</strong>
                </div>
                <div class="deadline-line">
                  <el-tag size="small" :type="reportDeadlineTagType(row)">{{ formatReportDeadlineState(row) }}</el-tag>
                  <span class="deadline-hint">{{ formatReportDeadlineHint(row) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="targetTitle" label="关联需求" min-width="200" show-overflow-tooltip />
          <el-table-column prop="description" label="问题说明" min-width="220" show-overflow-tooltip />
          <el-table-column label="操作" width="300" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="handlePartnerReportAction(row, 'PROCESSING')">受理</el-button>
              <el-button type="success" link @click.stop="handlePartnerReportAction(row, 'RESOLVED')">处理完成</el-button>
              <el-button type="danger" link @click.stop="handlePartnerReportAction(row, 'REJECTED')">驳回</el-button>
              <el-button type="warning" link @click.stop="openPartnerChatFromDemand(row.conversationId || '')" :disabled="!row.conversationId">会话核查</el-button>
              <el-button type="info" link @click.stop="openPartnerReportDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'partner-chat'" class="panel">
        <div class="section-head">
          <div>
            <h2>找搭子聊天监管</h2>
            <p class="section-subtitle">查看搭子沟通会话，巡检高风险消息，支持标记复核、关闭和恢复会话。</p>
          </div>
          <div class="toolbar">
            <el-input v-model="partnerChatKeyword" placeholder="搜索需求标题、对方昵称、最新消息、手机号" clearable class="toolbar-input" />
            <el-select v-model="partnerChatStatus" placeholder="会话状态" clearable class="toolbar-select">
              <el-option v-for="item in partnerConversationStatusOptions" :key="item" :label="formatPartnerConversationStatus(item)" :value="item" />
            </el-select>
            <el-select v-model="partnerChatRisk" placeholder="风险等级" clearable class="toolbar-select">
              <el-option v-for="item in partnerRiskLevelOptions" :key="item" :label="formatPartnerRiskLevel(item)" :value="item" />
            </el-select>
            <el-button type="primary" @click="refreshPartnerAdmin">刷新</el-button>
          </div>
        </div>

        <div class="rules-bar partner-highlight-bar">
          <span>活跃会话：<strong>{{ partnerStats.activeChats }}</strong></span>
          <span>待复核：<strong>{{ partnerStats.reviewChats }}</strong></span>
          <span>高风险：<strong>{{ partnerStats.highRiskChats }}</strong></span>
        </div>

        <el-table :data="partnerChatRows" stripe @row-click="openPartnerChatDetail">
          <el-table-column prop="id" label="会话ID" width="150" />
          <el-table-column prop="demandTitle" label="关联需求" min-width="200" show-overflow-tooltip />
          <el-table-column prop="counterpartyName" label="对方昵称" width="120" />
          <el-table-column label="状态" width="130">
            <template #default="{ row }">
              <el-tag :type="partnerConversationStatusTagType(row.status)">{{ formatPartnerConversationStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="风险等级" width="120">
            <template #default="{ row }">
              <el-tag :type="partnerRiskLevelTagType(row.riskLevel)">{{ formatPartnerRiskLevel(row.riskLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="风险标签" min-width="220">
            <template #default="{ row }">
              <div v-if="row.riskTags?.length" class="tag-inline">
                <el-tag v-for="tag in row.riskTags" :key="`${row.id}-${tag}`" size="small" :type="tag.includes('高风险') || tag.includes('关闭') || tag.includes('超时') ? 'danger' : tag.includes('复核') || tag.includes('处理中') ? 'warning' : 'info'">
                  {{ tag }}
                </el-tag>
              </div>
              <span v-else class="empty-inline compact">暂无风险</span>
            </template>
          </el-table-column>
          <el-table-column prop="lastMessage" label="最新消息" min-width="220" show-overflow-tooltip />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button type="warning" link @click.stop="handlePartnerChatAction(row, 'MARK_REVIEW')">标记复核</el-button>
              <el-button type="danger" link @click.stop="handlePartnerChatAction(row, 'CLOSE')">关闭会话</el-button>
              <el-button type="success" link @click.stop="handlePartnerChatAction(row, 'RESTORE')">恢复会话</el-button>
              <el-button type="info" link @click.stop="openPartnerChatDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'partner-stats'" class="panel">
        <div class="section-head">
          <div>
            <h2>找搭子活跃统计</h2>
            <p class="section-subtitle">查看找搭子模块的发布活跃度、报名转化、举报分布和高风险会话概况。</p>
          </div>
          <el-button type="primary" @click="refreshPartnerAdmin">刷新数据</el-button>
        </div>

        <div class="panel-grid partner-stats-grid">
          <div class="panel stat inset">
            <span>需求总数</span>
            <strong>{{ partnerStats.totalDemands }}</strong>
          </div>
          <div class="panel stat inset">
            <span>累计报名</span>
            <strong>{{ partnerStats.totalApplications }}</strong>
          </div>
          <div class="panel stat inset">
            <span>举报总量</span>
            <strong>{{ partnerStats.totalReports }}</strong>
          </div>
          <div class="panel stat inset">
            <span>高风险会话</span>
            <strong>{{ partnerStats.highRiskChats }}</strong>
          </div>
        </div>

        <div class="rules-bar partner-highlight-bar">
          <span>待审核需求：<strong>{{ partnerStats.pendingDemands }}</strong></span>
          <span>已通过需求：<strong>{{ partnerStats.approvedDemands }}</strong></span>
          <span>待受理举报：<strong>{{ partnerStats.pendingReports }}</strong></span>
          <span>待复核会话：<strong>{{ partnerStats.reviewChats }}</strong></span>
        </div>

        <div class="panel-grid partner-overview-grid">
          <div class="panel errand-summary-panel">
            <div class="mini-head">
              <h3>搭子类型分布</h3>
            </div>
            <div v-if="partnerStats.typeBreakdown.length" class="metrics-stack">
              <div v-for="item in partnerStats.typeBreakdown" :key="item.label" class="metric-row">
                <span>{{ item.label }}</span>
                <strong>{{ item.count }}</strong>
              </div>
            </div>
            <div v-else class="empty-inline">暂无统计数据</div>
          </div>

          <div class="panel errand-summary-panel">
            <div class="mini-head">
              <h3>举报类型分布</h3>
            </div>
            <div v-if="partnerStats.reportTypeBreakdown.length" class="summary-list">
              <div v-for="item in partnerStats.reportTypeBreakdown" :key="item.label" class="summary-item">
                <div>
                  <strong>{{ item.label }}</strong>
                  <span>找搭子举报工单</span>
                </div>
                <em>{{ item.count }} 单</em>
              </div>
            </div>
            <div v-else class="empty-inline">暂无统计数据</div>
          </div>

          <div class="panel errand-summary-panel">
            <div class="mini-head">
              <h3>会话风险分布</h3>
            </div>
            <div v-if="partnerStats.riskBreakdown.length" class="metrics-stack">
              <div v-for="item in partnerStats.riskBreakdown" :key="item.label" class="metric-row">
                <span>{{ formatPartnerRiskLevel(item.label) }}</span>
                <strong>{{ item.count }}</strong>
              </div>
            </div>
            <div v-else class="empty-inline">暂无统计数据</div>
          </div>

          <div class="panel errand-summary-panel">
            <div class="mini-head">
              <h3>重点需求</h3>
            </div>
            <div v-if="partnerStats.hotDemands.length" class="summary-list">
              <div v-for="item in partnerStats.hotDemands" :key="item.id" class="summary-item">
                <div>
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.type }}</span>
                </div>
                <em>{{ item.applyCount }} 报名 / {{ item.reportCount }} 举报</em>
              </div>
            </div>
            <div v-else class="empty-inline">暂无重点需求</div>
          </div>
        </div>
      </section>

      <section v-if="tab === 'beauty-review'" class="panel">
        <div class="section-head">
          <div>
            <h2>美妆内容审核</h2>
            <p class="section-subtitle">审核种草测评、妆容推荐、美妆 OOTD 等内容，拦截虚假、违规和侵权展示。</p>
          </div>
          <div class="toolbar">
            <el-input v-model="beautyReviewKeyword" placeholder="搜索标题、简介、测评、发布人" clearable class="toolbar-input" />
            <el-select v-model="beautyReviewCategory" placeholder="商品品类" clearable class="toolbar-select">
              <el-option v-for="item in beautyCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-select v-model="beautyReviewStatus" placeholder="审核状态" clearable class="toolbar-select">
              <el-option v-for="item in beautyStatusOptions" :key="item" :label="formatBeautyStatus(item)" :value="item" />
            </el-select>
            <el-button type="primary" @click="refreshBeautyAdmin">刷新</el-button>
          </div>
        </div>

        <div class="rules-bar beauty-highlight-bar">
          <span>待审核：<strong>{{ beautyStats.pendingGoods }}</strong></span>
          <span>已通过：<strong>{{ beautyStats.approvedGoods }}</strong></span>
          <span>已驳回：<strong>{{ beautyStats.rejectedGoods }}</strong></span>
          <span>待补充凭证：<strong>{{ beautyProofSummary.missingReceipt + beautyProofSummary.missingProductImage }}</strong></span>
        </div>

        <el-table :data="beautyReviewRows" stripe @row-click="openBeautyDetail">
          <el-table-column prop="id" label="商品ID" width="90" />
          <el-table-column prop="title" label="商品标题" min-width="220" show-overflow-tooltip />
          <el-table-column label="品类" width="120">
            <template #default="{ row }">{{ formatBeautyCategory(row.category) }}</template>
          </el-table-column>
          <el-table-column label="审核状态" width="120">
            <template #default="{ row }">
              <el-tag :type="beautyStatusTagType(row.status)">{{ formatBeautyStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发布来源" width="120">
            <template #default="{ row }">{{ formatBeautySourceType(row.sourceType) }}</template>
          </el-table-column>
          <el-table-column prop="creatorNickname" label="发布人" width="120" />
          <el-table-column label="内容摘要" min-width="280" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.evaluation || row.sceneText || row.summary }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="handleBeautyAction(row.id, 'APPROVE')">通过</el-button>
              <el-button type="danger" link @click.stop="handleBeautyAction(row.id, 'REJECT')">驳回</el-button>
              <el-button type="info" link @click.stop="openBeautyDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'beauty-governance'" class="panel">
        <div class="section-head">
          <div>
            <h2>美妆商品管理</h2>
            <p class="section-subtitle">维护平价美妆与宿舍好物库，执行录入、分类、上下架，并核验订单截图与实拍图。</p>
          </div>
          <div class="toolbar">
            <el-input v-model="beautyKeyword" placeholder="搜索标题、简介、发布人、来源" clearable class="toolbar-input" />
            <el-select v-model="beautyCategory" placeholder="商品品类" clearable class="toolbar-select">
              <el-option v-for="item in beautyCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-select v-model="beautyStatus" placeholder="商品状态" clearable class="toolbar-select">
              <el-option v-for="item in beautyStatusOptions" :key="item" :label="formatBeautyStatus(item)" :value="item" />
            </el-select>
            <el-button type="primary" plain @click="openBeautyCreate">新增商品</el-button>
            <el-button type="primary" @click="refreshBeautyAdmin">刷新</el-button>
          </div>
        </div>

        <div class="rules-bar beauty-proof-bar">
          <span>凭证完整：<strong>{{ beautyProofSummary.fullProof }}</strong></span>
          <span>缺订单截图：<strong>{{ beautyProofSummary.missingReceipt }}</strong></span>
          <span>缺实拍图：<strong>{{ beautyProofSummary.missingProductImage }}</strong></span>
          <span>售后申请：<strong>{{ beautyAppealSummary.total }}</strong></span>
        </div>

        <el-table :data="beautyGovernanceRows" stripe @row-click="openBeautyDetail">
          <el-table-column prop="id" label="商品ID" width="90" />
          <el-table-column prop="title" label="商品标题" min-width="220" show-overflow-tooltip />
          <el-table-column label="品类" width="120">
            <template #default="{ row }">{{ formatBeautyCategory(row.category) }}</template>
          </el-table-column>
          <el-table-column label="价格" width="100">
            <template #default="{ row }">￥{{ formatMoney(row.price) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="beautyStatusTagType(row.status)">{{ formatBeautyStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="来源" width="120">
            <template #default="{ row }">{{ formatBeautySourceType(row.sourceType) }}</template>
          </el-table-column>
          <el-table-column label="浏览 / 收藏" width="140">
            <template #default="{ row }">{{ row.viewCount }} / {{ row.favoriteCount }}</template>
          </el-table-column>
          <el-table-column label="风控标签" min-width="240">
            <template #default="{ row }">
              <div v-if="getBeautyRiskTags(row).length" class="tag-inline">
                <el-tag
                  v-for="tag in getBeautyRiskTags(row)"
                  :key="`${row.id}-${tag}`"
                  size="small"
                  :type="tag.includes('缺少') || tag.includes('驳回') ? 'danger' : tag.includes('待') || tag.includes('观察') ? 'warning' : 'info'"
                >
                  {{ tag }}
                </el-tag>
              </div>
              <span v-else class="empty-inline compact">凭证完整</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="320" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 'PENDING'" type="primary" link @click.stop="handleBeautyAction(row.id, 'APPROVE')">通过</el-button>
              <el-button v-if="row.status === 'PENDING'" type="danger" link @click.stop="handleBeautyAction(row.id, 'REJECT')">驳回</el-button>
              <el-button v-if="row.status === 'APPROVED'" type="warning" link @click.stop="handleBeautyAction(row.id, 'OFFLINE')">下架</el-button>
              <el-button v-if="row.status === 'OFFLINE' || row.status === 'REJECTED'" type="success" link @click.stop="handleBeautyAction(row.id, 'RESTORE')">恢复</el-button>
              <el-button type="primary" link @click.stop="openBeautyEdit(row)">编辑</el-button>
              <el-button type="info" link @click.stop="openBeautyDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'beauty-reports'" class="panel">
        <div class="section-head">
          <div>
            <h2>美妆售后仲裁</h2>
            <p class="section-subtitle">处理假货投诉、过敏、破损、描述不符等售后申请，平台介入核查并输出仲裁结果。</p>
          </div>
          <div class="toolbar">
            <el-input v-model="beautyAppealKeyword" placeholder="搜索商品、申诉原因、买卖双方、联系方式" clearable class="toolbar-input" />
            <el-select v-model="beautyAppealStatus" placeholder="工单状态" clearable class="toolbar-select">
              <el-option
                v-for="item in ['PENDING', 'PROCESSING', 'RESOLVED', 'REJECTED']"
                :key="item"
                :label="formatBeautyAppealStatus(item)"
                :value="item"
              />
            </el-select>
            <el-select v-model="beautyAppealType" placeholder="售后类型" clearable class="toolbar-select">
              <el-option v-for="item in beautyAppealTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-button type="primary" @click="refreshBeautyAdmin">刷新</el-button>
          </div>
        </div>

        <div class="rules-bar beauty-highlight-bar">
          <span>售后总数：<strong>{{ beautyAppealSummary.total }}</strong></span>
          <span>待受理：<strong>{{ beautyAppealSummary.pending }}</strong></span>
          <span>处理中：<strong>{{ beautyAppealSummary.processing }}</strong></span>
          <span>已裁定：<strong>{{ beautyAppealSummary.resolved }}</strong></span>
        </div>

        <el-table :data="beautyAppealRows" stripe @row-click="openBeautyAppealDetail">
          <el-table-column prop="id" label="工单ID" width="90" />
          <el-table-column prop="issueType" label="售后类型" width="120" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="beautyAppealStatusTagType(row.status)">{{ formatBeautyAppealStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="goodTitle" label="关联商品" min-width="220" show-overflow-tooltip />
          <el-table-column label="退款金额" width="110">
            <template #default="{ row }">￥{{ formatMoney(row.refundAmount) }}</template>
          </el-table-column>
          <el-table-column prop="reason" label="问题说明" min-width="260" show-overflow-tooltip />
          <el-table-column prop="proofSummary" label="凭证摘要" min-width="220" show-overflow-tooltip />
          <el-table-column label="操作" width="320" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="handleBeautyAppealAction(row, 'PROCESSING')">受理</el-button>
              <el-button type="success" link @click.stop="handleBeautyAppealAction(row, 'RESOLVED')">仲裁通过</el-button>
              <el-button type="danger" link @click.stop="handleBeautyAppealAction(row, 'REJECTED')">驳回</el-button>
              <el-button type="warning" link @click.stop="openBeautyDetail({ id: row.goodId })">商品详情</el-button>
              <el-button type="info" link @click.stop="openBeautyAppealDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'beauty-stats'" class="panel">
        <div class="section-head">
          <div>
            <h2>美妆数据统计</h2>
            <p class="section-subtitle">查看商品池规模、浏览收藏热度、模拟销量排行和售后类型分布，支撑日常运营决策。</p>
          </div>
          <el-button type="primary" @click="refreshBeautyAdmin">刷新数据</el-button>
        </div>

        <div class="panel-grid beauty-admin-kpi-grid">
          <div class="panel stat inset">
            <span>商品总数</span>
            <strong>{{ beautyStats.totalGoods }}</strong>
          </div>
          <div class="panel stat inset">
            <span>总浏览量</span>
            <strong>{{ beautyStats.totalViews }}</strong>
          </div>
          <div class="panel stat inset">
            <span>总收藏量</span>
            <strong>{{ beautyStats.totalFavorites }}</strong>
          </div>
          <div class="panel stat inset">
            <span>售后申请数</span>
            <strong>{{ beautyAppealSummary.total }}</strong>
          </div>
        </div>

        <div class="rules-bar beauty-highlight-bar beauty-stats-bar">
          <span>待审核：<strong>{{ beautyStats.pendingGoods }}</strong></span>
          <span>已通过：<strong>{{ beautyStats.approvedGoods }}</strong></span>
          <span>已下架：<strong>{{ beautyStats.offlineGoods }}</strong></span>
          <span>处理中售后：<strong>{{ beautyAppealSummary.processing }}</strong></span>
        </div>

        <div class="panel-grid beauty-admin-overview">
          <div class="panel beauty-summary-panel">
            <div class="mini-head">
              <h3>分类分布</h3>
            </div>
            <div v-if="beautyStats.categoryBreakdown?.length" class="metrics-stack">
              <div v-for="item in beautyStats.categoryBreakdown" :key="item.category" class="metric-row">
                <span>{{ formatBeautyCategory(item.category) }}</span>
                <strong>{{ item.count }}</strong>
              </div>
            </div>
            <div v-else class="empty-inline">暂无统计数据</div>
          </div>

          <div class="panel beauty-summary-panel">
            <div class="mini-head">
              <h3>热销模拟排行</h3>
            </div>
            <div v-if="beautySalesRanking.length" class="summary-list">
              <div v-for="item in beautySalesRanking" :key="`sales-${item.id}`" class="summary-item">
                <div>
                  <strong>{{ item.title }}</strong>
                  <span>{{ formatBeautyCategory(item.category) }} / {{ item.viewCount }} 浏览 / {{ item.favoriteCount }} 收藏</span>
                </div>
                <em>{{ item.sales }} 热度值</em>
              </div>
            </div>
            <div v-else class="empty-inline">暂无排行数据</div>
          </div>

          <div class="panel beauty-summary-panel">
            <div class="mini-head">
              <h3>热门浏览商品</h3>
            </div>
            <div v-if="beautyStats.topViewedGoods?.length" class="summary-list">
              <div v-for="item in beautyStats.topViewedGoods" :key="`beauty-view-${item.id}`" class="summary-item">
                <div>
                  <strong>{{ item.title }}</strong>
                  <span>{{ formatBeautyCategory(item.category) }} / {{ formatBeautyStatus(item.status) }}</span>
                </div>
                <em>{{ item.viewCount }} 浏览</em>
              </div>
            </div>
            <div v-else class="empty-inline">暂无统计数据</div>
          </div>

          <div class="panel beauty-summary-panel">
            <div class="mini-head">
              <h3>热门收藏商品</h3>
            </div>
            <div v-if="beautyStats.topFavoritedGoods?.length" class="summary-list">
              <div v-for="item in beautyStats.topFavoritedGoods" :key="`beauty-favorite-${item.id}`" class="summary-item">
                <div>
                  <strong>{{ item.title }}</strong>
                  <span>{{ formatBeautyCategory(item.category) }} / {{ formatBeautyStatus(item.status) }}</span>
                </div>
                <em>{{ item.favoriteCount }} 收藏</em>
              </div>
            </div>
            <div v-else class="empty-inline">暂无统计数据</div>
          </div>

          <div class="panel beauty-summary-panel">
            <div class="mini-head">
              <h3>售后类型分布</h3>
            </div>
            <div v-if="beautyAppealBreakdown.length" class="metrics-stack">
              <div v-for="item in beautyAppealBreakdown" :key="item.label" class="metric-row">
                <span>{{ item.label }}</span>
                <strong>{{ item.count }}</strong>
              </div>
            </div>
            <div v-else class="empty-inline">暂无售后数据</div>
          </div>
        </div>
      </section>

      <section v-if="tab === 'jobs-merchant'" class="panel">
        <div class="section-head">
          <div>
            <h2>用工资质审核</h2>
            <p class="section-subtitle">仅针对校外用工方做营业执照人工审核，学生发布技能互助不进入资质审核流程。</p>
          </div>
          <div class="toolbar">
            <el-input v-model="jobsMerchantKeyword" placeholder="搜索申请人、商家名、手机号、区域" clearable class="toolbar-input" />
            <el-select v-model="jobsMerchantStatus" placeholder="审核状态" clearable class="toolbar-select">
              <el-option v-for="item in jobsMerchantStatusOptions" :key="item" :label="formatJobsMerchantStatus(item)" :value="item" />
            </el-select>
            <el-button type="primary" @click="refreshJobsAdmin">刷新</el-button>
          </div>
        </div>

        <div class="rules-bar jobs-highlight-bar">
          <span>待审核：<strong>{{ jobsMerchants.filter((item) => item.status === 'PENDING').length }}</strong></span>
          <span>已通过：<strong>{{ jobsMerchants.filter((item) => item.status === 'APPROVED').length }}</strong></span>
          <span>已驳回：<strong>{{ jobsMerchants.filter((item) => item.status === 'REJECTED').length }}</strong></span>
        </div>

        <el-table :data="jobsMerchantRows" stripe @row-click="openJobsMerchantDetail">
          <el-table-column prop="id" label="申请ID" width="90" />
          <el-table-column prop="applicantName" label="申请人" width="120" />
          <el-table-column prop="businessName" label="商家名称" min-width="220" show-overflow-tooltip />
          <el-table-column prop="identityType" label="主体类型" width="130" />
          <el-table-column prop="area" label="所在区域" width="120" />
          <el-table-column prop="phone" label="联系电话" width="140" />
          <el-table-column label="审核状态" width="120">
            <template #default="{ row }">
              <el-tag :type="jobsMerchantStatusTagType(row.status)">{{ formatJobsMerchantStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="handleJobsMerchantReview(row, 'APPROVED')">通过</el-button>
              <el-button type="danger" link @click.stop="handleJobsMerchantReview(row, 'REJECTED')">驳回</el-button>
              <el-button type="info" link @click.stop="openJobsMerchantDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'jobs-posts'" class="panel">
        <div class="section-head">
          <div>
            <h2>岗位发布管控</h2>
            <p class="section-subtitle">预设校内 / 1 / 2 / 3 公里固定区间，拦截超范围岗位，并审核虚假、中介、高危、违规扣费岗位。</p>
          </div>
          <div class="toolbar">
            <el-input v-model="jobsPostKeyword" placeholder="搜索标题、分类、发布人、区域、描述" clearable class="toolbar-input" />
            <el-select v-model="jobsPostRole" placeholder="发布身份" clearable class="toolbar-select">
              <el-option v-for="item in jobsRoleOptions" :key="item" :label="formatJobsRole(item)" :value="item" />
            </el-select>
            <el-select v-model="jobsPostStatus" placeholder="岗位状态" clearable class="toolbar-select">
              <el-option v-for="item in jobsPostStatusOptions" :key="item" :label="formatJobsPostStatus(item)" :value="item" />
            </el-select>
            <el-select v-model="jobsPostDistance" placeholder="距离区间" clearable class="toolbar-select">
              <el-option v-for="item in jobsDistanceOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-button type="primary" @click="refreshJobsAdmin">刷新</el-button>
          </div>
        </div>

        <div class="rules-bar jobs-highlight-bar">
          <span>待审核：<strong>{{ jobsPostRiskSummary.pendingReview }}</strong></span>
          <span>超范围：<strong>{{ jobsPostRiskSummary.outOfRange }}</strong></span>
          <span>中介风险：<strong>{{ jobsPostRiskSummary.agencyRisk }}</strong></span>
          <span>扣费风险：<strong>{{ jobsPostRiskSummary.deductionRisk }}</strong></span>
        </div>

        <el-table :data="jobsPostRows" stripe @row-click="openJobsPostDetail">
          <el-table-column prop="id" label="岗位ID" width="90" />
          <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column label="身份" width="110">
            <template #default="{ row }">{{ formatJobsRole(row.role) }}</template>
          </el-table-column>
          <el-table-column prop="category" label="岗位类型" width="120" />
          <el-table-column label="形式" width="90">
            <template #default="{ row }">{{ formatJobsWorkMode(row.workMode) }}</template>
          </el-table-column>
          <el-table-column label="范围" width="120">
            <template #default="{ row }">{{ row.distanceRange }} / {{ row.actualDistanceKm }}km</template>
          </el-table-column>
          <el-table-column label="薪资" width="110">
            <template #default="{ row }">￥{{ formatMoney(row.salary) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="jobsPostStatusTagType(row.status)">{{ formatJobsPostStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="风险标签" min-width="240">
            <template #default="{ row }">
              <div v-if="row.riskTags.length" class="tag-inline">
                <el-tag v-for="tag in row.riskTags" :key="`${row.id}-${tag}`" size="small" :type="tag.includes('超范围') || tag.includes('扣费') ? 'danger' : tag.includes('高风险') || tag.includes('中介') || tag.includes('举报') ? 'warning' : 'info'">
                  {{ tag }}
                </el-tag>
              </div>
              <span v-else class="empty-inline compact">暂无风险</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="320" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="handleJobsPostAction(row, 'APPROVE')">通过</el-button>
              <el-button type="danger" link @click.stop="handleJobsPostAction(row, 'REJECT')">驳回</el-button>
              <el-button type="warning" link @click.stop="handleJobsPostAction(row, 'OFFLINE')">下架</el-button>
              <el-button type="danger" link @click.stop="handleJobsPostAction(row, 'BLOCK')">拦截</el-button>
              <el-button type="success" link @click.stop="handleJobsPostAction(row, 'RESTORE')">恢复</el-button>
              <el-button type="info" link @click.stop="openJobsPostDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'jobs-accounts'" class="panel">
        <div class="section-head">
          <div>
            <h2>用户 / 用工方管理</h2>
            <p class="section-subtitle">统一管理学生与用工方账号，手动控制发岗、报名、接单权限，并可限制或封禁违规主体。</p>
          </div>
          <div class="toolbar">
            <el-input v-model="jobsAccountKeyword" placeholder="搜索姓名、商家名、手机号" clearable class="toolbar-input" />
            <el-select v-model="jobsAccountRole" placeholder="账号身份" clearable class="toolbar-select">
              <el-option v-for="item in jobsRoleOptions" :key="item" :label="formatJobsRole(item)" :value="item" />
            </el-select>
            <el-select v-model="jobsAccountStatus" placeholder="账号状态" clearable class="toolbar-select">
              <el-option v-for="item in jobsAccountStatusOptions" :key="item" :label="formatJobsAccountStatus(item)" :value="item" />
            </el-select>
            <el-button type="primary" @click="refreshJobsAdmin">刷新</el-button>
          </div>
        </div>

        <el-table :data="jobsAccountRows" stripe @row-click="openJobsAccountDetail">
          <el-table-column prop="id" label="账号ID" width="90" />
          <el-table-column prop="name" label="名称" min-width="180" show-overflow-tooltip />
          <el-table-column label="身份" width="100">
            <template #default="{ row }">{{ formatJobsRole(row.role) }}</template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column label="账号状态" width="120">
            <template #default="{ row }">
              <el-tag :type="jobsAccountStatusTagType(row.status)">{{ formatJobsAccountStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="权限" min-width="220">
            <template #default="{ row }">
              发岗 {{ row.publishEnabled ? '开' : '关' }} / 报名 {{ row.applyEnabled ? '开' : '关' }} / 接单 {{ row.acceptEnabled ? '开' : '关' }}
            </template>
          </el-table-column>
          <el-table-column label="举报 / 纠纷" width="120">
            <template #default="{ row }">{{ row.reportCount }} / {{ row.disputeCount }}</template>
          </el-table-column>
          <el-table-column label="信用分" width="100">
            <template #default="{ row }">
              <span :class="['credit-value', row.creditScore < 60 ? 'danger' : row.creditScore < 80 ? 'warning' : 'success']">{{ row.creditScore }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="420" fixed="right">
            <template #default="{ row }">
              <el-button type="warning" link @click.stop="handleJobsAccountAction(row, 'LIMIT')">限制</el-button>
              <el-button type="danger" link @click.stop="handleJobsAccountAction(row, 'BAN')">封禁</el-button>
              <el-button type="success" link @click.stop="handleJobsAccountAction(row, 'RESTORE')">恢复</el-button>
              <el-button type="primary" link @click.stop="handleJobsAccountAction(row, 'TOGGLE_PUBLISH')">{{ row.publishEnabled ? '关发岗' : '开发岗' }}</el-button>
              <el-button type="primary" link @click.stop="handleJobsAccountAction(row, 'TOGGLE_APPLY')">{{ row.applyEnabled ? '关报名' : '开报名' }}</el-button>
              <el-button type="primary" link @click.stop="handleJobsAccountAction(row, 'TOGGLE_ACCEPT')">{{ row.acceptEnabled ? '关接单' : '开接单' }}</el-button>
              <el-button type="info" link @click.stop="openJobsAccountDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'jobs-rights'" class="panel">
        <div class="section-head">
          <div>
            <h2>举报工单管理</h2>
            <p class="section-subtitle">统一受理信息不符、薪资拖欠、强制加班、不合理安排、恶意刁难等工单，状态保留待处理、已处理、已完结。</p>
          </div>
          <div class="toolbar">
            <el-input v-model="jobsReportKeyword" placeholder="搜索岗位、举报人、被举报方、工单内容" clearable class="toolbar-input" />
            <el-select v-model="jobsReportStatus" placeholder="工单状态" clearable class="toolbar-select">
              <el-option v-for="item in jobsReportStatusOptions" :key="item" :label="formatJobsReportStatus(item)" :value="item" />
            </el-select>
            <el-select v-model="jobsReportType" placeholder="举报类型" clearable class="toolbar-select">
              <el-option v-for="item in jobsReportTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-button type="primary" @click="refreshJobsAdmin">刷新</el-button>
          </div>
        </div>

        <div class="rules-bar jobs-highlight-bar">
          <span>待处理：<strong>{{ jobsReports.filter((item) => item.status === 'PENDING').length }}</strong></span>
          <span>已处理：<strong>{{ jobsReports.filter((item) => item.status === 'RESOLVED').length }}</strong></span>
          <span>已完结：<strong>{{ jobsReports.filter((item) => item.status === 'CLOSED').length }}</strong></span>
          <span>薪资拖欠：<strong>{{ jobsStats.salaryDisputes }}</strong></span>
        </div>

        <el-table :data="jobsReportRows" stripe @row-click="openJobsReportDetail">
          <el-table-column prop="id" label="工单ID" width="90" />
          <el-table-column prop="reportType" label="举报类型" width="120" />
          <el-table-column label="工单状态" width="120">
            <template #default="{ row }">
              <el-tag :type="jobsReportStatusTagType(row.status)">{{ formatJobsReportStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="postTitle" label="关联岗位" min-width="220" show-overflow-tooltip />
          <el-table-column label="被举报方" width="160">
            <template #default="{ row }">{{ row.targetName }} / {{ formatJobsRole(row.targetRole) }}</template>
          </el-table-column>
          <el-table-column prop="description" label="问题说明" min-width="260" show-overflow-tooltip />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="handleJobsReportAction(row, 'RESOLVED')">已处理</el-button>
              <el-button type="success" link @click.stop="handleJobsReportAction(row, 'CLOSED')">已完结</el-button>
              <el-button type="warning" link @click.stop="openJobsReportDetail(row); openJobsPostFromReport()">岗位详情</el-button>
              <el-button type="info" link @click.stop="openJobsReportDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'jobs-credit'" class="panel">
        <div class="section-head">
          <div>
            <h2>双向信用体系</h2>
            <p class="section-subtitle">取消自动扣分与封禁，全部改为管理员手动调整信用分，并据此限制高纠纷主体和恶意举报、旷工学生。</p>
          </div>
          <el-button type="primary" @click="refreshJobsAdmin">刷新名单</el-button>
        </div>

        <div class="panel-grid beauty-admin-overview">
          <div class="panel beauty-summary-panel">
            <div class="mini-head">
              <h3>重点观察名单</h3>
            </div>
            <div v-if="jobsCreditWatchlist.length" class="summary-list">
              <div v-for="item in jobsCreditWatchlist" :key="item.id" class="summary-item">
                <div>
                  <strong>{{ item.name }}</strong>
                  <span>{{ formatJobsRole(item.role) }} / 纠纷 {{ item.disputeCount }} / 举报 {{ item.reportCount }}</span>
                </div>
                <em>{{ item.creditScore }} 分</em>
              </div>
            </div>
            <div v-else class="empty-inline">暂无重点观察对象</div>
          </div>

          <div class="panel beauty-summary-panel">
            <div class="mini-head">
              <h3>手动调分入口</h3>
            </div>
            <div v-if="jobsAccountRows.length" class="summary-list">
              <div v-for="item in jobsAccountRows.slice(0, 5)" :key="`credit-${item.id}`" class="summary-item">
                <div>
                  <strong>{{ item.name }}</strong>
                  <span>{{ formatJobsRole(item.role) }} / 当前状态 {{ formatJobsAccountStatus(item.status) }}</span>
                </div>
                <div class="drawer-actions">
                  <em>{{ item.creditScore }} 分</em>
                  <el-button type="primary" link @click="handleJobsCreditAdjust(item)">调整信用</el-button>
                </div>
              </div>
            </div>
            <div v-else class="empty-inline">暂无账号数据</div>
          </div>
        </div>
      </section>

      <section v-if="tab === 'jobs-skills'" class="panel">
        <div class="section-head">
          <div>
            <h2>技能标签库</h2>
            <p class="section-subtitle">维护文案、PPT、家教、新媒体、办公软件等技能标签，为学生技能互助和岗位筛选提供基础数据。</p>
          </div>
          <div class="toolbar">
            <el-input v-model="jobsSkillKeyword" placeholder="搜索技能标签或分类" clearable class="toolbar-input" />
            <el-select v-model="jobsSkillCategory" placeholder="标签分类" clearable class="toolbar-select">
              <el-option v-for="item in jobsSkillCategories" :key="item" :label="item" :value="item" />
            </el-select>
            <el-button type="primary" plain @click="createJobsSkill">新增标签</el-button>
          </div>
        </div>

        <el-table :data="jobsSkillRows" stripe>
          <el-table-column prop="id" label="标签ID" width="90" />
          <el-table-column prop="label" label="标签名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="140" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用中' : '已停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="usageCount" label="使用次数" width="110" />
          <el-table-column label="更新时间" width="180">
            <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleJobsSkillRename(row)">重命名</el-button>
              <el-button :type="row.enabled ? 'warning' : 'success'" link @click="handleJobsSkillToggle(row)">{{ row.enabled ? '停用' : '启用' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'jobs-stats'" class="panel">
        <div class="section-head">
          <div>
            <h2>勤工助学数据统计</h2>
            <p class="section-subtitle">查看岗位数量、求职人数、热门岗位、区域分布、薪资纠纷以及技能标签热度，辅助课程项目演示。</p>
          </div>
          <el-button type="primary" @click="refreshJobsAdmin">刷新数据</el-button>
        </div>

        <div class="panel-grid beauty-admin-kpi-grid">
          <div class="panel stat inset">
            <span>岗位数量</span>
            <strong>{{ jobsStats.totalPosts }}</strong>
          </div>
          <div class="panel stat inset">
            <span>求职人数</span>
            <strong>{{ jobsStats.totalApplicants }}</strong>
          </div>
          <div class="panel stat inset">
            <span>活跃账号</span>
            <strong>{{ jobsStats.activeUsers }}</strong>
          </div>
          <div class="panel stat inset">
            <span>薪资纠纷</span>
            <strong>{{ jobsStats.salaryDisputes }}</strong>
          </div>
        </div>

        <div class="rules-bar jobs-highlight-bar">
          <span>学生需求：<strong>{{ jobsStats.studentPosts }}</strong></span>
          <span>商家岗位：<strong>{{ jobsStats.employerPosts }}</strong></span>
          <span>待审核岗位：<strong>{{ jobsPostRiskSummary.pendingReview }}</strong></span>
          <span>超范围拦截：<strong>{{ jobsPostRiskSummary.outOfRange }}</strong></span>
        </div>

        <div class="panel-grid beauty-admin-overview">
          <div class="panel beauty-summary-panel">
            <div class="mini-head">
              <h3>热门岗位</h3>
            </div>
            <div v-if="jobsStats.hotPosts.length" class="summary-list">
              <div v-for="item in jobsStats.hotPosts" :key="item.id" class="summary-item">
                <div>
                  <strong>{{ item.title }}</strong>
                  <span>{{ formatJobsRole(item.role) }}</span>
                </div>
                <em>{{ item.count }} 人报名</em>
              </div>
            </div>
            <div v-else class="empty-inline">暂无统计数据</div>
          </div>

          <div class="panel beauty-summary-panel">
            <div class="mini-head">
              <h3>区域分布</h3>
            </div>
            <div v-if="jobsStats.areaBreakdown.length" class="metrics-stack">
              <div v-for="item in jobsStats.areaBreakdown" :key="item.label" class="metric-row">
                <span>{{ item.label }}</span>
                <strong>{{ item.count }}</strong>
              </div>
            </div>
            <div v-else class="empty-inline">暂无统计数据</div>
          </div>

          <div class="panel beauty-summary-panel">
            <div class="mini-head">
              <h3>举报类型分布</h3>
            </div>
            <div v-if="jobsStats.reportBreakdown.length" class="metrics-stack">
              <div v-for="item in jobsStats.reportBreakdown" :key="item.label" class="metric-row">
                <span>{{ item.label }}</span>
                <strong>{{ item.count }}</strong>
              </div>
            </div>
            <div v-else class="empty-inline">暂无举报统计</div>
          </div>

          <div class="panel beauty-summary-panel">
            <div class="mini-head">
              <h3>技能标签分布</h3>
            </div>
            <div v-if="jobsStats.skillBreakdown.length" class="metrics-stack">
              <div v-for="item in jobsStats.skillBreakdown" :key="item.label" class="metric-row">
                <span>{{ item.label }}</span>
                <strong>{{ item.count }}</strong>
              </div>
            </div>
            <div v-else class="empty-inline">暂无标签统计</div>
          </div>
        </div>
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
            v-for="item in allUsers"
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

    <el-drawer v-model="userDrawerVisible" title="用户详情" size="560px">
      <template v-if="userDetail">
        <div class="drawer-actions detail-toolbar">
          <el-button type="warning" @click="handleUserStatus(userDetail.userId, 'WARNED')">警告</el-button>
          <el-button type="danger" @click="handleUserStatus(userDetail.userId, 'ERRAND_RESTRICTED')">限制跑腿</el-button>
          <el-button v-if="!userDetail.reportRestricted" type="danger" plain @click="handleUserStatus(userDetail.userId, 'REPORT_RESTRICTED')">限制举报</el-button>
          <el-button v-else type="success" plain @click="handleUserStatus(userDetail.userId, 'REPORT_ACTIVE')">解除举报限制</el-button>
          <el-button type="danger" @click="handleUserStatus(userDetail.userId, 'TEMP_BANNED')">短期封禁</el-button>
          <el-button type="danger" plain @click="handleUserStatus(userDetail.userId, 'PERMANENT_BANNED')">永久封禁</el-button>
          <el-button type="info" plain @click="handleUserStatus(userDetail.userId, 'BLACKLISTED')">拉黑</el-button>
          <el-button v-if="userDetail.status !== 'ACTIVE'" type="success" @click="handleUserStatus(userDetail.userId, 'ACTIVE')">恢复正常</el-button>
        </div>
        <div class="detail-grid">
          <div class="detail-item"><span>手机号</span><strong>{{ userDetail.phone }}</strong></div>
          <div class="detail-item"><span>昵称</span><strong>{{ userDetail.nickname }}</strong></div>
          <div class="detail-item"><span>角色</span><strong>{{ userDetail.role }}</strong></div>
          <div class="detail-item"><span>状态</span><strong>{{ formatUserStatus(userDetail.status) }}</strong></div>
          <div class="detail-item"><span>举报权限</span><strong>{{ userDetail.reportRestricted ? '已限制' : '正常' }}</strong></div>
          <div class="detail-item"><span>实名状态</span><strong>{{ userDetail.realNameStatus }}</strong></div>
          <div class="detail-item"><span>性别</span><strong>{{ userDetail.gender || '-' }}</strong></div>
          <div class="detail-item full"><span>联系方式</span><strong>{{ userDetail.contactPhone || '-' }}</strong></div>
          <div class="detail-item full"><span>个人简介</span><strong>{{ userDetail.bio || '-' }}</strong></div>
          <div class="detail-item"><span>举报次数</span><strong>{{ userDetail.reportCount }}</strong></div>
          <div class="detail-item"><span>处理中举报</span><strong>{{ userDetail.processingReportCount }}</strong></div>
          <div class="detail-item"><span>被驳回举报</span><strong>{{ userDetail.rejectedReportCount }}</strong></div>
          <div class="detail-item"><span>跑腿发单数</span><strong>{{ userDetail.publishedErrandCount }}</strong></div>
          <div class="detail-item"><span>跑腿接单数</span><strong>{{ userDetail.acceptedErrandCount }}</strong></div>
          <div class="detail-item"><span>已完成订单</span><strong>{{ userDetail.completedErrandCount }}</strong></div>
          <div class="detail-item full"><span>最近处罚说明</span><strong>{{ userDetail.latestPunishmentRemark || '-' }}</strong></div>
          <div class="detail-item full"><span>最近账号治理</span><strong>{{ userDetail.latestAccountGovernanceRemark || '-' }}</strong></div>
          <div class="detail-item full"><span>最近举报权限变更</span><strong>{{ userDetail.latestReportGovernanceRemark || '-' }}</strong></div>
          <div class="detail-item"><span>注册时间</span><strong>{{ formatDate(userDetail.createdAt) }}</strong></div>
        </div>
        <div class="related-panel">
          <div class="related-title">治理轨迹</div>
          <div v-if="userDetail.governanceRecords?.length" class="governance-timeline">
            <div v-for="(item, index) in userDetail.governanceRecords" :key="`${item.category}-${item.actionLabel}-${index}`" class="governance-item">
              <div class="governance-item-head">
                <strong>{{ item.category }}</strong>
                <span>{{ item.actionLabel }}</span>
              </div>
              <p>{{ item.remark || '未填写处理说明' }}</p>
              <em>{{ formatDate(item.createdAt) }}</em>
            </div>
          </div>
          <div v-else class="audit-placeholder">暂无治理记录</div>
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
            <el-button type="primary" @click="handleReport(reportDetail, 'PROCESSING')">处理中</el-button>
            <el-button type="success" @click="handleReport(reportDetail, 'RESOLVED')">已处理</el-button>
            <el-button type="danger" @click="handleReport(reportDetail, 'REJECTED')">驳回</el-button>
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
        <div v-if="isErrandReport(reportDetail)" class="deadline-panel">
          <div class="audit-head">
            <div>
              <h3>处理时限</h3>
              <p>用于查看该举报工单的处理截止时间、当前是否超时，以及剩余处理窗口。</p>
            </div>
          </div>
          <div class="audit-meta">
            <div class="audit-meta-item">
              <span>处理截止时间</span>
              <strong>{{ formatReportDeadlineAt(reportDetail) }}</strong>
            </div>
            <div class="audit-meta-item">
              <span>当前状态</span>
              <div class="deadline-status-wrap">
                <el-tag :type="reportDeadlineTagType(reportDetail)">{{ formatReportDeadlineState(reportDetail) }}</el-tag>
              </div>
            </div>
            <div class="audit-meta-item full-width">
              <span>剩余时间 / 超时提示</span>
              <strong>{{ formatReportDeadlineHint(reportDetail) }}</strong>
            </div>
          </div>
        </div>
        <div v-if="reportDetail.attachments?.length" class="preview-section">
          <div v-for="item in reportDetail.attachments" :key="item.fileId" class="preview-card">
            <div class="preview-title">{{ item.originalName }}</div>
            <img :src="item.url" :alt="item.originalName" />
          </div>
        </div>
        <div v-if="isErrandReport(reportDetail)" class="audit-section">
          <div class="audit-head">
            <div>
              <h3>聊天核查区</h3>
              <p>用于后续核查恶意沟通、线下违规交易、骚扰消息等会话证据。</p>
            </div>
            <el-button type="primary" plain :disabled="!reportDetail.targetId" @click="openErrandFromReport">
              查看关联订单会话
            </el-button>
          </div>
          <div class="audit-meta">
            <div class="audit-meta-item">
              <span>关联订单</span>
              <strong>#{{ reportDetail.targetId || '-' }}</strong>
            </div>
            <div class="audit-meta-item">
              <span>会话状态</span>
              <strong>{{ errandConversationLoading ? '加载中' : errandConversationReview?.conversationId ? `会话 #${errandConversationReview.conversationId}` : '暂无可核查会话' }}</strong>
            </div>
          </div>
          <div v-if="errandConversationError" class="audit-placeholder">{{ errandConversationError }}</div>
          <template v-else-if="errandConversationReview">
            <div v-if="errandConversationReview.members?.length" class="audit-members">
              <div v-for="member in errandConversationReview.members" :key="member.userId" class="audit-member-card">
                <strong>{{ member.nickname || `用户#${member.userId}` }}</strong>
                <span>{{ member.phone || '-' }} / {{ formatUserRole(member.role) }}</span>
                <em>{{ formatUserStatus(member.status) }}</em>
              </div>
            </div>
            <div v-if="errandConversationReview.messages?.length" class="audit-timeline">
              <div v-for="message in errandConversationReview.messages" :key="message.id" class="audit-message">
                <div class="audit-message-meta">
                  <strong>{{ message.senderNickname || `用户#${message.senderId || '-'}` }}</strong>
                  <span>{{ formatUserRole(message.senderRole) }} · {{ formatMessageType(message.type) }} · {{ formatDate(message.createdAt) }}</span>
                </div>
                <img v-if="isImageMessage(message)" :src="message.content" :alt="message.senderNickname || '聊天图片'" class="audit-message-image" />
                <div v-else class="audit-message-content">{{ message.content || '-' }}</div>
              </div>
            </div>
            <div v-else class="audit-placeholder">当前会话暂无消息记录。</div>
          </template>
          <div v-else class="audit-placeholder">
            当前举报尚未关联可核查会话，可能订单还未生成沟通记录。
          </div>
        </div>
      </template>
    </el-drawer>

    <el-dialog
      v-model="reportHandleDialogVisible"
      :title="reportHandleForm.status === 'REJECTED' ? '驳回举报工单' : '处理举报工单'"
      width="560px"
      destroy-on-close
    >
      <el-form label-position="top" class="report-handle-form">
        <el-form-item label="处理状态">
          <el-tag :type="reportStatusTagType(reportHandleForm.status)">{{ formatReportStatus(reportHandleForm.status) }}</el-tag>
        </el-form-item>

        <el-form-item :label="reportHandleForm.status === 'REJECTED' ? '驳回备注' : '处理备注'">
          <el-input
            v-model="reportHandleForm.remark"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="请输入处理说明"
          />
        </el-form-item>

        <div v-if="canPunishReporter()" class="report-handle-penalty">
          <div class="report-handle-tip">
            当前支持将校园跑腿举报驳回结果直接联动到举报人治理，减少重复操作。
          </div>
          <el-form-item label="联动处罚">
            <el-select v-model="reportHandleForm.punishStatus" class="full-width" @change="handleReportPunishStatusChange">
              <el-option label="仅驳回，不追加处罚" value="NONE" />
              <el-option label="驳回并警告" value="WARNED" />
              <el-option label="驳回并限制跑腿" value="ERRAND_RESTRICTED" />
              <el-option label="驳回并限制举报" value="REPORT_RESTRICTED" />
              <el-option label="驳回并恢复正常" value="ACTIVE" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="reportHandleForm.punishStatus !== 'NONE'" label="处罚说明">
            <el-input
              v-model="reportHandleForm.punishRemark"
              type="textarea"
              :rows="3"
              maxlength="300"
              show-word-limit
              placeholder="请输入处罚说明"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="editor-footer">
          <span class="editor-tip">
            {{ canPunishReporter() ? '可在驳回举报时同步完成用户治理。' : '当前操作将仅更新举报工单状态。' }}
          </span>
          <div class="editor-actions">
            <el-button @click="reportHandleDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="reportHandling" :disabled="!reportHandleForm.remark.trim()" @click="submitReportHandle">
              确认处理
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

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

        <div v-if="errandDetail.relatedReports?.length" class="related-panel related-report-panel">
          <div class="related-title related-title-row">
            <span>关联举报工单</span>
            <div v-if="errandDetail.riskTags?.length" class="tag-inline">
              <el-tag v-for="tag in errandDetail.riskTags" :key="tag" size="small" :type="errandRiskTagType(tag)">{{ tag }}</el-tag>
            </div>
          </div>
          <div class="related-summary-grid">
            <div class="related-summary-card">
              <span>关联举报数</span>
              <strong>{{ errandDetail.relatedReports.length }}</strong>
            </div>
            <div class="related-summary-card">
              <span>处理中举报</span>
              <strong>{{ errandDetail.activeReportCount ?? getActiveReportCount(errandDetail.relatedReports) }}</strong>
            </div>
            <div class="related-summary-card">
              <span>超时举报</span>
              <strong>{{ errandDetail.overdueReportCount ?? getOverdueReportCount(errandDetail.relatedReports) }}</strong>
            </div>
          </div>
          <div v-for="item in errandDetail.relatedReports" :key="item.id" class="related-item related-report-item">
            <div class="related-report-head">
              <div class="related-report-title">
                <strong>#{{ item.id }} {{ item.reportType }}</strong>
                <span>{{ item.description }}</span>
              </div>
              <div class="related-report-tags">
                <el-tag size="small" :type="reportStatusTagType(item.status)">{{ formatReportStatus(item.status) }}</el-tag>
                <el-tag size="small" :type="reportDeadlineTagType(item)">{{ formatReportDeadlineState(item) }}</el-tag>
              </div>
            </div>
            <div class="related-report-meta">
              <div class="related-report-meta-item">
                <span>处理截止</span>
                <strong>{{ formatReportDeadlineAt(item) }}</strong>
              </div>
              <div class="related-report-meta-item">
                <span>时限提示</span>
                <strong>{{ formatReportDeadlineHint(item) }}</strong>
              </div>
              <div class="related-report-meta-item">
                <span>处理时间</span>
                <strong>{{ formatDate(item.handledAt) }}</strong>
              </div>
            </div>
            <div class="related-report-footer">
              <div class="related-report-footer-head">
                <span>处理备注</span>
                <el-button type="primary" link @click.stop="openReportFromErrand(item.id)">查看工单</el-button>
              </div>
              <p>{{ item.handleRemark || '暂未填写处理备注' }}</p>
            </div>
          </div>
        </div>

        <div class="audit-section">
          <div class="audit-head">
            <div>
              <h3>聊天核查区</h3>
              <p>后续可直接接入管理员会话查询接口，用于核查恶意下单、言语骚扰、线下违规交易等证据。</p>
            </div>
            <el-button type="primary" plain :disabled="!errandDetail.id" @click="refreshErrandConversationAudit">
              刷新聊天记录
            </el-button>
          </div>
          <div class="audit-meta">
            <div class="audit-meta-item">
              <span>会话 ID</span>
              <strong>{{ errandConversationReview?.conversationId || errandDetail.conversationId || '-' }}</strong>
            </div>
            <div class="audit-meta-item">
              <span>当前状态</span>
              <strong>{{ errandConversationLoading ? '加载中' : errandConversationReview?.messages?.length ? `已加载 ${errandConversationReview.messages.length} 条消息` : errandConversationError || '暂无关联会话' }}</strong>
            </div>
          </div>
          <div v-if="errandConversationError" class="audit-placeholder">{{ errandConversationError }}</div>
          <template v-else-if="errandConversationReview">
            <div v-if="errandConversationReview.members?.length" class="audit-members">
              <div v-for="member in errandConversationReview.members" :key="member.userId" class="audit-member-card">
                <strong>{{ member.nickname || `用户#${member.userId}` }}</strong>
                <span>{{ member.phone || '-' }} / {{ formatUserRole(member.role) }}</span>
                <em>{{ formatUserStatus(member.status) }}</em>
              </div>
            </div>
            <div v-if="errandConversationReview.messages?.length" class="audit-timeline">
              <div v-for="message in errandConversationReview.messages" :key="message.id" class="audit-message">
                <div class="audit-message-meta">
                  <strong>{{ message.senderNickname || `用户#${message.senderId || '-'}` }}</strong>
                  <span>{{ formatUserRole(message.senderRole) }} · {{ formatMessageType(message.type) }} · {{ formatDate(message.createdAt) }}</span>
                </div>
                <img v-if="isImageMessage(message)" :src="message.content" :alt="message.senderNickname || '聊天图片'" class="audit-message-image" />
                <div v-else class="audit-message-content">{{ message.content || '-' }}</div>
              </div>
            </div>
            <div v-else class="audit-placeholder">当前会话暂无消息记录。</div>
          </template>
          <div v-else class="audit-placeholder">
            当前订单还没有可核查的关联会话，待业务侧生成会话后即可在这里展示聊天记录。
          </div>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="partnerDemandDrawerVisible" title="找搭子需求详情" size="720px">
      <template v-if="partnerDemandDetail">
        <div class="drawer-head space-between">
          <span>找搭子需求详情</span>
          <div class="drawer-actions">
            <el-button type="primary" @click="handlePartnerDemandAction(partnerDemandDetail, 'APPROVE')">通过</el-button>
            <el-button type="danger" @click="handlePartnerDemandAction(partnerDemandDetail, 'REJECT')">驳回</el-button>
            <el-button type="warning" @click="handlePartnerDemandAction(partnerDemandDetail, 'OFFLINE')">下架</el-button>
            <el-button type="success" @click="handlePartnerDemandAction(partnerDemandDetail, 'RESTORE')">恢复</el-button>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-item"><span>需求标题</span><strong>{{ partnerDemandDetail.title }}</strong></div>
          <div class="detail-item"><span>审核状态</span><strong>{{ formatPartnerDemandStatus(partnerDemandDetail.status) }}</strong></div>
          <div class="detail-item"><span>搭子类型</span><strong>{{ partnerDemandDetail.type }}</strong></div>
          <div class="detail-item"><span>发布时间</span><strong>{{ formatDate(partnerDemandDetail.createdAt) }}</strong></div>
          <div class="detail-item"><span>发布人</span><strong>{{ partnerDemandDetail.publisherName }}</strong></div>
          <div class="detail-item"><span>联系方式</span><strong>{{ partnerDemandDetail.publisherPhone }}</strong></div>
          <div class="detail-item full"><span>活动地点</span><strong>{{ partnerDemandDetail.location }}</strong></div>
          <div class="detail-item full"><span>需求时间</span><strong>{{ partnerDemandDetail.schedule }}</strong></div>
          <div class="detail-item full"><span>需求描述</span><strong>{{ partnerDemandDetail.description }}</strong></div>
          <div class="detail-item"><span>报名人数</span><strong>{{ partnerDemandDetail.applyCount }}</strong></div>
          <div class="detail-item"><span>关联举报</span><strong>{{ partnerDemandDetail.reportCount }}</strong></div>
          <div class="detail-item full"><span>审核说明</span><strong>{{ partnerDemandDetail.reviewRemark || '-' }}</strong></div>
          <div class="detail-item"><span>审核时间</span><strong>{{ formatDate(partnerDemandDetail.reviewedAt) }}</strong></div>
          <div class="detail-item"><span>最新会话时间</span><strong>{{ formatDate(partnerDemandDetail.latestMessageAt) }}</strong></div>
        </div>

        <div v-if="partnerDemandDetail.needTags?.length" class="related-panel">
          <div class="related-title">需求标签</div>
          <div class="tag-inline">
            <span v-for="item in partnerDemandDetail.needTags" :key="item" class="tag-chip">{{ item }}</span>
          </div>
        </div>

        <div v-if="partnerDemandDetail.riskTags?.length" class="related-panel">
          <div class="related-title">风险标签</div>
          <div class="tag-inline">
            <el-tag v-for="item in partnerDemandDetail.riskTags" :key="item" size="small" :type="item.includes('超时') || item.includes('高风险') ? 'danger' : item.includes('待处理') || item.includes('下架') ? 'warning' : 'info'">
              {{ item }}
            </el-tag>
          </div>
        </div>

        <div v-if="currentPartnerDemandReports.length" class="related-panel">
          <div class="related-title">关联举报工单</div>
          <div v-for="item in currentPartnerDemandReports" :key="item.id" class="related-item related-report-item">
            <div class="related-report-head">
              <div class="related-report-title">
                <strong>#{{ item.id }} {{ item.reportType }}</strong>
                <span>{{ item.description }}</span>
              </div>
              <div class="related-report-tags">
                <el-tag size="small" :type="reportStatusTagType(item.status)">{{ formatReportStatus(item.status) }}</el-tag>
                <el-tag size="small" :type="reportDeadlineTagType(item)">{{ formatReportDeadlineState(item) }}</el-tag>
              </div>
            </div>
            <div class="related-report-footer">
              <div class="related-report-footer-head">
                <span>处理备注</span>
                <el-button type="primary" link @click="openPartnerReportFromDemand(item.id)">查看工单</el-button>
              </div>
              <p>{{ item.handleRemark || '暂未填写处理备注' }}</p>
            </div>
          </div>
        </div>

        <div v-if="currentPartnerDemandConversations.length" class="audit-section">
          <div class="audit-head">
            <div>
              <h3>关联会话核查</h3>
              <p>用于核查报名沟通中的押金引导、骚扰消息、站外导流等异常行为。</p>
            </div>
          </div>
          <div class="conversation-review-list">
            <div v-for="item in currentPartnerDemandConversations" :key="item.id" class="conversation-review-card">
              <div class="conversation-review-head">
                <strong>{{ item.counterpartyName }}</strong>
                <div class="related-report-tags">
                  <el-tag size="small" :type="partnerConversationStatusTagType(item.status)">{{ formatPartnerConversationStatus(item.status) }}</el-tag>
                  <el-tag size="small" :type="partnerRiskLevelTagType(item.riskLevel)">{{ formatPartnerRiskLevel(item.riskLevel) }}</el-tag>
                </div>
              </div>
              <p>{{ item.lastMessage }}</p>
              <div class="conversation-review-meta">{{ item.counterpartyPhone }} · {{ formatDate(item.lastMessageAt) }}</div>
              <el-button type="primary" link @click="openPartnerChatFromDemand(item.id)">查看会话</el-button>
            </div>
          </div>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="partnerReportDrawerVisible" title="找搭子举报详情" size="680px">
      <template v-if="partnerReportDetail">
        <div class="drawer-head space-between">
          <span>找搭子举报详情</span>
          <div class="drawer-actions">
            <el-button type="primary" @click="handlePartnerReportAction(partnerReportDetail, 'PROCESSING')">受理</el-button>
            <el-button type="success" @click="handlePartnerReportAction(partnerReportDetail, 'RESOLVED')">处理完成</el-button>
            <el-button type="danger" @click="handlePartnerReportAction(partnerReportDetail, 'REJECTED')">驳回</el-button>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-item"><span>工单ID</span><strong>#{{ partnerReportDetail.id }}</strong></div>
          <div class="detail-item"><span>状态</span><strong>{{ formatReportStatus(partnerReportDetail.status) }}</strong></div>
          <div class="detail-item"><span>举报类型</span><strong>{{ partnerReportDetail.reportType }}</strong></div>
          <div class="detail-item"><span>关联需求</span><strong>{{ partnerReportDetail.targetTitle }}</strong></div>
          <div class="detail-item"><span>举报人</span><strong>{{ partnerReportDetail.reporterName }}</strong></div>
          <div class="detail-item"><span>联系方式</span><strong>{{ partnerReportDetail.contactPhone }}</strong></div>
          <div class="detail-item full"><span>举报说明</span><strong>{{ partnerReportDetail.description }}</strong></div>
          <div class="detail-item full"><span>证据摘要</span><strong>{{ partnerReportDetail.evidenceSummary || '-' }}</strong></div>
          <div class="detail-item"><span>处理截止</span><strong>{{ formatReportDeadlineAt(partnerReportDetail) }}</strong></div>
          <div class="detail-item"><span>时限状态</span><strong>{{ formatReportDeadlineState(partnerReportDetail) }}</strong></div>
          <div class="detail-item"><span>处理时间</span><strong>{{ formatDate(partnerReportDetail.handledAt) }}</strong></div>
          <div class="detail-item"><span>创建时间</span><strong>{{ formatDate(partnerReportDetail.createdAt) }}</strong></div>
          <div class="detail-item full"><span>处理备注</span><strong>{{ partnerReportDetail.handleRemark || '-' }}</strong></div>
        </div>

        <div class="audit-section">
          <div class="audit-head">
            <div>
              <h3>联动核查</h3>
              <p>可直接回看被举报需求与关联会话，减少运营来回切换页面。</p>
            </div>
            <div class="drawer-actions">
              <el-button type="warning" plain @click="openPartnerDemandFromReport">查看需求</el-button>
              <el-button type="primary" plain :disabled="!partnerReportDetail.conversationId" @click="openPartnerChatFromReport">查看会话</el-button>
            </div>
          </div>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="partnerChatDrawerVisible" title="找搭子会话详情" size="760px">
      <template v-if="partnerChatDetail">
        <div class="drawer-head space-between">
          <span>找搭子会话详情</span>
          <div class="drawer-actions">
            <el-button type="warning" @click="handlePartnerChatAction(partnerChatDetail, 'MARK_REVIEW')">标记复核</el-button>
            <el-button type="danger" @click="handlePartnerChatAction(partnerChatDetail, 'CLOSE')">关闭会话</el-button>
            <el-button type="success" @click="handlePartnerChatAction(partnerChatDetail, 'RESTORE')">恢复会话</el-button>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-item"><span>会话ID</span><strong>{{ partnerChatDetail.id }}</strong></div>
          <div class="detail-item"><span>会话状态</span><strong>{{ formatPartnerConversationStatus(partnerChatDetail.status) }}</strong></div>
          <div class="detail-item"><span>风险等级</span><strong>{{ formatPartnerRiskLevel(partnerChatDetail.riskLevel) }}</strong></div>
          <div class="detail-item"><span>关联需求</span><strong>{{ partnerChatDetail.demandTitle }}</strong></div>
          <div class="detail-item"><span>对方昵称</span><strong>{{ partnerChatDetail.counterpartyName }}</strong></div>
          <div class="detail-item"><span>联系方式</span><strong>{{ partnerChatDetail.counterpartyPhone }}</strong></div>
          <div class="detail-item"><span>未读数</span><strong>{{ partnerChatDetail.unreadCount }}</strong></div>
          <div class="detail-item"><span>最新消息时间</span><strong>{{ formatDate(partnerChatDetail.lastMessageAt) }}</strong></div>
          <div class="detail-item full"><span>复核备注</span><strong>{{ partnerChatDetail.reviewRemark || '-' }}</strong></div>
        </div>

        <div v-if="partnerChatDetail.riskTags?.length" class="related-panel">
          <div class="related-title">风险标签</div>
          <div class="tag-inline">
            <el-tag v-for="item in partnerChatDetail.riskTags" :key="item" size="small" :type="item.includes('高风险') || item.includes('关闭') || item.includes('超时') ? 'danger' : item.includes('复核') || item.includes('处理中') ? 'warning' : 'info'">
              {{ item }}
            </el-tag>
          </div>
        </div>

        <div class="audit-section">
          <div class="audit-head">
            <div>
              <h3>聊天记录</h3>
              <p>支持人工核查押金、骚扰、站外引流等风险消息。</p>
            </div>
            <el-button type="primary" plain @click="openPartnerDemandFromChat">查看关联需求</el-button>
          </div>
          <div v-if="partnerChatDetail.messages?.length" class="audit-timeline">
            <div v-for="item in partnerChatDetail.messages" :key="item.id" class="audit-message">
              <div class="audit-message-meta">
                <strong>{{ item.senderName }}</strong>
                <span>{{ item.senderRole }} · {{ formatMessageType(item.type) }} · {{ formatDate(item.createdAt) }}</span>
              </div>
              <div class="audit-message-content">{{ item.content }}</div>
            </div>
          </div>
          <div v-else class="audit-placeholder">当前会话暂无聊天记录。</div>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="beautyAppealDrawerVisible" title="售后仲裁详情" size="720px">
      <template v-if="beautyAppealDetail">
        <div class="drawer-head space-between">
          <span>售后工单详情</span>
          <div class="drawer-actions">
            <el-button type="primary" @click="handleBeautyAppealAction(beautyAppealDetail, 'PROCESSING')">受理</el-button>
            <el-button type="success" @click="handleBeautyAppealAction(beautyAppealDetail, 'RESOLVED')">仲裁通过</el-button>
            <el-button type="danger" @click="handleBeautyAppealAction(beautyAppealDetail, 'REJECTED')">驳回</el-button>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-item"><span>工单ID</span><strong>{{ beautyAppealDetail.id }}</strong></div>
          <div class="detail-item"><span>处理状态</span><strong>{{ formatBeautyAppealStatus(beautyAppealDetail.status) }}</strong></div>
          <div class="detail-item"><span>售后类型</span><strong>{{ beautyAppealDetail.issueType }}</strong></div>
          <div class="detail-item"><span>关联商品</span><strong>{{ beautyAppealDetail.goodTitle }}</strong></div>
          <div class="detail-item"><span>商品品类</span><strong>{{ formatBeautyCategory(beautyAppealDetail.category) }}</strong></div>
          <div class="detail-item"><span>退款金额</span><strong>￥{{ formatMoney(beautyAppealDetail.refundAmount) }}</strong></div>
          <div class="detail-item"><span>买家</span><strong>{{ beautyAppealDetail.buyerName }}</strong></div>
          <div class="detail-item"><span>卖家</span><strong>{{ beautyAppealDetail.sellerName }}</strong></div>
          <div class="detail-item"><span>联系电话</span><strong>{{ beautyAppealDetail.contactPhone }}</strong></div>
          <div class="detail-item"><span>提交时间</span><strong>{{ formatDate(beautyAppealDetail.createdAt) }}</strong></div>
          <div class="detail-item"><span>处理时间</span><strong>{{ formatDate(beautyAppealDetail.handledAt) }}</strong></div>
          <div class="detail-item full"><span>问题说明</span><strong>{{ beautyAppealDetail.reason }}</strong></div>
          <div class="detail-item full"><span>凭证摘要</span><strong>{{ beautyAppealDetail.proofSummary }}</strong></div>
          <div class="detail-item full"><span>处理备注</span><strong>{{ beautyAppealDetail.handleRemark || '-' }}</strong></div>
        </div>

        <div class="audit-section">
          <div class="audit-head">
            <div>
              <h3>平台仲裁建议</h3>
              <p>优先核对原订单截图、实拍图、沟通记录，再决定退款、驳回或转人工复核。</p>
            </div>
            <el-button type="primary" plain @click="openBeautyFromAppeal">查看关联商品</el-button>
          </div>
          <div class="audit-meta">
            <div class="audit-meta-item"><span>凭证完整度</span><strong>{{ beautyAppealDetail.proofSummary.includes('订单') || beautyAppealDetail.proofSummary.includes('截图') ? '已附核心凭证' : '建议补充订单截图' }}</strong></div>
            <div class="audit-meta-item"><span>当前建议</span><strong>{{ beautyAppealDetail.status === 'PENDING' ? '优先受理并人工核查' : beautyAppealDetail.status === 'PROCESSING' ? '继续比对买卖双方证据' : beautyAppealDetail.status === 'RESOLVED' ? '已完成仲裁闭环' : '当前凭证不足，建议补充后重提' }}</strong></div>
            <div class="audit-meta-item full-width"><span>处理说明</span><strong>{{ beautyAppealDetail.handleRemark || '当前尚未填写处理说明。' }}</strong></div>
          </div>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="beautyDrawerVisible" title="美妆好物详情" size="760px">
      <template v-if="beautyDetail">
        <div class="drawer-head space-between">
          <span>美妆好物详情</span>
          <div class="drawer-actions">
            <el-button type="primary" plain @click="openBeautyEdit(beautyDetail)">编辑内容</el-button>
            <el-button
              v-if="beautyDetail.status === 'PENDING'"
              type="primary"
              @click="handleBeautyAction(beautyDetail.id, 'APPROVE')"
            >
              通过
            </el-button>
            <el-button
              v-if="beautyDetail.status === 'PENDING'"
              type="danger"
              @click="handleBeautyAction(beautyDetail.id, 'REJECT')"
            >
              驳回
            </el-button>
            <el-button
              v-if="beautyDetail.status === 'APPROVED'"
              type="warning"
              @click="handleBeautyAction(beautyDetail.id, 'OFFLINE')"
            >
              下架
            </el-button>
            <el-button
              v-if="beautyDetail.status === 'OFFLINE' || beautyDetail.status === 'REJECTED'"
              type="success"
              @click="handleBeautyAction(beautyDetail.id, 'RESTORE')"
            >
              恢复上架
            </el-button>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-item"><span>商品标题</span><strong>{{ beautyDetail.title }}</strong></div>
          <div class="detail-item"><span>状态</span><strong>{{ formatBeautyStatus(beautyDetail.status) }}</strong></div>
          <div class="detail-item"><span>品类</span><strong>{{ formatBeautyCategory(beautyDetail.category) }}</strong></div>
          <div class="detail-item"><span>价格</span><strong>￥{{ beautyDetail.price }}</strong></div>
          <div class="detail-item"><span>发布人</span><strong>{{ beautyDetail.creatorNickname || '-' }}</strong></div>
          <div class="detail-item"><span>来源</span><strong>{{ formatBeautySourceType(beautyDetail.sourceType) }}</strong></div>
          <div class="detail-item"><span>收藏量</span><strong>{{ beautyDetail.favoriteCount }}</strong></div>
          <div class="detail-item"><span>浏览量</span><strong>{{ beautyDetail.viewCount }}</strong></div>
          <div class="detail-item full"><span>商品简介</span><strong>{{ beautyDetail.summary }}</strong></div>
          <div class="detail-item full"><span>使用说明</span><strong>{{ beautyDetail.usageGuide || '-' }}</strong></div>
          <div class="detail-item full"><span>适用场景</span><strong>{{ beautyDetail.sceneText || '-' }}</strong></div>
          <div class="detail-item full"><span>种草测评</span><strong>{{ beautyDetail.evaluation || '-' }}</strong></div>
          <div class="detail-item full"><span>宿舍使用体验</span><strong>{{ beautyDetail.dormExperience || '-' }}</strong></div>
          <div class="detail-item full"><span>避坑提示</span><strong>{{ beautyDetail.avoidanceGuide || '-' }}</strong></div>
          <div class="detail-item full"><span>驳回原因</span><strong>{{ beautyDetail.rejectReason || '-' }}</strong></div>
          <div class="detail-item"><span>审核人</span><strong>{{ beautyDetail.reviewedBy || '-' }}</strong></div>
          <div class="detail-item"><span>审核时间</span><strong>{{ formatDate(beautyDetail.reviewedAt) }}</strong></div>
          <div class="detail-item"><span>发布时间</span><strong>{{ formatDate(beautyDetail.publishedAt) }}</strong></div>
          <div class="detail-item"><span>创建时间</span><strong>{{ formatDate(beautyDetail.createdAt) }}</strong></div>
        </div>

        <div v-if="getBeautyRiskTags(beautyDetail).length" class="related-panel">
          <div class="related-title">风控标签</div>
          <div class="tag-inline">
            <el-tag
              v-for="item in getBeautyRiskTags(beautyDetail)"
              :key="`${beautyDetail.id}-${item}`"
              size="small"
              :type="item.includes('缺少') || item.includes('驳回') ? 'danger' : item.includes('待') || item.includes('观察') ? 'warning' : 'info'"
            >
              {{ item }}
            </el-tag>
          </div>
        </div>

        <div v-if="beautyDetail.skinTags?.length" class="related-panel">
          <div class="related-title">肤质 / 标签</div>
          <div class="tag-inline">
            <span v-for="item in beautyDetail.skinTags" :key="item" class="tag-chip">{{ item }}</span>
          </div>
        </div>

        <div v-if="beautyDetail.reviewList?.length" class="related-panel">
          <div class="related-title">用户评价</div>
          <div v-for="item in beautyDetail.reviewList" :key="item.id || item.user" class="related-item">
            <strong>{{ item.user || '匿名用户' }}</strong>
            <span>{{ item.content }}</span>
          </div>
        </div>

        <div v-if="beautyDetail.productImage || beautyDetail.receiptImage || beautyDetail.gallery?.length" class="preview-section">
          <div v-if="beautyDetail.productImage" class="preview-card">
            <div class="preview-title">商品实拍图</div>
            <img :src="beautyDetail.productImage.url" :alt="beautyDetail.productImage.originalName" />
          </div>
          <div v-if="beautyDetail.receiptImage" class="preview-card">
            <div class="preview-title">购买凭证</div>
            <img :src="beautyDetail.receiptImage.url" :alt="beautyDetail.receiptImage.originalName" />
          </div>
          <div
            v-for="(item, index) in (beautyDetail.gallery || []).slice(0, 4)"
            :key="`${beautyDetail.id}-gallery-${index}`"
            class="preview-card"
          >
            <div class="preview-title">展示图 {{ index + 1 }}</div>
            <img :src="item" :alt="`gallery-${index}`" />
          </div>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="jobsMerchantDrawerVisible" title="用工资质详情" size="720px">
      <template v-if="jobsMerchantDetail">
        <div class="drawer-head space-between">
          <span>用工资质详情</span>
          <div class="drawer-actions">
            <el-button type="primary" @click="handleJobsMerchantReview(jobsMerchantDetail, 'APPROVED')">审核通过</el-button>
            <el-button type="danger" @click="handleJobsMerchantReview(jobsMerchantDetail, 'REJECTED')">驳回重提</el-button>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-item"><span>申请ID</span><strong>{{ jobsMerchantDetail.id }}</strong></div>
          <div class="detail-item"><span>审核状态</span><strong>{{ formatJobsMerchantStatus(jobsMerchantDetail.status) }}</strong></div>
          <div class="detail-item"><span>申请人</span><strong>{{ jobsMerchantDetail.applicantName }}</strong></div>
          <div class="detail-item"><span>联系电话</span><strong>{{ jobsMerchantDetail.phone }}</strong></div>
          <div class="detail-item full"><span>商家名称</span><strong>{{ jobsMerchantDetail.businessName }}</strong></div>
          <div class="detail-item"><span>主体类型</span><strong>{{ jobsMerchantDetail.identityType }}</strong></div>
          <div class="detail-item"><span>所在区域</span><strong>{{ jobsMerchantDetail.area }}</strong></div>
          <div class="detail-item"><span>提交时间</span><strong>{{ formatDate(jobsMerchantDetail.submittedAt) }}</strong></div>
          <div class="detail-item"><span>审核时间</span><strong>{{ formatDate(jobsMerchantDetail.reviewedAt) }}</strong></div>
          <div class="detail-item full"><span>审核说明</span><strong>{{ jobsMerchantDetail.reviewRemark || '-' }}</strong></div>
        </div>

        <div class="preview-section">
          <div class="preview-card">
            <div class="preview-title">营业执照照片</div>
            <img :src="jobsMerchantDetail.licenseImage" :alt="jobsMerchantDetail.businessName" />
          </div>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="jobsPostDrawerVisible" title="岗位详情" size="760px">
      <template v-if="jobsPostDetail">
        <div class="drawer-head space-between">
          <span>岗位详情</span>
          <div class="drawer-actions">
            <el-button type="primary" @click="handleJobsPostAction(jobsPostDetail, 'APPROVE')">通过</el-button>
            <el-button type="danger" @click="handleJobsPostAction(jobsPostDetail, 'REJECT')">驳回</el-button>
            <el-button type="warning" @click="handleJobsPostAction(jobsPostDetail, 'OFFLINE')">下架</el-button>
            <el-button type="danger" plain @click="handleJobsPostAction(jobsPostDetail, 'BLOCK')">拦截</el-button>
            <el-button type="success" @click="handleJobsPostAction(jobsPostDetail, 'RESTORE')">恢复</el-button>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-item"><span>岗位标题</span><strong>{{ jobsPostDetail.title }}</strong></div>
          <div class="detail-item"><span>岗位状态</span><strong>{{ formatJobsPostStatus(jobsPostDetail.status) }}</strong></div>
          <div class="detail-item"><span>发布身份</span><strong>{{ formatJobsRole(jobsPostDetail.role) }}</strong></div>
          <div class="detail-item"><span>岗位类型</span><strong>{{ jobsPostDetail.category }}</strong></div>
          <div class="detail-item"><span>工作形式</span><strong>{{ formatJobsWorkMode(jobsPostDetail.workMode) }}</strong></div>
          <div class="detail-item"><span>薪资 / 结算</span><strong>￥{{ formatMoney(jobsPostDetail.salary) }} / {{ jobsPostDetail.settlement }}</strong></div>
          <div class="detail-item"><span>固定区间</span><strong>{{ jobsPostDetail.distanceRange }}</strong></div>
          <div class="detail-item"><span>实际距离</span><strong>{{ jobsPostDetail.actualDistanceKm }} km</strong></div>
          <div class="detail-item"><span>发布人</span><strong>{{ jobsPostDetail.publisherName }}</strong></div>
          <div class="detail-item"><span>联系电话</span><strong>{{ jobsPostDetail.publisherPhone }}</strong></div>
          <div class="detail-item full"><span>岗位区域</span><strong>{{ jobsPostDetail.area }}</strong></div>
          <div class="detail-item full"><span>工作时间</span><strong>{{ jobsPostDetail.schedule }}</strong></div>
          <div class="detail-item full"><span>岗位描述</span><strong>{{ jobsPostDetail.description }}</strong></div>
          <div class="detail-item full"><span>岗位要求</span><strong>{{ jobsPostDetail.requirement }}</strong></div>
          <div class="detail-item"><span>报名人数</span><strong>{{ jobsPostDetail.applicantCount }}</strong></div>
          <div class="detail-item"><span>创建时间</span><strong>{{ formatDate(jobsPostDetail.createdAt) }}</strong></div>
          <div class="detail-item full"><span>审核说明</span><strong>{{ jobsPostDetail.reviewRemark || '-' }}</strong></div>
        </div>

        <div v-if="jobsPostDetail.riskTags.length" class="related-panel">
          <div class="related-title">风险标签</div>
          <div class="tag-inline">
            <el-tag v-for="item in jobsPostDetail.riskTags" :key="item" size="small" :type="item.includes('超范围') || item.includes('扣费') ? 'danger' : item.includes('高风险') || item.includes('中介') || item.includes('举报') ? 'warning' : 'info'">
              {{ item }}
            </el-tag>
          </div>
        </div>

        <div v-if="currentJobsPostReports.length" class="related-panel">
          <div class="related-title">关联举报工单</div>
          <div v-for="item in currentJobsPostReports" :key="item.id" class="related-item related-report-item">
            <div class="related-report-head">
              <div class="related-report-title">
                <strong>#{{ item.id }} {{ item.reportType }}</strong>
                <span>{{ item.description }}</span>
              </div>
              <div class="related-report-tags">
                <el-tag size="small" :type="jobsReportStatusTagType(item.status)">{{ formatJobsReportStatus(item.status) }}</el-tag>
              </div>
            </div>
            <div class="related-report-footer">
              <div class="related-report-footer-head">
                <span>公示结果</span>
                <el-button type="primary" link @click="openJobsReportDetail(item)">查看工单</el-button>
              </div>
              <p>{{ item.publicResult || item.handleRemark || '当前暂无处理结果。' }}</p>
            </div>
          </div>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="jobsReportDrawerVisible" title="举报工单详情" size="720px">
      <template v-if="jobsReportDetail">
        <div class="drawer-head space-between">
          <span>举报工单详情</span>
          <div class="drawer-actions">
            <el-button type="primary" @click="handleJobsReportAction(jobsReportDetail, 'RESOLVED')">标记已处理</el-button>
            <el-button type="success" @click="handleJobsReportAction(jobsReportDetail, 'CLOSED')">标记已完结</el-button>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-item"><span>工单ID</span><strong>{{ jobsReportDetail.id }}</strong></div>
          <div class="detail-item"><span>工单状态</span><strong>{{ formatJobsReportStatus(jobsReportDetail.status) }}</strong></div>
          <div class="detail-item"><span>举报类型</span><strong>{{ jobsReportDetail.reportType }}</strong></div>
          <div class="detail-item"><span>关联岗位</span><strong>{{ jobsReportDetail.postTitle }}</strong></div>
          <div class="detail-item"><span>举报人</span><strong>{{ jobsReportDetail.reporterName }}</strong></div>
          <div class="detail-item"><span>被举报方</span><strong>{{ jobsReportDetail.targetName }} / {{ formatJobsRole(jobsReportDetail.targetRole) }}</strong></div>
          <div class="detail-item"><span>提交时间</span><strong>{{ formatDate(jobsReportDetail.createdAt) }}</strong></div>
          <div class="detail-item"><span>处理时间</span><strong>{{ formatDate(jobsReportDetail.handledAt) }}</strong></div>
          <div class="detail-item full"><span>问题描述</span><strong>{{ jobsReportDetail.description }}</strong></div>
          <div class="detail-item full"><span>处理说明</span><strong>{{ jobsReportDetail.handleRemark || '-' }}</strong></div>
          <div class="detail-item full"><span>公示结果</span><strong>{{ jobsReportDetail.publicResult || '-' }}</strong></div>
        </div>

        <div class="audit-section">
          <div class="audit-head">
            <div>
              <h3>联动处理</h3>
              <p>处理举报时可直接回看关联岗位，并同步下架或限制相关主体权限。</p>
            </div>
            <el-button type="primary" plain @click="openJobsPostFromReport">查看关联岗位</el-button>
          </div>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="jobsAccountDrawerVisible" title="账号详情" size="760px">
      <template v-if="jobsAccountDetail">
        <div class="drawer-head space-between">
          <span>账号详情</span>
          <div class="drawer-actions">
            <el-button type="warning" @click="handleJobsAccountAction(jobsAccountDetail, 'LIMIT')">限制账号</el-button>
            <el-button type="danger" @click="handleJobsAccountAction(jobsAccountDetail, 'BAN')">封禁账号</el-button>
            <el-button type="success" @click="handleJobsAccountAction(jobsAccountDetail, 'RESTORE')">恢复正常</el-button>
            <el-button type="primary" plain @click="handleJobsCreditAdjust(jobsAccountDetail)">调整信用</el-button>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-item"><span>账号ID</span><strong>{{ jobsAccountDetail.id }}</strong></div>
          <div class="detail-item"><span>账号状态</span><strong>{{ formatJobsAccountStatus(jobsAccountDetail.status) }}</strong></div>
          <div class="detail-item"><span>名称</span><strong>{{ jobsAccountDetail.name }}</strong></div>
          <div class="detail-item"><span>身份</span><strong>{{ formatJobsRole(jobsAccountDetail.role) }}</strong></div>
          <div class="detail-item"><span>手机号</span><strong>{{ jobsAccountDetail.phone }}</strong></div>
          <div class="detail-item"><span>信用分</span><strong>{{ jobsAccountDetail.creditScore }}</strong></div>
          <div class="detail-item"><span>发岗权限</span><strong>{{ jobsAccountDetail.publishEnabled ? '开启' : '关闭' }}</strong></div>
          <div class="detail-item"><span>报名权限</span><strong>{{ jobsAccountDetail.applyEnabled ? '开启' : '关闭' }}</strong></div>
          <div class="detail-item"><span>接单权限</span><strong>{{ jobsAccountDetail.acceptEnabled ? '开启' : '关闭' }}</strong></div>
          <div class="detail-item"><span>举报次数</span><strong>{{ jobsAccountDetail.reportCount }}</strong></div>
          <div class="detail-item"><span>纠纷次数</span><strong>{{ jobsAccountDetail.disputeCount }}</strong></div>
          <div class="detail-item"><span>创建时间</span><strong>{{ formatDate(jobsAccountDetail.createdAt) }}</strong></div>
          <div class="detail-item full"><span>最近处理备注</span><strong>{{ jobsAccountDetail.latestRemark || '-' }}</strong></div>
        </div>

        <div v-if="currentJobsAccountPosts.length" class="related-panel">
          <div class="related-title">关联岗位</div>
          <div v-for="item in currentJobsAccountPosts" :key="item.id" class="related-item">
            <strong>{{ item.title }}</strong>
            <span>{{ item.category }} / {{ formatJobsPostStatus(item.status) }} / {{ item.applicantCount }} 人报名</span>
          </div>
        </div>

        <div v-if="currentJobsAccountReports.length" class="related-panel">
          <div class="related-title">关联工单</div>
          <div v-for="item in currentJobsAccountReports" :key="item.id" class="related-item">
            <strong>#{{ item.id }} {{ item.reportType }}</strong>
            <span>{{ formatJobsReportStatus(item.status) }} / {{ item.postTitle }}</span>
          </div>
        </div>
      </template>
    </el-drawer>

    <el-dialog
      v-model="beautyEditorVisible"
      :title="beautyEditorMode === 'create' ? '新增美妆好物' : '编辑美妆好物'"
      width="760px"
      destroy-on-close
    >
      <el-form label-position="top" class="beauty-editor-form">
        <div class="editor-grid">
          <el-form-item label="商品标题">
            <el-input v-model="beautyForm.title" maxlength="120" placeholder="请输入商品标题" />
          </el-form-item>
          <el-form-item label="商品品类">
            <el-select v-model="beautyForm.category" placeholder="请选择品类">
              <el-option v-for="item in beautyCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="商品价格">
            <el-input-number v-model="beautyForm.price" :min="0.01" :precision="2" :step="1" class="editor-number" />
          </el-form-item>
          <el-form-item label="肤质 / 标签">
            <el-input
              v-model="beautyForm.skinTagsText"
              placeholder="多个标签用中文逗号分隔，例如 黄皮友好，新手适用"
            />
          </el-form-item>
          <el-form-item label="商品简介" class="full">
            <el-input v-model="beautyForm.summary" type="textarea" :rows="3" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="产品使用说明" class="full">
            <el-input v-model="beautyForm.usageGuide" type="textarea" :rows="3" maxlength="1000" show-word-limit />
          </el-form-item>
          <el-form-item label="适用场景说明" class="full">
            <el-input v-model="beautyForm.sceneText" type="textarea" :rows="2" maxlength="255" show-word-limit />
          </el-form-item>
          <el-form-item label="种草测评" class="full">
            <el-input v-model="beautyForm.evaluation" type="textarea" :rows="4" maxlength="2000" show-word-limit />
          </el-form-item>
          <el-form-item label="宿舍使用体验" class="full">
            <el-input v-model="beautyForm.dormExperience" type="textarea" :rows="4" maxlength="2000" show-word-limit />
          </el-form-item>
          <el-form-item label="新手避坑指南" class="full">
            <el-input v-model="beautyForm.avoidanceGuide" type="textarea" :rows="3" maxlength="2000" show-word-limit />
          </el-form-item>
        </div>

        <div class="upload-grid">
          <el-form-item label="商品实拍图" class="upload-item">
            <div class="upload-stack">
              <el-upload
                :show-file-list="false"
                accept="image/*"
                :http-request="uploadBeautyProductImage"
              >
                <el-button type="primary" plain>上传商品图</el-button>
              </el-upload>
              <img v-if="beautyForm.productImage?.url" :src="beautyForm.productImage.url" alt="product" class="upload-preview" />
              <div v-else class="upload-placeholder">请上传商品实拍图</div>
            </div>
          </el-form-item>

          <el-form-item label="订单购买凭证" class="upload-item">
            <div class="upload-stack">
              <el-upload
                :show-file-list="false"
                accept="image/*"
                :http-request="uploadBeautyReceiptImage"
              >
                <el-button type="success" plain>上传购买凭证</el-button>
              </el-upload>
              <img v-if="beautyForm.receiptImage?.url" :src="beautyForm.receiptImage.url" alt="receipt" class="upload-preview" />
              <div v-else class="upload-placeholder">请上传订单购买凭证</div>
            </div>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="editor-footer">
          <span class="editor-tip">{{ beautyEditorTip }}</span>
          <div class="editor-actions">
            <el-button @click="beautyEditorVisible = false">取消</el-button>
            <el-button type="primary" :loading="beautySubmitting" :disabled="!beautyFormValid" @click="submitBeautyForm">
              {{ beautyEditorMode === 'create' ? '立即保存并上架' : '保存修改' }}
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

type UserRow = {
  id: number
  phone: string
  nickname: string
  role: string
  status: string
  reportRestricted?: number
}

type AdminUserDetail = {
  userId: number
  phone: string
  nickname: string
  avatarUrl?: string
  role: string
  status: string
  reportRestricted?: number
  realNameStatus: string
  gender?: string
  contactPhone?: string
  bio?: string
  reportCount: number
  publishedErrandCount: number
  acceptedErrandCount: number
  completedErrandCount: number
  processingReportCount: number
  rejectedReportCount: number
  latestPunishmentRemark?: string
  latestAccountGovernanceRemark?: string
  latestReportGovernanceRemark?: string
  governanceRecords?: Array<{
    category: string
    actionLabel: string
    remark?: string
    createdAt?: string
  }>
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

type UploadResponse = FileAttachment

type ReportDetail = {
  id: number
  module: string
  targetType: string
  targetId?: number
  submitterId?: number
  reporterUserId?: number
  reportType: string
  description: string
  contactPhone?: string
  status: string
  handleRemark?: string
  handledBy?: number
  handledAt?: string
  attachments?: FileAttachment[]
  processDeadline?: string
  processingDeadline?: string
  handleDeadline?: string
  deadlineAt?: string
  dueAt?: string
  deadline?: string
  expireAt?: string
  processingDueAt?: string
  handleDueAt?: string
  isOverdue?: boolean
  overdue?: boolean
  timedOut?: boolean
  timeout?: boolean
  expired?: boolean
  remainingMillis?: number
  remainingMs?: number
  remainingSeconds?: number
  remainingMinutes?: number
  remainingTimeText?: string
  remainingDurationText?: string
  remainingText?: string
  remainText?: string
  remainLabel?: string
  deadlineStatus?: string
}

type AdminConversationMember = {
  userId: number
  nickname?: string
  phone?: string
  avatarUrl?: string
  role?: string
  status?: string
  unreadCount?: number
  joinedAt?: string
}

type AdminMessageReview = {
  id: number
  conversationId: number
  senderId?: number
  senderNickname?: string
  senderAvatarUrl?: string
  senderRole?: string
  type?: string
  content?: string
  createdAt?: string
}

type AdminErrandConversationReview = {
  orderId: number
  orderNo?: string
  orderStatus?: string
  conversationId: number
  conversationType?: string
  conversationTitle?: string
  publisher?: ErrandCounterparty
  runner?: ErrandCounterparty
  members?: AdminConversationMember[]
  messages?: AdminMessageReview[]
}

type ReportRow = {
  id: number
  module: string
  targetType: string
  targetId?: number
  submitterId?: number
  reporterUserId?: number
  reportType: string
  description: string
  contactPhone?: string
  status: string
  handleRemark?: string
  handledBy?: number
  handledAt?: string
  createdAt?: string
  processDeadline?: string
  processingDeadline?: string
  handleDeadline?: string
  deadlineAt?: string
  dueAt?: string
  deadline?: string
  expireAt?: string
  processingDueAt?: string
  handleDueAt?: string
  isOverdue?: boolean
  overdue?: boolean
  timedOut?: boolean
  timeout?: boolean
  expired?: boolean
  remainingMillis?: number
  remainingMs?: number
  remainingSeconds?: number
  remainingMinutes?: number
  remainingTimeText?: string
  remainingDurationText?: string
  remainingText?: string
  remainText?: string
  remainLabel?: string
  deadlineStatus?: string
}

type BeautyReview = {
  id?: string
  user?: string
  content?: string
}

type BeautyGood = {
  id: number
  category: string
  title: string
  price: number
  summary: string
  usageGuide?: string
  sceneText?: string
  evaluation?: string
  dormExperience?: string
  avoidanceGuide?: string
  creatorNickname?: string
  status: string
  sourceType?: string
  rejectReason?: string
  reviewedBy?: number
  reviewedAt?: string
  favoriteCount: number
  viewCount: number
  publishedAt?: string
  createdAt?: string
  skinTags?: string[]
  reviewList?: BeautyReview[]
  gallery?: string[]
  productImage?: FileAttachment
  receiptImage?: FileAttachment
}

type BeautyFormState = {
  id?: number
  category: string
  title: string
  price: number | null
  summary: string
  usageGuide: string
  sceneText: string
  evaluation: string
  dormExperience: string
  avoidanceGuide: string
  skinTagsText: string
  productImageFileId: number | null
  receiptFileId: number | null
  productImage: FileAttachment | null
  receiptImage: FileAttachment | null
}

type BeautyStats = {
  totalGoods: number
  pendingGoods: number
  approvedGoods: number
  rejectedGoods: number
  offlineGoods: number
  totalFavorites: number
  totalViews: number
  topViewedGoods?: Array<{
    id: number
    title: string
    category: string
    favoriteCount: number
    viewCount: number
    status: string
  }>
  topFavoritedGoods?: Array<{
    id: number
    title: string
    category: string
    favoriteCount: number
    viewCount: number
    status: string
  }>
  categoryBreakdown?: Array<{
    category: string
    count: number
  }>
}

type BeautyAppealStatus = 'PENDING' | 'PROCESSING' | 'RESOLVED' | 'REJECTED'

type BeautyAppeal = {
  id: number
  goodId: number
  goodTitle: string
  category: string
  issueType: string
  reason: string
  buyerName: string
  sellerName: string
  contactPhone: string
  status: BeautyAppealStatus
  proofSummary: string
  refundAmount: number
  createdAt: string
  handledAt?: string
  handleRemark?: string
}

type PartnerDemandStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'OFFLINE'
type PartnerConversationStatus = 'ACTIVE' | 'PENDING_REVIEW' | 'CLOSED'
type PartnerRiskLevel = 'LOW' | 'MEDIUM' | 'HIGH'

type PartnerDemand = {
  id: number
  title: string
  type: string
  location: string
  schedule: string
  publisherName: string
  publisherUserId: number
  publisherPhone: string
  description: string
  needTags: string[]
  status: PartnerDemandStatus
  baseRiskTags?: string[]
  riskTags: string[]
  reportCount: number
  applyCount: number
  latestMessageAt?: string
  createdAt: string
  reviewedAt?: string
  reviewRemark?: string
}

type PartnerReport = {
  id: number
  module: 'PARTNER'
  targetType: 'DEMAND'
  targetId: number
  targetTitle: string
  reportType: string
  description: string
  reporterName: string
  reporterUserId: number
  contactPhone: string
  status: 'PENDING' | 'PROCESSING' | 'RESOLVED' | 'REJECTED'
  conversationId?: string
  handleRemark?: string
  handledAt?: string
  createdAt: string
  deadlineAt?: string
  remainingMinutes?: number
  evidenceSummary?: string
}

type PartnerChatMessage = {
  id: string
  senderName: string
  senderRole: string
  type: 'TEXT' | 'IMAGE' | 'SYSTEM'
  content: string
  createdAt: string
}

type PartnerConversation = {
  id: string
  demandId: number
  demandTitle: string
  demandType: string
  counterpartyName: string
  counterpartyUserId: number
  counterpartyPhone: string
  status: PartnerConversationStatus
  riskLevel: PartnerRiskLevel
  baseRiskTags?: string[]
  riskTags: string[]
  unreadCount: number
  lastMessage: string
  lastMessageAt: string
  createdAt: string
  reviewRemark?: string
  messages: PartnerChatMessage[]
}

type JobsMerchantStatus = 'PENDING' | 'APPROVED' | 'REJECTED'
type JobsPostStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'OFFLINE' | 'BLOCKED'
type JobsReportStatus = 'PENDING' | 'RESOLVED' | 'CLOSED'
type JobsUserRole = 'STUDENT' | 'EMPLOYER'

type JobsMerchantQualification = {
  id: number
  applicantName: string
  businessName: string
  phone: string
  identityType: string
  area: string
  licenseImage: string
  status: JobsMerchantStatus
  submittedAt: string
  reviewedAt?: string
  reviewRemark?: string
}

type JobsPost = {
  id: number
  role: JobsUserRole
  publisherName: string
  publisherPhone: string
  title: string
  category: string
  workMode: 'ONLINE' | 'OFFLINE'
  distanceRange: '校内' | '1公里' | '2公里' | '3公里'
  actualDistanceKm: number
  area: string
  salary: number
  settlement: string
  schedule: string
  applicantCount: number
  status: JobsPostStatus
  riskTags: string[]
  description: string
  requirement: string
  createdAt: string
  reviewedAt?: string
  reviewRemark?: string
}

type JobsAccount = {
  id: number
  name: string
  role: JobsUserRole
  phone: string
  status: 'ACTIVE' | 'LIMITED' | 'BANNED'
  publishEnabled: boolean
  applyEnabled: boolean
  acceptEnabled: boolean
  reportCount: number
  disputeCount: number
  creditScore: number
  latestRemark?: string
  createdAt: string
}

type JobsReport = {
  id: number
  postId: number
  postTitle: string
  reportType: string
  reporterName: string
  targetName: string
  targetRole: JobsUserRole
  status: JobsReportStatus
  description: string
  handleRemark?: string
  publicResult?: string
  createdAt: string
  handledAt?: string
}

type JobsSkillTag = {
  id: number
  label: string
  category: string
  enabled: boolean
  usageCount: number
  updatedAt: string
}

type JobsStats = {
  totalPosts: number
  studentPosts: number
  employerPosts: number
  activeUsers: number
  totalApplicants: number
  salaryDisputes: number
  hotPosts: Array<{
    id: number
    title: string
    count: number
    role: JobsUserRole
  }>
  areaBreakdown: Array<{
    label: string
    count: number
  }>
  reportBreakdown: Array<{
    label: string
    count: number
  }>
  skillBreakdown: Array<{
    label: string
    count: number
  }>
}

type PartnerStats = {
  totalDemands: number
  pendingDemands: number
  approvedDemands: number
  offlineDemands: number
  totalReports: number
  pendingReports: number
  processingReports: number
  activeChats: number
  reviewChats: number
  highRiskChats: number
  totalApplications: number
  typeBreakdown: Array<{
    label: string
    count: number
  }>
  reportTypeBreakdown: Array<{
    label: string
    count: number
  }>
  riskBreakdown: Array<{
    label: string
    count: number
  }>
  hotDemands: Array<{
    id: number
    title: string
    type: string
    reportCount: number
    applyCount: number
  }>
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
  handleRemark?: string
  handledAt?: string
  processDeadline?: string
  processingDeadline?: string
  handleDeadline?: string
  deadlineAt?: string
  dueAt?: string
  deadline?: string
  expireAt?: string
  processingDueAt?: string
  handleDueAt?: string
  isOverdue?: boolean
  overdue?: boolean
  timedOut?: boolean
  timeout?: boolean
  expired?: boolean
  remainingMillis?: number
  remainingMs?: number
  remainingSeconds?: number
  remainingMinutes?: number
  remainingTimeText?: string
  remainingDurationText?: string
  remainingText?: string
  remainText?: string
  remainLabel?: string
  deadlineStatus?: string
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
  activeReportCount?: number
  overdueReportCount?: number
  riskTags?: string[]
}

type ErrandStatsItem = {
  label: string
  count: number
}

type ErrandStats = {
  totalOrders: number
  publicOrders: number
  lockedOrders: number
  archivedOrders: number
  acceptedOrders: number
  completedOrders: number
  cancelledOrders: number
  disputedOrders: number
  expiredOrders: number
  acceptanceRate: number
  completionRate: number
  cancelRate: number
  grossTransactionAmount: number
  activeTransactionAmount: number
  completedTransactionAmount: number
  reportTotal: number
  processingReports: number
  resolvedReports: number
  rejectedReports: number
  statusBreakdown: ErrandStatsItem[]
  reportTypeBreakdown: ErrandStatsItem[]
}

type ErrandRules = {
  urgentFee: number
  fragileFee: number
  publishLimitPerUser: number
  acceptLimitPerUser: number
  autoExpireHours: number
  minBaseFee: number
  maxBaseFee: number
}

type PagedResult<T> = {
  records: T[]
  total: number
  current: number
  size: number
}

type ReportHandleStatus = 'PROCESSING' | 'RESOLVED' | 'REJECTED'
type PunishStatus = 'NONE' | 'WARNED' | 'ERRAND_RESTRICTED' | 'REPORT_RESTRICTED' | 'ACTIVE'

type ReportHandleFormState = {
  id: number | null
  status: ReportHandleStatus
  remark: string
  punishStatus: PunishStatus
  punishRemark: string
  module: string
  reporterUserId: number | null
}

type ReportLike = ReportDetail | ReportRow | RelatedReport | PartnerReport | null | undefined

const router = useRouter()
const navigationGroups = [
  {
    title: '公共模块',
    items: [
      { key: 'overview', label: '平台概览', group: '公共模块', description: '汇总平台总体运行情况，并查看公共统计看板。' },
      { key: 'users', label: '用户治理', group: '公共模块', description: '处理用户权限、处罚与恢复操作。' },
      { key: 'verification', label: '实名审核', group: '公共模块', description: '审核实名资料，保证平台身份真实性。' },
      { key: 'reports', label: '举报工单', group: '公共模块', description: '统一受理全平台举报反馈与处理进度。' },
      { key: 'message', label: '系统消息', group: '公共模块', description: '向全体用户或指定用户发送系统通知。' },
    ],
  },
  {
    title: '校园跑腿',
    items: [
      { key: 'errand-stats', label: '数据统计', group: '校园跑腿', description: '查看订单发布量、接单率、完成率、违规工单量与收支流水统计。' },
      { key: 'errand-orders', label: '订单监控', group: '校园跑腿', description: '实时查看公共订单广场、已接单、已完成、已取消全量订单，并监控锁定、释放、归档状态。' },
      { key: 'errand-reports', label: '举报工单处理', group: '校园跑腿', description: '受理恶意下单、无故拒单、超时未履约、恶意加价等举报工单并处理结果。' },
      { key: 'errand-governance', label: '用户权限与处罚', group: '校园跑腿', description: '对跑腿模块违规用户执行警告、限制、封禁、拉黑与恢复操作。' },
      { key: 'errand-rules', label: '交易规则配置', group: '校园跑腿', description: '维护加急、易碎品增值费及发单、接单和费用核算规则。' },
    ],
  },
  {
    title: '找搭子',
    items: [
      { key: 'partner-content', label: '内容审核', group: '找搭子', description: '对找搭子广场的动态、招募内容和资料展示进行内容审核。' },
      { key: 'partner-reports', label: '举报治理', group: '找搭子', description: '统一处理找搭子模块中的虚假邀约、骚扰举报和违规反馈。' },
      { key: 'partner-chat', label: '聊天监管', group: '找搭子', description: '预留找搭子聊天监管入口，后续可接入聊天记录巡检与风险会话处置。' },
      { key: 'partner-stats', label: '活跃统计', group: '找搭子', description: '查看找搭子模块的发布活跃度、匹配量、举报量等核心数据。' },
    ],
  },
  {
    title: '美妆选购',
    items: [
      { key: 'beauty-review', label: '内容审核', group: '美妆选购', description: '审核种草测评、妆容推荐、美妆 OOTD，拦截违规、虚假和侵权内容。' },
      { key: 'beauty-governance', label: '商品管理', group: '美妆选购', description: '管理平价美妆与宿舍好物库，执行录入、分类、上下架及凭证风控。' },
      { key: 'beauty-reports', label: '售后仲裁', group: '美妆选购', description: '处理假货投诉、过敏、破损、描述不符等售后申请并给出仲裁结果。' },
      { key: 'beauty-stats', label: '数据统计', group: '美妆选购', description: '查看热门产品销量、浏览、收藏和售后类型分布等核心指标。' },
    ],
  },
  {
    title: '勤工助学',
    items: [
      { key: 'jobs-merchant', label: '用工资质审核', group: '勤工助学', description: '人工审核校外用工方营业执照照片，简化入驻，不做复杂地址和门店核验。' },
      { key: 'jobs-posts', label: '岗位发布管控', group: '勤工助学', description: '审核学生互助需求与商家兼职岗位，拦截超范围、虚假、中介和高危岗位。' },
      { key: 'jobs-accounts', label: '用户 / 用工方管理', group: '勤工助学', description: '统一管理学生和用工方账号，手动设置发岗、报名、接单权限并封禁违规主体。' },
      { key: 'jobs-rights', label: '举报工单管理', group: '勤工助学', description: '受理信息不符、薪资拖欠、强制加班等举报工单，并公示平台仲裁结果。' },
      { key: 'jobs-credit', label: '双向信用体系', group: '勤工助学', description: '管理员手动调整信用分，限制纠纷多的用工方和恶意举报、旷工学生。' },
      { key: 'jobs-stats', label: '数据统计', group: '勤工助学', description: '查看岗位数量、求职人数、热门岗位、区域分布与薪资纠纷统计。' },
      { key: 'jobs-skills', label: '技能标签库', group: '勤工助学', description: '维护文案、PPT、家教、新媒体、办公软件等技能标签基础数据。' },
    ],
  },
]
const errandStatusOptions = ['PUBLISHED', 'ACCEPTED', 'IN_PROGRESS', 'DELIVERING', 'COMPLETED', 'CANCELLED', 'DISPUTED', 'EXPIRED']
const errandServiceTypeOptions = ['PICKUP', 'DELIVERY', 'PURCHASE', 'PRINT']
const errandFlowStateOptions = ['PUBLIC', 'LOCKED', 'ARCHIVED']
const errandAlertTypeOptions = [
  { label: '争议中', value: 'DISPUTED' },
  { label: '临近接单超时', value: 'EXPIRING_ACCEPT' },
  { label: '超时未接单', value: 'EXPIRED_UNACCEPTED' },
  { label: '举报待处理', value: 'REPORT_PENDING' },
  { label: '举报已超时', value: 'REPORT_OVERDUE' },
]
const userRoleOptions = ['USER', 'ADMIN']
const userStatusOptions = ['ACTIVE', 'WARNED', 'ERRAND_RESTRICTED', 'REPORT_RESTRICTED', 'TEMP_BANNED', 'PERMANENT_BANNED', 'BLACKLISTED', 'DISABLED']
const reportStatusOptions = ['PENDING', 'PROCESSING', 'RESOLVED', 'REJECTED']
const reportModuleOptions = ['ERRAND', 'PARTNER', 'BEAUTY', 'JOBS']
const reportTypeOptions = ['恶意下单', '无故拒单', '超时未履约', '恶意加价', '物品损坏', '言语骚扰', '虚假信息', '恶意差评', '线下违规交易']
const pageSizeOptions = [10, 20, 50]
const paginationLayout = 'total, sizes, prev, pager, next'
const beautyCategoryOptions = [
  { label: '护肤彩妆', value: 'MAKEUP' },
  { label: '个护收纳', value: 'CARE' },
  { label: '配套好物', value: 'ACCESSORY' },
]
const beautyStatusOptions = ['PENDING', 'APPROVED', 'REJECTED', 'OFFLINE']
const beautyAppealTypeOptions = ['假货投诉', '过敏反馈', '商品破损', '描述不符']
const partnerTypeOptions = ['学习搭子', '运动搭子', '饭搭子', '出行搭子', '游戏搭子', '自律搭子']
const partnerDemandStatusOptions: PartnerDemandStatus[] = ['PENDING', 'APPROVED', 'REJECTED', 'OFFLINE']
const partnerConversationStatusOptions: PartnerConversationStatus[] = ['ACTIVE', 'PENDING_REVIEW', 'CLOSED']
const partnerRiskLevelOptions: PartnerRiskLevel[] = ['LOW', 'MEDIUM', 'HIGH']
const jobsMerchantStatusOptions: JobsMerchantStatus[] = ['PENDING', 'APPROVED', 'REJECTED']
const jobsPostStatusOptions: JobsPostStatus[] = ['PENDING', 'APPROVED', 'REJECTED', 'OFFLINE', 'BLOCKED']
const jobsAccountStatusOptions = ['ACTIVE', 'LIMITED', 'BANNED']
const jobsRoleOptions: JobsUserRole[] = ['STUDENT', 'EMPLOYER']
const jobsDistanceOptions = ['校内', '1公里', '2公里', '3公里']
const jobsReportStatusOptions: JobsReportStatus[] = ['PENDING', 'RESOLVED', 'CLOSED']
const jobsReportTypeOptions = ['信息不符', '薪资拖欠', '强制加班', '不合理安排', '恶意刁难']
const jobsSkillCategories = ['技能服务', '家教辅导', '办公支持', '新媒体运营']

const tab = ref('overview')
const expandedGroups = ref(
  Object.fromEntries(navigationGroups.map((group) => [group.title, group.title === navigationGroups[0]?.title])) as Record<string, boolean>
)
const allUsers = ref<UserRow[]>([])
const users = ref<UserRow[]>([])
const governanceUserRows = ref<UserRow[]>([])
const verifications = ref<any[]>([])
const allReports = ref<ReportRow[]>([])
const reports = ref<ReportRow[]>([])
const errandReportRows = ref<ReportRow[]>([])
const errandOrders = ref<ErrandOrder[]>([])
const beautyGoods = ref<BeautyGood[]>([])
const beautyAppeals = ref<BeautyAppeal[]>([])
const systemMessage = ref('系统通知：欢迎体验校园跑腿模块。')
const messageScope = ref<'all' | 'selected'>('all')
const selectedUserIds = ref<number[]>([])
const errandKeyword = ref('')
const errandStatus = ref('')
const errandServiceType = ref('')
const errandFlowState = ref('')
const errandAlertType = ref('')
const userKeyword = ref('')
const userRole = ref('')
const userStatus = ref('')
const governanceKeyword = ref('')
const governanceStatus = ref('')
const reportKeyword = ref('')
const reportModule = ref('')
const reportStatus = ref('')
const reportType = ref('')
const errandReportKeyword = ref('')
const errandReportStatus = ref('')
const errandReportType = ref('')
const beautyKeyword = ref('')
const beautyCategory = ref('')
const beautyStatus = ref('')
const beautyReviewKeyword = ref('')
const beautyReviewCategory = ref('')
const beautyReviewStatus = ref('')
const beautyAppealKeyword = ref('')
const beautyAppealStatus = ref('')
const beautyAppealType = ref('')
const errandOrderCurrent = ref(1)
const errandOrderPageSize = ref(10)
const errandOrderTotal = ref(0)
const errandReportCurrent = ref(1)
const errandReportPageSize = ref(10)
const errandReportTotal = ref(0)
const governanceCurrent = ref(1)
const governancePageSize = ref(10)
const governanceTotal = ref(0)
const errandRules = ref<ErrandRules>({
  urgentFee: 2,
  fragileFee: 3,
  publishLimitPerUser: 3,
  acceptLimitPerUser: 5,
  autoExpireHours: 4,
  minBaseFee: 1,
  maxBaseFee: 50,
})
const errandStats = ref<ErrandStats>({
  totalOrders: 0,
  publicOrders: 0,
  lockedOrders: 0,
  archivedOrders: 0,
  acceptedOrders: 0,
  completedOrders: 0,
  cancelledOrders: 0,
  disputedOrders: 0,
  expiredOrders: 0,
  acceptanceRate: 0,
  completionRate: 0,
  cancelRate: 0,
  grossTransactionAmount: 0,
  activeTransactionAmount: 0,
  completedTransactionAmount: 0,
  reportTotal: 0,
  processingReports: 0,
  resolvedReports: 0,
  rejectedReports: 0,
  statusBreakdown: [],
  reportTypeBreakdown: [],
})
const beautyStats = ref<BeautyStats>({
  totalGoods: 0,
  pendingGoods: 0,
  approvedGoods: 0,
  rejectedGoods: 0,
  offlineGoods: 0,
  totalFavorites: 0,
  totalViews: 0,
  topViewedGoods: [],
  topFavoritedGoods: [],
  categoryBreakdown: [],
})
const jobsStats = ref<JobsStats>({
  totalPosts: 0,
  studentPosts: 0,
  employerPosts: 0,
  activeUsers: 0,
  totalApplicants: 0,
  salaryDisputes: 0,
  hotPosts: [],
  areaBreakdown: [],
  reportBreakdown: [],
  skillBreakdown: [],
})
const partnerStats = ref<PartnerStats>({
  totalDemands: 0,
  pendingDemands: 0,
  approvedDemands: 0,
  offlineDemands: 0,
  totalReports: 0,
  pendingReports: 0,
  processingReports: 0,
  activeChats: 0,
  reviewChats: 0,
  highRiskChats: 0,
  totalApplications: 0,
  typeBreakdown: [],
  reportTypeBreakdown: [],
  riskBreakdown: [],
  hotDemands: [],
})
const partnerDemands = ref<PartnerDemand[]>([])
const partnerReports = ref<PartnerReport[]>([])
const partnerConversations = ref<PartnerConversation[]>([])
const jobsMerchants = ref<JobsMerchantQualification[]>([])
const jobsPosts = ref<JobsPost[]>([])
const jobsAccounts = ref<JobsAccount[]>([])
const jobsReports = ref<JobsReport[]>([])
const jobsSkills = ref<JobsSkillTag[]>([])

const userDrawerVisible = ref(false)
const verificationDrawerVisible = ref(false)
const reportDrawerVisible = ref(false)
const errandDrawerVisible = ref(false)
const beautyDrawerVisible = ref(false)
const beautyAppealDrawerVisible = ref(false)
const partnerDemandDrawerVisible = ref(false)
const partnerReportDrawerVisible = ref(false)
const partnerChatDrawerVisible = ref(false)
const jobsMerchantDrawerVisible = ref(false)
const jobsPostDrawerVisible = ref(false)
const jobsReportDrawerVisible = ref(false)
const jobsAccountDrawerVisible = ref(false)
const beautyEditorVisible = ref(false)
const beautyEditorMode = ref<'create' | 'edit'>('create')
const beautySubmitting = ref(false)
const reportHandleDialogVisible = ref(false)
const reportHandling = ref(false)

const userDetail = ref<AdminUserDetail | null>(null)
const verificationDetail = ref<VerificationDetail | null>(null)
const reportDetail = ref<ReportDetail | null>(null)
const errandDetail = ref<ErrandOrder | null>(null)
const beautyDetail = ref<BeautyGood | null>(null)
const beautyAppealDetail = ref<BeautyAppeal | null>(null)
const partnerDemandDetail = ref<PartnerDemand | null>(null)
const partnerReportDetail = ref<PartnerReport | null>(null)
const partnerChatDetail = ref<PartnerConversation | null>(null)
const jobsMerchantDetail = ref<JobsMerchantQualification | null>(null)
const jobsPostDetail = ref<JobsPost | null>(null)
const jobsReportDetail = ref<JobsReport | null>(null)
const jobsAccountDetail = ref<JobsAccount | null>(null)
const errandConversationReview = ref<AdminErrandConversationReview | null>(null)
const errandConversationLoading = ref(false)
const errandConversationError = ref('')
const reportClock = ref(Date.now())
const reportHandleForm = ref<ReportHandleFormState>({
  id: null,
  status: 'PROCESSING',
  remark: '',
  punishStatus: 'NONE',
  punishRemark: '',
  module: '',
  reporterUserId: null,
})

const createEmptyBeautyForm = (): BeautyFormState => ({
  category: '',
  title: '',
  price: null,
  summary: '',
  usageGuide: '',
  sceneText: '',
  evaluation: '',
  dormExperience: '',
  avoidanceGuide: '',
  skinTagsText: '',
  productImageFileId: null,
  receiptFileId: null,
  productImage: null,
  receiptImage: null,
})

const beautyForm = ref<BeautyFormState>(createEmptyBeautyForm())
const partnerContentKeyword = ref('')
const partnerContentType = ref('')
const partnerContentStatus = ref('')
const partnerContentRisk = ref('')
const partnerReportKeyword = ref('')
const partnerReportStatus = ref('')
const partnerReportType = ref('')
const partnerChatKeyword = ref('')
const partnerChatStatus = ref('')
const partnerChatRisk = ref('')
const jobsMerchantKeyword = ref('')
const jobsMerchantStatus = ref('')
const jobsPostKeyword = ref('')
const jobsPostStatus = ref('')
const jobsPostRole = ref('')
const jobsPostDistance = ref('')
const jobsAccountKeyword = ref('')
const jobsAccountRole = ref('')
const jobsAccountStatus = ref('')
const jobsReportKeyword = ref('')
const jobsReportStatus = ref('')
const jobsReportType = ref('')
const jobsSkillKeyword = ref('')
const jobsSkillCategory = ref('')
const currentTabMeta = computed(() => {
  const allItems = navigationGroups.flatMap((group) => group.items)
  return allItems.find((item) => item.key === tab.value) || allItems[0]
})
const currentGroupTitle = computed(() => currentTabMeta.value.group)
const beautyEditorTip = computed(() =>
  beautyFormValid.value ? '商品信息完整，可直接保存到美妆好物列表。' : '请完善标题、品类、价格、简介和两张图片后再保存。'
)
const beautyFormValid = computed(() => {
  return Boolean(
    beautyForm.value.title.trim() &&
      beautyForm.value.category &&
      beautyForm.value.price &&
      Number(beautyForm.value.price) > 0 &&
      beautyForm.value.summary.trim() &&
      beautyForm.value.productImageFileId &&
      beautyForm.value.receiptFileId
  )
})

const currentErrandAlertSummary = computed(() => ({
  disputed: errandOrders.value.filter((item) => hasErrandRiskTag(item, '争议中')).length,
  expiringAccept: errandOrders.value.filter((item) => hasErrandRiskTag(item, '临近接单超时')).length,
  expiredUnaccepted: errandOrders.value.filter((item) => hasErrandRiskTag(item, '超时未接单')).length,
  reportPending: errandOrders.value.filter((item) => hasErrandRiskTag(item, '举报待处理')).length,
  reportOverdue: errandOrders.value.filter((item) => hasErrandRiskTag(item, '举报已超时')).length,
}))

const getBeautyRiskTags = (good: BeautyGood) => {
  const tags: string[] = []
  if (!good.receiptImage?.url) {
    tags.push('缺少订单凭证')
  }
  if (!good.productImage?.url) {
    tags.push('缺少实拍图')
  }
  if (!good.evaluation || good.evaluation.trim().length < 20) {
    tags.push('测评信息偏少')
  }
  if (String(good.status || '').toUpperCase() === 'PENDING') {
    tags.push('待内容审核')
  }
  if (String(good.status || '').toUpperCase() === 'OFFLINE') {
    tags.push('已下架观察')
  }
  if (String(good.status || '').toUpperCase() === 'REJECTED') {
    tags.push('已驳回')
  }
  return tags
}

const beautyReviewRows = computed(() =>
  [...beautyGoods.value]
    .filter((item) => {
      const keyword = beautyReviewKeyword.value.trim().toLowerCase()
      if (
        keyword &&
        !`${item.title} ${item.summary} ${item.evaluation || ''} ${item.sceneText || ''} ${item.creatorNickname || ''}`
          .toLowerCase()
          .includes(keyword)
      ) {
        return false
      }
      if (beautyReviewCategory.value && item.category !== beautyReviewCategory.value) {
        return false
      }
      if (beautyReviewStatus.value && item.status !== beautyReviewStatus.value) {
        return false
      }
      return true
    })
    .sort((left, right) => new Date(right.createdAt || 0).getTime() - new Date(left.createdAt || 0).getTime())
)

const beautyGovernanceRows = computed(() =>
  [...beautyGoods.value]
    .filter((item) => {
      const keyword = beautyKeyword.value.trim().toLowerCase()
      if (
        keyword &&
        !`${item.title} ${item.summary} ${item.creatorNickname || ''} ${item.sourceType || ''}`.toLowerCase().includes(keyword)
      ) {
        return false
      }
      if (beautyCategory.value && item.category !== beautyCategory.value) {
        return false
      }
      if (beautyStatus.value && item.status !== beautyStatus.value) {
        return false
      }
      return true
    })
    .sort((left, right) => new Date(right.createdAt || 0).getTime() - new Date(left.createdAt || 0).getTime())
)

const beautyAppealRows = computed(() =>
  [...beautyAppeals.value]
    .filter((item) => {
      const keyword = beautyAppealKeyword.value.trim().toLowerCase()
      if (
        keyword &&
        !`${item.goodTitle} ${item.reason} ${item.buyerName} ${item.sellerName} ${item.contactPhone}`.toLowerCase().includes(keyword)
      ) {
        return false
      }
      if (beautyAppealStatus.value && item.status !== beautyAppealStatus.value) {
        return false
      }
      if (beautyAppealType.value && item.issueType !== beautyAppealType.value) {
        return false
      }
      return true
    })
    .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())
)

const beautySalesRanking = computed(() =>
  [...beautyGoods.value]
    .map((item) => ({
      id: item.id,
      title: item.title,
      category: item.category,
      sales: Math.max(1, Number(item.favoriteCount || 0) * 3 + Math.floor(Number(item.viewCount || 0) / 20) + (item.status === 'APPROVED' ? 6 : 0)),
      viewCount: Number(item.viewCount || 0),
      favoriteCount: Number(item.favoriteCount || 0),
    }))
    .sort((left, right) => right.sales - left.sales || right.viewCount - left.viewCount)
    .slice(0, 5)
)

const beautyAppealBreakdown = computed(() =>
  beautyAppealTypeOptions
    .map((label) => ({
      label,
      count: beautyAppeals.value.filter((item) => item.issueType === label).length,
    }))
    .filter((item) => item.count > 0)
)

const beautyAppealSummary = computed(() => ({
  total: beautyAppeals.value.length,
  pending: beautyAppeals.value.filter((item) => item.status === 'PENDING').length,
  processing: beautyAppeals.value.filter((item) => item.status === 'PROCESSING').length,
  resolved: beautyAppeals.value.filter((item) => item.status === 'RESOLVED').length,
}))

const beautyProofSummary = computed(() => ({
  fullProof: beautyGoods.value.filter((item) => item.productImage?.url && item.receiptImage?.url).length,
  missingReceipt: beautyGoods.value.filter((item) => !item.receiptImage?.url).length,
  missingProductImage: beautyGoods.value.filter((item) => !item.productImage?.url).length,
}))

const getUniqueTags = (items: Array<string | undefined | null>) => Array.from(new Set(items.filter(Boolean) as string[]))

const partnerContentRows = computed(() =>
  [...partnerDemands.value]
    .filter((item) => {
      const keyword = partnerContentKeyword.value.trim()
      if (
        keyword &&
        !`${item.title} ${item.location} ${item.publisherName} ${item.description} ${(item.needTags || []).join(' ')}`
          .toLowerCase()
          .includes(keyword.toLowerCase())
      ) {
        return false
      }
      if (partnerContentType.value && item.type !== partnerContentType.value) {
        return false
      }
      if (partnerContentStatus.value && item.status !== partnerContentStatus.value) {
        return false
      }
      if (partnerContentRisk.value && !item.riskTags.includes(partnerContentRisk.value)) {
        return false
      }
      return true
    })
    .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())
)

const partnerReportRows = computed(() =>
  [...partnerReports.value]
    .filter((item) => {
      const keyword = partnerReportKeyword.value.trim()
      if (
        keyword &&
        !`${item.targetTitle} ${item.reportType} ${item.description} ${item.reporterName} ${item.contactPhone}`
          .toLowerCase()
          .includes(keyword.toLowerCase())
      ) {
        return false
      }
      if (partnerReportStatus.value && item.status !== partnerReportStatus.value) {
        return false
      }
      if (partnerReportType.value && item.reportType !== partnerReportType.value) {
        return false
      }
      return true
    })
    .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())
)

const partnerChatRows = computed(() =>
  [...partnerConversations.value]
    .filter((item) => {
      const keyword = partnerChatKeyword.value.trim()
      if (
        keyword &&
        !`${item.demandTitle} ${item.counterpartyName} ${item.lastMessage} ${item.counterpartyPhone}`
          .toLowerCase()
          .includes(keyword.toLowerCase())
      ) {
        return false
      }
      if (partnerChatStatus.value && item.status !== partnerChatStatus.value) {
        return false
      }
      if (partnerChatRisk.value && item.riskLevel !== partnerChatRisk.value) {
        return false
      }
      return true
    })
    .sort((left, right) => new Date(right.lastMessageAt).getTime() - new Date(left.lastMessageAt).getTime())
)

const currentPartnerDemandReports = computed(() =>
  partnerDemandDetail.value ? partnerReports.value.filter((item) => item.targetId === partnerDemandDetail.value?.id) : []
)

const currentPartnerDemandConversations = computed(() =>
  partnerDemandDetail.value ? partnerConversations.value.filter((item) => item.demandId === partnerDemandDetail.value?.id) : []
)

const refreshOpenPartnerDetails = async () => {
  if (partnerDemandDrawerVisible.value && partnerDemandDetail.value?.id) {
    try {
      partnerDemandDetail.value = (await api.get(`/admin/partner/demands/${partnerDemandDetail.value.id}`)) as PartnerDemand
    } catch {
      partnerDemandDetail.value = null
      partnerDemandDrawerVisible.value = false
    }
  }
  if (partnerReportDrawerVisible.value && partnerReportDetail.value?.id) {
    try {
      partnerReportDetail.value = (await api.get(`/admin/partner/reports/${partnerReportDetail.value.id}`)) as PartnerReport
    } catch {
      partnerReportDetail.value = null
      partnerReportDrawerVisible.value = false
    }
  }
  if (partnerChatDrawerVisible.value && partnerChatDetail.value?.id) {
    try {
      partnerChatDetail.value = (await api.get(`/admin/partner/conversations/${partnerChatDetail.value.id}`)) as PartnerConversation
    } catch {
      partnerChatDetail.value = null
      partnerChatDrawerVisible.value = false
    }
  }
}

const refreshOpenJobsDetails = async () => {
  if (jobsMerchantDrawerVisible.value && jobsMerchantDetail.value?.id) {
    try {
      jobsMerchantDetail.value = (await api.get(`/admin/jobs/merchants/${jobsMerchantDetail.value.id}`)) as JobsMerchantQualification
    } catch {
      jobsMerchantDetail.value = null
      jobsMerchantDrawerVisible.value = false
    }
  }
  if (jobsPostDrawerVisible.value && jobsPostDetail.value?.id) {
    try {
      jobsPostDetail.value = (await api.get(`/admin/jobs/posts/${jobsPostDetail.value.id}`)) as JobsPost
    } catch {
      jobsPostDetail.value = null
      jobsPostDrawerVisible.value = false
    }
  }
  if (jobsReportDrawerVisible.value && jobsReportDetail.value?.id) {
    try {
      jobsReportDetail.value = (await api.get(`/admin/jobs/reports/${jobsReportDetail.value.id}`)) as JobsReport
    } catch {
      jobsReportDetail.value = null
      jobsReportDrawerVisible.value = false
    }
  }
  if (jobsAccountDrawerVisible.value && jobsAccountDetail.value?.id) {
    try {
      jobsAccountDetail.value = (await api.get(`/admin/jobs/accounts/${jobsAccountDetail.value.id}`)) as JobsAccount
    } catch {
      jobsAccountDetail.value = null
      jobsAccountDrawerVisible.value = false
    }
  }
}

const jobsMerchantRows = computed(() =>
  [...jobsMerchants.value]
    .filter((item) => {
      const keyword = jobsMerchantKeyword.value.trim().toLowerCase()
      if (keyword && !`${item.applicantName} ${item.businessName} ${item.phone} ${item.identityType} ${item.area}`.toLowerCase().includes(keyword)) {
        return false
      }
      if (jobsMerchantStatus.value && item.status !== jobsMerchantStatus.value) {
        return false
      }
      return true
    })
    .sort((left, right) => new Date(right.submittedAt).getTime() - new Date(left.submittedAt).getTime())
)

const jobsPostRows = computed(() =>
  [...jobsPosts.value]
    .filter((item) => {
      const keyword = jobsPostKeyword.value.trim().toLowerCase()
      if (keyword && !`${item.title} ${item.category} ${item.publisherName} ${item.area} ${item.description} ${item.requirement}`.toLowerCase().includes(keyword)) {
        return false
      }
      if (jobsPostStatus.value && item.status !== jobsPostStatus.value) {
        return false
      }
      if (jobsPostRole.value && item.role !== jobsPostRole.value) {
        return false
      }
      if (jobsPostDistance.value && item.distanceRange !== jobsPostDistance.value) {
        return false
      }
      return true
    })
    .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())
)

const jobsAccountRows = computed(() =>
  [...jobsAccounts.value]
    .filter((item) => {
      const keyword = jobsAccountKeyword.value.trim().toLowerCase()
      if (keyword && !`${item.name} ${item.phone}`.toLowerCase().includes(keyword)) {
        return false
      }
      if (jobsAccountRole.value && item.role !== jobsAccountRole.value) {
        return false
      }
      if (jobsAccountStatus.value && item.status !== jobsAccountStatus.value) {
        return false
      }
      return true
    })
    .sort((left, right) => right.creditScore - left.creditScore || new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())
)

const jobsReportRows = computed(() =>
  [...jobsReports.value]
    .filter((item) => {
      const keyword = jobsReportKeyword.value.trim().toLowerCase()
      if (keyword && !`${item.postTitle} ${item.reporterName} ${item.targetName} ${item.description}`.toLowerCase().includes(keyword)) {
        return false
      }
      if (jobsReportStatus.value && item.status !== jobsReportStatus.value) {
        return false
      }
      if (jobsReportType.value && item.reportType !== jobsReportType.value) {
        return false
      }
      return true
    })
    .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())
)

const jobsSkillRows = computed(() =>
  [...jobsSkills.value]
    .filter((item) => {
      const keyword = jobsSkillKeyword.value.trim().toLowerCase()
      if (keyword && !`${item.label} ${item.category}`.toLowerCase().includes(keyword)) {
        return false
      }
      if (jobsSkillCategory.value && item.category !== jobsSkillCategory.value) {
        return false
      }
      return true
    })
    .sort((left, right) => Number(right.enabled) - Number(left.enabled) || right.usageCount - left.usageCount)
)

const jobsPostRiskSummary = computed(() => ({
  outOfRange: jobsPosts.value.filter((item) => item.riskTags.some((tag) => tag.includes('超出固定区间'))).length,
  agencyRisk: jobsPosts.value.filter((item) => item.riskTags.some((tag) => tag.includes('中介'))).length,
  deductionRisk: jobsPosts.value.filter((item) => item.riskTags.some((tag) => tag.includes('扣费'))).length,
  pendingReview: jobsPosts.value.filter((item) => item.status === 'PENDING').length,
}))

const jobsCreditWatchlist = computed(() =>
  [...jobsAccounts.value]
    .filter((item) => item.creditScore <= 80 || item.disputeCount >= 2 || item.reportCount >= 2)
    .sort((left, right) => left.creditScore - right.creditScore || right.disputeCount - left.disputeCount)
)

const currentJobsPostReports = computed(() =>
  jobsPostDetail.value ? jobsReports.value.filter((item) => item.postId === jobsPostDetail.value?.id) : []
)

const currentJobsAccountPosts = computed(() =>
  jobsAccountDetail.value
    ? jobsPosts.value.filter((item) => item.publisherName === jobsAccountDetail.value?.name && item.role === jobsAccountDetail.value?.role)
    : []
)

const currentJobsAccountReports = computed(() =>
  jobsAccountDetail.value
    ? jobsReports.value.filter((item) => item.targetName === jobsAccountDetail.value?.name || item.reporterName === jobsAccountDetail.value?.name)
    : []
)

const toPagedResult = <T>(payload: any, fallbackCurrent: number, fallbackSize: number): PagedResult<T> => {
  if (Array.isArray(payload)) {
    return {
      records: payload,
      total: payload.length,
      current: fallbackCurrent,
      size: fallbackSize,
    }
  }
  if (Array.isArray(payload?.list)) {
    return {
      records: payload.list,
      total: Number(payload?.total ?? payload.list.length),
      current: Number(payload?.current ?? fallbackCurrent),
      size: Number(payload?.size ?? fallbackSize),
    }
  }
  if (Array.isArray(payload?.items)) {
    return {
      records: payload.items,
      total: Number(payload?.total ?? payload.items.length),
      current: Number(payload?.current ?? fallbackCurrent),
      size: Number(payload?.size ?? fallbackSize),
    }
  }
  return {
    records: Array.isArray(payload?.records) ? payload.records : [],
    total: Number(payload?.total ?? 0),
    current: Number(payload?.current ?? fallbackCurrent),
    size: Number(payload?.size ?? fallbackSize),
  }
}

const toArrayResult = <T>(payload: any): T[] => {
  if (Array.isArray(payload)) {
    return payload as T[]
  }
  if (Array.isArray(payload?.records)) {
    return payload.records as T[]
  }
  if (Array.isArray(payload?.list)) {
    return payload.list as T[]
  }
  if (Array.isArray(payload?.items)) {
    return payload.items as T[]
  }
  if (Array.isArray(payload?.content)) {
    return payload.content as T[]
  }
  return []
}

const sortGovernanceUsers = (list: UserRow[]) =>
  [...list].sort((left, right) => {
    if (left.status === right.status) {
      return right.id - left.id
    }
    if (left.status === 'ACTIVE') {
      return 1
    }
    if (right.status === 'ACTIVE') {
      return -1
    }
    return 0
  })

const paginateLocal = <T>(items: T[], current: number, size: number): PagedResult<T> => {
  const safeCurrent = Math.max(1, current)
  const safeSize = Math.max(1, size)
  const start = (safeCurrent - 1) * safeSize
  return {
    records: items.slice(start, start + safeSize),
    total: items.length,
    current: safeCurrent,
    size: safeSize,
  }
}

const loadUsers = async () => {
  users.value = (await api.get('/admin/users', {
    params: {
      keyword: userKeyword.value,
      role: userRole.value,
      status: userStatus.value,
    },
  })) as UserRow[]
}

const loadGovernanceUsers = async () => {
  const payload = await api.get('/admin/users', {
    params: {
      keyword: governanceKeyword.value,
      role: 'USER',
      status: governanceStatus.value,
      current: governanceCurrent.value,
      size: governancePageSize.value,
    },
  })
  if (Array.isArray(payload)) {
    const paged = paginateLocal(sortGovernanceUsers(payload as UserRow[]), governanceCurrent.value, governancePageSize.value)
    governanceUserRows.value = paged.records
    governanceTotal.value = paged.total
    return
  }
  const paged = toPagedResult<UserRow>(payload, governanceCurrent.value, governancePageSize.value)
  governanceUserRows.value = paged.records
  governanceTotal.value = paged.total
}

const loadReports = async () => {
  const data = (await api.get('/admin/reports', {
    params: {
      current: 1,
      size: 100,
      keyword: reportKeyword.value,
      module: reportModule.value,
      status: reportStatus.value,
      reportType: reportType.value,
    },
  })) as any
  reports.value = data.records || []
}

const loadErrandReports = async () => {
  const payload = await api.get('/admin/reports', {
    params: {
      current: errandReportCurrent.value,
      size: errandReportPageSize.value,
      module: 'ERRAND',
      keyword: errandReportKeyword.value,
      status: errandReportStatus.value,
      reportType: errandReportType.value,
    },
  })
  const data = toPagedResult<ReportRow>(payload, errandReportCurrent.value, errandReportPageSize.value)
  errandReportRows.value = [...data.records].sort((left: ReportRow, right: ReportRow) => (right.id || 0) - (left.id || 0))
  errandReportTotal.value = data.total
}

const loadAll = async () => {
  allUsers.value = (await api.get('/admin/users')) as UserRow[]
  users.value = [...allUsers.value]
  verifications.value = ((await api.get('/admin/verifications')) as any).records || []
  allReports.value = ((await api.get('/admin/reports', {
    params: { current: 1, size: 100 },
  })) as any).records || []
  reports.value = [...allReports.value]
  await Promise.all([
    loadGovernanceUsers(),
    loadErrandOrders(),
    loadErrandReports(),
    loadErrandRules(),
    loadErrandStats(),
    loadPartnerAdmin(),
    loadBeautyGoods(),
    loadBeautyAppeals(),
    loadBeautyStats(),
    loadJobsAdmin(),
  ])
}

const loadErrandOrders = async () => {
  const payload = await api.get('/admin/errand/orders', {
    params: {
      keyword: errandKeyword.value,
      status: errandStatus.value,
      serviceType: errandServiceType.value,
      flowState: errandFlowState.value,
      alertType: errandAlertType.value,
      current: errandOrderCurrent.value,
      size: errandOrderPageSize.value,
    },
  })
  const data = toPagedResult<ErrandOrder>(payload, errandOrderCurrent.value, errandOrderPageSize.value)
  errandOrders.value = data.records
  errandOrderTotal.value = data.total
}

const loadErrandRules = async () => {
  errandRules.value = (await api.get('/admin/errand/rules')) as typeof errandRules.value
}

const loadErrandStats = async () => {
  errandStats.value = (await api.get('/admin/errand/stats')) as ErrandStats
}

const loadErrandConversationReview = async (orderId: number) => {
  errandConversationLoading.value = true
  errandConversationError.value = ''
  try {
    errandConversationReview.value = (await api.get(`/admin/errand/orders/${orderId}/conversation`)) as AdminErrandConversationReview
  } catch (error: any) {
    errandConversationReview.value = null
    errandConversationError.value = error?.message || '聊天核查数据加载失败'
  } finally {
    errandConversationLoading.value = false
  }
}

const loadBeautyGoods = async () => {
  const payload = await api.get('/admin/beauty/goods', {
    params: {
      current: 1,
      size: 200,
    },
  })
  beautyGoods.value = toPagedResult<BeautyGood>(payload, 1, 200).records
}

const loadBeautyAppeals = async () => {
  const payload = await api.get('/admin/beauty/appeals', {
    params: {
      current: 1,
      size: 200,
    },
  })
  beautyAppeals.value = toArrayResult<BeautyAppeal>(payload)
}

const loadBeautyStats = async () => {
  beautyStats.value = (await api.get('/admin/beauty/stats')) as BeautyStats
}

const loadPartnerDemands = async () => {
  const payload = await api.get('/admin/partner/demands', {
    params: {
      current: 1,
      size: 200,
    },
  })
  partnerDemands.value = toArrayResult<PartnerDemand>(payload)
}

const loadPartnerReports = async () => {
  const payload = await api.get('/admin/partner/reports', {
    params: {
      current: 1,
      size: 200,
    },
  })
  partnerReports.value = toArrayResult<PartnerReport>(payload)
}

const loadPartnerConversations = async () => {
  const payload = await api.get('/admin/partner/conversations', {
    params: {
      current: 1,
      size: 200,
    },
  })
  partnerConversations.value = toArrayResult<PartnerConversation>(payload)
}

const loadPartnerStats = async () => {
  partnerStats.value = (await api.get('/admin/partner/stats')) as PartnerStats
}

const loadPartnerAdmin = async () => {
  await Promise.all([loadPartnerDemands(), loadPartnerReports(), loadPartnerConversations(), loadPartnerStats()])
}

const loadJobsMerchants = async () => {
  const payload = await api.get('/admin/jobs/merchants', {
    params: {
      current: 1,
      size: 200,
    },
  })
  jobsMerchants.value = toArrayResult<JobsMerchantQualification>(payload)
}

const loadJobsPosts = async () => {
  const payload = await api.get('/admin/jobs/posts', {
    params: {
      current: 1,
      size: 200,
    },
  })
  jobsPosts.value = toArrayResult<JobsPost>(payload)
}

const loadJobsAccounts = async () => {
  const payload = await api.get('/admin/jobs/accounts', {
    params: {
      current: 1,
      size: 200,
    },
  })
  jobsAccounts.value = toArrayResult<JobsAccount>(payload)
}

const loadJobsReports = async () => {
  const payload = await api.get('/admin/jobs/reports', {
    params: {
      current: 1,
      size: 200,
    },
  })
  jobsReports.value = toArrayResult<JobsReport>(payload)
}

const loadJobsSkills = async () => {
  const payload = await api.get('/admin/jobs/skills', {
    params: {
      current: 1,
      size: 200,
    },
  })
  jobsSkills.value = toArrayResult<JobsSkillTag>(payload)
}

const loadJobsStats = async () => {
  jobsStats.value = (await api.get('/admin/jobs/stats')) as JobsStats
}

const loadJobsAdmin = async () => {
  await Promise.all([loadJobsMerchants(), loadJobsPosts(), loadJobsAccounts(), loadJobsReports(), loadJobsSkills(), loadJobsStats()])
}

const openBeautyCreate = () => {
  beautyEditorMode.value = 'create'
  beautyDrawerVisible.value = false
  beautyForm.value = createEmptyBeautyForm()
  beautyEditorVisible.value = true
}

const openBeautyDetail = async (row: { id: number } | BeautyGood | null) => {
  if (!row?.id) {
    return
  }
  beautyDetail.value = (await api.get(`/admin/beauty/goods/${row.id}`)) as BeautyGood
  beautyDrawerVisible.value = true
}

const openBeautyAppealDetail = async (row: BeautyAppeal) => {
  beautyAppealDetail.value = null
  beautyAppealDrawerVisible.value = true
  try {
    beautyAppealDetail.value = (await api.get(`/admin/beauty/appeals/${row.id}`)) as BeautyAppeal
  } catch (error) {
    beautyAppealDrawerVisible.value = false
    throw error
  }
}

const openBeautyFromAppeal = async () => {
  if (!beautyAppealDetail.value?.goodId) {
    ElMessage.warning('当前售后工单未关联商品')
    return
  }
  await openBeautyDetail({ id: beautyAppealDetail.value.goodId })
}

const refreshBeautyAdmin = async () => {
  await Promise.all([loadBeautyGoods(), loadBeautyAppeals(), loadBeautyStats()])
  if (beautyDetail.value?.id) {
    beautyDetail.value = (await api.get(`/admin/beauty/goods/${beautyDetail.value.id}`)) as BeautyGood
  }
  if (beautyAppealDetail.value?.id) {
    try {
      beautyAppealDetail.value = (await api.get(`/admin/beauty/appeals/${beautyAppealDetail.value.id}`)) as BeautyAppeal
    } catch {
      beautyAppealDetail.value = null
      beautyAppealDrawerVisible.value = false
    }
  }
  ElMessage.success('美妆管理数据已刷新')
}

const openJobsMerchantDetail = async (row: JobsMerchantQualification) => {
  jobsMerchantDetail.value = null
  jobsMerchantDrawerVisible.value = true
  try {
    jobsMerchantDetail.value = (await api.get(`/admin/jobs/merchants/${row.id}`)) as JobsMerchantQualification
  } catch (error) {
    jobsMerchantDrawerVisible.value = false
    throw error
  }
}

const openJobsPostDetail = async (row: JobsPost) => {
  jobsPostDetail.value = null
  jobsPostDrawerVisible.value = true
  try {
    jobsPostDetail.value = (await api.get(`/admin/jobs/posts/${row.id}`)) as JobsPost
  } catch (error) {
    jobsPostDrawerVisible.value = false
    throw error
  }
}

const openJobsAccountDetail = async (row: JobsAccount) => {
  jobsAccountDetail.value = null
  jobsAccountDrawerVisible.value = true
  try {
    jobsAccountDetail.value = (await api.get(`/admin/jobs/accounts/${row.id}`)) as JobsAccount
  } catch (error) {
    jobsAccountDrawerVisible.value = false
    throw error
  }
}

const openJobsReportDetail = async (row: JobsReport) => {
  jobsReportDetail.value = null
  jobsReportDrawerVisible.value = true
  try {
    jobsReportDetail.value = (await api.get(`/admin/jobs/reports/${row.id}`)) as JobsReport
  } catch (error) {
    jobsReportDrawerVisible.value = false
    throw error
  }
}

const openJobsPostFromReport = () => {
  if (!jobsReportDetail.value) {
    return
  }
  const target = jobsPosts.value.find((item) => item.id === jobsReportDetail.value?.postId)
  if (!target) {
    ElMessage.warning('当前工单未关联有效岗位')
    return
  }
  openJobsPostDetail(target)
}

const refreshJobsAdmin = async () => {
  await loadJobsAdmin()
  await refreshOpenJobsDetails()
  ElMessage.success('勤工助学管理数据已刷新')
}

const handleJobsMerchantReview = async (row: JobsMerchantQualification, status: JobsMerchantStatus) => {
  const labelMap: Record<JobsMerchantStatus, string> = {
    PENDING: '待处理',
    APPROVED: '通过',
    REJECTED: '驳回',
  }
  const defaultRemarkMap: Record<JobsMerchantStatus, string> = {
    PENDING: '保持待审核状态',
    APPROVED: '资质照片清晰，允许发布校外兼职岗位。',
    REJECTED: '资质信息不清晰，请补充营业执照照片后重提。',
  }
  const remark = await ElMessageBox.prompt(`请输入${labelMap[status]}说明`, '用工资质审核', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: defaultRemarkMap[status],
    inputPlaceholder: '请输入审核说明',
  }).then(({ value }) => value || defaultRemarkMap[status])

  await api.post(`/admin/jobs/merchants/${row.id}/review`, {
    status,
    remark,
  })
  await loadJobsAdmin()
  if (jobsMerchantDrawerVisible.value && jobsMerchantDetail.value?.id === row.id) {
    jobsMerchantDetail.value = (await api.get(`/admin/jobs/merchants/${row.id}`)) as JobsMerchantQualification
  }
  ElMessage.success(`资质审核已${labelMap[status]}`)
}

const handleJobsPostAction = async (row: JobsPost, action: 'APPROVE' | 'REJECT' | 'OFFLINE' | 'RESTORE' | 'BLOCK') => {
  const labelMap = {
    APPROVE: '通过',
    REJECT: '驳回',
    OFFLINE: '下架',
    RESTORE: '恢复',
    BLOCK: '拦截',
  }
  const defaultRemarkMap = {
    APPROVE: '岗位信息完整且在固定范围内，允许展示。',
    REJECT: '岗位信息存在虚假或不合规描述，已驳回。',
    OFFLINE: '岗位存在争议，先下架等待复核。',
    RESTORE: '岗位复核通过，恢复展示。',
    BLOCK: '岗位命中超范围或高风险规则，已拦截。',
  }
  const remark = await ElMessageBox.prompt(`请输入${labelMap[action]}说明`, '岗位发布管控', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: defaultRemarkMap[action],
    inputPlaceholder: '请输入处理说明',
  }).then(({ value }) => value || defaultRemarkMap[action])

  await api.post(`/admin/jobs/posts/${row.id}/action`, {
    action,
    remark,
  })
  await loadJobsAdmin()
  if (jobsPostDrawerVisible.value && jobsPostDetail.value?.id === row.id) {
    jobsPostDetail.value = (await api.get(`/admin/jobs/posts/${row.id}`)) as JobsPost
  }
  ElMessage.success(`岗位已${labelMap[action]}`)
}

const handleJobsAccountAction = async (
  row: JobsAccount,
  action: 'LIMIT' | 'BAN' | 'RESTORE' | 'TOGGLE_PUBLISH' | 'TOGGLE_APPLY' | 'TOGGLE_ACCEPT'
) => {
  const labelMap = {
    LIMIT: '限制账号',
    BAN: '封禁账号',
    RESTORE: '恢复正常',
    TOGGLE_PUBLISH: row.publishEnabled ? '关闭发岗权限' : '开启发岗权限',
    TOGGLE_APPLY: row.applyEnabled ? '关闭报名权限' : '开启报名权限',
    TOGGLE_ACCEPT: row.acceptEnabled ? '关闭接单权限' : '开启接单权限',
  }
  const defaultRemarkMap = {
    LIMIT: '因近期纠纷较多，临时限制账号关键操作。',
    BAN: '因多次违规，已封禁账号。',
    RESTORE: '复核后恢复账号正常权限。',
    TOGGLE_PUBLISH: row.publishEnabled ? '关闭发岗权限，待进一步复核。' : '恢复发岗权限。',
    TOGGLE_APPLY: row.applyEnabled ? '关闭报名权限，控制爽约风险。' : '恢复报名权限。',
    TOGGLE_ACCEPT: row.acceptEnabled ? '关闭接单权限，待行为恢复正常。' : '恢复接单权限。',
  }
  const remark = await ElMessageBox.prompt(`请输入${labelMap[action]}说明`, '用户 / 用工方管理', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: defaultRemarkMap[action],
    inputPlaceholder: '请输入处理说明',
  }).then(({ value }) => value || defaultRemarkMap[action])

  await api.post(`/admin/jobs/accounts/${row.id}/action`, {
    action,
    remark,
  })
  await loadJobsAdmin()
  if (jobsAccountDrawerVisible.value && jobsAccountDetail.value?.id === row.id) {
    jobsAccountDetail.value = (await api.get(`/admin/jobs/accounts/${row.id}`)) as JobsAccount
  }
  ElMessage.success(labelMap[action])
}

const handleJobsReportAction = async (row: JobsReport, status: JobsReportStatus) => {
  const labelMap: Record<JobsReportStatus, string> = {
    PENDING: '待处理',
    RESOLVED: '已处理',
    CLOSED: '已完结',
  }
  const defaultRemarkMap: Record<JobsReportStatus, string> = {
    PENDING: '保持待处理状态',
    RESOLVED: '平台已介入核查，正在推动双方整改或补偿。',
    CLOSED: '平台已完成仲裁并公示结果，工单完结。',
  }
  const remark = await ElMessageBox.prompt(`请输入${labelMap[status]}说明`, '举报工单管理', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: defaultRemarkMap[status],
    inputPlaceholder: '请输入处理说明',
  }).then(({ value }) => value || defaultRemarkMap[status])

  await api.post(`/admin/jobs/reports/${row.id}/action`, {
    status,
    remark,
    publicResult:
      status === 'RESOLVED'
        ? '平台已介入处理，相关结果已同步至岗位与信用记录。'
        : status === 'CLOSED'
          ? '工单已完结，结果保留在公示记录中。'
          : '',
  })
  await loadJobsAdmin()
  if (jobsReportDrawerVisible.value && jobsReportDetail.value?.id === row.id) {
    jobsReportDetail.value = (await api.get(`/admin/jobs/reports/${row.id}`)) as JobsReport
  }
  ElMessage.success(`举报工单已更新为${labelMap[status]}`)
}

const handleJobsCreditAdjust = async (row: JobsAccount) => {
  const value = await ElMessageBox.prompt('请输入新的信用分', '双向信用体系', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: String(row.creditScore),
    inputPlaceholder: '0 - 100',
  }).then(({ value }) => value)
  const nextScore = Math.max(0, Math.min(100, Number(value || row.creditScore)))
  const remark = await ElMessageBox.prompt('请输入信用调整说明', '双向信用体系', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: nextScore < row.creditScore ? '根据近期纠纷与差评情况，下调信用分。' : '根据近期正常履约表现，上调信用分。',
    inputPlaceholder: '请输入处理说明',
  }).then(({ value }) => value || '管理员手动调整信用分')

  await api.post(`/admin/jobs/accounts/${row.id}/credit`, {
    creditScore: nextScore,
    remark,
  })
  await loadJobsAdmin()
  if (jobsAccountDrawerVisible.value && jobsAccountDetail.value?.id === row.id) {
    jobsAccountDetail.value = (await api.get(`/admin/jobs/accounts/${row.id}`)) as JobsAccount
  }
  ElMessage.success('信用分已更新')
}

const handleJobsSkillToggle = async (row: JobsSkillTag) => {
  await api.post(`/admin/jobs/skills/${row.id}/toggle`, {
    enabled: !row.enabled,
  })
  await loadJobsAdmin()
  ElMessage.success(row.enabled ? '技能标签已停用' : '技能标签已启用')
}

const handleJobsSkillRename = async (row: JobsSkillTag) => {
  const value = await ElMessageBox.prompt('请输入新的技能标签名称', '技能标签库', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: row.label,
    inputPlaceholder: '例如：PPT制作',
  }).then(({ value }) => value || row.label)
  await api.put(`/admin/jobs/skills/${row.id}`, {
    label: value.trim() || row.label,
    category: row.category,
  })
  await loadJobsAdmin()
  ElMessage.success('技能标签已更新')
}

const createJobsSkill = async () => {
  const label = await ElMessageBox.prompt('请输入新增技能标签名称', '技能标签库', {
    confirmButtonText: '下一步',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：AI 排版',
  }).then(({ value }) => value?.trim() || '')
  if (!label) {
    return
  }
  const category = await ElMessageBox.prompt('请输入标签分类', '技能标签库', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: '技能服务',
    inputPlaceholder: '例如：办公支持',
  }).then(({ value }) => value?.trim() || '技能服务')

  await api.post('/admin/jobs/skills', {
    label,
    category,
  })
  await loadJobsAdmin()
  ElMessage.success('技能标签已新增')
}

const handleBeautyAppealAction = async (row: BeautyAppeal, status: BeautyAppealStatus) => {
  const labelMap: Record<BeautyAppealStatus, string> = {
    PENDING: '待受理',
    PROCESSING: '处理中',
    RESOLVED: '已仲裁通过',
    REJECTED: '已驳回',
  }
  const defaultRemarkMap: Record<BeautyAppealStatus, string> = {
    PENDING: '售后申请已提交，等待平台受理',
    PROCESSING: '平台已受理售后申请，正在核查订单截图与实拍图',
    RESOLVED: '已核实申请成立，平台支持退款/补发处理',
    REJECTED: '现有凭证不足，暂不支持当前售后申请',
  }
  const remark = await ElMessageBox.prompt(`请输入${labelMap[status]}说明`, '美妆售后仲裁', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: defaultRemarkMap[status],
    inputPlaceholder: '请输入处理说明',
  }).then(({ value }) => value || defaultRemarkMap[status])

  await api.post(`/admin/beauty/appeals/${row.id}/action`, {
    status,
    remark,
  })

  await Promise.all([loadBeautyGoods(), loadBeautyAppeals(), loadBeautyStats()])

  if (beautyAppealDrawerVisible.value && beautyAppealDetail.value?.id === row.id) {
    beautyAppealDetail.value = (await api.get(`/admin/beauty/appeals/${row.id}`)) as BeautyAppeal
  }
  if (beautyDrawerVisible.value && beautyDetail.value?.id === row.goodId) {
    beautyDetail.value = (await api.get(`/admin/beauty/goods/${row.goodId}`)) as BeautyGood
  }
  ElMessage.success(`售后工单已更新为${labelMap[status]}`)
}

const openPartnerDemandDetail = async (row: PartnerDemand) => {
  partnerDemandDetail.value = null
  partnerDemandDrawerVisible.value = true
  try {
    partnerDemandDetail.value = (await api.get(`/admin/partner/demands/${row.id}`)) as PartnerDemand
  } catch (error) {
    partnerDemandDrawerVisible.value = false
    throw error
  }
}

const openPartnerReportDetail = async (row: PartnerReport) => {
  partnerReportDetail.value = null
  partnerReportDrawerVisible.value = true
  try {
    partnerReportDetail.value = (await api.get(`/admin/partner/reports/${row.id}`)) as PartnerReport
  } catch (error) {
    partnerReportDrawerVisible.value = false
    throw error
  }
}

const openPartnerChatDetail = async (row: PartnerConversation) => {
  partnerChatDetail.value = null
  partnerChatDrawerVisible.value = true
  try {
    partnerChatDetail.value = (await api.get(`/admin/partner/conversations/${row.id}`)) as PartnerConversation
  } catch (error) {
    partnerChatDrawerVisible.value = false
    throw error
  }
}

const refreshPartnerAdmin = async () => {
  await loadPartnerAdmin()
  await refreshOpenPartnerDetails()
  ElMessage.success('找搭子管理数据已刷新')
}

const openPartnerDemandFromReport = async () => {
  if (!partnerReportDetail.value) {
    return
  }
  const target = partnerDemands.value.find((item) => item.id === partnerReportDetail.value?.targetId)
  if (!target) {
    ElMessage.warning('当前举报未关联有效需求')
    return
  }
  await openPartnerDemandDetail(target)
}

const openPartnerChatFromReport = async () => {
  if (!partnerReportDetail.value?.conversationId) {
    ElMessage.warning('当前举报暂无关联会话')
    return
  }
  const target = partnerConversations.value.find((item) => item.id === partnerReportDetail.value?.conversationId)
  if (!target) {
    ElMessage.warning('当前举报暂无关联会话')
    return
  }
  await openPartnerChatDetail(target)
}

const openPartnerDemandFromChat = async () => {
  if (!partnerChatDetail.value) {
    return
  }
  const target = partnerDemands.value.find((item) => item.id === partnerChatDetail.value?.demandId)
  if (!target) {
    ElMessage.warning('当前会话未关联有效需求')
    return
  }
  await openPartnerDemandDetail(target)
}

const openPartnerReportFromDemand = async (reportId: number) => {
  const target = partnerReports.value.find((item) => item.id === reportId)
  if (!target) {
    ElMessage.warning('关联举报工单不存在')
    return
  }
  await openPartnerReportDetail(target)
}

const openPartnerChatFromDemand = async (conversationId: string) => {
  const target = partnerConversations.value.find((item) => item.id === conversationId)
  if (!target) {
    ElMessage.warning('关联会话不存在')
    return
  }
  await openPartnerChatDetail(target)
}

const handlePartnerDemandAction = async (row: PartnerDemand, action: 'APPROVE' | 'REJECT' | 'OFFLINE' | 'RESTORE') => {
  const actionLabelMap = {
    APPROVE: '通过',
    REJECT: '驳回',
    OFFLINE: '下架',
    RESTORE: '恢复上架',
  }
  const defaultRemarkMap = {
    APPROVE: '内容合规，允许继续展示',
    REJECT: '内容包含不适宜展示信息，已驳回',
    OFFLINE: '检测到潜在风险，先下架复核',
    RESTORE: '复核通过，恢复公开展示',
  }
  const remark = await ElMessageBox.prompt(`请输入${actionLabelMap[action]}说明`, '找搭子内容审核', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: defaultRemarkMap[action],
    inputPlaceholder: '请输入处理说明',
  }).then(({ value }) => value || defaultRemarkMap[action])

  await api.post(`/admin/partner/demands/${row.id}/action`, { action, remark })
  await loadPartnerAdmin()
  if (partnerDemandDrawerVisible.value && partnerDemandDetail.value?.id === row.id) {
    partnerDemandDetail.value = (await api.get(`/admin/partner/demands/${row.id}`)) as PartnerDemand
  }
  ElMessage.success(`需求已${actionLabelMap[action]}`)
}

const handlePartnerReportAction = async (row: PartnerReport, status: PartnerReport['status']) => {
  const labelMap: Record<PartnerReport['status'], string> = {
    PENDING: '待受理',
    PROCESSING: '处理中',
    RESOLVED: '已处理',
    REJECTED: '已驳回',
  }
  const defaultRemarkMap: Record<PartnerReport['status'], string> = {
    PENDING: '待管理员处理',
    PROCESSING: '已受理举报，正在核查聊天和截图凭证',
    RESOLVED: '已核实并完成处理',
    REJECTED: '现有凭证不足，暂不支持该举报',
  }
  const remark = await ElMessageBox.prompt(`请输入${labelMap[status]}说明`, '找搭子举报治理', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: defaultRemarkMap[status],
    inputPlaceholder: '请输入处理说明',
  }).then(({ value }) => value || defaultRemarkMap[status])

  await api.post(`/admin/partner/reports/${row.id}/action`, { status, remark })
  await loadPartnerAdmin()
  if (partnerReportDrawerVisible.value && partnerReportDetail.value?.id === row.id) {
    partnerReportDetail.value = (await api.get(`/admin/partner/reports/${row.id}`)) as PartnerReport
  }
  ElMessage.success(`举报工单已更新为${labelMap[status]}`)
}

const handlePartnerChatAction = async (row: PartnerConversation, action: 'MARK_REVIEW' | 'CLOSE' | 'RESTORE') => {
  const labelMap = {
    MARK_REVIEW: '标记复核',
    CLOSE: '关闭会话',
    RESTORE: '恢复会话',
  }
  const defaultRemarkMap = {
    MARK_REVIEW: '命中高风险沟通特征，进入人工复核',
    CLOSE: '核查存在违规沟通，已关闭会话',
    RESTORE: '复核后恢复正常沟通权限',
  }
  const remark = await ElMessageBox.prompt(`请输入${labelMap[action]}说明`, '找搭子聊天监管', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: defaultRemarkMap[action],
    inputPlaceholder: '请输入处理说明',
  }).then(({ value }) => value || defaultRemarkMap[action])

  await api.post(`/admin/partner/conversations/${row.id}/action`, { action, remark })
  await loadPartnerAdmin()
  if (partnerChatDrawerVisible.value && partnerChatDetail.value?.id === row.id) {
    partnerChatDetail.value = (await api.get(`/admin/partner/conversations/${row.id}`)) as PartnerConversation
  }
  ElMessage.success(`会话已${labelMap[action]}`)
}

const saveErrandRules = async () => {
  await api.post('/admin/errand/rules', errandRules.value)
  ElMessage.success('配置已保存')
  await loadErrandStats()
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
  errandConversationReview.value = null
  errandConversationError.value = ''
  if (isErrandReport(reportDetail.value) && reportDetail.value.targetId) {
    await loadErrandConversationReview(reportDetail.value.targetId)
  }
  reportDrawerVisible.value = true
}

const openReportFromErrand = async (reportId: number) => {
  try {
    await openReportDetail({ id: reportId })
  } catch (error: any) {
    ElMessage.error(error?.message || '关联举报工单详情暂不可用')
  }
}

const openErrandDetail = async (row: { id: number }) => {
  errandDetail.value = (await api.get(`/admin/errand/orders/${row.id}`)) as ErrandOrder
  errandConversationReview.value = null
  errandConversationError.value = ''
  await loadErrandConversationReview(row.id)
  errandDrawerVisible.value = true
}

const openErrandFromRow = async (row: { targetId?: number | null }) => {
  if (!row.targetId) {
    ElMessage.warning('当前工单没有关联订单')
    return
  }
  await openErrandDetail({ id: row.targetId })
}

const openErrandFromReport = async () => {
  if (!reportDetail.value?.targetId) {
    ElMessage.warning('当前工单没有关联订单')
    return
  }
  reportDrawerVisible.value = false
  try {
    await openErrandDetail({ id: reportDetail.value.targetId })
  } catch (error: any) {
    reportDrawerVisible.value = true
    ElMessage.error(error?.message || '关联订单详情暂不可用')
  }
}

const refreshErrandConversationAudit = async () => {
  const orderId = errandDetail.value?.id || reportDetail.value?.targetId
  if (!orderId) {
    ElMessage.warning('当前没有可核查的关联订单')
    return
  }
  await loadErrandConversationReview(orderId)
}

const fillBeautyForm = (detail: BeautyGood) => {
  beautyForm.value = {
    id: detail.id,
    category: detail.category || '',
    title: detail.title || '',
    price: detail.price ?? null,
    summary: detail.summary || '',
    usageGuide: detail.usageGuide || '',
    sceneText: detail.sceneText || '',
    evaluation: detail.evaluation || '',
    dormExperience: detail.dormExperience || '',
    avoidanceGuide: detail.avoidanceGuide || '',
    skinTagsText: (detail.skinTags || []).join('，'),
    productImageFileId: detail.productImage?.fileId || null,
    receiptFileId: detail.receiptImage?.fileId || null,
    productImage: detail.productImage || null,
    receiptImage: detail.receiptImage || null,
  }
}

const openBeautyEdit = async (row: { id: number } | BeautyGood | null) => {
  if (!row?.id) {
    return
  }
  beautyEditorMode.value = 'edit'
  beautyDrawerVisible.value = false
  const detail = (await api.get(`/admin/beauty/goods/${row.id}`)) as BeautyGood
  fillBeautyForm(detail)
  beautyEditorVisible.value = true
}

const parseSkinTags = () =>
  beautyForm.value.skinTagsText
    .split(/[，,]/)
    .map((item) => item.trim())
    .filter(Boolean)

const buildBeautyPayload = () => ({
  category: beautyForm.value.category,
  title: beautyForm.value.title.trim(),
  price: beautyForm.value.price,
  summary: beautyForm.value.summary.trim(),
  usageGuide: beautyForm.value.usageGuide.trim(),
  sceneText: beautyForm.value.sceneText.trim(),
  evaluation: beautyForm.value.evaluation.trim(),
  dormExperience: beautyForm.value.dormExperience.trim(),
  avoidanceGuide: beautyForm.value.avoidanceGuide.trim(),
  skinTags: parseSkinTags().length ? parseSkinTags() : ['平价好物'],
  productImageFileId: beautyForm.value.productImageFileId,
  receiptFileId: beautyForm.value.receiptFileId,
})

const uploadBeautyAsset = async (file: File, bizType: string): Promise<UploadResponse> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', bizType)
  return (await api.post('/files/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })) as UploadResponse
}

const uploadBeautyProductImage = async (options: any) => {
  try {
    const file = await uploadBeautyAsset(options.file, 'beauty_admin_product')
    beautyForm.value.productImageFileId = file.fileId
    beautyForm.value.productImage = file
    options.onSuccess?.(file)
    ElMessage.success('商品图上传成功')
  } catch (error: any) {
    options.onError?.(error)
    ElMessage.error(error.message || '商品图上传失败')
  }
}

const uploadBeautyReceiptImage = async (options: any) => {
  try {
    const file = await uploadBeautyAsset(options.file, 'beauty_admin_receipt')
    beautyForm.value.receiptFileId = file.fileId
    beautyForm.value.receiptImage = file
    options.onSuccess?.(file)
    ElMessage.success('购买凭证上传成功')
  } catch (error: any) {
    options.onError?.(error)
    ElMessage.error(error.message || '购买凭证上传失败')
  }
}

const submitBeautyForm = async () => {
  if (!beautyFormValid.value) {
    ElMessage.warning('请先完善美妆商品信息')
    return
  }
  beautySubmitting.value = true
  try {
    const payload = buildBeautyPayload()
    if (beautyEditorMode.value === 'create') {
      await api.post('/admin/beauty/goods', payload)
      ElMessage.success('美妆好物已新增并上架')
    } else if (beautyForm.value.id) {
      await api.put(`/admin/beauty/goods/${beautyForm.value.id}`, payload)
      ElMessage.success('美妆好物已更新')
      if (beautyDetail.value?.id === beautyForm.value.id) {
        beautyDetail.value = (await api.get(`/admin/beauty/goods/${beautyForm.value.id}`)) as BeautyGood
      }
    }
    beautyEditorVisible.value = false
    await Promise.all([loadBeautyGoods(), loadBeautyAppeals(), loadBeautyStats()])
  } finally {
    beautySubmitting.value = false
  }
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

const reportDefaultRemarkMap: Record<ReportHandleStatus, string> = {
  PROCESSING: '后台已受理，正在处理中',
  RESOLVED: '后台已确认处理完成',
  REJECTED: '后台驳回该工单，请查看备注',
}

const userStatusDefaultRemarkMap = {
  ACTIVE: '后台已恢复账号正常使用权限',
  WARNED: '存在轻微违规行为，已进行规则提醒',
  ERRAND_RESTRICTED: '因跑腿相关违规，已限制跑腿发单、接单与举报功能',
  REPORT_RESTRICTED: '因恶意或失实举报，已限制提交举报工单',
  REPORT_ACTIVE: '后台已恢复举报功能使用权限',
  TEMP_BANNED: '因多次违规，账号已进入短期封禁状态',
  PERMANENT_BANNED: '因严重违规，账号已永久封禁',
  BLACKLISTED: '因恶意行为，账号已加入黑名单',
} as const

const getReportReporterUserId = (report?: Partial<ReportRow & ReportDetail> | null) => {
  if (!report) {
    return null
  }
  return report.reporterUserId ?? report.submitterId ?? null
}

const canPunishReporter = () =>
  reportHandleForm.value.status === 'REJECTED' &&
  String(reportHandleForm.value.module || '').toUpperCase() === 'ERRAND' &&
  !!reportHandleForm.value.reporterUserId

const getPunishDefaultRemark = (status: PunishStatus) => {
  if (status === 'NONE') {
    return ''
  }
  return userStatusDefaultRemarkMap[status]
}

const handleReportPunishStatusChange = (value: PunishStatus) => {
  reportHandleForm.value.punishRemark = getPunishDefaultRemark(value)
}

const openReportHandleDialog = async (row: ReportRow | ReportDetail, status: ReportHandleStatus) => {
  let reporterUserId = getReportReporterUserId(row)
  let module = row.module || ''
  if ((status === 'REJECTED' || reporterUserId == null || !module) && row.id && (!reportDetail.value || reportDetail.value.id !== row.id)) {
    const detail = (await api.get(`/admin/reports/${row.id}`)) as ReportDetail
    reporterUserId = getReportReporterUserId(detail)
    module = detail.module || module
  } else if (reportDetail.value?.id === row.id) {
    reporterUserId = reporterUserId ?? getReportReporterUserId(reportDetail.value)
    module = module || reportDetail.value.module || ''
  }

  reportHandleForm.value = {
    id: row.id,
    status,
    remark: reportDefaultRemarkMap[status],
    punishStatus: 'NONE',
    punishRemark: '',
    module,
    reporterUserId,
  }
  reportHandleDialogVisible.value = true
}

const submitReportHandle = async () => {
  if (!reportHandleForm.value.id) {
    return
  }
  reportHandling.value = true
  try {
    await api.post(`/admin/reports/${reportHandleForm.value.id}/handle`, {
      status: reportHandleForm.value.status,
      remark: reportHandleForm.value.remark.trim(),
      punishStatus: canPunishReporter() && reportHandleForm.value.punishStatus !== 'NONE' ? reportHandleForm.value.punishStatus : '',
      punishRemark: canPunishReporter() && reportHandleForm.value.punishStatus !== 'NONE' ? reportHandleForm.value.punishRemark.trim() : '',
    })
    ElMessage.success(reportHandleForm.value.status === 'REJECTED' ? '举报工单已驳回' : '举报工单处理成功')
    reportHandleDialogVisible.value = false

    if (reportDrawerVisible.value && reportDetail.value?.id === reportHandleForm.value.id) {
      reportDetail.value = (await api.get(`/admin/reports/${reportHandleForm.value.id}`)) as ReportDetail
    }
    if (errandDrawerVisible.value && errandDetail.value?.relatedReports?.some((item) => item.id === reportHandleForm.value.id) && errandDetail.value?.id) {
      errandDetail.value = (await api.get(`/admin/errand/orders/${errandDetail.value.id}`)) as ErrandOrder
    }
    if (canPunishReporter() && reportHandleForm.value.punishStatus !== 'NONE' && userDrawerVisible.value && userDetail.value?.userId === reportHandleForm.value.reporterUserId) {
      userDetail.value = (await api.get(`/admin/users/${reportHandleForm.value.reporterUserId}`)) as AdminUserDetail
    }
    allReports.value = ((await api.get('/admin/reports', {
      params: { current: 1, size: 100 },
    })) as any).records || []
    allUsers.value = (await api.get('/admin/users')) as UserRow[]
    await Promise.all([loadReports(), loadErrandReports(), loadErrandStats(), loadUsers(), loadGovernanceUsers()])
  } finally {
    reportHandling.value = false
  }
}

const handleReport = async (row: ReportRow | ReportDetail, status: ReportHandleStatus) => {
  await openReportHandleDialog(row, status)
}

const handleUserStatus = async (id: number, status: 'ACTIVE' | 'WARNED' | 'ERRAND_RESTRICTED' | 'REPORT_RESTRICTED' | 'REPORT_ACTIVE' | 'TEMP_BANNED' | 'PERMANENT_BANNED' | 'BLACKLISTED') => {
  const remark = await ElMessageBox.prompt('请输入处罚说明', '用户权限与处罚', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: userStatusDefaultRemarkMap[status],
    inputPlaceholder: '请输入处理说明',
  }).then(({ value }) => value || userStatusDefaultRemarkMap[status])

  await api.post(`/admin/users/${id}/status`, {
    status,
    remark,
  })
  ElMessage.success(status === 'ACTIVE' ? '账号状态已恢复正常' : status === 'REPORT_ACTIVE' ? '举报权限已恢复正常' : '用户处罚状态已更新')

  if (userDrawerVisible.value && userDetail.value?.userId === id) {
    userDetail.value = (await api.get(`/admin/users/${id}`)) as AdminUserDetail
  }
  allUsers.value = (await api.get('/admin/users')) as UserRow[]
  await Promise.all([loadUsers(), loadGovernanceUsers()])
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
  await Promise.all([loadErrandOrders(), loadErrandStats()])
}

const handleBeautyAction = async (id: number, action: 'APPROVE' | 'REJECT' | 'OFFLINE' | 'RESTORE') => {
  let remark = ''
  if (action === 'REJECT' || action === 'OFFLINE') {
    const promptTitle = action === 'REJECT' ? '请输入驳回原因' : '请输入下架备注'
    const defaultRemark = action === 'REJECT' ? '内容不符合平台展示规范' : '后台人工下架该商品'
    remark = await ElMessageBox.prompt(promptTitle, '处理美妆商品', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputValue: defaultRemark,
      inputPlaceholder: '请输入处理说明',
    }).then(({ value }) => value || defaultRemark)
  }

  await api.post(`/admin/beauty/goods/${id}/action`, {
    action,
    remark,
  })
  ElMessage.success('美妆商品状态已更新')
  if (beautyDrawerVisible.value && beautyDetail.value?.id === id) {
    beautyDetail.value = (await api.get(`/admin/beauty/goods/${id}`)) as BeautyGood
  }
  await Promise.all([loadBeautyGoods(), loadBeautyAppeals(), loadBeautyStats()])
  if (beautyAppealDrawerVisible.value && beautyAppealDetail.value?.goodId === id) {
    beautyAppealDetail.value = beautyAppealDetail.value?.id
      ? ((await api.get(`/admin/beauty/appeals/${beautyAppealDetail.value.id}`)) as BeautyAppeal)
      : null
  }
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

const refreshErrandAdmin = async () => {
  await Promise.all([loadErrandOrders(), loadErrandReports(), loadGovernanceUsers(), loadErrandRules(), loadErrandStats()])
  ElMessage.success('跑腿监管数据已刷新')
}

const handleErrandOrderSizeChange = async (size: number) => {
  errandOrderPageSize.value = size
  errandOrderCurrent.value = 1
  await loadErrandOrders()
}

const searchErrandOrders = async () => {
  errandOrderCurrent.value = 1
  await loadErrandOrders()
}

const handleErrandReportSizeChange = async (size: number) => {
  errandReportPageSize.value = size
  errandReportCurrent.value = 1
  await loadErrandReports()
}

const searchErrandReports = async () => {
  errandReportCurrent.value = 1
  await loadErrandReports()
}

const handleGovernanceSizeChange = async (size: number) => {
  governancePageSize.value = size
  governanceCurrent.value = 1
  await loadGovernanceUsers()
}

const searchGovernanceUsers = async () => {
  governanceCurrent.value = 1
  await loadGovernanceUsers()
}

const toggleGroup = (title: string) => {
  const isCurrentOpen = expandedGroups.value[title]
  expandedGroups.value = Object.fromEntries(
    navigationGroups.map((group) => [group.title, false])
  ) as Record<string, boolean>
  expandedGroups.value[title] = !isCurrentOpen
}

watch(
  currentGroupTitle,
  (groupTitle) => {
    expandedGroups.value = Object.fromEntries(
      navigationGroups.map((group) => [group.title, group.title === groupTitle])
    ) as Record<string, boolean>
  },
  { immediate: true }
)

const logout = () => {
  localStorage.removeItem('adminToken')
  router.push('/login')
}

const formatDate = (value?: string) => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (!Number.isNaN(date.getTime())) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(
      date.getHours()
    ).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
  }
  return value.replace('T', ' ')
}

const formatCounterparty = (value?: ErrandCounterparty) => {
  if (!value) {
    return '-'
  }
  return `${value.nickname} / ${value.phone}`
}

const formatUserStatus = (value?: string) => {
  const map: Record<string, string> = {
    ACTIVE: '正常',
    WARNED: '已警告',
    ERRAND_RESTRICTED: '跑腿受限',
    TEMP_BANNED: '短期封禁',
    PERMANENT_BANNED: '永久封禁',
    BLACKLISTED: '已拉黑',
    DISABLED: '禁用',
  }
  return map[String(value || '').toUpperCase()] || '未知状态'
}

const formatUserRole = (value?: string) => {
  const map: Record<string, string> = {
    USER: '普通用户',
    ADMIN: '管理员',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const userStatusTagType = (value?: string) => {
  const map: Record<string, string> = {
    ACTIVE: 'success',
    WARNED: 'warning',
    ERRAND_RESTRICTED: 'danger',
    TEMP_BANNED: 'danger',
    PERMANENT_BANNED: 'danger',
    BLACKLISTED: 'info',
    DISABLED: 'info',
  }
  return map[String(value || '').toUpperCase()] || 'info'
}

const formatErrandStatus = (value?: string) => {
  const map: Record<string, string> = {
    PUBLISHED: '公共广场',
    ACCEPTED: '已接单',
    IN_PROGRESS: '履约中',
    DELIVERING: '派送中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    DISPUTED: '争议中',
    EXPIRED: '已过期',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const formatErrandServiceType = (value?: string) => {
  const map: Record<string, string> = {
    PICKUP: '代取',
    DELIVERY: '配送',
    PURCHASE: '代买',
    PRINT: '打印',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const formatErrandFlowState = (value?: string) => {
  const map: Record<string, string> = {
    PUBLIC: '公开广场',
    LOCKED: '锁定履约',
    ARCHIVED: '已归档',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const formatPartnerDemandStatus = (value?: PartnerDemandStatus | string) => {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    OFFLINE: '已下架',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const formatBeautyStatus = (value?: string) => {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已上架',
    REJECTED: '已驳回',
    OFFLINE: '已下架',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const beautyStatusTagType = (value?: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    OFFLINE: 'info',
  }
  return map[String(value || '').toUpperCase()] || 'info'
}

const formatBeautyCategory = (value?: string) => {
  const map: Record<string, string> = {
    MAKEUP: '护肤彩妆',
    CARE: '个护收纳',
    ACCESSORY: '配套好物',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const formatBeautySourceType = (value?: string) => {
  const map: Record<string, string> = {
    USER: '用户发布',
    SYSTEM: '后台录入',
  }
  return map[String(value || '').toUpperCase()] || (value || '后台录入')
}

const formatBeautyAppealStatus = (value?: BeautyAppealStatus | string) => {
  const map: Record<string, string> = {
    PENDING: '待受理',
    PROCESSING: '处理中',
    RESOLVED: '已裁定',
    REJECTED: '已驳回',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const beautyAppealStatusTagType = (value?: BeautyAppealStatus | string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    PROCESSING: 'primary',
    RESOLVED: 'success',
    REJECTED: 'info',
  }
  return map[String(value || '').toUpperCase()] || 'info'
}

const formatJobsMerchantStatus = (value?: JobsMerchantStatus | string) => {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已驳回',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const jobsMerchantStatusTagType = (value?: JobsMerchantStatus | string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
  }
  return map[String(value || '').toUpperCase()] || 'info'
}

const formatJobsPostStatus = (value?: JobsPostStatus | string) => {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    OFFLINE: '已下架',
    BLOCKED: '已拦截',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const jobsPostStatusTagType = (value?: JobsPostStatus | string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    OFFLINE: 'info',
    BLOCKED: 'danger',
  }
  return map[String(value || '').toUpperCase()] || 'info'
}

const formatJobsReportStatus = (value?: JobsReportStatus | string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    RESOLVED: '已处理',
    CLOSED: '已完结',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const jobsReportStatusTagType = (value?: JobsReportStatus | string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    RESOLVED: 'primary',
    CLOSED: 'success',
  }
  return map[String(value || '').toUpperCase()] || 'info'
}

const formatJobsAccountStatus = (value?: string) => {
  const map: Record<string, string> = {
    ACTIVE: '正常',
    LIMITED: '已限制',
    BANNED: '已封禁',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const jobsAccountStatusTagType = (value?: string) => {
  const map: Record<string, string> = {
    ACTIVE: 'success',
    LIMITED: 'warning',
    BANNED: 'danger',
  }
  return map[String(value || '').toUpperCase()] || 'info'
}

const formatJobsRole = (value?: JobsUserRole | string) => {
  const map: Record<string, string> = {
    STUDENT: '学生',
    EMPLOYER: '用工方',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const formatJobsWorkMode = (value?: string) => {
  const map: Record<string, string> = {
    ONLINE: '线上',
    OFFLINE: '线下',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const partnerDemandStatusTagType = (value?: PartnerDemandStatus | string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    OFFLINE: 'info',
  }
  return map[String(value || '').toUpperCase()] || 'info'
}

const formatPartnerConversationStatus = (value?: PartnerConversationStatus | string) => {
  const map: Record<string, string> = {
    ACTIVE: '正常会话',
    PENDING_REVIEW: '待人工复核',
    CLOSED: '已关闭',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const partnerConversationStatusTagType = (value?: PartnerConversationStatus | string) => {
  const map: Record<string, string> = {
    ACTIVE: 'success',
    PENDING_REVIEW: 'warning',
    CLOSED: 'info',
  }
  return map[String(value || '').toUpperCase()] || 'info'
}

const formatPartnerRiskLevel = (value?: PartnerRiskLevel | string) => {
  const map: Record<string, string> = {
    LOW: '低风险',
    MEDIUM: '中风险',
    HIGH: '高风险',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const partnerRiskLevelTagType = (value?: PartnerRiskLevel | string) => {
  const map: Record<string, string> = {
    LOW: 'success',
    MEDIUM: 'warning',
    HIGH: 'danger',
  }
  return map[String(value || '').toUpperCase()] || 'info'
}

const partnerContentRiskOptions = computed(() => getUniqueTags(partnerDemands.value.flatMap((item) => item.riskTags)))
const partnerReportTypeOptionList = computed(() => getUniqueTags(partnerReports.value.map((item) => item.reportType)))

const errandStatusTagType = (value?: string) => {
  const map: Record<string, string> = {
    PUBLISHED: 'primary',
    ACCEPTED: 'warning',
    IN_PROGRESS: 'warning',
    DELIVERING: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info',
    DISPUTED: 'danger',
    EXPIRED: 'info',
  }
  return map[String(value || '').toUpperCase()] || 'info'
}

const formatErrandFlow = (row: ErrandOrder) => {
  const status = String(row.status || '').toUpperCase()
  if (status === 'PUBLISHED') {
    return '公开待接单'
  }
  if (status === 'ACCEPTED' || status === 'IN_PROGRESS' || status === 'DELIVERING') {
    return '锁定履约中'
  }
  if (status === 'DISPUTED') {
    return '锁定争议中'
  }
  return '已归档'
}

const hasErrandRiskTag = (row: ErrandOrder, tag: string) => {
  return Array.isArray(row.riskTags) && row.riskTags.includes(tag)
}

const errandRiskTagType = (tag: string) => {
  if (tag === '举报已超时' || tag === '争议中') {
    return 'danger'
  }
  if (tag === '举报待处理' || tag === '临近接单超时') {
    return 'warning'
  }
  return 'info'
}

const getActiveReportCount = (reports?: RelatedReport[]) =>
  (reports || []).filter((item) => ['PENDING', 'PROCESSING'].includes(String(item.status || '').toUpperCase())).length

const getOverdueReportCount = (reports?: RelatedReport[]) => (reports || []).filter((item) => isReportOverdue(item)).length

const formatReportStatus = (value?: string) => {
  const map: Record<string, string> = {
    PENDING: '待受理',
    PROCESSING: '处理中',
    RESOLVED: '已处理',
    REJECTED: '已驳回',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const formatModuleName = (value?: string) => {
  const map: Record<string, string> = {
    ERRAND: '校园跑腿',
    PARTNER: '找搭子',
    BEAUTY: '美妆选购',
    JOBS: '勤工助学',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const isErrandReport = (value?: ReportDetail | ReportRow | null) => String(value?.module || '').toUpperCase() === 'ERRAND'

const reportDeadlineKeys = ['processDeadline', 'processingDeadline', 'handleDeadline', 'deadlineAt', 'dueAt', 'deadline', 'expireAt', 'processingDueAt', 'handleDueAt']
const reportOverdueKeys = ['isOverdue', 'overdue', 'timedOut', 'timeout', 'expired']
const reportRemainingTextKeys = ['remainingTimeText', 'remainingDurationText', 'remainingText', 'remainText', 'remainLabel']
const reportRemainingNumberKeys = ['remainingMillis', 'remainingMs', 'remainingSeconds', 'remainingMinutes']

const getFirstReportValue = <T = unknown>(report: ReportLike, keys: string[]): T | undefined => {
  if (!report) {
    return undefined
  }
  for (const key of keys) {
    const value = (report as Record<string, unknown>)[key]
    if (value !== undefined && value !== null && value !== '') {
      return value as T
    }
  }
  return undefined
}

const getReportDeadlineDate = (report: ReportLike) => {
  const rawValue = getFirstReportValue<string>(report, reportDeadlineKeys)
  if (!rawValue) {
    return null
  }
  const date = new Date(rawValue)
  return Number.isNaN(date.getTime()) ? null : date
}

const getReportRemainingMs = (report: ReportLike) => {
  const key = reportRemainingNumberKeys.find((item) => {
    const value = report ? (report as Record<string, unknown>)[item] : undefined
    return typeof value === 'number' && Number.isFinite(value)
  })
  if (!key || !report) {
    return null
  }
  const value = Number((report as Record<string, unknown>)[key])
  if (key === 'remainingSeconds') {
    return value * 1000
  }
  if (key === 'remainingMinutes') {
    return value * 60 * 1000
  }
  return value
}

const getReportRemainingText = (report: ReportLike) => getFirstReportValue<string>(report, reportRemainingTextKeys)

const isReportOverdue = (report: ReportLike) => {
  const overdueValue = getFirstReportValue<boolean>(report, reportOverdueKeys)
  if (typeof overdueValue === 'boolean') {
    return overdueValue
  }
  const remainingMs = getReportRemainingMs(report)
  if (typeof remainingMs === 'number') {
    return remainingMs < 0
  }
  const deadline = getReportDeadlineDate(report)
  if (!deadline) {
    return false
  }
  return deadline.getTime() <= reportClock.value
}

const getComputedRemainingMs = (report: ReportLike) => {
  const remainingMs = getReportRemainingMs(report)
  if (typeof remainingMs === 'number') {
    return remainingMs
  }
  const deadline = getReportDeadlineDate(report)
  if (!deadline) {
    return null
  }
  return deadline.getTime() - reportClock.value
}

const formatDurationDelta = (value: number) => {
  const absValue = Math.abs(value)
  const totalMinutes = Math.floor(absValue / 60000)
  const days = Math.floor(totalMinutes / (24 * 60))
  const hours = Math.floor((totalMinutes % (24 * 60)) / 60)
  const minutes = totalMinutes % 60
  const parts: string[] = []
  if (days > 0) {
    parts.push(`${days}天`)
  }
  if (hours > 0) {
    parts.push(`${hours}小时`)
  }
  if (minutes > 0) {
    parts.push(`${minutes}分`)
  }
  if (!parts.length) {
    return '不足1分钟'
  }
  return parts.join('')
}

const formatReportDeadlineAt = (report: ReportLike) => {
  const rawValue = getFirstReportValue<string>(report, reportDeadlineKeys)
  return rawValue ? formatDate(rawValue) : '后端暂未返回'
}

const formatReportDeadlineState = (report: ReportLike) => {
  const statusValue = String(getFirstReportValue<string>(report, ['deadlineStatus']) || '').toUpperCase()
  if (statusValue === 'OVERDUE' || statusValue === 'TIMEOUT' || statusValue === 'EXPIRED') {
    return '已超时'
  }
  if (statusValue === 'NEAR_TIMEOUT' || statusValue === 'EXPIRING') {
    return '即将超时'
  }
  if (isReportOverdue(report)) {
    return '已超时'
  }
  const remainingMs = getComputedRemainingMs(report)
  if (typeof remainingMs === 'number' && remainingMs <= 60 * 60 * 1000) {
    return '即将超时'
  }
  if (getReportDeadlineDate(report)) {
    return '时限内'
  }
  return '未返回'
}

const formatReportDeadlineHint = (report: ReportLike) => {
  const text = getReportRemainingText(report)
  if (text) {
    return text
  }
  const remainingMs = getComputedRemainingMs(report)
  if (typeof remainingMs === 'number') {
    return remainingMs < 0 ? `已超时 ${formatDurationDelta(remainingMs)}` : `剩余 ${formatDurationDelta(remainingMs)}`
  }
  return '后端暂未返回剩余时限信息'
}

const reportDeadlineTagType = (report: ReportLike) => {
  const state = formatReportDeadlineState(report)
  if (state === '已超时') {
    return 'danger'
  }
  if (state === '即将超时') {
    return 'warning'
  }
  if (state === '时限内') {
    return 'success'
  }
  return 'info'
}

const formatMessageType = (value?: string) => {
  const map: Record<string, string> = {
    TEXT: '文本',
    IMAGE: '图片',
    SYSTEM: '系统',
  }
  return map[String(value || '').toUpperCase()] || (value || '-')
}

const isImageMessage = (value?: AdminMessageReview) => String(value?.type || '').toUpperCase() === 'IMAGE'

const reportStatusTagType = (value?: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    PROCESSING: 'primary',
    RESOLVED: 'success',
    REJECTED: 'info',
  }
  return map[String(value || '').toUpperCase()] || 'info'
}

const formatPercent = (value?: number) => `${Number(value || 0).toFixed(2)}%`

const formatMoney = (value?: number) => Number(value || 0).toFixed(2)

let reportClockTimer: number | undefined

onMounted(() => {
  loadAll()
  reportClockTimer = window.setInterval(() => {
    reportClock.value = Date.now()
  }, 30000)
})

onBeforeUnmount(() => {
  if (reportClockTimer) {
    window.clearInterval(reportClockTimer)
  }
})
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

.sidebar-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-group + .sidebar-group {
  margin-top: 8px;
}

.sidebar-group-toggle {
  width: 100%;
  border: 0;
  background: transparent;
  color: inherit;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 6px 8px;
  cursor: pointer;
}

.sidebar-group-title {
  font-size: 16px;
  font-weight: 800;
  color: rgba(226, 232, 240, 0.98);
}

.sidebar-group-arrow {
  font-size: 14px;
  color: rgba(148, 163, 184, 0.92);
}

.sidebar-group-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 12px;
}

.sidebar button {
  border: 0;
  border-radius: 14px;
  padding: 12px 14px;
  text-align: left;
  background: transparent;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  font-size: 14px;
}

.sidebar button.active,
.sidebar button:hover {
  background: rgba(59, 130, 246, 0.25);
  color: #fff;
}

.content {
  padding: 24px;
}

.content-hero {
  margin-bottom: 20px;
  padding: 22px 24px;
  border-radius: 22px;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 60%, #eef2ff 100%);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.05);
}

.content-group {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.content-hero h1 {
  margin: 0;
  font-size: 30px;
  color: #0f172a;
}

.content-desc {
  margin: 10px 0 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.7;
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.errand-overview-grid {
  margin-top: 20px;
}

.errand-admin-overview {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-top: 20px;
}

.beauty-overview-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-top: 20px;
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

.stat.inset {
  background: linear-gradient(180deg, #f8fbff 0%, #eef5ff 100%);
  box-shadow: none;
}

.stat.inset strong {
  font-size: 34px;
}

.errand-summary-panel {
  min-height: 320px;
}

.beauty-summary-panel {
  min-height: 260px;
}

.mini-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.mini-head h3 {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
}

.metrics-stack,
.summary-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.metric-row,
.summary-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
}

.metric-row span,
.summary-item span {
  color: #64748b;
  font-size: 13px;
}

.metric-row strong,
.summary-item strong,
.summary-item em {
  color: #0f172a;
  font-style: normal;
}

.summary-item > div {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.summary-item strong {
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.summary-item em {
  flex-shrink: 0;
  font-weight: 700;
  color: #2563eb;
}

.empty-inline {
  min-height: 180px;
  border-radius: 16px;
  background: #f8fafc;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-inline.compact {
  min-height: 34px;
  justify-content: flex-start;
  padding: 0;
  background: transparent;
  color: #94a3b8;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-subtitle {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
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

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.deadline-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.deadline-line {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.deadline-label {
  color: #64748b;
  font-size: 12px;
}

.deadline-line strong {
  color: #0f172a;
  font-size: 13px;
}

.deadline-hint {
  color: #475569;
  font-size: 12px;
  line-height: 1.5;
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

.errand-admin-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.errand-kpi-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-top: 18px;
}

.errand-highlight-bar {
  background: linear-gradient(180deg, #f7fbff 0%, #eff6ff 100%);
}

.beauty-highlight-bar {
  background: linear-gradient(180deg, #fff7ed 0%, #fff1f2 100%);
}

.beauty-proof-bar {
  background: linear-gradient(180deg, #fffdf4 0%, #fff7ed 100%);
}

.beauty-proof-bar strong,
.beauty-highlight-bar strong {
  color: #9a3412;
}

.beauty-admin-kpi-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-top: 18px;
}

.beauty-admin-overview {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-top: 20px;
}

.rules-config-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.config-card {
  padding: 18px;
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.config-label {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.config-hint {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.module-placeholder {
  margin-top: 16px;
  padding: 24px;
  border-radius: 18px;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
}

.module-placeholder strong {
  display: block;
  font-size: 18px;
  color: #0f172a;
}

.module-placeholder p {
  margin: 12px 0 0;
  color: #64748b;
  line-height: 1.75;
}

.range-inline {
  display: flex;
  align-items: center;
  gap: 10px;
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

.report-handle-form {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.report-handle-penalty {
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f8fafc 0%, #eff6ff 100%);
  border: 1px solid #dbeafe;
}

.report-handle-tip {
  margin-bottom: 14px;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
}

.full-width {
  width: 100%;
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

.detail-toolbar {
  margin-bottom: 16px;
}

.preview-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.deadline-panel {
  margin-top: 20px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid #dbeafe;
  background: linear-gradient(180deg, #f8fbff 0%, #f8fafc 100%);
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

.related-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.related-report-panel {
  border: 1px solid #e2e8f0;
}

.related-summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.related-summary-card {
  padding: 14px 16px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e2e8f0;
}

.related-summary-card span {
  display: block;
  color: #64748b;
  font-size: 12px;
  margin-bottom: 8px;
}

.related-summary-card strong {
  color: #0f172a;
  font-size: 22px;
  line-height: 1;
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

.related-report-item {
  gap: 12px;
}

.related-report-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.related-report-title {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.related-report-title strong {
  color: #0f172a;
  font-size: 14px;
}

.related-report-title span {
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
}

.related-report-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.related-report-meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.related-report-meta-item {
  padding: 12px 14px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #e2e8f0;
}

.related-report-meta-item span {
  display: block;
  color: #64748b;
  font-size: 12px;
  margin-bottom: 6px;
}

.related-report-meta-item strong {
  color: #0f172a;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-all;
}

.related-report-footer {
  padding: 12px 14px;
  border-radius: 12px;
  background: #fff;
  border: 1px dashed #cbd5e1;
}

.related-report-footer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.related-report-footer span {
  display: block;
  color: #64748b;
  font-size: 12px;
}

.related-report-footer p {
  margin: 0;
  color: #0f172a;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
}

.governance-timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.governance-item {
  padding: 14px 16px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e2e8f0;
}

.governance-item-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.governance-item-head strong {
  color: #0f172a;
  font-size: 14px;
}

.governance-item-head span {
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
}

.governance-item p {
  margin: 10px 0 8px;
  color: #475569;
  line-height: 1.7;
}

.governance-item em {
  color: #94a3b8;
  font-size: 12px;
  font-style: normal;
}

.audit-section {
  margin-top: 22px;
  padding: 18px;
  border-radius: 18px;
  border: 1px dashed #cbd5e1;
  background: linear-gradient(180deg, #f8fbff 0%, #f8fafc 100%);
}

.audit-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.audit-head h3 {
  margin: 0;
  font-size: 16px;
  color: #0f172a;
}

.audit-head p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.audit-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.audit-meta-item {
  border-radius: 14px;
  background: #fff;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
}

.audit-meta-item.full-width {
  grid-column: 1 / -1;
}

.audit-meta-item span {
  display: block;
  color: #64748b;
  margin-bottom: 8px;
  font-size: 13px;
}

.audit-meta-item strong {
  color: #0f172a;
  font-size: 14px;
  word-break: break-all;
}

.deadline-status-wrap {
  display: flex;
  align-items: center;
  min-height: 24px;
}

.audit-placeholder {
  margin-top: 16px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.86);
  color: #475569;
  line-height: 1.8;
}

.audit-members {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.audit-member-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e2e8f0;
}

.audit-member-card strong {
  color: #0f172a;
  font-size: 14px;
}

.audit-member-card span,
.audit-member-card em {
  color: #64748b;
  font-size: 13px;
  font-style: normal;
}

.audit-timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.audit-message {
  padding: 14px 16px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e2e8f0;
}

.audit-message-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.audit-message-meta strong {
  color: #0f172a;
  font-size: 14px;
}

.audit-message-meta span {
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.audit-message-content {
  color: #334155;
  line-height: 1.8;
  word-break: break-word;
}

.audit-message-image {
  width: min(260px, 100%);
  max-height: 260px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  object-fit: cover;
  display: block;
}

.beauty-stats-bar strong {
  color: #0f172a;
}

.tag-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-chip {
  padding: 8px 14px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4338ca;
  font-size: 13px;
  font-weight: 600;
}

.beauty-editor-form {
  margin-top: 8px;
}

.editor-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.editor-grid :deep(.full) {
  grid-column: 1 / -1;
}

.editor-number,
.editor-grid :deep(.el-select) {
  width: 100%;
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.upload-item {
  margin-bottom: 0;
}

.upload-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.upload-preview,
.upload-placeholder {
  width: 100%;
  height: 220px;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.upload-preview {
  object-fit: cover;
  display: block;
}

.upload-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 14px;
}

.editor-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.editor-tip {
  color: #64748b;
  font-size: 13px;
}

.editor-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

@media (max-width: 1280px) {
  .panel-grid,
  .errand-kpi-grid,
  .beauty-admin-kpi-grid,
  .rules-config-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .beauty-overview-grid,
  .beauty-admin-overview,
  .errand-admin-overview {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .dashboard {
    grid-template-columns: 1fr;
  }

  .sidebar {
    min-height: auto;
  }

  .content-hero h1 {
    font-size: 24px;
  }

  .panel-grid,
  .errand-kpi-grid,
  .beauty-admin-kpi-grid,
  .rules-config-grid,
  .detail-grid,
  .audit-meta,
  .preview-section,
  .editor-grid,
  .upload-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-input,
  .toolbar-select {
    width: 100%;
  }
}
</style>
