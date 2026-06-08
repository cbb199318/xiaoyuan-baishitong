package com.campus.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.platform.common.BusinessException;
import com.campus.platform.config.AppProperties;
import com.campus.platform.dto.JobsApplicationCreateRequest;
import com.campus.platform.dto.JobsPostCreateRequest;
import com.campus.platform.dto.JobsPostVisibilityRequest;
import com.campus.platform.dto.JobsResumeSaveRequest;
import com.campus.platform.entity.Conversation;
import com.campus.platform.entity.ConversationMember;
import com.campus.platform.entity.FileAsset;
import com.campus.platform.entity.JobsAccount;
import com.campus.platform.entity.JobsApplication;
import com.campus.platform.entity.JobsFavorite;
import com.campus.platform.entity.JobsMerchantQualification;
import com.campus.platform.entity.JobsPost;
import com.campus.platform.entity.JobsPostChat;
import com.campus.platform.entity.JobsResume;
import com.campus.platform.entity.Message;
import com.campus.platform.entity.User;
import com.campus.platform.entity.UserProfile;
import com.campus.platform.enums.ConversationType;
import com.campus.platform.mapper.ConversationMapper;
import com.campus.platform.mapper.ConversationMemberMapper;
import com.campus.platform.mapper.FileAssetMapper;
import com.campus.platform.mapper.JobsAccountMapper;
import com.campus.platform.mapper.JobsApplicationMapper;
import com.campus.platform.mapper.JobsFavoriteMapper;
import com.campus.platform.mapper.JobsMerchantQualificationMapper;
import com.campus.platform.mapper.JobsPostChatMapper;
import com.campus.platform.mapper.JobsPostMapper;
import com.campus.platform.mapper.JobsResumeMapper;
import com.campus.platform.mapper.MessageMapper;
import com.campus.platform.mapper.UserMapper;
import com.campus.platform.mapper.UserProfileMapper;
import com.campus.platform.vo.FileAssetVO;
import com.campus.platform.vo.JobsConversationVO;
import com.campus.platform.vo.JobsMobileApplicationVO;
import com.campus.platform.vo.JobsMobilePostVO;
import com.campus.platform.vo.JobsResumeVO;
import com.campus.platform.vo.JobsReviewProgressVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobsService {

    private static final DateTimeFormatter MOBILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Set<String> PUBLIC_VISIBLE_STATUSES = Set.of("APPROVED");

    private final JobsPostMapper jobsPostMapper;
    private final JobsFavoriteMapper jobsFavoriteMapper;
    private final JobsApplicationMapper jobsApplicationMapper;
    private final JobsPostChatMapper jobsPostChatMapper;
    private final JobsResumeMapper jobsResumeMapper;
    private final JobsAccountMapper jobsAccountMapper;
    private final JobsMerchantQualificationMapper jobsMerchantQualificationMapper;
    private final ConversationMapper conversationMapper;
    private final ConversationMemberMapper conversationMemberMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final FileAssetMapper fileAssetMapper;
    private final AppProperties appProperties;

    public List<JobsMobilePostVO> listVisiblePosts(String keyword, String category, Long userId) {
        List<JobsPost> posts = jobsPostMapper.selectList(
            new LambdaQueryWrapper<JobsPost>()
                .eq(JobsPost::getPublicVisible, 1)
                .in(JobsPost::getStatus, PUBLIC_VISIBLE_STATUSES)
                .eq(category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category), JobsPost::getCategory, category)
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                    .like(JobsPost::getTitle, keyword)
                    .or()
                    .like(JobsPost::getCategory, keyword)
                    .or()
                    .like(JobsPost::getJobType, keyword)
                    .or()
                    .like(JobsPost::getArea, keyword)
                    .or()
                    .like(JobsPost::getDescription, keyword))
                .orderByDesc(JobsPost::getCreatedAt)
                .orderByDesc(JobsPost::getId)
        );
        return toMobilePosts(posts, userId);
    }

    @Transactional
    public JobsMobilePostVO detail(Long postId, Long userId) {
        JobsPost post = requirePost(postId);
        boolean canView = isVisibleToPublic(post) || Objects.equals(post.getAccountId(), userId);
        if (!canView) {
            throw new BusinessException(403, "无权查看该岗位");
        }
        return toMobilePost(post, userId, countFavorite(post.getId()), resolveQualification(post));
    }

    public List<JobsMobilePostVO> myPosts(Long userId) {
        List<JobsPost> posts = jobsPostMapper.selectList(
            new LambdaQueryWrapper<JobsPost>()
                .eq(JobsPost::getAccountId, userId)
                .orderByDesc(JobsPost::getCreatedAt)
                .orderByDesc(JobsPost::getId)
        );
        return toMobilePosts(posts, userId);
    }

    public List<JobsMobilePostVO> favorites(Long userId) {
        List<JobsFavorite> favorites = jobsFavoriteMapper.selectActiveByUserId(userId);
        List<JobsPost> posts = new ArrayList<>();
        for (JobsFavorite favorite : favorites) {
            JobsPost post = jobsPostMapper.selectById(favorite.getPostId());
            if (post != null && (isVisibleToPublic(post) || Objects.equals(post.getAccountId(), userId))) {
                posts.add(post);
            }
        }
        posts.sort(Comparator.comparing(JobsPost::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(JobsPost::getId, Comparator.nullsLast(Comparator.reverseOrder())));
        return toMobilePosts(posts, userId);
    }

    @Transactional
    public JobsMobilePostVO favorite(Long userId, Long postId) {
        JobsPost post = requirePost(postId);
        if (!isVisibleToPublic(post) && !Objects.equals(post.getAccountId(), userId)) {
            throw new BusinessException("该岗位暂不可收藏");
        }
        JobsFavorite existing = jobsFavoriteMapper.selectAny(postId, userId);
        if (existing == null) {
            JobsFavorite favorite = new JobsFavorite();
            favorite.setPostId(postId);
            favorite.setUserId(userId);
            jobsFavoriteMapper.insert(favorite);
        } else if (existing.getDeleted() != null && existing.getDeleted() == 1) {
            existing.setDeleted(0);
            jobsFavoriteMapper.updateById(existing);
        }
        return toMobilePost(post, userId, countFavorite(postId), resolveQualification(post));
    }

    @Transactional
    public JobsMobilePostVO unfavorite(Long userId, Long postId) {
        JobsPost post = requirePost(postId);
        JobsFavorite existing = jobsFavoriteMapper.selectAny(postId, userId);
        if (existing != null && (existing.getDeleted() == null || existing.getDeleted() == 0)) {
            jobsFavoriteMapper.hardDeleteById(existing.getId());
        }
        return toMobilePost(post, userId, countFavorite(postId), resolveQualification(post));
    }

    @Transactional
    public JobsMobilePostVO createPost(Long userId, JobsPostCreateRequest request) {
        User user = requireUser(userId);
        String roleType = normalizeRoleType(request.getRoleType());
        if (isBusiness(roleType) && !Objects.equals(resolvePublishRole(userId), "BUSINESS")) {
            throw new BusinessException("请先在个人资料中切换为商家身份");
        }
        JobsAccount account = ensureAccount(user, roleType);
        validateAccountPermission(account, true);

        JobsPost post = new JobsPost();
        applyPostContent(post, user, request, roleType);
        post.setAccountId(account.getId());
        post.setApplicantCount(0);
        post.setHeadCount(1);
        post.setCreatedAt(null);
        post.setUpdatedAt(null);
        if (isBusiness(roleType)) {
            post.setStatus("PENDING");
            post.setPublicVisible(0);
            post.setReviewRemark("岗位已提交审核，待管理员处理。");
            upsertQualification(account, user, request);
        } else {
            post.setStatus("APPROVED");
            post.setPublicVisible(1);
            post.setReviewRemark("学生互助需求已直接发布。");
        }
        jobsPostMapper.insert(post);
        return toMobilePost(post, userId, countFavorite(post.getId()), resolveQualification(post));
    }

    @Transactional
    public JobsMobilePostVO updatePost(Long userId, Long postId, JobsPostCreateRequest request) {
        JobsPost post = requireOwnPost(userId, postId);
        User user = requireUser(userId);
        String roleType = normalizeRoleType(request.getRoleType());
        JobsAccount account = ensureAccount(user, roleType);
        validateAccountPermission(account, true);
        applyPostContent(post, user, request, roleType);
        if (isBusiness(roleType)) {
            post.setStatus("PENDING");
            post.setPublicVisible(0);
            post.setReviewedAt(null);
            post.setReviewRemark("岗位已重新提交审核。");
            upsertQualification(account, user, request);
        } else {
            post.setStatus("APPROVED");
            post.setPublicVisible(1);
            post.setReviewRemark("学生互助需求已更新。");
        }
        jobsPostMapper.updateById(post);
        return toMobilePost(post, userId, countFavorite(post.getId()), resolveQualification(post));
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        JobsPost post = requireOwnPost(userId, postId);
        jobsPostMapper.deleteById(post.getId());
    }

    @Transactional
    public JobsMobilePostVO updateVisibility(Long userId, Long postId, JobsPostVisibilityRequest request) {
        JobsPost post = requireOwnPost(userId, postId);
        boolean nextVisible = Boolean.TRUE.equals(request.getPublicVisible());
        if (isBusiness(toMobileRole(post.getRole()))) {
            if (nextVisible && !"APPROVED".equalsIgnoreCase(post.getStatus())) {
                throw new BusinessException("当前岗位尚未审核通过，暂不能公开展示");
            }
            post.setPublicVisible(nextVisible ? 1 : 0);
        } else {
            post.setPublicVisible(nextVisible ? 1 : 0);
            post.setStatus(nextVisible ? "APPROVED" : "OFFLINE");
        }
        jobsPostMapper.updateById(post);
        return toMobilePost(post, userId, countFavorite(post.getId()), resolveQualification(post));
    }

    @Transactional
    public JobsMobileApplicationVO apply(Long userId, JobsApplicationCreateRequest request) {
        JobsPost post = requirePost(request.getPostId());
        if (!isVisibleToPublic(post)) {
            throw new BusinessException("该岗位当前不可报名");
        }
        if (Objects.equals(post.getAccountId(), userId)) {
            throw new BusinessException("自己发布的岗位无需报名");
        }
        JobsAccount account = ensureAccount(requireUser(userId), resolvePublishRole(userId));
        validateAccountPermission(account, false);

        JobsApplication existing = jobsApplicationMapper.selectOne(
            new LambdaQueryWrapper<JobsApplication>()
                .eq(JobsApplication::getPostId, post.getId())
                .eq(JobsApplication::getApplicantId, userId)
                .last("limit 1")
        );
        JobsApplication application = existing == null ? new JobsApplication() : existing;
        application.setPostId(post.getId());
        application.setApplicantId(userId);
        application.setPublisherId(post.getAccountId());
        application.setTitle(post.getTitle());
        application.setLocation(post.getArea());
        application.setTimeText(post.getSchedule());
        application.setSalaryText(formatSalary(post.getSalary(), post.getSettlement(), isBusiness(toMobileRole(post.getRole()))));
        application.setPublisherName(post.getPublisherName());
        application.setPublisherPhone(post.getPublisherPhone());
        application.setRoleType(toMobileRole(post.getRole()));
        application.setStatus(resolveApplicationStatus(post));
        application.setConversationId(request.getConversationId());
        if (application.getId() == null) {
            jobsApplicationMapper.insert(application);
        } else {
            jobsApplicationMapper.updateById(application);
        }
        post.setApplicantCount(Math.max(safeInt(post.getApplicantCount()), 0) + (existing == null ? 1 : 0));
        jobsPostMapper.updateById(post);
        return toApplicationVo(application, post);
    }

    public List<JobsMobileApplicationVO> myApplications(Long userId) {
        List<JobsApplication> applications = jobsApplicationMapper.selectList(
            new LambdaQueryWrapper<JobsApplication>()
                .eq(JobsApplication::getApplicantId, userId)
                .orderByDesc(JobsApplication::getCreatedAt)
                .orderByDesc(JobsApplication::getId)
        );
        Map<Long, JobsPost> postMap = jobsPostMapper.selectList(new LambdaQueryWrapper<JobsPost>()
                .in(!applications.isEmpty(), JobsPost::getId, applications.stream().map(JobsApplication::getPostId).toList()))
            .stream()
            .collect(Collectors.toMap(JobsPost::getId, Function.identity(), (left, right) -> left));
        return applications.stream()
            .map(item -> toApplicationVo(item, postMap.get(item.getPostId())))
            .toList();
    }

    public List<JobsConversationVO> conversations(Long userId) {
        List<JobsPostChat> chats = jobsPostChatMapper.selectList(
            new LambdaQueryWrapper<JobsPostChat>()
                .and(wrapper -> wrapper
                    .eq(JobsPostChat::getApplicantId, userId)
                    .or()
                    .eq(JobsPostChat::getPublisherId, userId))
                .orderByDesc(JobsPostChat::getUpdatedAt)
                .orderByDesc(JobsPostChat::getId)
        );
        if (chats.isEmpty()) {
            return List.of();
        }
        Map<Long, JobsPost> postMap = jobsPostMapper.selectList(new LambdaQueryWrapper<JobsPost>()
                .in(JobsPost::getId, chats.stream().map(JobsPostChat::getPostId).distinct().toList()))
            .stream()
            .collect(Collectors.toMap(JobsPost::getId, Function.identity(), (left, right) -> left));
        Map<Long, Conversation> conversationMap = conversationMapper.selectList(new LambdaQueryWrapper<Conversation>()
                .in(Conversation::getId, chats.stream().map(JobsPostChat::getConversationId).distinct().toList()))
            .stream()
            .collect(Collectors.toMap(Conversation::getId, Function.identity(), (left, right) -> left));
        Map<Long, ConversationMember> memberMap = conversationMemberMapper.selectList(new LambdaQueryWrapper<ConversationMember>()
                .in(ConversationMember::getConversationId, chats.stream().map(JobsPostChat::getConversationId).distinct().toList())
                .eq(ConversationMember::getUserId, userId)
                .eq(ConversationMember::getHidden, false))
            .stream()
            .collect(Collectors.toMap(ConversationMember::getConversationId, Function.identity(), (left, right) -> left));
        Map<Long, Message> lastMessageMap = resolveLastMessages(conversationMap.values().stream().toList());

        return chats.stream()
            .map(chat -> toConversationVo(chat, userId, postMap.get(chat.getPostId()), conversationMap.get(chat.getConversationId()),
                memberMap.get(chat.getConversationId()), lastMessageMap.get(chat.getConversationId())))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(JobsConversationVO::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    @Transactional
    public JobsConversationVO ensureConversation(Long postId, Long userId) {
        JobsPost post = requirePost(postId);
        if (!isVisibleToPublic(post) && !Objects.equals(post.getAccountId(), userId)) {
            throw new BusinessException("该岗位当前不可发起沟通");
        }
        if (Objects.equals(post.getAccountId(), userId)) {
            throw new BusinessException("自己发布的岗位无需发起私聊");
        }
        JobsPostChat chat = jobsPostChatMapper.selectOne(new LambdaQueryWrapper<JobsPostChat>()
            .eq(JobsPostChat::getPostId, postId)
            .eq(JobsPostChat::getApplicantId, userId)
            .last("limit 1"));
        if (chat == null) {
            Conversation conversation = new Conversation();
            conversation.setType(ConversationType.PRIVATE.name());
            conversation.setTitle("勤工岗位 " + post.getTitle());
            conversationMapper.insert(conversation);

            createConversationMember(conversation.getId(), post.getAccountId());
            createConversationMember(conversation.getId(), userId);

            chat = new JobsPostChat();
            chat.setPostId(postId);
            chat.setConversationId(conversation.getId());
            chat.setApplicantId(userId);
            chat.setPublisherId(post.getAccountId());
            jobsPostChatMapper.insert(chat);
        } else {
            createConversationMember(chat.getConversationId(), chat.getPublisherId());
            createConversationMember(chat.getConversationId(), chat.getApplicantId());
            restoreConversationMember(chat.getConversationId(), post.getAccountId());
            restoreConversationMember(chat.getConversationId(), userId);
        }

        JobsApplication application = jobsApplicationMapper.selectOne(new LambdaQueryWrapper<JobsApplication>()
            .eq(JobsApplication::getPostId, postId)
            .eq(JobsApplication::getApplicantId, userId)
            .last("limit 1"));
        if (application != null && !String.valueOf(chat.getConversationId()).equals(defaultText(application.getConversationId(), ""))) {
            application.setConversationId(String.valueOf(chat.getConversationId()));
            jobsApplicationMapper.updateById(application);
        }
        return conversationDetail(chat.getConversationId(), userId);
    }

    public JobsConversationVO conversationDetail(Long conversationId, Long userId) {
        ConversationMember member = conversationMemberMapper.selectOne(new LambdaQueryWrapper<ConversationMember>()
            .eq(ConversationMember::getConversationId, conversationId)
            .eq(ConversationMember::getUserId, userId)
            .last("limit 1"));
        if (member == null) {
            throw new BusinessException(403, "无权查看该会话");
        }
        JobsPostChat chat = jobsPostChatMapper.selectOne(new LambdaQueryWrapper<JobsPostChat>()
            .eq(JobsPostChat::getConversationId, conversationId)
            .last("limit 1"));
        if (chat == null) {
            throw new BusinessException("当前会话未关联勤工助学岗位");
        }
        JobsPost post = jobsPostMapper.selectById(chat.getPostId());
        Conversation conversation = conversationMapper.selectById(conversationId);
        Message lastMessage = conversation == null || conversation.getLastMessageId() == null
            ? null
            : messageMapper.selectById(conversation.getLastMessageId());
        return toConversationVo(chat, userId, post, conversation, member, lastMessage);
    }

    public JobsResumeVO getResume(Long userId) {
        JobsResume resume = jobsResumeMapper.selectOne(new LambdaQueryWrapper<JobsResume>().eq(JobsResume::getUserId, userId));
        User user = requireUser(userId);
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        return JobsResumeVO.builder()
            .realName(resume == null ? defaultText(user.getNickname(), "") : defaultText(resume.getRealName(), user.getNickname()))
            .major(resume == null ? "" : defaultText(resume.getMajor(), ""))
            .grade(resume == null ? "" : defaultText(resume.getGrade(), ""))
            .skills(resume == null ? "" : defaultText(resume.getSkills(), ""))
            .availableTime(resume == null ? "" : defaultText(resume.getAvailableTime(), ""))
            .contactPhone(resume == null ? defaultText(profile == null ? null : profile.getContactPhone(), user.getPhone()) : defaultText(resume.getContactPhone(), user.getPhone()))
            .introduction(resume == null ? defaultText(profile == null ? null : profile.getBio(), "") : defaultText(resume.getIntroduction(), ""))
            .updatedAt(resume == null || resume.getUpdatedAt() == null ? "" : resume.getUpdatedAt().format(MOBILE_TIME_FORMATTER))
            .build();
    }

    @Transactional
    public JobsResumeVO saveResume(Long userId, JobsResumeSaveRequest request) {
        JobsResume resume = jobsResumeMapper.selectOne(new LambdaQueryWrapper<JobsResume>().eq(JobsResume::getUserId, userId));
        if (resume == null) {
            resume = new JobsResume();
            resume.setUserId(userId);
        }
        resume.setRealName(request.getRealName());
        resume.setMajor(request.getMajor());
        resume.setGrade(request.getGrade());
        resume.setSkills(request.getSkills());
        resume.setAvailableTime(request.getAvailableTime());
        resume.setContactPhone(request.getContactPhone());
        resume.setIntroduction(request.getIntroduction());
        if (resume.getId() == null) {
            jobsResumeMapper.insert(resume);
        } else {
            jobsResumeMapper.updateById(resume);
        }
        return getResume(userId);
    }

    public JobsReviewProgressVO reviewProgress(Long userId) {
        JobsMerchantQualification qualification = jobsMerchantQualificationMapper.selectOne(
            new LambdaQueryWrapper<JobsMerchantQualification>()
                .eq(JobsMerchantQualification::getAccountId, userId)
                .orderByDesc(JobsMerchantQualification::getSubmittedAt)
                .last("limit 1")
        );
        JobsPost latestPost = jobsPostMapper.selectOne(
            new LambdaQueryWrapper<JobsPost>()
                .eq(JobsPost::getAccountId, userId)
                .orderByDesc(JobsPost::getCreatedAt)
                .orderByDesc(JobsPost::getId)
                .last("limit 1")
        );
        return JobsReviewProgressVO.builder()
            .qualificationStatus(toReviewStatus(qualification))
            .qualificationRejectReason(qualification == null ? "" : defaultText(qualification.getReviewRemark(), ""))
            .lastSubmittedAt(qualification == null || qualification.getSubmittedAt() == null ? "" : qualification.getSubmittedAt().format(MOBILE_TIME_FORMATTER))
            .latestJobId(latestPost == null ? null : latestPost.getId())
            .build();
    }

    private List<JobsMobilePostVO> toMobilePosts(List<JobsPost> posts, Long userId) {
        if (posts.isEmpty()) {
            return List.of();
        }
        Map<Long, Integer> favoriteCountMap = countFavorites(posts.stream().map(JobsPost::getId).toList());
        Map<Long, JobsMerchantQualification> qualificationMap = resolveQualifications(posts);
        return posts.stream()
            .map(item -> toMobilePost(item, userId, favoriteCountMap.getOrDefault(item.getId(), 0), qualificationMap.get(item.getAccountId())))
            .toList();
    }

    private JobsMobilePostVO toMobilePost(JobsPost post, Long userId, int favoriteCount, JobsMerchantQualification qualification) {
        String roleType = toMobileRole(post.getRole());
        String status = toMobileStatus(post, roleType);
        return JobsMobilePostVO.builder()
            .id(post.getId())
            .roleType(roleType)
            .publisherId(post.getAccountId())
            .publisherPhone(post.getPublisherPhone())
            .publisherAvatarColor(defaultAvatarColor(roleType))
            .category(toCategoryCode(post.getCategory(), post.getJobType()))
            .jobType(defaultText(post.getJobType(), inferJobType(post.getCategory(), roleType)))
            .jobTypeLabel(toJobTypeLabel(defaultText(post.getJobType(), inferJobType(post.getCategory(), roleType))))
            .title(post.getTitle())
            .serviceMode(defaultText(post.getWorkMode(), "ONLINE"))
            .location(post.getArea())
            .timeText(defaultText(post.getSchedule(), ""))
            .salaryText(formatSalary(post.getSalary(), post.getSettlement(), isBusiness(roleType)))
            .summary(defaultText(post.getDescription(), ""))
            .content(defaultText(post.getDescription(), ""))
            .requirement(defaultText(post.getRequirement(), ""))
            .headCount(Math.max(safeInt(post.getHeadCount()), 1))
            .filledCount(Math.max(safeInt(post.getApplicantCount()), 0))
            .expired(false)
            .createdAt(post.getCreatedAt() == null ? "" : post.getCreatedAt().format(MOBILE_TIME_FORMATTER))
            .status(status)
            .publicVisible(safeBool(post.getPublicVisible()))
            .favoriteCount(favoriteCount)
            .publisherName(defaultText(post.getPublisherName(), "岗位发布者"))
            .credentialFile(buildCredentialFile(qualification))
            .qualificationRejectReason(resolveQualificationRejectReason(roleType, qualification, post))
            .build();
    }

    private JobsMobileApplicationVO toApplicationVo(JobsApplication application, JobsPost post) {
        return JobsMobileApplicationVO.builder()
            .id(application.getId())
            .jobId(application.getPostId())
            .applicantId(application.getApplicantId())
            .title(post == null ? application.getTitle() : defaultText(post.getTitle(), application.getTitle()))
            .location(post == null ? application.getLocation() : defaultText(post.getArea(), application.getLocation()))
            .timeText(post == null ? application.getTimeText() : defaultText(post.getSchedule(), application.getTimeText()))
            .salaryText(post == null
                ? application.getSalaryText()
                : formatSalary(post.getSalary(), post.getSettlement(), isBusiness(toMobileRole(post.getRole()))))
            .publisherName(post == null ? application.getPublisherName() : defaultText(post.getPublisherName(), application.getPublisherName()))
            .publisherPhone(post == null ? application.getPublisherPhone() : defaultText(post.getPublisherPhone(), application.getPublisherPhone()))
            .roleType(post == null ? application.getRoleType() : toMobileRole(post.getRole()))
            .status(resolveApplicationStatus(post == null ? null : post, application.getStatus()))
            .conversationId(defaultText(application.getConversationId(), ""))
            .appliedAt(application.getCreatedAt() == null ? "" : application.getCreatedAt().format(MOBILE_TIME_FORMATTER))
            .build();
    }

    private JobsConversationVO toConversationVo(JobsPostChat chat,
                                                Long currentUserId,
                                                JobsPost post,
                                                Conversation conversation,
                                                ConversationMember member,
                                                Message lastMessage) {
        if (chat == null || post == null || conversation == null || member == null) {
            return null;
        }
        boolean isPublisher = Objects.equals(chat.getPublisherId(), currentUserId);
        Long counterpartyId = isPublisher ? chat.getApplicantId() : chat.getPublisherId();
        User counterparty = userMapper.selectById(counterpartyId);
        return JobsConversationVO.builder()
            .id(conversation.getId())
            .jobId(post.getId())
            .jobTitle(post.getTitle())
            .counterpartyName(counterparty == null ? "岗位沟通" : defaultText(counterparty.getNickname(), "岗位沟通"))
            .counterpartyPhone(counterparty == null ? "" : defaultText(counterparty.getPhone(), ""))
            .counterpartyAvatarColor(defaultAvatarColor(isPublisher ? "STUDENT" : toMobileRole(post.getRole())))
            .latestMessage(lastMessage == null ? "" : defaultText(lastMessage.getContent(), ""))
            .latestMessageType(lastMessage == null ? "TEXT" : defaultText(lastMessage.getType(), "TEXT"))
            .updatedAt(resolveConversationTime(conversation, lastMessage))
            .unreadCount(safeInt(member.getUnreadCount()))
            .build();
    }

    private void applyPostContent(JobsPost post, User user, JobsPostCreateRequest request, String roleType) {
        post.setRole(toStorageRole(roleType));
        post.setPublisherName(defaultText(user.getNickname(), "校园用户"));
        post.setPublisherPhone(defaultText(user.getPhone(), ""));
        post.setTitle(request.getTitle().trim());
        post.setCategory(toCategoryLabel(request.getJobType()));
        post.setJobType(request.getJobType().trim());
        post.setWorkMode(defaultText(request.getServiceMode(), "ONLINE"));
        post.setDistanceRange("OFFLINE".equalsIgnoreCase(request.getServiceMode()) ? "校内" : "线上");
        post.setActualDistanceKm("OFFLINE".equalsIgnoreCase(request.getServiceMode()) ? new BigDecimal("0.50") : BigDecimal.ZERO);
        post.setArea(defaultText(request.getLocation(), ""));
        post.setSalary(request.getSalary().setScale(2, RoundingMode.HALF_UP));
        post.setSettlement(isBusiness(roleType) ? "按小时结算" : "按次结算");
        post.setSchedule((request.getServiceDate() + " " + request.getServiceTime()).trim());
        post.setDescription(defaultText(request.getSummary(), "发布者暂未补充更多说明。"));
        post.setRequirement(defaultText(request.getSummary(), "请与发布者进一步沟通具体服务标准与交付细节。"));
    }

    private JobsAccount ensureAccount(User user, String roleType) {
        JobsAccount account = jobsAccountMapper.selectById(user.getId());
        if (account != null) {
            account.setName(defaultText(user.getNickname(), account.getName()));
            account.setPhone(defaultText(user.getPhone(), account.getPhone()));
            account.setRole(toStorageRole(roleType));
            jobsAccountMapper.updateById(account);
            return account;
        }
        account = new JobsAccount();
        account.setId(user.getId());
        account.setName(defaultText(user.getNickname(), "校园用户"));
        account.setRole(toStorageRole(roleType));
        account.setPhone(defaultText(user.getPhone(), ""));
        account.setStatus("ACTIVE");
        account.setPublishEnabled(1);
        account.setApplyEnabled(1);
        account.setAcceptEnabled(1);
        account.setReportCount(0);
        account.setDisputeCount(0);
        account.setCreditScore(100);
        account.setLatestRemark("用户端自动创建勤工助学账户");
        jobsAccountMapper.insert(account);
        return account;
    }

    private void validateAccountPermission(JobsAccount account, boolean publishAction) {
        if ("BANNED".equalsIgnoreCase(account.getStatus())) {
            throw new BusinessException("当前账号已被限制使用勤工助学功能");
        }
        if (publishAction && !safeBool(account.getPublishEnabled())) {
            throw new BusinessException("当前账号暂无发布权限");
        }
        if (!publishAction && !safeBool(account.getApplyEnabled())) {
            throw new BusinessException("当前账号暂无报名权限");
        }
    }

    private void upsertQualification(JobsAccount account, User user, JobsPostCreateRequest request) {
        if (request.getCredentialFileId() == null) {
            throw new BusinessException("商家发布请先上传资质照片");
        }
        FileAsset credential = requireFile(request.getCredentialFileId(), user.getId(), "商家资质图片不存在");
        JobsMerchantQualification qualification = jobsMerchantQualificationMapper.selectOne(
            new LambdaQueryWrapper<JobsMerchantQualification>()
                .eq(JobsMerchantQualification::getAccountId, account.getId())
                .orderByDesc(JobsMerchantQualification::getSubmittedAt)
                .last("limit 1")
        );
        if (qualification == null) {
            qualification = new JobsMerchantQualification();
            qualification.setAccountId(account.getId());
        }
        qualification.setApplicantName(defaultText(user.getNickname(), "商家用户"));
        qualification.setBusinessName(defaultText(user.getNickname(), "校园商家"));
        qualification.setPhone(defaultText(user.getPhone(), ""));
        qualification.setIdentityType("营业执照");
        qualification.setArea(defaultText(request.getLocation(), "校园周边"));
        qualification.setLicenseFileId(credential.getId());
        qualification.setLicenseImage(publicUrl(credential));
        qualification.setStatus("PENDING");
        qualification.setSubmittedAt(LocalDateTime.now());
        qualification.setReviewedAt(null);
        qualification.setReviewRemark("");
        if (qualification.getId() == null) {
            jobsMerchantQualificationMapper.insert(qualification);
        } else {
            jobsMerchantQualificationMapper.updateById(qualification);
        }
    }

    private JobsMerchantQualification resolveQualification(JobsPost post) {
        if (!isBusiness(toMobileRole(post.getRole()))) {
            return null;
        }
        return jobsMerchantQualificationMapper.selectOne(
            new LambdaQueryWrapper<JobsMerchantQualification>()
                .eq(JobsMerchantQualification::getAccountId, post.getAccountId())
                .orderByDesc(JobsMerchantQualification::getSubmittedAt)
                .last("limit 1")
        );
    }

    private Map<Long, JobsMerchantQualification> resolveQualifications(List<JobsPost> posts) {
        List<Long> businessAccountIds = posts.stream()
            .filter(item -> isBusiness(toMobileRole(item.getRole())))
            .map(JobsPost::getAccountId)
            .distinct()
            .toList();
        if (businessAccountIds.isEmpty()) {
            return Map.of();
        }
        return jobsMerchantQualificationMapper.selectList(
                new LambdaQueryWrapper<JobsMerchantQualification>()
                    .in(JobsMerchantQualification::getAccountId, businessAccountIds)
                    .orderByDesc(JobsMerchantQualification::getSubmittedAt))
            .stream()
            .collect(Collectors.toMap(JobsMerchantQualification::getAccountId, Function.identity(), (left, right) -> left));
    }

    private FileAssetVO buildCredentialFile(JobsMerchantQualification qualification) {
        if (qualification == null) {
            return null;
        }
        if (qualification.getLicenseFileId() != null) {
            FileAsset asset = fileAssetMapper.selectById(qualification.getLicenseFileId());
            if (asset != null) {
                return new FileAssetVO(asset.getId(), publicUrl(asset), asset.getOriginalName());
            }
        }
        if (qualification.getLicenseImage() == null || qualification.getLicenseImage().isBlank()) {
            return null;
        }
        return new FileAssetVO(qualification.getLicenseFileId(), qualification.getLicenseImage(), "商家资质");
    }

    private int countFavorite(Long postId) {
        Long count = jobsFavoriteMapper.selectCount(
            new LambdaQueryWrapper<JobsFavorite>()
                .eq(JobsFavorite::getPostId, postId)
                .eq(JobsFavorite::getDeleted, 0)
        );
        return count == null ? 0 : count.intValue();
    }

    private Map<Long, Integer> countFavorites(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        List<JobsFavorite> favorites = jobsFavoriteMapper.selectList(
            new LambdaQueryWrapper<JobsFavorite>()
                .in(JobsFavorite::getPostId, postIds)
                .eq(JobsFavorite::getDeleted, 0)
        );
        return favorites.stream()
            .collect(Collectors.groupingBy(JobsFavorite::getPostId, Collectors.summingInt(item -> 1)));
    }

    private Map<Long, Message> resolveLastMessages(List<Conversation> conversations) {
        List<Long> messageIds = conversations.stream()
            .map(Conversation::getLastMessageId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (messageIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Message> byMessageId = messageMapper.selectList(new LambdaQueryWrapper<Message>().in(Message::getId, messageIds))
            .stream()
            .collect(Collectors.toMap(Message::getId, Function.identity(), (left, right) -> left));
        return conversations.stream()
            .filter(item -> item.getLastMessageId() != null)
            .collect(Collectors.toMap(Conversation::getId, item -> byMessageId.get(item.getLastMessageId()), (left, right) -> left));
    }

    private JobsPost requirePost(Long postId) {
        JobsPost post = jobsPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("岗位不存在");
        }
        return post;
    }

    private JobsPost requireOwnPost(Long userId, Long postId) {
        JobsPost post = requirePost(postId);
        if (!Objects.equals(post.getAccountId(), userId)) {
            throw new BusinessException(403, "无权操作该岗位");
        }
        return post;
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private FileAsset requireFile(Long fileId, Long uploaderId, String message) {
        FileAsset asset = fileAssetMapper.selectById(fileId);
        if (asset == null) {
            throw new BusinessException(message);
        }
        if (uploaderId != null && !Objects.equals(asset.getUploaderId(), uploaderId)) {
            throw new BusinessException(403, "无权使用该文件");
        }
        return asset;
    }

    private void createConversationMember(Long conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            return;
        }
        ConversationMember member = conversationMemberMapper.selectOne(new LambdaQueryWrapper<ConversationMember>()
            .eq(ConversationMember::getConversationId, conversationId)
            .eq(ConversationMember::getUserId, userId)
            .last("limit 1"));
        if (member != null) {
            return;
        }
        member = new ConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setUnreadCount(0);
        member.setHidden(false);
        conversationMemberMapper.insert(member);
    }

    private void restoreConversationMember(Long conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            return;
        }
        ConversationMember member = conversationMemberMapper.selectOne(new LambdaQueryWrapper<ConversationMember>()
            .eq(ConversationMember::getConversationId, conversationId)
            .eq(ConversationMember::getUserId, userId)
            .last("limit 1"));
        if (member == null || !Boolean.TRUE.equals(member.getHidden())) {
            return;
        }
        member.setHidden(false);
        conversationMemberMapper.updateById(member);
    }

    private boolean isVisibleToPublic(JobsPost post) {
        return safeBool(post.getPublicVisible()) && PUBLIC_VISIBLE_STATUSES.contains(String.valueOf(post.getStatus()).toUpperCase(Locale.ROOT));
    }

    private String resolvePublishRole(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        if (profile == null || profile.getPublishRole() == null || profile.getPublishRole().isBlank()) {
            return "STUDENT";
        }
        return "BUSINESS".equalsIgnoreCase(profile.getPublishRole()) ? "BUSINESS" : "STUDENT";
    }

    private String toStorageRole(String roleType) {
        return isBusiness(roleType) ? "EMPLOYER" : "STUDENT";
    }

    private String toMobileRole(String role) {
        return "EMPLOYER".equalsIgnoreCase(role) ? "BUSINESS" : "STUDENT";
    }

    private boolean isBusiness(String roleType) {
        return "BUSINESS".equalsIgnoreCase(roleType);
    }

    private String normalizeRoleType(String roleType) {
        return isBusiness(roleType) ? "BUSINESS" : "STUDENT";
    }

    private String toCategoryCode(String categoryLabel, String jobType) {
        if (jobType != null && !jobType.isBlank()) {
            return switch (jobType.toUpperCase(Locale.ROOT)) {
                case "PPT", "DOC_EDIT" -> "PPT_DOC";
                case "COPYWRITING" -> "COPYWRITING";
                case "TUTOR" -> "TUTOR";
                case "CAMPUS_HELP" -> "ONLINE_HELP";
                case "OFFLINE_PARTTIME" -> "CAMPUS_PARTTIME";
                case "HOLIDAY_JOB" -> "HOLIDAY_PARTTIME";
                default -> "ONLINE_HELP";
            };
        }
        if (categoryLabel == null) {
            return "ONLINE_HELP";
        }
        return switch (categoryLabel) {
            case "PPT&文档服务", "PPT制作", "文档格式修改" -> "PPT_DOC";
            case "文案代写" -> "COPYWRITING";
            case "家教辅导" -> "TUTOR";
            case "校内兼职" -> "CAMPUS_PARTTIME";
            case "假期兼职", "假期岗位" -> "HOLIDAY_PARTTIME";
            default -> "ONLINE_HELP";
        };
    }

    private String toCategoryLabel(String jobType) {
        return switch (String.valueOf(jobType).toUpperCase(Locale.ROOT)) {
            case "PPT", "DOC_EDIT" -> "PPT&文档服务";
            case "COPYWRITING" -> "文案代写";
            case "TUTOR" -> "家教辅导";
            case "OFFLINE_PARTTIME" -> "校内兼职";
            case "HOLIDAY_JOB" -> "假期兼职";
            default -> "线上互助";
        };
    }

    private String inferJobType(String categoryLabel, String roleType) {
        if ("校内兼职".equals(categoryLabel)) {
            return "OFFLINE_PARTTIME";
        }
        if ("假期兼职".equals(categoryLabel)) {
            return "HOLIDAY_JOB";
        }
        if ("文案代写".equals(categoryLabel)) {
            return "COPYWRITING";
        }
        if ("家教辅导".equals(categoryLabel)) {
            return "TUTOR";
        }
        if ("PPT&文档服务".equals(categoryLabel)) {
            return "PPT";
        }
        return isBusiness(roleType) ? "OFFLINE_PARTTIME" : "CAMPUS_HELP";
    }

    private String toJobTypeLabel(String jobType) {
        return switch (String.valueOf(jobType).toUpperCase(Locale.ROOT)) {
            case "PPT" -> "PPT制作";
            case "DOC_EDIT" -> "文档格式修改";
            case "COPYWRITING" -> "文案代写";
            case "TUTOR" -> "家教辅导";
            case "OFFLINE_PARTTIME" -> "线下兼职";
            case "HOLIDAY_JOB" -> "假期岗位";
            default -> "校园互助";
        };
    }

    private String toMobileStatus(JobsPost post, String roleType) {
        String status = String.valueOf(post.getStatus()).toUpperCase(Locale.ROOT);
        if (isBusiness(roleType)) {
            if ("PENDING".equals(status)) {
                return "PENDING_REVIEW";
            }
            if ("REJECTED".equals(status)) {
                return "REJECTED";
            }
            if ("OFFLINE".equals(status) || "BLOCKED".equals(status)) {
                return "OFFLINE";
            }
            return "APPROVED";
        }
        if ("OFFLINE".equals(status)) {
            return "OFFLINE";
        }
        if ("COMPLETED".equals(status)) {
            return "COMPLETED";
        }
        return "ACTIVE";
    }

    private String resolveApplicationStatus(JobsPost post) {
        if (post == null) {
            return "PENDING";
        }
        return resolveApplicationStatus(post, null);
    }

    private String resolveApplicationStatus(JobsPost post, String currentStatus) {
        if (currentStatus != null && !currentStatus.isBlank()) {
            return currentStatus;
        }
        if (post == null) {
            return "PENDING";
        }
        String postStatus = String.valueOf(post.getStatus()).toUpperCase(Locale.ROOT);
        if ("OFFLINE".equals(postStatus) || "BLOCKED".equals(postStatus) || "REJECTED".equals(postStatus)) {
            return "EXPIRED";
        }
        return "PENDING";
    }

    private String toReviewStatus(JobsMerchantQualification qualification) {
        if (qualification == null) {
            return "UNSUBMITTED";
        }
        return switch (String.valueOf(qualification.getStatus()).toUpperCase(Locale.ROOT)) {
            case "APPROVED" -> "APPROVED";
            case "REJECTED" -> "REJECTED";
            default -> "PENDING_REVIEW";
        };
    }

    private String resolveQualificationRejectReason(String roleType, JobsMerchantQualification qualification, JobsPost post) {
        if (!isBusiness(roleType)) {
            return "";
        }
        if (qualification != null && "REJECTED".equalsIgnoreCase(qualification.getStatus())) {
            return defaultText(qualification.getReviewRemark(), "");
        }
        if ("REJECTED".equalsIgnoreCase(post.getStatus())) {
            return defaultText(post.getReviewRemark(), "");
        }
        return "";
    }

    private String formatSalary(BigDecimal salary, String settlement, boolean business) {
        String amount = salary == null ? "0" : salary.stripTrailingZeros().toPlainString();
        if (business) {
            return amount + " 元 / " + ("按小时结算".equals(settlement) ? "小时" : "天");
        }
        return amount + " 元 / 次";
    }

    private String resolveConversationTime(Conversation conversation, Message lastMessage) {
        LocalDateTime value = lastMessage == null ? conversation.getUpdatedAt() : lastMessage.getCreatedAt();
        if (value == null) {
            value = conversation.getUpdatedAt();
        }
        return value == null ? "" : value.format(MOBILE_TIME_FORMATTER);
    }

    private String publicUrl(FileAsset asset) {
        return appProperties.getFile().getPublicBaseUrl() + asset.getRelativePath();
    }

    private String defaultAvatarColor(String roleType) {
        return isBusiness(roleType)
            ? "linear-gradient(135deg, #34d399, #059669)"
            : "linear-gradient(135deg, #60a5fa, #2563eb)";
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean safeBool(Integer value) {
        return value != null && value == 1;
    }
}
