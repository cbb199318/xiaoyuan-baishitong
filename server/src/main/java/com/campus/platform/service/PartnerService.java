package com.campus.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.platform.common.BusinessException;
import com.campus.platform.dto.PartnerConversationMessageRequest;
import com.campus.platform.dto.PartnerConversationStatusRequest;
import com.campus.platform.dto.PartnerDemandSaveRequest;
import com.campus.platform.entity.PartnerConversation;
import com.campus.platform.entity.PartnerConversationMessage;
import com.campus.platform.entity.PartnerDemand;
import com.campus.platform.entity.PartnerReport;
import com.campus.platform.entity.User;
import com.campus.platform.entity.UserProfile;
import com.campus.platform.mapper.PartnerConversationMapper;
import com.campus.platform.mapper.PartnerConversationMessageMapper;
import com.campus.platform.mapper.PartnerDemandMapper;
import com.campus.platform.mapper.PartnerReportMapper;
import com.campus.platform.mapper.UserMapper;
import com.campus.platform.mapper.UserProfileMapper;
import com.campus.platform.vo.PartnerMobileChatMessageVO;
import com.campus.platform.vo.PartnerMobileConversationVO;
import com.campus.platform.vo.PartnerMobileDemandVO;
import com.campus.platform.vo.PartnerMobileProfileVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private static final Set<String> PUBLIC_VISIBLE_STATUSES = Set.of("APPROVED");

    private final PartnerConversationMapper partnerConversationMapper;
    private final PartnerConversationMessageMapper partnerConversationMessageMapper;
    private final PartnerDemandMapper partnerDemandMapper;
    private final PartnerReportMapper partnerReportMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final ObjectMapper objectMapper;

    public List<PartnerMobileDemandVO> listVisibleDemands(String keyword, String type, Long publisherId) {
        String normalizedType = hasText(type) && !"ALL".equalsIgnoreCase(type) ? canonicalType(type) : "";
        String legacyDisplayType = hasText(normalizedType) ? displayType(normalizedType) : "";
        List<PartnerDemand> demands = partnerDemandMapper.selectList(
            new LambdaQueryWrapper<PartnerDemand>()
                .in(PartnerDemand::getStatus, PUBLIC_VISIBLE_STATUSES)
                .eq(publisherId != null, PartnerDemand::getPublisherUserId, publisherId)
                .and(hasText(normalizedType), wrapper -> wrapper
                    .eq(PartnerDemand::getType, normalizedType)
                    .or()
                    .eq(PartnerDemand::getType, legacyDisplayType)
                    .or()
                    .eq(PartnerDemand::getType, type))
                .and(hasText(keyword), wrapper -> wrapper
                    .like(PartnerDemand::getTitle, keyword)
                    .or()
                    .like(PartnerDemand::getType, keyword)
                    .or()
                    .like(PartnerDemand::getLocation, keyword)
                    .or()
                    .like(PartnerDemand::getDescription, keyword)
                    .or()
                    .like(PartnerDemand::getPublisherName, keyword))
                .orderByDesc(PartnerDemand::getCreatedAt)
                .orderByDesc(PartnerDemand::getId)
        );
        return toMobileDemands(demands);
    }

    public PartnerMobileDemandVO detail(Long id, Long currentUserId) {
        PartnerDemand demand = requireDemand(id);
        boolean canView = PUBLIC_VISIBLE_STATUSES.contains(normalize(demand.getStatus()))
            || Objects.equals(demand.getPublisherUserId(), currentUserId);
        if (!canView) {
            throw new BusinessException(403, "当前需求暂不可查看");
        }
        return toMobileDemand(demand);
    }

    public List<PartnerMobileDemandVO> myDemands(Long userId) {
        List<PartnerDemand> demands = partnerDemandMapper.selectList(
            new LambdaQueryWrapper<PartnerDemand>()
                .eq(PartnerDemand::getPublisherUserId, userId)
                .orderByDesc(PartnerDemand::getCreatedAt)
                .orderByDesc(PartnerDemand::getId)
        );
        return toMobileDemands(demands);
    }

    @Transactional
    public PartnerMobileDemandVO createDemand(Long userId, PartnerDemandSaveRequest request) {
        User user = requireUser(userId);
        PartnerDemand demand = new PartnerDemand();
        applyDemandContent(demand, user, request);
        demand.setPublisherUserId(userId);
        demand.setPublisherName(defaultText(user.getNickname(), "搭子同学"));
        demand.setPublisherPhone(user.getPhone());
        demand.setApplyCount(0);
        demand.setStatus("APPROVED");
        demand.setBaseRiskTagsJson(writeTags(List.of()));
        demand.setReviewRemark("前台发布成功，当前已公开展示。");
        demand.setReviewedAt(LocalDateTime.now());
        partnerDemandMapper.insert(demand);
        return toMobileDemand(demand);
    }

    @Transactional
    public PartnerMobileDemandVO updateDemand(Long userId, Long id, PartnerDemandSaveRequest request) {
        PartnerDemand demand = requireOwnDemand(userId, id);
        User user = requireUser(userId);
        applyDemandContent(demand, user, request);
        if ("REJECTED".equalsIgnoreCase(demand.getStatus()) || "OFFLINE".equalsIgnoreCase(demand.getStatus())) {
            demand.setStatus("APPROVED");
            demand.setReviewRemark("需求已更新，重新恢复公开展示。");
        }
        partnerDemandMapper.updateById(demand);
        return toMobileDemand(demand);
    }

    @Transactional
    public PartnerMobileDemandVO offlineDemand(Long userId, Long id) {
        PartnerDemand demand = requireOwnDemand(userId, id);
        demand.setStatus("OFFLINE");
        demand.setReviewRemark("发布者已主动下架该需求。");
        partnerDemandMapper.updateById(demand);
        return toMobileDemand(demand);
    }

    public PartnerMobileProfileVO userProfile(Long userId) {
        User user = requireUser(userId);
        UserProfile profile = userProfileMapper.selectOne(
            new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId)
                .last("limit 1")
        );
        List<PartnerDemand> demands = partnerDemandMapper.selectList(
            new LambdaQueryWrapper<PartnerDemand>()
                .eq(PartnerDemand::getPublisherUserId, userId)
        );
        return PartnerMobileProfileVO.builder()
            .userId(userId)
            .nickname(defaultText(user.getNickname(), "搭子同学"))
            .phone(defaultText(user.getPhone(), ""))
            .bio(profile == null ? "" : defaultText(profile.getBio(), ""))
            .tags(List.of())
            .demandCount(demands.size())
            .activeDemandCount((int) demands.stream().filter(item -> !"OFFLINE".equalsIgnoreCase(item.getStatus())).count())
            .build();
    }

    public List<PartnerMobileConversationVO> conversations(Long userId) {
        List<PartnerConversation> conversations = partnerConversationMapper.selectList(
            new LambdaQueryWrapper<PartnerConversation>()
                .orderByDesc(PartnerConversation::getLastMessageAt)
                .orderByDesc(PartnerConversation::getUpdatedAt)
        );
        List<Long> demandIds = conversations.stream().map(PartnerConversation::getDemandId).distinct().toList();
        Map<Long, PartnerDemand> demandMap = demandIds.isEmpty()
            ? Map.of()
            : partnerDemandMapper.selectList(new LambdaQueryWrapper<PartnerDemand>().in(PartnerDemand::getId, demandIds))
                .stream()
                .collect(Collectors.toMap(PartnerDemand::getId, item -> item));

        return conversations.stream()
            .filter(item -> {
                PartnerDemand demand = demandMap.get(item.getDemandId());
                if (demand == null) {
                    return false;
                }
                return Objects.equals(demand.getPublisherUserId(), userId) || Objects.equals(item.getCounterpartyUserId(), userId);
            })
            .map(item -> toMobileConversation(item, demandMap.get(item.getDemandId())))
            .toList();
    }

    public PartnerMobileConversationVO conversationDetail(Long userId, String conversationId) {
        PartnerConversation conversation = requireConversation(conversationId);
        PartnerDemand demand = requireDemand(conversation.getDemandId());
        validateConversationMember(userId, conversation, demand);
        return toMobileConversation(conversation, demand);
    }

    @Transactional
    public PartnerMobileConversationVO apply(Long userId, Long demandId) {
        PartnerDemand demand = requireDemand(demandId);
        if (!PUBLIC_VISIBLE_STATUSES.contains(normalize(demand.getStatus()))) {
            throw new BusinessException("该搭子需求当前不可申请");
        }
        if (Objects.equals(demand.getPublisherUserId(), userId)) {
            throw new BusinessException("自己发布的需求无需申请");
        }
        User applicant = requireUser(userId);
        String conversationId = buildConversationId(demandId, userId);
        PartnerConversation existing = partnerConversationMapper.selectById(conversationId);
        if (existing != null) {
            return conversationDetail(userId, conversationId);
        }

        LocalDateTime now = LocalDateTime.now();
        PartnerConversation conversation = new PartnerConversation();
        conversation.setId(conversationId);
        conversation.setDemandId(demand.getId());
        conversation.setDemandTitle(displayType(demand.getType()) + " · " + defaultText(demand.getLocation(), "待定地点"));
        conversation.setDemandType(canonicalType(demand.getType()));
        conversation.setCounterpartyName(defaultText(applicant.getNickname(), "搭子同学"));
        conversation.setCounterpartyUserId(userId);
        conversation.setCounterpartyPhone(applicant.getPhone());
        conversation.setStatus("PENDING");
        conversation.setRiskLevel("LOW");
        conversation.setBaseRiskTagsJson("[]");
        conversation.setReviewRemark(null);
        conversation.setUnreadCount(1);
        conversation.setLastMessage("已发起搭子申请");
        conversation.setLastMessageAt(now);
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        partnerConversationMapper.insert(conversation);

        PartnerConversationMessage message = new PartnerConversationMessage();
        message.setConversationId(conversationId);
        message.setSenderName(defaultText(applicant.getNickname(), "搭子同学"));
        message.setSenderRole("报名者");
        message.setType("TEXT");
        message.setContent("你好，我想申请你的" + displayType(demand.getType()) + "需求，方便继续聊一下活动安排吗？");
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        partnerConversationMessageMapper.insert(message);

        demand.setApplyCount((demand.getApplyCount() == null ? 0 : demand.getApplyCount()) + 1);
        partnerDemandMapper.updateById(demand);
        return conversationDetail(userId, conversationId);
    }

    @Transactional
    public PartnerMobileConversationVO sendMessage(Long userId, String conversationId, PartnerConversationMessageRequest request) {
        PartnerConversation conversation = requireConversation(conversationId);
        PartnerDemand demand = requireDemand(conversation.getDemandId());
        validateConversationMember(userId, conversation, demand);
        User user = requireUser(userId);
        LocalDateTime now = LocalDateTime.now();

        PartnerConversationMessage message = new PartnerConversationMessage();
        message.setConversationId(conversationId);
        message.setSenderName(defaultText(user.getNickname(), "搭子同学"));
        message.setSenderRole(Objects.equals(userId, demand.getPublisherUserId()) ? "发布者" : "报名者");
        message.setType(normalizeMessageType(request.getType()));
        message.setContent(request.getContent().trim());
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        partnerConversationMessageMapper.insert(message);

        conversation.setLastMessage("IMAGE".equalsIgnoreCase(message.getType()) ? "[图片消息]" : message.getContent());
        conversation.setLastMessageAt(now);
        if (!Objects.equals(userId, demand.getPublisherUserId())) {
            conversation.setUnreadCount((conversation.getUnreadCount() == null ? 0 : conversation.getUnreadCount()) + 1);
        }
        partnerConversationMapper.updateById(conversation);
        return conversationDetail(userId, conversationId);
    }

    @Transactional
    public PartnerMobileConversationVO markConversationRead(Long userId, String conversationId) {
        PartnerConversation conversation = requireConversation(conversationId);
        PartnerDemand demand = requireDemand(conversation.getDemandId());
        validateConversationMember(userId, conversation, demand);
        if (Objects.equals(userId, demand.getPublisherUserId()) && (conversation.getUnreadCount() == null || conversation.getUnreadCount() > 0)) {
            conversation.setUnreadCount(0);
            partnerConversationMapper.updateById(conversation);
        }
        return conversationDetail(userId, conversationId);
    }

    @Transactional
    public PartnerMobileConversationVO updateConversationStatus(Long userId, String conversationId, PartnerConversationStatusRequest request) {
        PartnerConversation conversation = requireConversation(conversationId);
        PartnerDemand demand = requireDemand(conversation.getDemandId());
        validateConversationMember(userId, conversation, demand);
        if (!Objects.equals(userId, demand.getPublisherUserId())) {
            throw new BusinessException(403, "只有发布者可以处理申请状态");
        }
        String status = normalize(request.getStatus());
        if (!Set.of("PENDING", "ACCEPTED", "REJECTED").contains(status)) {
            throw new BusinessException("当前申请状态不支持");
        }

        LocalDateTime now = LocalDateTime.now();
        conversation.setStatus(status);
        conversation.setLastMessage(defaultText(demand.getPublisherName(), "发布者") + switch (status) {
            case "ACCEPTED" -> "已同意申请，已匹配成功";
            case "REJECTED" -> "已拒绝申请";
            default -> "更新了申请状态";
        });
        conversation.setLastMessageAt(now);
        partnerConversationMapper.updateById(conversation);

        PartnerConversationMessage message = new PartnerConversationMessage();
        message.setConversationId(conversationId);
        message.setSenderName("系统通知");
        message.setSenderRole("系统通知");
        message.setType("TEXT");
        message.setContent(conversation.getLastMessage());
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        partnerConversationMessageMapper.insert(message);
        return conversationDetail(userId, conversationId);
    }

    private List<PartnerMobileDemandVO> toMobileDemands(List<PartnerDemand> demands) {
        Map<Long, Integer> reportCountMap = partnerReportMapper.selectList(new LambdaQueryWrapper<PartnerReport>())
            .stream()
            .collect(Collectors.groupingBy(PartnerReport::getTargetId, Collectors.summingInt(item -> 1)));
        return demands.stream()
            .map(item -> toMobileDemand(item, reportCountMap.getOrDefault(item.getId(), 0)))
            .toList();
    }

    private PartnerMobileDemandVO toMobileDemand(PartnerDemand demand) {
        int reportCount = Math.toIntExact(partnerReportMapper.selectCount(
            new LambdaQueryWrapper<PartnerReport>().eq(PartnerReport::getTargetId, demand.getId())
        ));
        return toMobileDemand(demand, reportCount);
    }

    private PartnerMobileDemandVO toMobileDemand(PartnerDemand demand, int reportCount) {
        int applyCount = demand.getApplyCount() == null ? 0 : demand.getApplyCount();
        int totalSlots = Math.max(1, applyCount + 1);
        int remainingSlots = "OFFLINE".equalsIgnoreCase(demand.getStatus()) ? 0 : Math.max(0, totalSlots - applyCount);
        String canonicalType = canonicalType(demand.getType());
        return PartnerMobileDemandVO.builder()
            .id(demand.getId())
            .title(defaultText(demand.getTitle(), buildTitle(canonicalType, demand.getLocation())))
            .type(canonicalType)
            .timeText(demand.getSchedule())
            .location(demand.getLocation())
            .needTags(readTags(demand.getNeedTagsJson()))
            .userTags(List.of())
            .description(demand.getDescription())
            .remainingSlots(remainingSlots)
            .totalSlots(totalSlots)
            .nickname(defaultText(demand.getPublisherName(), "搭子同学"))
            .publisherId(demand.getPublisherUserId())
            .status(normalize(demand.getStatus()))
            .applyCount(applyCount)
            .reportCount(reportCount)
            .reviewRemark(demand.getReviewRemark())
            .reviewedAt(demand.getReviewedAt())
            .createdAt(demand.getCreatedAt())
            .updatedAt(demand.getUpdatedAt())
            .build();
    }

    private PartnerMobileConversationVO toMobileConversation(PartnerConversation conversation, PartnerDemand demand) {
        List<PartnerMobileChatMessageVO> messages = partnerConversationMessageMapper.selectList(
                new LambdaQueryWrapper<PartnerConversationMessage>()
                    .eq(PartnerConversationMessage::getConversationId, conversation.getId())
                    .orderByAsc(PartnerConversationMessage::getCreatedAt)
                    .orderByAsc(PartnerConversationMessage::getId))
            .stream()
            .map(item -> PartnerMobileChatMessageVO.builder()
                .id(String.valueOf(item.getId()))
                .senderId(resolveMessageSenderId(item, demand, conversation))
                .senderName(item.getSenderName())
                .type(item.getType())
                .content(item.getContent())
                .createdAt(item.getCreatedAt())
                .build())
            .toList();
        return PartnerMobileConversationVO.builder()
            .id(conversation.getId())
            .demandId(demand.getId())
            .demandTitle(displayType(demand.getType()) + " · " + defaultText(demand.getLocation(), "待定地点"))
            .demandSummary(demand.getDescription())
            .demandType(canonicalType(demand.getType()))
            .demandLocation(demand.getLocation())
            .publisherId(demand.getPublisherUserId())
            .publisherName(demand.getPublisherName())
            .applicantId(conversation.getCounterpartyUserId())
            .applicantName(conversation.getCounterpartyName())
            .status(normalize(conversation.getStatus()))
            .latestMessage(defaultText(conversation.getLastMessage(), ""))
            .latestMessageType(messages.isEmpty() ? "TEXT" : defaultText(messages.get(messages.size() - 1).getType(), "TEXT"))
            .unreadCount(conversation.getUnreadCount() == null ? 0 : conversation.getUnreadCount())
            .updatedAt(conversation.getLastMessageAt() == null ? conversation.getUpdatedAt() : conversation.getLastMessageAt())
            .messages(messages)
            .build();
    }

    private void applyDemandContent(PartnerDemand demand, User user, PartnerDemandSaveRequest request) {
        String canonicalType = canonicalType(request.getType());
        demand.setType(canonicalType);
        demand.setTitle(buildTitle(canonicalType, request.getLocation()));
        demand.setSchedule(request.getSchedule().trim());
        demand.setLocation(request.getLocation().trim());
        demand.setDescription(request.getDescription().trim());
        demand.setNeedTagsJson(writeTags(request.getNeedTags()));
        demand.setPublisherName(defaultText(user.getNickname(), "搭子同学"));
        demand.setPublisherPhone(user.getPhone());
    }

    private PartnerDemand requireDemand(Long id) {
        PartnerDemand demand = partnerDemandMapper.selectById(id);
        if (demand == null) {
            throw new BusinessException("搭子需求不存在");
        }
        return demand;
    }

    private PartnerDemand requireOwnDemand(Long userId, Long id) {
        PartnerDemand demand = requireDemand(id);
        if (!Objects.equals(demand.getPublisherUserId(), userId)) {
            throw new BusinessException(403, "无权操作该搭子需求");
        }
        return demand;
    }

    private PartnerConversation requireConversation(String conversationId) {
        PartnerConversation conversation = partnerConversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException("搭子会话不存在");
        }
        return conversation;
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private void validateConversationMember(Long userId, PartnerConversation conversation, PartnerDemand demand) {
        boolean isPublisher = Objects.equals(userId, demand.getPublisherUserId());
        boolean isApplicant = Objects.equals(userId, conversation.getCounterpartyUserId());
        if (!isPublisher && !isApplicant) {
            throw new BusinessException(403, "无权访问该搭子会话");
        }
    }

    private List<String> readTags(String json) {
        if (!hasText(json)) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(objectMapper.readValue(json, new TypeReference<List<String>>() {
            }));
        } catch (Exception exception) {
            return new ArrayList<>();
        }
    }

    private String writeTags(List<String> tags) {
        List<String> safeTags = tags == null ? List.of() : tags.stream()
            .filter(this::hasText)
            .map(String::trim)
            .distinct()
            .limit(8)
            .toList();
        try {
            return objectMapper.writeValueAsString(safeTags);
        } catch (Exception exception) {
            return "[]";
        }
    }

    private Long resolveMessageSenderId(PartnerConversationMessage message, PartnerDemand demand, PartnerConversation conversation) {
        return switch (defaultText(message.getSenderRole(), "")) {
            case "发布者" -> demand.getPublisherUserId();
            case "报名者" -> conversation.getCounterpartyUserId();
            default -> 0L;
        };
    }

    private String buildTitle(String type, String location) {
        return displayType(type) + " · " + defaultText(location, "待定地点");
    }

    private String buildConversationId(Long demandId, Long userId) {
        return "partner-" + demandId + "-" + userId;
    }

    private String normalizeMessageType(String value) {
        return "IMAGE".equals(normalize(value)) ? "IMAGE" : "TEXT";
    }

    private String canonicalType(String value) {
        String normalized = normalize(value);
        return switch (normalized) {
            case "学习搭子", "STUDY" -> "STUDY";
            case "饭搭子", "干饭搭子", "MEAL" -> "MEAL";
            case "运动搭子", "SPORT" -> "SPORT";
            case "出行搭子", "TRAVEL" -> "TRAVEL";
            case "探店搭子", "EXPLORE" -> "EXPLORE";
            default -> normalized;
        };
    }

    private String displayType(String value) {
        return switch (canonicalType(value)) {
            case "STUDY" -> "学习搭子";
            case "MEAL" -> "干饭搭子";
            case "SPORT" -> "运动搭子";
            case "TRAVEL" -> "出行搭子";
            case "EXPLORE" -> "探店搭子";
            default -> defaultText(value, "搭子需求");
        };
    }

    private String normalize(String value) {
        return String.valueOf(value == null ? "" : value).trim().toUpperCase(Locale.ROOT);
    }

    private String defaultText(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
