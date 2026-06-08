package com.campus.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.platform.common.BusinessException;
import com.campus.platform.config.AppProperties;
import com.campus.platform.dto.ErrandAdminUpdateRequest;
import com.campus.platform.dto.ErrandOrderCreateRequest;
import com.campus.platform.dto.ErrandOrderSquareQuery;
import com.campus.platform.dto.ErrandRuleUpdateRequest;
import com.campus.platform.entity.AdminOperationLog;
import com.campus.platform.entity.Conversation;
import com.campus.platform.entity.ConversationMember;
import com.campus.platform.entity.ErrandOrder;
import com.campus.platform.entity.ErrandOrderAttachment;
import com.campus.platform.entity.ErrandOrderChat;
import com.campus.platform.entity.ErrandRuleConfig;
import com.campus.platform.entity.FileAsset;
import com.campus.platform.entity.ReportTicket;
import com.campus.platform.entity.User;
import com.campus.platform.enums.UserStatus;
import com.campus.platform.enums.ConversationType;
import com.campus.platform.enums.ErrandOrderStatus;
import com.campus.platform.enums.ErrandServiceType;
import com.campus.platform.mapper.AdminOperationLogMapper;
import com.campus.platform.mapper.ConversationMapper;
import com.campus.platform.mapper.ConversationMemberMapper;
import com.campus.platform.mapper.ErrandOrderAttachmentMapper;
import com.campus.platform.mapper.ErrandOrderChatMapper;
import com.campus.platform.mapper.ErrandOrderMapper;
import com.campus.platform.mapper.ErrandRuleConfigMapper;
import com.campus.platform.mapper.FileAssetMapper;
import com.campus.platform.mapper.ReportTicketMapper;
import com.campus.platform.mapper.UserMapper;
import com.campus.platform.vo.ErrandConversationVO;
import com.campus.platform.vo.ErrandCounterpartyVO;
import com.campus.platform.vo.ErrandOrderVO;
import com.campus.platform.vo.ErrandRuleVO;
import com.campus.platform.vo.ErrandStatsItemVO;
import com.campus.platform.vo.ErrandStatsVO;
import com.campus.platform.vo.FileAssetVO;
import com.campus.platform.vo.ReportVO;
import com.campus.platform.vo.AdminConversationSnapshotVO;
import com.campus.platform.vo.AdminErrandConversationReviewVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ErrandOrderService {

    private static final long REPORT_HANDLE_DEADLINE_HOURS = 24L;
    private static final long ACCEPT_DEADLINE_WARNING_MINUTES = 60L;

    private final ErrandOrderMapper errandOrderMapper;
    private final ErrandOrderAttachmentMapper errandOrderAttachmentMapper;
    private final ErrandOrderChatMapper errandOrderChatMapper;
    private final ErrandRuleConfigMapper errandRuleConfigMapper;
    private final ConversationMapper conversationMapper;
    private final ConversationMemberMapper conversationMemberMapper;
    private final UserMapper userMapper;
    private final FileAssetMapper fileAssetMapper;
    private final ReportTicketMapper reportTicketMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final MessageService messageService;
    private final AppProperties appProperties;

    public Page<ErrandOrderVO> square(ErrandOrderSquareQuery query, Long userId) {
        expireOrdersIfNeeded();
        LambdaQueryWrapper<ErrandOrder> wrapper = new LambdaQueryWrapper<ErrandOrder>()
            .eq(ErrandOrder::getStatus, ErrandOrderStatus.PUBLISHED.name())
            .eq(ErrandOrder::getPublicVisible, 1)
            .ge(ErrandOrder::getAcceptDeadline, LocalDateTime.now())
            .orderByDesc(ErrandOrder::getCreatedAt);
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.and(item -> item
                .like(ErrandOrder::getServiceType, query.getKeyword())
                .or().like(ErrandOrder::getPickupAddress, query.getKeyword())
                .or().like(ErrandOrder::getDeliveryAddress, query.getKeyword())
                .or().like(ErrandOrder::getRemark, query.getKeyword()));
        }
        Page<ErrandOrder> page = errandOrderMapper.selectPage(Page.of(query.getCurrent(), query.getSize()), wrapper);
        Page<ErrandOrderVO> result = Page.of(query.getCurrent(), query.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(item -> toVo(item, userId)).collect(Collectors.toList()));
        return result;
    }

    @Transactional
    public ErrandOrderVO create(Long userId, ErrandOrderCreateRequest request) {
        validateServiceType(request.getServiceType());
        ensureErrandAccess(userId, "当前账号已被限制使用跑腿服务");
        validateCreateLimit(userId);
        validateBaseFee(request.getBaseFee());
        ErrandRuleConfig ruleConfig = getEffectiveRuleConfig();
        ErrandOrder order = new ErrandOrder();
        order.setOrderNo(generateOrderNo());
        order.setPublisherId(userId);
        order.setServiceType(request.getServiceType());
        order.setPickupAddress(request.getPickupAddress());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPickupTimeText(request.getPickupTimeText());
        order.setDetailContent(request.getDetailContent());
        order.setRemark(request.getRemark());
        order.setBaseFee(request.getBaseFee());
        order.setUrgentFlag(request.getUrgentFlag() ? 1 : 0);
        order.setFragileFlag(request.getFragileFlag() ? 1 : 0);
        order.setUrgentFee(request.getUrgentFlag() ? ruleConfig.getUrgentFee() : BigDecimal.ZERO);
        order.setFragileFee(request.getFragileFlag() ? ruleConfig.getFragileFee() : BigDecimal.ZERO);
        order.setTotalFee(order.getBaseFee().add(order.getUrgentFee()).add(order.getFragileFee()));
        order.setStatus(ErrandOrderStatus.PUBLISHED.name());
        order.setAcceptDeadline(LocalDateTime.now().plusHours(ruleConfig.getAutoExpireHours()));
        order.setPublicVisible(1);
        errandOrderMapper.insert(order);

        for (Long fileId : request.getAttachmentFileIds()) {
            ErrandOrderAttachment attachment = new ErrandOrderAttachment();
            attachment.setOrderId(order.getId());
            attachment.setFileId(fileId);
            errandOrderAttachmentMapper.insert(attachment);
        }
        return toVo(order, userId);
    }

    public ErrandOrderVO detail(Long orderId, Long userId) {
        expireOrdersIfNeeded();
        ErrandOrder order = getOrder(orderId);
        ensureOrderVisibleToUser(order, userId);
        return toVo(order, userId);
    }

    public ErrandOrderVO adminDetail(Long orderId) {
        expireOrdersIfNeeded();
        ErrandOrder order = getOrder(orderId);
        return toVo(order, null);
    }

    public AdminErrandConversationReviewVO adminConversationReviewByOrderId(Long orderId) {
        expireOrdersIfNeeded();
        ErrandOrder order = getOrder(orderId);
        Long conversationId = fetchConversationId(orderId);
        if (conversationId == null) {
            throw new BusinessException("当前订单暂未产生关联会话");
        }
        return buildAdminConversationReview(order, conversationId);
    }

    public AdminErrandConversationReviewVO adminConversationReviewByConversationId(Long conversationId) {
        expireOrdersIfNeeded();
        ErrandOrderChat chat = errandOrderChatMapper.selectOne(new LambdaQueryWrapper<ErrandOrderChat>()
            .eq(ErrandOrderChat::getConversationId, conversationId));
        if (chat == null) {
            throw new BusinessException("当前会话未关联跑腿订单");
        }
        ErrandOrder order = getOrder(chat.getOrderId());
        return buildAdminConversationReview(order, conversationId);
    }

    @Transactional
    public ErrandOrderVO accept(Long orderId, Long userId) {
        ensureErrandAccess(userId, "当前账号已被限制接单，请联系管理员处理");
        validateAcceptLimit(userId);
        ErrandOrder order = getOrder(orderId);
        if (Objects.equals(order.getPublisherId(), userId)) {
            throw new BusinessException("不能承接自己发布的订单");
        }
        if (!ErrandOrderStatus.PUBLISHED.name().equals(order.getStatus()) || order.getRunnerId() != null) {
            throw new BusinessException("订单已被承接");
        }
        if (order.getAcceptDeadline().isBefore(LocalDateTime.now())) {
            order.setStatus(ErrandOrderStatus.EXPIRED.name());
            order.setPublicVisible(0);
            errandOrderMapper.updateById(order);
            throw new BusinessException("订单已超时");
        }
        order.setRunnerId(userId);
        order.setStatus(ErrandOrderStatus.ACCEPTED.name());
        order.setAcceptedAt(LocalDateTime.now());
        order.setPublicVisible(0);
        errandOrderMapper.updateById(order);

        Long conversationId = ensureConversation(order);
        messageService.sendConversationNotice(conversationId, "订单已被承接，双方可开始沟通：" + order.getOrderNo());
        return toVo(order, userId, conversationId);
    }

    @Transactional
    public ErrandOrderVO start(Long orderId, Long userId) {
        ErrandOrder order = getOrder(orderId);
        ensureRunner(order, userId);
        if (!ErrandOrderStatus.ACCEPTED.name().equals(order.getStatus())) {
            throw new BusinessException("当前订单不可开始履约");
        }
        order.setStatus(ErrandOrderStatus.IN_PROGRESS.name());
        errandOrderMapper.updateById(order);
        notifyOrderProgress(order, "跑腿订单已开始处理：" + order.getOrderNo());
        return toVo(order, userId);
    }

    @Transactional
    public ErrandOrderVO deliver(Long orderId, Long userId) {
        ErrandOrder order = getOrder(orderId);
        ensureRunner(order, userId);
        if (!(ErrandOrderStatus.ACCEPTED.name().equals(order.getStatus()) || ErrandOrderStatus.IN_PROGRESS.name().equals(order.getStatus()))) {
            throw new BusinessException("当前订单不可更新为处理中");
        }
        order.setStatus(ErrandOrderStatus.DELIVERING.name());
        order.setPickedUpAt(LocalDateTime.now());
        order.setDeliveredAt(LocalDateTime.now());
        errandOrderMapper.updateById(order);
        notifyOrderProgress(order, "跑腿订单已进入派送/处理中：" + order.getOrderNo());
        return toVo(order, userId);
    }

    @Transactional
    public ErrandOrderVO complete(Long orderId, Long userId) {
        ErrandOrder order = getOrder(orderId);
        if (!Objects.equals(order.getPublisherId(), userId)) {
            throw new BusinessException("只有发布人可以确认完成");
        }
        if (!ErrandOrderStatus.DELIVERING.name().equals(order.getStatus())) {
            throw new BusinessException("当前订单不可确认完成");
        }
        order.setStatus(ErrandOrderStatus.COMPLETED.name());
        order.setCompletedAt(LocalDateTime.now());
        errandOrderMapper.updateById(order);
        notifyOrderProgress(order, "跑腿订单已完成：" + order.getOrderNo());
        return toVo(order, userId);
    }

    @Transactional
    public ErrandOrderVO cancel(Long orderId, Long userId) {
        ErrandOrder order = getOrder(orderId);
        if (!Objects.equals(order.getPublisherId(), userId)) {
            throw new BusinessException("只有发布人可以取消订单");
        }
        if (!ErrandOrderStatus.PUBLISHED.name().equals(order.getStatus())) {
            throw new BusinessException("当前订单不可取消");
        }
        order.setStatus(ErrandOrderStatus.CANCELLED.name());
        order.setPublicVisible(0);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason("发布人主动取消");
        errandOrderMapper.updateById(order);
        return toVo(order, userId);
    }

    public List<ErrandOrderVO> myPublished(Long userId) {
        expireOrdersIfNeeded();
        return errandOrderMapper.selectList(new LambdaQueryWrapper<ErrandOrder>()
                .eq(ErrandOrder::getPublisherId, userId)
                .orderByDesc(ErrandOrder::getCreatedAt))
            .stream()
            .map(item -> toVo(item, userId))
            .collect(Collectors.toList());
    }

    public List<ErrandOrderVO> myAccepted(Long userId) {
        expireOrdersIfNeeded();
        return errandOrderMapper.selectList(new LambdaQueryWrapper<ErrandOrder>()
                .eq(ErrandOrder::getRunnerId, userId)
                .orderByDesc(ErrandOrder::getCreatedAt))
            .stream()
            .map(item -> toVo(item, userId))
            .collect(Collectors.toList());
    }

    @Transactional
    public ErrandConversationVO getConversation(Long orderId, Long userId) {
        ErrandOrder order = getOrder(orderId);
        ensureParticipant(order, userId);
        return new ErrandConversationVO(ensureConversation(order));
    }

    public void markDisputedByReport(Long orderId) {
        ErrandOrder order = getOrder(orderId);
        if (!ErrandOrderStatus.COMPLETED.name().equals(order.getStatus())
            && !ErrandOrderStatus.CANCELLED.name().equals(order.getStatus())
            && !ErrandOrderStatus.EXPIRED.name().equals(order.getStatus())) {
            order.setStatus(ErrandOrderStatus.DISPUTED.name());
            order.setPublicVisible(0);
            errandOrderMapper.updateById(order);
            notifyOrderProgress(order, "跑腿订单已进入争议处理：" + order.getOrderNo());
        }
    }

    public Page<ErrandOrderVO> adminPage(String keyword, String status, String serviceType, String flowState, String alertType, int current, int size) {
        expireOrdersIfNeeded();
        LambdaQueryWrapper<ErrandOrder> wrapper = new LambdaQueryWrapper<ErrandOrder>().orderByDesc(ErrandOrder::getCreatedAt);
        if (keyword != null && !keyword.isBlank()) {
            List<Long> matchedUserIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getPhone, keyword)
                    .or().like(User::getNickname, keyword))
                .stream()
                .map(User::getId)
                .toList();
            wrapper.and(item -> item
                .like(ErrandOrder::getOrderNo, keyword)
                .or().like(ErrandOrder::getPickupAddress, keyword)
                .or().like(ErrandOrder::getDeliveryAddress, keyword)
                .or(!matchedUserIds.isEmpty(), user -> user.in(ErrandOrder::getPublisherId, matchedUserIds))
                .or(!matchedUserIds.isEmpty(), user -> user.in(ErrandOrder::getRunnerId, matchedUserIds)));
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(ErrandOrder::getStatus, status);
        }
        if (serviceType != null && !serviceType.isBlank()) {
            wrapper.eq(ErrandOrder::getServiceType, serviceType);
        }
        if (flowState != null && !flowState.isBlank()) {
            switch (flowState.toUpperCase()) {
                case "PUBLIC" -> wrapper
                    .eq(ErrandOrder::getStatus, ErrandOrderStatus.PUBLISHED.name())
                    .eq(ErrandOrder::getPublicVisible, 1);
                case "LOCKED" -> wrapper
                    .in(ErrandOrder::getStatus, List.of(
                        ErrandOrderStatus.ACCEPTED.name(),
                        ErrandOrderStatus.IN_PROGRESS.name(),
                        ErrandOrderStatus.DELIVERING.name(),
                        ErrandOrderStatus.DISPUTED.name()
                    ))
                    .eq(ErrandOrder::getPublicVisible, 0);
                case "ARCHIVED" -> wrapper.in(ErrandOrder::getStatus, List.of(
                    ErrandOrderStatus.COMPLETED.name(),
                    ErrandOrderStatus.CANCELLED.name(),
                    ErrandOrderStatus.EXPIRED.name()
                ));
                default -> throw new BusinessException("不支持的流转状态筛选条件");
            }
        }
        if (alertType != null && !alertType.isBlank()) {
            List<ErrandOrderVO> filtered = errandOrderMapper.selectList(wrapper).stream()
                .map(item -> toVo(item, null))
                .filter(item -> matchesAlertType(item, alertType))
                .collect(Collectors.toList());
            long total = filtered.size();
            int start = Math.max(0, (current - 1) * size);
            int end = Math.min(filtered.size(), start + size);
            List<ErrandOrderVO> pageRecords = start >= end ? Collections.emptyList() : filtered.subList(start, end);
            Page<ErrandOrderVO> result = Page.of(current, size, total);
            result.setRecords(pageRecords);
            return result;
        }

        Page<ErrandOrder> page = errandOrderMapper.selectPage(Page.of(current, size), wrapper);
        Page<ErrandOrderVO> result = Page.of(current, size, page.getTotal());
        result.setRecords(page.getRecords().stream().map(item -> toVo(item, null)).collect(Collectors.toList()));
        return result;
    }

    @Transactional
    public ErrandOrderVO adminUpdate(Long adminId, Long orderId, ErrandAdminUpdateRequest request) {
        ErrandOrder order = getOrder(orderId);
        String noticeMessage = null;
        switch (request.getAction()) {
            case "FORCE_CANCEL" -> {
                order.setStatus(ErrandOrderStatus.CANCELLED.name());
                order.setPublicVisible(0);
                order.setCancelledAt(LocalDateTime.now());
                order.setCancelReason(request.getRemark() == null || request.getRemark().isBlank() ? "后台强制取消" : request.getRemark());
                noticeMessage = "跑腿订单已被后台取消：" + order.getOrderNo();
            }
            case "RESTORE_PUBLISHED" -> {
                order.setStatus(ErrandOrderStatus.PUBLISHED.name());
                order.setRunnerId(null);
                order.setAcceptedAt(null);
                order.setPickedUpAt(null);
                order.setDeliveredAt(null);
                order.setCompletedAt(null);
                order.setCancelledAt(null);
                order.setCancelReason(null);
                order.setPublicVisible(1);
                noticeMessage = "跑腿订单已恢复公开，可重新接单：" + order.getOrderNo();
            }
            case "MARK_DISPUTED" -> {
                order.setStatus(ErrandOrderStatus.DISPUTED.name());
                order.setPublicVisible(0);
                noticeMessage = "跑腿订单已进入争议处理：" + order.getOrderNo();
            }
            case "MARK_COMPLETED" -> {
                order.setStatus(ErrandOrderStatus.COMPLETED.name());
                order.setCompletedAt(LocalDateTime.now());
                order.setPublicVisible(0);
                noticeMessage = "跑腿订单已由后台确认完成：" + order.getOrderNo();
            }
            default -> throw new BusinessException("不支持的订单处理动作");
        }
        errandOrderMapper.updateById(order);
        if (noticeMessage != null) {
            notifyOrderProgress(order, noticeMessage);
        }

        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(adminId);
        log.setOperationType("ERRAND_ORDER_" + request.getAction());
        log.setTargetType("ERRAND_ORDER");
        log.setTargetId(orderId);
        log.setRemark(request.getRemark());
        adminOperationLogMapper.insert(log);
        return toVo(order, null);
    }

    public ErrandRuleVO rules() {
        return toRuleVo(getEffectiveRuleConfig());
    }

    public ErrandRuleVO updateRules(Long adminId, ErrandRuleUpdateRequest request) {
        if (request.getMaxBaseFee().compareTo(request.getMinBaseFee()) < 0) {
            throw new BusinessException("最高基础费不能低于最低基础费");
        }
        ErrandRuleConfig config = getOrCreateRuleConfig();
        config.setUrgentFee(request.getUrgentFee());
        config.setFragileFee(request.getFragileFee());
        config.setPublishLimitPerUser(request.getPublishLimitPerUser());
        config.setAcceptLimitPerUser(request.getAcceptLimitPerUser());
        config.setAutoExpireHours(request.getAutoExpireHours());
        config.setMinBaseFee(request.getMinBaseFee());
        config.setMaxBaseFee(request.getMaxBaseFee());
        errandRuleConfigMapper.updateById(config);
        syncRuntimeRuleConfig(config);

        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(adminId);
        log.setOperationType("ERRAND_RULE_UPDATE");
        log.setTargetType("ERRAND_RULE");
        log.setTargetId(config.getId());
        log.setRemark("更新跑腿规则配置");
        adminOperationLogMapper.insert(log);
        return rules();
    }

    public ErrandStatsVO adminStats() {
        expireOrdersIfNeeded();
        long totalOrders = countOrders(null);
        long publicOrders = errandOrderMapper.selectCount(new LambdaQueryWrapper<ErrandOrder>()
            .eq(ErrandOrder::getStatus, ErrandOrderStatus.PUBLISHED.name())
            .eq(ErrandOrder::getPublicVisible, 1));
        long lockedOrders = errandOrderMapper.selectCount(new LambdaQueryWrapper<ErrandOrder>()
            .in(ErrandOrder::getStatus, List.of(
                ErrandOrderStatus.ACCEPTED.name(),
                ErrandOrderStatus.IN_PROGRESS.name(),
                ErrandOrderStatus.DELIVERING.name(),
                ErrandOrderStatus.DISPUTED.name()
            ))
            .eq(ErrandOrder::getPublicVisible, 0));
        long archivedOrders = errandOrderMapper.selectCount(new LambdaQueryWrapper<ErrandOrder>()
            .in(ErrandOrder::getStatus, List.of(
                ErrandOrderStatus.COMPLETED.name(),
                ErrandOrderStatus.CANCELLED.name(),
                ErrandOrderStatus.EXPIRED.name()
            )));
        long acceptedOrders = countOrders(ErrandOrderStatus.ACCEPTED.name());
        long completedOrders = countOrders(ErrandOrderStatus.COMPLETED.name());
        long cancelledOrders = countOrders(ErrandOrderStatus.CANCELLED.name());
        long disputedOrders = countOrders(ErrandOrderStatus.DISPUTED.name());
        long expiredOrders = countOrders(ErrandOrderStatus.EXPIRED.name());
        long processingReports = countErrandReportsByStatus("PROCESSING");
        long resolvedReports = countErrandReportsByStatus("RESOLVED");
        long rejectedReports = countErrandReportsByStatus("REJECTED");
        long reportTotal = countErrandReportsByStatus(null);

        BigDecimal grossTransactionAmount = sumOrderAmount(List.of(
            ErrandOrderStatus.PUBLISHED.name(),
            ErrandOrderStatus.ACCEPTED.name(),
            ErrandOrderStatus.IN_PROGRESS.name(),
            ErrandOrderStatus.DELIVERING.name(),
            ErrandOrderStatus.COMPLETED.name(),
            ErrandOrderStatus.DISPUTED.name()
        ));
        BigDecimal activeTransactionAmount = sumOrderAmount(List.of(
            ErrandOrderStatus.ACCEPTED.name(),
            ErrandOrderStatus.IN_PROGRESS.name(),
            ErrandOrderStatus.DELIVERING.name(),
            ErrandOrderStatus.DISPUTED.name()
        ));
        BigDecimal completedTransactionAmount = sumOrderAmount(List.of(ErrandOrderStatus.COMPLETED.name()));

        List<ErrandStatsItemVO> statusBreakdown = new ArrayList<>();
        for (ErrandOrderStatus item : ErrandOrderStatus.values()) {
            statusBreakdown.add(new ErrandStatsItemVO(item.name(), countOrders(item.name())));
        }

        List<ErrandStatsItemVO> reportTypeBreakdown = reportTicketMapper.selectList(new LambdaQueryWrapper<ReportTicket>()
                .eq(ReportTicket::getModule, "errand")
                .orderByDesc(ReportTicket::getCreatedAt))
            .stream()
            .collect(Collectors.groupingBy(ReportTicket::getReportType, Collectors.counting()))
            .entrySet()
            .stream()
            .map(entry -> new ErrandStatsItemVO(entry.getKey(), entry.getValue()))
            .sorted((left, right) -> Long.compare(right.getCount(), left.getCount()))
            .collect(Collectors.toList());

        return ErrandStatsVO.builder()
            .totalOrders(totalOrders)
            .publicOrders(publicOrders)
            .lockedOrders(lockedOrders)
            .archivedOrders(archivedOrders)
            .acceptedOrders(acceptedOrders)
            .completedOrders(completedOrders)
            .cancelledOrders(cancelledOrders)
            .disputedOrders(disputedOrders)
            .expiredOrders(expiredOrders)
            .acceptanceRate(buildRate(acceptedOrders + completedOrders + disputedOrders, totalOrders))
            .completionRate(buildRate(completedOrders, totalOrders))
            .cancelRate(buildRate(cancelledOrders + expiredOrders, totalOrders))
            .grossTransactionAmount(grossTransactionAmount)
            .activeTransactionAmount(activeTransactionAmount)
            .completedTransactionAmount(completedTransactionAmount)
            .reportTotal(reportTotal)
            .processingReports(processingReports)
            .resolvedReports(resolvedReports)
            .rejectedReports(rejectedReports)
            .statusBreakdown(statusBreakdown)
            .reportTypeBreakdown(reportTypeBreakdown)
            .build();
    }

    public boolean exists(Long orderId) {
        return errandOrderMapper.selectById(orderId) != null;
    }

    private void validateServiceType(String serviceType) {
        try {
            ErrandServiceType.valueOf(serviceType);
        } catch (Exception ex) {
            throw new BusinessException("服务类型不合法");
        }
    }

    private ErrandOrder getOrder(Long orderId) {
        ErrandOrder order = errandOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("跑腿订单不存在");
        }
        return order;
    }

    private void ensureRunner(ErrandOrder order, Long userId) {
        if (!Objects.equals(order.getRunnerId(), userId)) {
            throw new BusinessException("只有接单人可以执行该操作");
        }
    }

    private void ensureParticipant(ErrandOrder order, Long userId) {
        if (userId == null || (!Objects.equals(order.getPublisherId(), userId) && !Objects.equals(order.getRunnerId(), userId))) {
            throw new BusinessException(403, "无权访问该订单会话");
        }
    }

    private void ensureOrderVisibleToUser(ErrandOrder order, Long userId) {
        boolean isParticipant = userId != null && (Objects.equals(order.getPublisherId(), userId) || Objects.equals(order.getRunnerId(), userId));
        boolean isPublicOrder = ErrandOrderStatus.PUBLISHED.name().equals(order.getStatus())
            && order.getPublicVisible() != null
            && order.getPublicVisible() == 1;
        if (!isParticipant && !isPublicOrder) {
            throw new BusinessException(403, "无权查看该订单");
        }
    }

    private void expireOrdersIfNeeded() {
        List<ErrandOrder> expiring = errandOrderMapper.selectList(new LambdaQueryWrapper<ErrandOrder>()
            .eq(ErrandOrder::getStatus, ErrandOrderStatus.PUBLISHED.name())
            .eq(ErrandOrder::getPublicVisible, 1)
            .lt(ErrandOrder::getAcceptDeadline, LocalDateTime.now()));
        for (ErrandOrder order : expiring) {
            order.setStatus(ErrandOrderStatus.EXPIRED.name());
            order.setPublicVisible(0);
            errandOrderMapper.updateById(order);
        }
    }

    private String generateOrderNo() {
        return "ET" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + System.currentTimeMillis() % 1000;
    }

    private Long ensureConversation(ErrandOrder order) {
        ErrandOrderChat chat = errandOrderChatMapper.selectOne(new LambdaQueryWrapper<ErrandOrderChat>().eq(ErrandOrderChat::getOrderId, order.getId()));
        if (chat != null) {
            createMember(chat.getConversationId(), order.getPublisherId());
            if (order.getRunnerId() != null) {
                createMember(chat.getConversationId(), order.getRunnerId());
            }
            return chat.getConversationId();
        }
        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.PRIVATE.name());
        conversation.setTitle("跑腿订单 " + order.getOrderNo());
        conversationMapper.insert(conversation);

        createMember(conversation.getId(), order.getPublisherId());
        if (order.getRunnerId() != null) {
            createMember(conversation.getId(), order.getRunnerId());
        }

        ErrandOrderChat orderChat = new ErrandOrderChat();
        orderChat.setOrderId(order.getId());
        orderChat.setConversationId(conversation.getId());
        errandOrderChatMapper.insert(orderChat);
        return conversation.getId();
    }

    private void createMember(Long conversationId, Long userId) {
        ConversationMember member = conversationMemberMapper.selectOne(new LambdaQueryWrapper<ConversationMember>()
            .eq(ConversationMember::getConversationId, conversationId)
            .eq(ConversationMember::getUserId, userId));
        if (member != null) {
            return;
        }
        member = new ConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setUnreadCount(0);
        conversationMemberMapper.insert(member);
    }

    private void notifyOrderProgress(ErrandOrder order, String message) {
        Long conversationId = ensureConversation(order);
        messageService.sendConversationNotice(conversationId, message);
    }

    private AdminErrandConversationReviewVO buildAdminConversationReview(ErrandOrder order, Long conversationId) {
        AdminConversationSnapshotVO snapshot = messageService.adminConversationSnapshot(conversationId);
        User publisher = userMapper.selectById(order.getPublisherId());
        User runner = order.getRunnerId() == null ? null : userMapper.selectById(order.getRunnerId());
        return AdminErrandConversationReviewVO.builder()
            .orderId(order.getId())
            .orderNo(order.getOrderNo())
            .orderStatus(order.getStatus())
            .conversationId(snapshot.getConversationId())
            .conversationType(snapshot.getConversationType())
            .conversationTitle(snapshot.getConversationTitle())
            .publisher(toCounterparty(publisher))
            .runner(toCounterparty(runner))
            .members(snapshot.getMembers())
            .messages(snapshot.getMessages())
            .build();
    }

    private ErrandOrderVO toVo(ErrandOrder order, Long currentUserId) {
        return toVo(order, currentUserId, fetchConversationId(order.getId()));
    }

    private ErrandOrderVO toVo(ErrandOrder order, Long currentUserId, Long conversationId) {
        User publisher = userMapper.selectById(order.getPublisherId());
        User runner = order.getRunnerId() == null ? null : userMapper.selectById(order.getRunnerId());
        List<ReportVO> relatedReports = listRelatedReports(order.getId());
        List<String> riskTags = buildRiskTags(order, relatedReports);
        int activeReportCount = (int) relatedReports.stream()
            .filter(report -> "PENDING".equalsIgnoreCase(report.getStatus()) || "PROCESSING".equalsIgnoreCase(report.getStatus()))
            .count();
        int overdueReportCount = (int) relatedReports.stream()
            .filter(report -> Boolean.TRUE.equals(report.getIsOverdue()))
            .count();
        return ErrandOrderVO.builder()
            .id(order.getId())
            .orderNo(order.getOrderNo())
            .serviceType(order.getServiceType())
            .pickupAddress(order.getPickupAddress())
            .deliveryAddress(order.getDeliveryAddress())
            .pickupTimeText(order.getPickupTimeText())
            .detailContent(order.getDetailContent())
            .remark(order.getRemark())
            .baseFee(order.getBaseFee())
            .urgentFee(order.getUrgentFee())
            .fragileFee(order.getFragileFee())
            .totalFee(order.getTotalFee())
            .urgentFlag(order.getUrgentFlag() != null && order.getUrgentFlag() == 1)
            .fragileFlag(order.getFragileFlag() != null && order.getFragileFlag() == 1)
            .status(order.getStatus())
            .acceptDeadline(order.getAcceptDeadline())
            .acceptedAt(order.getAcceptedAt())
            .pickedUpAt(order.getPickedUpAt())
            .deliveredAt(order.getDeliveredAt())
            .completedAt(order.getCompletedAt())
            .cancelledAt(order.getCancelledAt())
            .cancelReason(order.getCancelReason())
            .publicVisible(order.getPublicVisible() != null && order.getPublicVisible() == 1)
            .publisher(toCounterparty(publisher))
            .runner(toCounterparty(runner))
            .attachments(listAttachments(order.getId()))
            .relatedReports(relatedReports)
            .activeReportCount(activeReportCount)
            .overdueReportCount(overdueReportCount)
            .riskTags(riskTags)
            .conversationId(conversationId)
            .canAccept(currentUserId != null && !Objects.equals(order.getPublisherId(), currentUserId)
                && ErrandOrderStatus.PUBLISHED.name().equals(order.getStatus()) && order.getRunnerId() == null)
            .canCancel(currentUserId != null && Objects.equals(order.getPublisherId(), currentUserId)
                && ErrandOrderStatus.PUBLISHED.name().equals(order.getStatus()))
            .canStart(currentUserId != null && Objects.equals(order.getRunnerId(), currentUserId)
                && ErrandOrderStatus.ACCEPTED.name().equals(order.getStatus()))
            .canDeliver(currentUserId != null && Objects.equals(order.getRunnerId(), currentUserId)
                && (ErrandOrderStatus.ACCEPTED.name().equals(order.getStatus()) || ErrandOrderStatus.IN_PROGRESS.name().equals(order.getStatus())))
            .canComplete(currentUserId != null && Objects.equals(order.getPublisherId(), currentUserId)
                && ErrandOrderStatus.DELIVERING.name().equals(order.getStatus()))
            .canReport(currentUserId != null && (Objects.equals(order.getPublisherId(), currentUserId) || Objects.equals(order.getRunnerId(), currentUserId)))
            .createdAt(order.getCreatedAt())
            .build();
    }

    private Long fetchConversationId(Long orderId) {
        ErrandOrderChat chat = errandOrderChatMapper.selectOne(new LambdaQueryWrapper<ErrandOrderChat>().eq(ErrandOrderChat::getOrderId, orderId));
        return chat == null ? null : chat.getConversationId();
    }

    private List<FileAssetVO> listAttachments(Long orderId) {
        return errandOrderAttachmentMapper.selectList(new LambdaQueryWrapper<ErrandOrderAttachment>().eq(ErrandOrderAttachment::getOrderId, orderId))
            .stream()
            .map(item -> {
                FileAsset asset = fileAssetMapper.selectById(item.getFileId());
                if (asset == null) {
                    return null;
                }
                return FileAssetVO.builder()
                    .fileId(asset.getId())
                    .url(appProperties.getFile().getPublicBaseUrl() + asset.getRelativePath())
                    .originalName(asset.getOriginalName())
                    .build();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private ErrandCounterpartyVO toCounterparty(User user) {
        if (user == null) {
            return null;
        }
        return ErrandCounterpartyVO.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .phone(user.getPhone())
            .build();
    }

    private List<ReportVO> listRelatedReports(Long orderId) {
        return reportTicketMapper.selectList(new LambdaQueryWrapper<ReportTicket>()
                .eq(ReportTicket::getModule, "errand")
                .eq(ReportTicket::getTargetType, "order")
                .eq(ReportTicket::getTargetId, orderId)
                .orderByDesc(ReportTicket::getCreatedAt))
            .stream()
            .map(this::toRelatedReportVo)
            .collect(Collectors.toList());
    }

    private ReportVO toRelatedReportVo(ReportTicket report) {
        LocalDateTime deadlineAt = report.getCreatedAt() == null ? null : report.getCreatedAt().plusHours(REPORT_HANDLE_DEADLINE_HOURS);
        LocalDateTime referenceTime = report.getHandledAt() != null ? report.getHandledAt() : LocalDateTime.now();
        Long remainingMinutes = deadlineAt == null ? null : ChronoUnit.MINUTES.between(referenceTime, deadlineAt);
        Boolean isOverdue = remainingMinutes == null ? null : remainingMinutes < 0;
        return ReportVO.builder()
            .id(report.getId())
            .module(report.getModule())
            .targetType(report.getTargetType())
            .targetId(report.getTargetId())
            .reportType(report.getReportType())
            .description(report.getDescription())
            .contactPhone(report.getContactPhone())
            .status(report.getStatus())
            .handleRemark(report.getHandleRemark())
            .handledBy(report.getHandledBy())
            .handledAt(report.getHandledAt())
            .deadlineAt(deadlineAt)
            .isOverdue(isOverdue)
            .remainingMinutes(remainingMinutes)
            .createdAt(report.getCreatedAt())
            .build();
    }

    private List<String> buildRiskTags(ErrandOrder order, List<ReportVO> relatedReports) {
        List<String> tags = new ArrayList<>();
        String status = String.valueOf(order.getStatus()).toUpperCase();
        LocalDateTime now = LocalDateTime.now();
        if ("DISPUTED".equals(status)) {
            tags.add("争议中");
        }
        if ("PUBLISHED".equals(status) && order.getAcceptDeadline() != null) {
            long minutes = ChronoUnit.MINUTES.between(now, order.getAcceptDeadline());
            if (minutes >= 0 && minutes <= ACCEPT_DEADLINE_WARNING_MINUTES) {
                tags.add("临近接单超时");
            }
        }
        if ("EXPIRED".equals(status)) {
            tags.add("超时未接单");
        }
        boolean hasActiveReports = relatedReports.stream()
            .anyMatch(report -> "PENDING".equalsIgnoreCase(report.getStatus()) || "PROCESSING".equalsIgnoreCase(report.getStatus()));
        if (hasActiveReports) {
            tags.add("举报待处理");
        }
        boolean hasOverdueReports = relatedReports.stream().anyMatch(report -> Boolean.TRUE.equals(report.getIsOverdue()));
        if (hasOverdueReports) {
            tags.add("举报已超时");
        }
        return tags;
    }

    private boolean matchesAlertType(ErrandOrderVO order, String alertType) {
        return switch (String.valueOf(alertType).toUpperCase()) {
            case "DISPUTED" -> order.getRiskTags() != null && order.getRiskTags().contains("争议中");
            case "EXPIRING_ACCEPT" -> order.getRiskTags() != null && order.getRiskTags().contains("临近接单超时");
            case "EXPIRED_UNACCEPTED" -> order.getRiskTags() != null && order.getRiskTags().contains("超时未接单");
            case "REPORT_PENDING" -> order.getRiskTags() != null && order.getRiskTags().contains("举报待处理");
            case "REPORT_OVERDUE" -> order.getRiskTags() != null && order.getRiskTags().contains("举报已超时");
            default -> true;
        };
    }

    private void ensureErrandAccess(Long userId, String blockedMessage) {
        User user = userMapper.selectById(userId);
        if (user == null || !UserStatus.canUseErrand(user.getStatus())) {
            throw new BusinessException(403, blockedMessage);
        }
    }

    private void validateCreateLimit(Long userId) {
        Integer limit = getEffectiveRuleConfig().getPublishLimitPerUser();
        if (limit == null || limit <= 0) {
            return;
        }
        Long current = errandOrderMapper.selectCount(new LambdaQueryWrapper<ErrandOrder>()
            .eq(ErrandOrder::getPublisherId, userId)
            .in(ErrandOrder::getStatus, List.of(
                ErrandOrderStatus.PUBLISHED.name(),
                ErrandOrderStatus.ACCEPTED.name(),
                ErrandOrderStatus.IN_PROGRESS.name(),
                ErrandOrderStatus.DELIVERING.name(),
                ErrandOrderStatus.DISPUTED.name()
            )));
        if (current != null && current >= limit) {
            throw new BusinessException("当前发单进行中数量已达上限，请先处理已有订单");
        }
    }

    private void validateAcceptLimit(Long userId) {
        Integer limit = getEffectiveRuleConfig().getAcceptLimitPerUser();
        if (limit == null || limit <= 0) {
            return;
        }
        Long current = errandOrderMapper.selectCount(new LambdaQueryWrapper<ErrandOrder>()
            .eq(ErrandOrder::getRunnerId, userId)
            .in(ErrandOrder::getStatus, List.of(
                ErrandOrderStatus.ACCEPTED.name(),
                ErrandOrderStatus.IN_PROGRESS.name(),
                ErrandOrderStatus.DELIVERING.name(),
                ErrandOrderStatus.DISPUTED.name()
            )));
        if (current != null && current >= limit) {
            throw new BusinessException("当前接单进行中数量已达上限，请先完成或处理现有订单");
        }
    }

    private void validateBaseFee(BigDecimal baseFee) {
        if (baseFee == null) {
            throw new BusinessException("基础费用不能为空");
        }
        ErrandRuleConfig config = getEffectiveRuleConfig();
        BigDecimal min = config.getMinBaseFee();
        BigDecimal max = config.getMaxBaseFee();
        if (min != null && baseFee.compareTo(min) < 0) {
            throw new BusinessException("基础费用不能低于平台最低限额");
        }
        if (max != null && baseFee.compareTo(max) > 0) {
            throw new BusinessException("基础费用不能高于平台最高限额");
        }
    }

    private long countOrders(String status) {
        return errandOrderMapper.selectCount(
            status == null
                ? new LambdaQueryWrapper<>()
                : new LambdaQueryWrapper<ErrandOrder>().eq(ErrandOrder::getStatus, status)
        );
    }

    private long countErrandReportsByStatus(String status) {
        LambdaQueryWrapper<ReportTicket> wrapper = new LambdaQueryWrapper<ReportTicket>()
            .eq(ReportTicket::getModule, "errand");
        if (status != null && !status.isBlank()) {
            wrapper.eq(ReportTicket::getStatus, status);
        }
        return reportTicketMapper.selectCount(wrapper);
    }

    private BigDecimal sumOrderAmount(List<String> statuses) {
        return errandOrderMapper.selectList(new LambdaQueryWrapper<ErrandOrder>().in(ErrandOrder::getStatus, statuses))
            .stream()
            .map(ErrandOrder::getTotalFee)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal buildRate(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private ErrandRuleConfig getEffectiveRuleConfig() {
        ErrandRuleConfig config = errandRuleConfigMapper.selectOne(
            new LambdaQueryWrapper<ErrandRuleConfig>().eq(ErrandRuleConfig::getConfigKey, "default")
        );
        if (config != null) {
            syncRuntimeRuleConfig(config);
            return config;
        }
        return buildFallbackRuleConfig();
    }

    private ErrandRuleConfig getOrCreateRuleConfig() {
        ErrandRuleConfig config = errandRuleConfigMapper.selectOne(
            new LambdaQueryWrapper<ErrandRuleConfig>().eq(ErrandRuleConfig::getConfigKey, "default")
        );
        if (config != null) {
            return config;
        }
        config = buildFallbackRuleConfig();
        config.setConfigKey("default");
        errandRuleConfigMapper.insert(config);
        return config;
    }

    private ErrandRuleConfig buildFallbackRuleConfig() {
        ErrandRuleConfig config = new ErrandRuleConfig();
        config.setConfigKey("default");
        config.setUrgentFee(appProperties.getErrand().getUrgentFee());
        config.setFragileFee(appProperties.getErrand().getFragileFee());
        config.setPublishLimitPerUser(appProperties.getErrand().getPublishLimitPerUser());
        config.setAcceptLimitPerUser(appProperties.getErrand().getAcceptLimitPerUser());
        config.setAutoExpireHours(appProperties.getErrand().getAutoExpireHours());
        config.setMinBaseFee(appProperties.getErrand().getMinBaseFee());
        config.setMaxBaseFee(appProperties.getErrand().getMaxBaseFee());
        return config;
    }

    private void syncRuntimeRuleConfig(ErrandRuleConfig config) {
        appProperties.getErrand().setUrgentFee(config.getUrgentFee());
        appProperties.getErrand().setFragileFee(config.getFragileFee());
        appProperties.getErrand().setPublishLimitPerUser(config.getPublishLimitPerUser());
        appProperties.getErrand().setAcceptLimitPerUser(config.getAcceptLimitPerUser());
        appProperties.getErrand().setAutoExpireHours(config.getAutoExpireHours());
        appProperties.getErrand().setMinBaseFee(config.getMinBaseFee());
        appProperties.getErrand().setMaxBaseFee(config.getMaxBaseFee());
    }

    private ErrandRuleVO toRuleVo(ErrandRuleConfig config) {
        return new ErrandRuleVO(
            config.getUrgentFee(),
            config.getFragileFee(),
            config.getPublishLimitPerUser(),
            config.getAcceptLimitPerUser(),
            config.getAutoExpireHours(),
            config.getMinBaseFee(),
            config.getMaxBaseFee()
        );
    }
}
