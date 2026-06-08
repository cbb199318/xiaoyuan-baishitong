package com.campus.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.platform.common.BusinessException;
import com.campus.platform.config.AppProperties;
import com.campus.platform.dto.AdminBeautyGoodSaveRequest;
import com.campus.platform.dto.AdminBeautyAppealActionRequest;
import com.campus.platform.dto.AdminBeautyUpdateRequest;
import com.campus.platform.dto.BeautyGoodCreateRequest;
import com.campus.platform.entity.AdminOperationLog;
import com.campus.platform.entity.BeautyAppeal;
import com.campus.platform.entity.BeautyFavorite;
import com.campus.platform.entity.BeautyGood;
import com.campus.platform.entity.FileAsset;
import com.campus.platform.entity.User;
import com.campus.platform.enums.RoleType;
import com.campus.platform.mapper.AdminOperationLogMapper;
import com.campus.platform.mapper.BeautyAppealMapper;
import com.campus.platform.mapper.BeautyFavoriteMapper;
import com.campus.platform.mapper.BeautyGoodMapper;
import com.campus.platform.mapper.FileAssetMapper;
import com.campus.platform.mapper.UserMapper;
import com.campus.platform.vo.BeautyAppealVO;
import com.campus.platform.vo.BeautyGoodVO;
import com.campus.platform.vo.BeautyStatsCategoryVO;
import com.campus.platform.vo.BeautyStatsItemVO;
import com.campus.platform.vo.BeautyReviewVO;
import com.campus.platform.vo.BeautyStatsVO;
import com.campus.platform.vo.FileAssetVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BeautyGoodService {

    private final BeautyGoodMapper beautyGoodMapper;
    private final BeautyAppealMapper beautyAppealMapper;
    private final BeautyFavoriteMapper beautyFavoriteMapper;
    private final FileAssetMapper fileAssetMapper;
    private final UserMapper userMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    public Page<BeautyGoodVO> pageApproved(String keyword, String category, String priceRange, String skinTag, int current, int size, Long userId) {
        List<BeautyGood> goods = beautyGoodMapper.selectList(
            new LambdaQueryWrapper<BeautyGood>()
                .eq(BeautyGood::getStatus, "APPROVED")
                .eq(category != null && !category.isBlank(), BeautyGood::getCategory, category)
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                    .like(BeautyGood::getTitle, keyword)
                    .or()
                    .like(BeautyGood::getSummary, keyword)
                    .or()
                    .like(BeautyGood::getUsageGuide, keyword))
                .orderByDesc(BeautyGood::getPublishedAt)
                .orderByDesc(BeautyGood::getCreatedAt)
        );
        List<BeautyGood> filtered = goods.stream()
            .filter(item -> matchPriceRange(item, priceRange))
            .filter(item -> matchSkinTag(item, skinTag))
            .toList();
        return buildVoPage(filtered, current, size, userId);
    }

    @Transactional
    public BeautyGoodVO detail(Long id, Long userId) {
        BeautyGood good = beautyGoodMapper.selectById(id);
        if (good == null) {
            throw new BusinessException("好物不存在");
        }
        boolean canView = "APPROVED".equalsIgnoreCase(good.getStatus()) || Objects.equals(good.getCreatorId(), userId) || isAdmin(userId);
        if (!canView) {
            throw new BusinessException(403, "无权查看该好物");
        }
        good.setViewCount((good.getViewCount() == null ? 0 : good.getViewCount()) + 1);
        beautyGoodMapper.updateById(good);
        return toVo(good, userId);
    }

    @Transactional
    public BeautyGoodVO create(Long userId, BeautyGoodCreateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        FileAsset productImage = requireFile(request.getProductImageFileId(), userId, "商品实拍图不存在");
        FileAsset receiptImage = requireFile(request.getReceiptFileId(), userId, "购买凭证不存在");

        BeautyGood good = new BeautyGood();
        good.setCategory(request.getCategory());
        good.setTitle(request.getTitle());
        good.setPrice(request.getPrice());
        good.setSummary(request.getSummary());
        good.setUsageGuide(request.getUsageGuide());
        good.setSceneText(defaultText(request.getUsageGuide(), "适合学生日常通勤、宿舍便携收纳和基础入门选购场景。"));
        good.setEvaluation(defaultText(request.getUsageGuide(), request.getSummary()));
        good.setDormExperience(defaultText(request.getUsageGuide(), "支持宿舍场景下快速参考商品外观、价格和使用体验。"));
        good.setAvoidanceGuide("建议先核对购买凭证、产品实拍和自身需求，再决定是否入手。");
        good.setSkinTagsJson(writeJson(List.of("用户发布", categoryLabel(request.getCategory()))));
        good.setReviewJson(writeJson(Collections.emptyList()));
        good.setGalleryJson(writeJson(List.of(publicUrl(productImage), publicUrl(receiptImage))));
        good.setImageUrl(publicUrl(productImage));
        good.setProductImageFileId(productImage.getId());
        good.setReceiptFileId(receiptImage.getId());
        good.setCreatorId(userId);
        good.setCreatorNickname(user.getNickname());
        good.setStatus("PENDING");
        good.setSourceType("USER");
        good.setFavoriteCount(0);
        good.setViewCount(0);
        beautyGoodMapper.insert(good);
        return toVo(good, userId);
    }

    @Transactional
    public BeautyGoodVO adminCreate(Long adminId, AdminBeautyGoodSaveRequest request) {
        User admin = userMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        BeautyGood good = new BeautyGood();
        applyBeautyContent(good, adminId, request);
        good.setCreatorId(adminId);
        good.setCreatorNickname(defaultText(admin.getNickname(), "后台管理员"));
        good.setStatus("APPROVED");
        good.setSourceType("SYSTEM");
        good.setRejectReason(null);
        good.setReviewedBy(adminId);
        good.setReviewedAt(LocalDateTime.now());
        good.setFavoriteCount(0);
        good.setViewCount(0);
        good.setPublishedAt(LocalDateTime.now());
        beautyGoodMapper.insert(good);
        insertAdminLog(adminId, "CREATE_BEAUTY_GOOD", good.getId(), "后台新增美妆好物");
        return toVo(good, null);
    }

    @Transactional
    public BeautyGoodVO adminUpdateContent(Long adminId, Long id, AdminBeautyGoodSaveRequest request) {
        BeautyGood good = beautyGoodMapper.selectById(id);
        if (good == null) {
            throw new BusinessException("好物不存在");
        }
        applyBeautyContent(good, adminId, request);
        if (good.getPublishedAt() == null && "APPROVED".equalsIgnoreCase(good.getStatus())) {
            good.setPublishedAt(LocalDateTime.now());
        }
        beautyGoodMapper.updateById(good);
        insertAdminLog(adminId, "UPDATE_BEAUTY_GOOD", id, "后台编辑美妆好物");
        return toVo(good, null);
    }

    public List<BeautyGoodVO> myGoods(Long userId) {
        return beautyGoodMapper.selectList(
                new LambdaQueryWrapper<BeautyGood>()
                    .eq(BeautyGood::getCreatorId, userId)
                    .orderByDesc(BeautyGood::getCreatedAt))
            .stream()
            .map(item -> toVo(item, userId))
            .toList();
    }

    public List<BeautyGoodVO> favorites(Long userId) {
        List<BeautyFavorite> favorites = beautyFavoriteMapper.selectActiveByUserId(userId);
        List<BeautyGoodVO> result = new ArrayList<>();
        for (BeautyFavorite favorite : favorites) {
            BeautyGood good = beautyGoodMapper.selectById(favorite.getGoodId());
            if (good != null && "APPROVED".equalsIgnoreCase(good.getStatus())) {
                result.add(toVo(good, userId));
            }
        }
        return result;
    }

    @Transactional
    public BeautyGoodVO favorite(Long userId, Long goodId) {
        BeautyGood good = beautyGoodMapper.selectById(goodId);
        if (good == null || !"APPROVED".equalsIgnoreCase(good.getStatus())) {
            throw new BusinessException("该好物暂不可收藏");
        }
        BeautyFavorite existing = beautyFavoriteMapper.selectAny(goodId, userId);
        if (existing == null) {
            BeautyFavorite favorite = new BeautyFavorite();
            favorite.setGoodId(goodId);
            favorite.setUserId(userId);
            beautyFavoriteMapper.insert(favorite);
        } else if (existing.getDeleted() != null && existing.getDeleted() == 1) {
            existing.setDeleted(0);
            beautyFavoriteMapper.updateById(existing);
        } else {
            return toVo(good, userId);
        }
        good.setFavoriteCount((good.getFavoriteCount() == null ? 0 : good.getFavoriteCount()) + 1);
        beautyGoodMapper.updateById(good);
        return toVo(good, userId);
    }

    @Transactional
    public BeautyGoodVO unfavorite(Long userId, Long goodId) {
        BeautyGood good = beautyGoodMapper.selectById(goodId);
        if (good == null) {
            throw new BusinessException("好物不存在");
        }
        BeautyFavorite existing = beautyFavoriteMapper.selectAny(goodId, userId);
        if (existing != null && (existing.getDeleted() == null || existing.getDeleted() == 0)) {
            beautyFavoriteMapper.hardDeleteById(existing.getId());
            int nextCount = Math.max(0, (good.getFavoriteCount() == null ? 0 : good.getFavoriteCount()) - 1);
            good.setFavoriteCount(nextCount);
            beautyGoodMapper.updateById(good);
        }
        return toVo(good, userId);
    }

    public Page<BeautyGoodVO> adminPage(String keyword, String category, String status, int current, int size) {
        List<BeautyGood> goods = beautyGoodMapper.selectList(
            new LambdaQueryWrapper<BeautyGood>()
                .eq(category != null && !category.isBlank(), BeautyGood::getCategory, category)
                .eq(status != null && !status.isBlank(), BeautyGood::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                    .like(BeautyGood::getTitle, keyword)
                    .or()
                    .like(BeautyGood::getSummary, keyword)
                    .or()
                    .like(BeautyGood::getCreatorNickname, keyword))
                .orderByDesc(BeautyGood::getCreatedAt)
        );
        return buildVoPage(goods, current, size, null);
    }

    public BeautyGoodVO adminDetail(Long id) {
        BeautyGood good = beautyGoodMapper.selectById(id);
        return good == null ? null : toVo(good, null);
    }

    @Transactional
    public BeautyGoodVO adminUpdate(Long adminId, Long id, AdminBeautyUpdateRequest request) {
        BeautyGood good = beautyGoodMapper.selectById(id);
        if (good == null) {
            throw new BusinessException("好物不存在");
        }
        switch (String.valueOf(request.getAction()).toUpperCase()) {
            case "APPROVE" -> {
                good.setStatus("APPROVED");
                good.setRejectReason(null);
                good.setReviewedBy(adminId);
                good.setReviewedAt(LocalDateTime.now());
                if (good.getPublishedAt() == null) {
                    good.setPublishedAt(LocalDateTime.now());
                }
            }
            case "REJECT" -> {
                good.setStatus("REJECTED");
                good.setRejectReason(defaultText(request.getRemark(), "内容不符合平台展示规范"));
                good.setReviewedBy(adminId);
                good.setReviewedAt(LocalDateTime.now());
            }
            case "OFFLINE" -> {
                good.setStatus("OFFLINE");
                good.setReviewedBy(adminId);
                good.setReviewedAt(LocalDateTime.now());
            }
            case "RESTORE" -> {
                good.setStatus("APPROVED");
                good.setReviewedBy(adminId);
                good.setReviewedAt(LocalDateTime.now());
                if (good.getPublishedAt() == null) {
                    good.setPublishedAt(LocalDateTime.now());
                }
            }
            default -> throw new BusinessException("不支持的美妆商品处理动作");
        }
        beautyGoodMapper.updateById(good);
        insertAdminLog(
            adminId,
            "HANDLE_BEAUTY_GOOD",
            id,
            request.getAction() + (request.getRemark() == null || request.getRemark().isBlank() ? "" : (":" + request.getRemark()))
        );
        return toVo(good, null);
    }

    public BeautyStatsVO stats() {
        int total = countByStatus(null);
        int pending = countByStatus("PENDING");
        int approved = countByStatus("APPROVED");
        int rejected = countByStatus("REJECTED");
        int offline = countByStatus("OFFLINE");
        List<BeautyGood> goods = beautyGoodMapper.selectList(new LambdaQueryWrapper<BeautyGood>());
        int totalFavorites = goods.stream().mapToInt(item -> item.getFavoriteCount() == null ? 0 : item.getFavoriteCount()).sum();
        int totalViews = goods.stream().mapToInt(item -> item.getViewCount() == null ? 0 : item.getViewCount()).sum();
        List<BeautyStatsItemVO> topViewedGoods = goods.stream()
            .filter(item -> (item.getViewCount() == null ? 0 : item.getViewCount()) > 0)
            .sorted(Comparator
                .comparing((BeautyGood item) -> item.getViewCount() == null ? 0 : item.getViewCount(), Comparator.reverseOrder())
                .thenComparing(item -> item.getFavoriteCount() == null ? 0 : item.getFavoriteCount(), Comparator.reverseOrder())
                .thenComparing(BeautyGood::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .map(this::toStatsItem)
            .toList();
        List<BeautyStatsItemVO> topFavoritedGoods = goods.stream()
            .filter(item -> (item.getFavoriteCount() == null ? 0 : item.getFavoriteCount()) > 0)
            .sorted(Comparator
                .comparing((BeautyGood item) -> item.getFavoriteCount() == null ? 0 : item.getFavoriteCount(), Comparator.reverseOrder())
                .thenComparing(item -> item.getViewCount() == null ? 0 : item.getViewCount(), Comparator.reverseOrder())
                .thenComparing(BeautyGood::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .map(this::toStatsItem)
            .toList();
        List<BeautyStatsCategoryVO> categoryBreakdown = goods.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                item -> categoryLabel(item.getCategory()),
                java.util.stream.Collectors.collectingAndThen(java.util.stream.Collectors.counting(), Long::intValue)
            ))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
            .map(entry -> BeautyStatsCategoryVO.builder()
                .category(entry.getKey())
                .count(entry.getValue())
                .build())
            .toList();
        return BeautyStatsVO.builder()
            .totalGoods(total)
            .pendingGoods(pending)
            .approvedGoods(approved)
            .rejectedGoods(rejected)
            .offlineGoods(offline)
            .totalFavorites(totalFavorites)
            .totalViews(totalViews)
            .topViewedGoods(topViewedGoods)
            .topFavoritedGoods(topFavoritedGoods)
            .categoryBreakdown(categoryBreakdown)
            .build();
    }

    public List<BeautyAppealVO> listAppeals() {
        return beautyAppealMapper.selectList(
                new LambdaQueryWrapper<BeautyAppeal>()
                    .orderByDesc(BeautyAppeal::getCreatedAt)
                    .orderByDesc(BeautyAppeal::getId))
            .stream()
            .map(this::toAppealVo)
            .toList();
    }

    public BeautyAppealVO appealDetail(Long id) {
        return toAppealVo(getAppeal(id));
    }

    @Transactional
    public BeautyAppealVO updateAppeal(Long adminId, Long id, AdminBeautyAppealActionRequest request) {
        BeautyAppeal appeal = getAppeal(id);
        String status = normalizeStatus(request.getStatus());
        if (!List.of("PENDING", "PROCESSING", "RESOLVED", "REJECTED").contains(status)) {
            throw new BusinessException("美妆售后状态不支持");
        }
        appeal.setStatus(status);
        appeal.setHandleRemark(defaultText(request.getRemark(), defaultAppealRemark(status)));
        appeal.setHandledAt("PENDING".equals(status) ? null : LocalDateTime.now());
        beautyAppealMapper.updateById(appeal);

        if ("RESOLVED".equals(status)) {
            BeautyGood good = beautyGoodMapper.selectById(appeal.getGoodId());
            if (good != null && "APPROVED".equalsIgnoreCase(good.getStatus())) {
                good.setStatus("OFFLINE");
                good.setReviewedBy(adminId);
                good.setReviewedAt(LocalDateTime.now());
                beautyGoodMapper.updateById(good);
            }
        }

        insertAdminLog(adminId, "UPDATE_BEAUTY_APPEAL", id, appeal.getHandleRemark());
        return appealDetail(id);
    }

    private int countByStatus(String status) {
        Long count = beautyGoodMapper.selectCount(
            status == null
                ? new LambdaQueryWrapper<>()
                : new LambdaQueryWrapper<BeautyGood>().eq(BeautyGood::getStatus, status)
        );
        return count == null ? 0 : count.intValue();
    }

    private BeautyAppeal getAppeal(Long id) {
        BeautyAppeal appeal = beautyAppealMapper.selectById(id);
        if (appeal == null) {
            throw new BusinessException("售后工单不存在");
        }
        return appeal;
    }

    private BeautyAppealVO toAppealVo(BeautyAppeal appeal) {
        return BeautyAppealVO.builder()
            .id(appeal.getId())
            .goodId(appeal.getGoodId())
            .goodTitle(appeal.getGoodTitle())
            .category(appeal.getCategory())
            .issueType(appeal.getIssueType())
            .reason(appeal.getReason())
            .buyerName(appeal.getBuyerName())
            .sellerName(appeal.getSellerName())
            .contactPhone(appeal.getContactPhone())
            .status(appeal.getStatus())
            .proofSummary(appeal.getProofSummary())
            .refundAmount(appeal.getRefundAmount())
            .createdAt(appeal.getCreatedAt())
            .handledAt(appeal.getHandledAt())
            .handleRemark(appeal.getHandleRemark())
            .build();
    }

    private BeautyStatsItemVO toStatsItem(BeautyGood good) {
        return BeautyStatsItemVO.builder()
            .id(good.getId())
            .title(good.getTitle())
            .category(categoryLabel(good.getCategory()))
            .favoriteCount(good.getFavoriteCount() == null ? 0 : good.getFavoriteCount())
            .viewCount(good.getViewCount() == null ? 0 : good.getViewCount())
            .status(good.getStatus())
            .build();
    }

    private void applyBeautyContent(BeautyGood good, Long operatorId, AdminBeautyGoodSaveRequest request) {
        FileAsset productImage = requireAnyFile(request.getProductImageFileId(), "商品实拍图不存在");
        FileAsset receiptImage = requireAnyFile(request.getReceiptFileId(), "购买凭证不存在");
        List<String> tags = normalizeSkinTags(request.getSkinTags());
        good.setCategory(request.getCategory());
        good.setTitle(request.getTitle());
        good.setPrice(request.getPrice());
        good.setSummary(request.getSummary());
        good.setUsageGuide(request.getUsageGuide());
        good.setSceneText(defaultText(request.getSceneText(), "适合学生日常通勤、宿舍便携收纳和基础入门选购场景。"));
        good.setEvaluation(defaultText(request.getEvaluation(), defaultText(request.getUsageGuide(), request.getSummary())));
        good.setDormExperience(defaultText(request.getDormExperience(), "支持宿舍场景下快速参考商品外观、价格和使用体验。"));
        good.setAvoidanceGuide(defaultText(request.getAvoidanceGuide(), "建议先核对购买凭证、产品实拍和自身需求，再决定是否入手。"));
        good.setSkinTagsJson(writeJson(tags));
        if (good.getReviewJson() == null || good.getReviewJson().isBlank()) {
            good.setReviewJson(writeJson(Collections.emptyList()));
        }
        good.setGalleryJson(writeJson(List.of(publicUrl(productImage), publicUrl(receiptImage))));
        good.setImageUrl(publicUrl(productImage));
        good.setProductImageFileId(productImage.getId());
        good.setReceiptFileId(receiptImage.getId());
    }

    private List<String> normalizeSkinTags(List<String> source) {
        if (source == null) {
            return List.of("平价好物");
        }
        List<String> tags = source.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(item -> !item.isBlank())
            .distinct()
            .limit(8)
            .toList();
        return tags.isEmpty() ? List.of("平价好物") : tags;
    }

    private void insertAdminLog(Long adminId, String operationType, Long targetId, String remark) {
        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(adminId);
        log.setOperationType(operationType);
        log.setTargetType("BEAUTY_GOOD");
        log.setTargetId(targetId);
        log.setRemark(remark);
        adminOperationLogMapper.insert(log);
    }

    private Page<BeautyGoodVO> buildVoPage(List<BeautyGood> source, int current, int size, Long userId) {
        Page<BeautyGoodVO> page = Page.of(current, size);
        int fromIndex = Math.max(0, (current - 1) * size);
        int toIndex = Math.min(source.size(), fromIndex + size);
        List<BeautyGoodVO> records = fromIndex >= source.size()
            ? Collections.emptyList()
            : source.subList(fromIndex, toIndex).stream().map(item -> toVo(item, userId)).toList();
        page.setRecords(records);
        page.setTotal(source.size());
        return page;
    }

    private BeautyGoodVO toVo(BeautyGood good, Long userId) {
        return BeautyGoodVO.builder()
            .id(good.getId())
            .category(good.getCategory())
            .title(good.getTitle())
            .price(good.getPrice())
            .summary(good.getSummary())
            .usageGuide(good.getUsageGuide())
            .sceneText(good.getSceneText())
            .evaluation(good.getEvaluation())
            .dormExperience(good.getDormExperience())
            .avoidanceGuide(good.getAvoidanceGuide())
            .skinTags(readStringList(good.getSkinTagsJson()))
            .reviewList(readReviews(good.getReviewJson()))
            .gallery(readStringList(good.getGalleryJson()))
            .imageUrl(resolveImageUrl(good))
            .productImage(toFileVo(good.getProductImageFileId()))
            .receiptImage(toFileVo(good.getReceiptFileId()))
            .creatorId(good.getCreatorId())
            .creatorNickname(good.getCreatorNickname())
            .status(good.getStatus())
            .sourceType(good.getSourceType())
            .rejectReason(good.getRejectReason())
            .reviewedBy(good.getReviewedBy())
            .reviewedAt(good.getReviewedAt())
            .favoriteCount(good.getFavoriteCount() == null ? 0 : good.getFavoriteCount())
            .viewCount(good.getViewCount() == null ? 0 : good.getViewCount())
            .favorite(userId != null && isFavorited(userId, good.getId()))
            .publishedAt(good.getPublishedAt())
            .createdAt(good.getCreatedAt())
            .build();
    }

    private boolean isFavorited(Long userId, Long goodId) {
        return beautyFavoriteMapper.selectCount(
            new LambdaQueryWrapper<BeautyFavorite>()
                .eq(BeautyFavorite::getUserId, userId)
                .eq(BeautyFavorite::getGoodId, goodId)
        ) > 0;
    }

    private List<String> readStringList(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {
            });
        } catch (Exception error) {
            return Collections.emptyList();
        }
    }

    private List<BeautyReviewVO> readReviews(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<BeautyReviewVO>>() {
            });
        } catch (Exception error) {
            return Collections.emptyList();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception error) {
            throw new BusinessException(500, "美妆数据序列化失败");
        }
    }

    private FileAssetVO toFileVo(Long fileId) {
        if (fileId == null) {
            return null;
        }
        FileAsset asset = fileAssetMapper.selectById(fileId);
        if (asset == null) {
            return null;
        }
        return FileAssetVO.builder()
            .fileId(asset.getId())
            .url(publicUrl(asset))
            .originalName(asset.getOriginalName())
            .build();
    }

    private String resolveImageUrl(BeautyGood good) {
        if (good.getImageUrl() != null && !good.getImageUrl().isBlank()) {
            return good.getImageUrl();
        }
        FileAssetVO productImage = toFileVo(good.getProductImageFileId());
        if (productImage != null) {
            return productImage.getUrl();
        }
        FileAssetVO receiptImage = toFileVo(good.getReceiptFileId());
        return receiptImage == null ? "" : receiptImage.getUrl();
    }

    private FileAsset requireFile(Long fileId, Long userId, String message) {
        FileAsset asset = fileAssetMapper.selectById(fileId);
        if (asset == null || !Objects.equals(asset.getUploaderId(), userId)) {
            throw new BusinessException(message);
        }
        return asset;
    }

    private FileAsset requireAnyFile(Long fileId, String message) {
        FileAsset asset = fileAssetMapper.selectById(fileId);
        if (asset == null) {
            throw new BusinessException(message);
        }
        return asset;
    }

    private boolean matchPriceRange(BeautyGood item, String priceRange) {
        if (priceRange == null || priceRange.isBlank()) {
            return true;
        }
        if (item.getPrice() == null) {
            return false;
        }
        return switch (priceRange) {
            case "UNDER_50" -> item.getPrice().doubleValue() < 50;
            case "BETWEEN_50_100" -> item.getPrice().doubleValue() >= 50 && item.getPrice().doubleValue() <= 100;
            case "OVER_100" -> item.getPrice().doubleValue() > 100;
            default -> true;
        };
    }

    private boolean matchSkinTag(BeautyGood item, String skinTag) {
        if (skinTag == null || skinTag.isBlank()) {
            return true;
        }
        return readStringList(item.getSkinTagsJson()).stream().anyMatch(tag -> tag.contains(skinTag));
    }

    private String defaultText(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }

    private String categoryLabel(String category) {
        return switch (String.valueOf(category).toUpperCase()) {
            case "MAKEUP" -> "护肤彩妆";
            case "CARE" -> "个护收纳";
            case "ACCESSORY" -> "配套好物";
            default -> "平价好物";
        };
    }

    private String normalizeStatus(String status) {
        return String.valueOf(status).trim().toUpperCase();
    }

    private String defaultAppealRemark(String status) {
        return switch (status) {
            case "PENDING" -> "售后申请已提交，等待平台受理";
            case "PROCESSING" -> "平台已受理售后申请，正在核查订单截图与实拍图";
            case "RESOLVED" -> "已核实申请成立，平台支持退款或补发处理";
            case "REJECTED" -> "现有凭证不足，暂不支持当前售后申请";
            default -> "平台已更新售后工单状态";
        };
    }

    private boolean isAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && RoleType.ADMIN.name().equalsIgnoreCase(user.getRole());
    }

    private String publicUrl(FileAsset asset) {
        return appProperties.getFile().getPublicBaseUrl() + asset.getRelativePath();
    }
}
