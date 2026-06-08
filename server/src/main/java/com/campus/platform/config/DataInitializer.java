package com.campus.platform.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.platform.entity.ErrandOrder;
import com.campus.platform.entity.ErrandRuleConfig;
import com.campus.platform.entity.BeautyAppeal;
import com.campus.platform.entity.JobsAccount;
import com.campus.platform.entity.JobsMerchantQualification;
import com.campus.platform.entity.JobsPost;
import com.campus.platform.entity.JobsReport;
import com.campus.platform.entity.JobsSkillTag;
import com.campus.platform.entity.PartnerConversation;
import com.campus.platform.entity.PartnerConversationMessage;
import com.campus.platform.entity.PartnerDemand;
import com.campus.platform.entity.PartnerReport;
import com.campus.platform.entity.BeautyGood;
import com.campus.platform.entity.User;
import com.campus.platform.entity.UserProfile;
import com.campus.platform.enums.ErrandOrderStatus;
import com.campus.platform.enums.ErrandServiceType;
import com.campus.platform.enums.RoleType;
import com.campus.platform.enums.UserStatus;
import com.campus.platform.mapper.BeautyAppealMapper;
import com.campus.platform.mapper.BeautyGoodMapper;
import com.campus.platform.mapper.ErrandOrderMapper;
import com.campus.platform.mapper.ErrandRuleConfigMapper;
import com.campus.platform.mapper.JobsAccountMapper;
import com.campus.platform.mapper.JobsMerchantQualificationMapper;
import com.campus.platform.mapper.JobsPostMapper;
import com.campus.platform.mapper.JobsReportMapper;
import com.campus.platform.mapper.JobsSkillTagMapper;
import com.campus.platform.mapper.PartnerConversationMapper;
import com.campus.platform.mapper.PartnerConversationMessageMapper;
import com.campus.platform.mapper.PartnerDemandMapper;
import com.campus.platform.mapper.PartnerReportMapper;
import com.campus.platform.mapper.UserMapper;
import com.campus.platform.mapper.UserProfileMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(10)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final ErrandOrderMapper errandOrderMapper;
    private final ErrandRuleConfigMapper errandRuleConfigMapper;
    private final BeautyAppealMapper beautyAppealMapper;
    private final BeautyGoodMapper beautyGoodMapper;
    private final JobsAccountMapper jobsAccountMapper;
    private final JobsMerchantQualificationMapper jobsMerchantQualificationMapper;
    private final JobsPostMapper jobsPostMapper;
    private final JobsReportMapper jobsReportMapper;
    private final JobsSkillTagMapper jobsSkillTagMapper;
    private final PartnerDemandMapper partnerDemandMapper;
    private final PartnerReportMapper partnerReportMapper;
    private final PartnerConversationMapper partnerConversationMapper;
    private final PartnerConversationMessageMapper partnerConversationMessageMapper;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    public void run(String... args) {
        ensureErrandRuleConfig();
        ensureUser("18800000000", "admin123", "系统管理员", RoleType.ADMIN.name(), "默认管理员账号");
        ensureUser("18800000001", "user123", "默认演示用户", RoleType.USER.name(), "默认用户账号");
        ensureUser("18800000002", "user123", "南苑同学", RoleType.USER.name(), "演示跑腿发单用户");
        ensureUser("18800000003", "user123", "北苑同学", RoleType.USER.name(), "演示跑腿发单用户");
        ensureUser("18800000004", "user123", "图书馆同学", RoleType.USER.name(), "演示跑腿发单用户");
        ensureUser("18800000005", "user123", "晚风同学", RoleType.USER.name(), "常用美妆与找搭子演示用户");
        ensureUser("18800000006", "user123", "晨曦同学", RoleType.USER.name(), "常用勤工助学演示用户");
        ensureUser("18800000007", "user123", "社团小助理", RoleType.USER.name(), "擅长校园协作与线上接单");
        ensureUser("18800000008", "user123", "咖啡店主理人", RoleType.USER.name(), "用于商家与兼职岗位演示");
        ensureSeedErrandOrders();
        ensureSeedBeautyGoods();
        ensureSeedBeautyAppeals();
        ensureSeedJobsAdmin();
        ensureSeedPartnerAdmin();
        normalizeSeedPresentationData();
    }

    private void ensureErrandRuleConfig() {
        if (!tableExists("errand_rule_config")) {
            return;
        }
        ErrandRuleConfig config = errandRuleConfigMapper.selectOne(
            new LambdaQueryWrapper<ErrandRuleConfig>().eq(ErrandRuleConfig::getConfigKey, "default")
        );
        if (config != null) {
            return;
        }
        config = new ErrandRuleConfig();
        config.setConfigKey("default");
        config.setUrgentFee(appProperties.getErrand().getUrgentFee());
        config.setFragileFee(appProperties.getErrand().getFragileFee());
        config.setPublishLimitPerUser(appProperties.getErrand().getPublishLimitPerUser());
        config.setAcceptLimitPerUser(appProperties.getErrand().getAcceptLimitPerUser());
        config.setAutoExpireHours(appProperties.getErrand().getAutoExpireHours());
        config.setMinBaseFee(appProperties.getErrand().getMinBaseFee());
        config.setMaxBaseFee(appProperties.getErrand().getMaxBaseFee());
        errandRuleConfigMapper.insert(config);
    }

    private void ensureUser(String phone, String password, String nickname, String role, String bio) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user != null) {
            return;
        }
        user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE.name());
        user.setReportRestricted(0);
        userMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setContactPhone(user.getPhone());
        profile.setBio(bio);
        profile.setPublishRole("STUDENT");
        userProfileMapper.insert(profile);
    }

    private void ensureSeedErrandOrders() {
        User user1 = findUser("18800000001");
        User user2 = findUser("18800000002");
        User user3 = findUser("18800000003");
        User user4 = findUser("18800000004");
        User user5 = findUser("18800000005");
        User user6 = findUser("18800000006");
        if (user1 == null || user2 == null || user3 == null || user4 == null || user5 == null || user6 == null) {
            return;
        }

        createPublishedOrder(
            "ER-SEED-001",
            user2.getId(),
            ErrandServiceType.PICKUP.name(),
            "南苑菜鸟驿站 3 号取件柜",
            "一食堂东门自提点",
            "今天 18:30 前",
            "帮忙代取一件快递，取件码在聊天里发你。",
            "纸箱不大，拿到后放前台即可。",
            new BigDecimal("5.00"),
            true,
            false,
            6,
            1
        );

        createPublishedOrder(
            "ER-SEED-002",
            user3.getId(),
            ErrandServiceType.DELIVERY.name(),
            "图书馆一楼服务台",
            "北苑 8 号楼宿舍门口",
            "今晚 20:00 前",
            "帮送一份复习资料到宿舍楼下，到了打电话。",
            "资料装在蓝色文件袋里。",
            new BigDecimal("8.00"),
            false,
            false,
            5,
            2
        );

        createPublishedOrder(
            "ER-SEED-003",
            user4.getId(),
            ErrandServiceType.PURCHASE.name(),
            "校内超市",
            "体育馆西门",
            "中午 12:30 前",
            "帮买两瓶矿泉水和一份面包，价格按实际沟通。",
            "天气热，优先尽快送到。",
            new BigDecimal("10.00"),
            true,
            false,
            4,
            3
        );

        createPublishedOrder(
            "ER-SEED-004",
            user1.getId(),
            ErrandServiceType.PRINT.name(),
            "创新楼打印店",
            "行政楼 A 座门口",
            "明天早上 09:00 前",
            "帮忙打印论文 36 页，双面黑白装订。",
            "文件会在聊天里发，注意别漏页。",
            new BigDecimal("12.00"),
            false,
            true,
            8,
            4
        );

        createPublishedOrder(
            "ER-SEED-005",
            user2.getId(),
            ErrandServiceType.PICKUP.name(),
            "西门外卖柜",
            "南苑 2 号楼大厅",
            "今晚 19:00 前",
            "外卖到了但临时开会，帮拿到宿舍楼下。",
            "饮品较多，注意平稳。",
            new BigDecimal("6.00"),
            false,
            true,
            3,
            5
        );

        createPublishedOrder(
            "ER-SEED-006",
            user3.getId(),
            ErrandServiceType.DELIVERY.name(),
            "实验楼 B 栋大厅",
            "东操场看台入口",
            "下午 17:30 前",
            "把一把折叠伞送到东操场，人到了联系。",
            "伞是黑色的，袋子上有名字。",
            new BigDecimal("7.50"),
            false,
            false,
            7,
            6
        );

        createPublishedOrder(
            "ER-SEED-007",
            user5.getId(),
            ErrandServiceType.PURCHASE.name(),
            "校医院旁便利店",
            "研究生公寓门口",
            "今晚 21:00 前",
            "帮忙带一盒创可贴、一瓶无糖乌龙和一包纸巾，到楼下电话联系。",
            "腿伤不方便下楼，希望代买后尽快送达。",
            new BigDecimal("9.00"),
            false,
            false,
            5,
            7
        );

        createPublishedOrder(
            "ER-SEED-008",
            user6.getId(),
            ErrandServiceType.PICKUP.name(),
            "北苑快递驿站 2 号架",
            "行政楼后门值班室",
            "下午 16:30 前",
            "代取实验耗材快递一件，包装偏长但不重。",
            "到行政楼后门放门卫处并拍照告知。",
            new BigDecimal("8.50"),
            true,
            true,
            4,
            8
        );

        createSeedErrandOrder(
            "ER-SEED-009",
            user1.getId(),
            user5.getId(),
            ErrandServiceType.DELIVERY.name(),
            "艺术楼 102 教室",
            "新媒体中心前台",
            "今天 14:30 前",
            "帮送社团海报样张到新媒体中心，路上注意别折到。",
            "已被接单，等待送达。",
            new BigDecimal("6.00"),
            false,
            false,
            ErrandOrderStatus.ACCEPTED.name(),
            0,
            LocalDateTime.now().plusHours(2),
            LocalDateTime.now().minusMinutes(40),
            null,
            null,
            null,
            null,
            null
        );

        createSeedErrandOrder(
            "ER-SEED-010",
            user2.getId(),
            user6.getId(),
            ErrandServiceType.PRINT.name(),
            "教学楼 B 栋打印点",
            "外语楼三层办公室",
            "今天 15:20 前",
            "打印答辩资料并送到外语楼办公室，文件已发给接单同学。",
            "打印完成后正在配送途中。",
            new BigDecimal("11.00"),
            false,
            true,
            ErrandOrderStatus.DELIVERING.name(),
            0,
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().minusMinutes(35),
            null,
            null,
            null,
            null
        );

        createSeedErrandOrder(
            "ER-SEED-011",
            user3.getId(),
            user4.getId(),
            ErrandServiceType.PICKUP.name(),
            "西门奶茶店取餐台",
            "图书馆南门长椅",
            "昨天 19:00 前",
            "帮忙带一杯无糖奶茶到图书馆门口，到了发消息。",
            "订单已顺利完成。",
            new BigDecimal("5.50"),
            false,
            false,
            ErrandOrderStatus.COMPLETED.name(),
            0,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusDays(1).plusHours(1),
            LocalDateTime.now().minusDays(1).plusHours(2),
            LocalDateTime.now().minusDays(1).plusHours(3),
            LocalDateTime.now().minusDays(1).plusHours(3).plusMinutes(10),
            null,
            null
        );

        createSeedErrandOrder(
            "ER-SEED-012",
            user5.getId(),
            null,
            ErrandServiceType.PURCHASE.name(),
            "校外药店",
            "宿舍楼下",
            "昨晚 20:00 前",
            "帮忙买退烧贴和温度计，后来已由室友代买。",
            "发布后取消，不再需要。",
            new BigDecimal("12.00"),
            true,
            false,
            ErrandOrderStatus.CANCELLED.name(),
            0,
            LocalDateTime.now().minusDays(1),
            null,
            null,
            null,
            null,
            LocalDateTime.now().minusHours(16),
            "室友已顺路帮忙购买"
        );

        createSeedErrandOrder(
            "ER-SEED-013",
            user6.getId(),
            user2.getId(),
            ErrandServiceType.DELIVERY.name(),
            "实验楼 A 栋门口",
            "南苑 6 号楼",
            "前天 18:00 前",
            "帮送实验记录本回宿舍，接单后超过约定时间未送达。",
            "当前进入争议处理中。",
            new BigDecimal("7.00"),
            false,
            false,
            ErrandOrderStatus.DISPUTED.name(),
            0,
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now().minusDays(2).plusMinutes(20),
            null,
            null,
            null,
            null,
            null
        );
    }

    private void createPublishedOrder(String orderNo,
                                      Long publisherId,
                                      String serviceType,
                                      String pickupAddress,
                                      String deliveryAddress,
                                      String pickupTimeText,
                                      String detailContent,
                                      String remark,
                                      BigDecimal baseFee,
                                      boolean urgent,
                                      boolean fragile,
                                      int deadlineHours,
                                      int orderIndex) {
        createSeedErrandOrder(
            orderNo,
            publisherId,
            null,
            serviceType,
            pickupAddress,
            deliveryAddress,
            pickupTimeText,
            detailContent,
            remark,
            baseFee,
            urgent,
            fragile,
            ErrandOrderStatus.PUBLISHED.name(),
            1,
            LocalDateTime.now().plusHours(deadlineHours),
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    private void createSeedErrandOrder(String orderNo,
                                       Long publisherId,
                                       Long runnerId,
                                       String serviceType,
                                       String pickupAddress,
                                       String deliveryAddress,
                                       String pickupTimeText,
                                       String detailContent,
                                       String remark,
                                       BigDecimal baseFee,
                                       boolean urgent,
                                       boolean fragile,
                                       String status,
                                       int publicVisible,
                                       LocalDateTime acceptDeadline,
                                       LocalDateTime acceptedAt,
                                       LocalDateTime pickedUpAt,
                                       LocalDateTime deliveredAt,
                                       LocalDateTime completedAt,
                                       LocalDateTime cancelledAt,
                                       String cancelReason) {
        ErrandOrder exists = errandOrderMapper.selectOne(
            new LambdaQueryWrapper<ErrandOrder>().eq(ErrandOrder::getOrderNo, orderNo)
        );
        if (exists != null) {
            return;
        }
        ErrandOrder order = new ErrandOrder();
        order.setOrderNo(orderNo);
        order.setPublisherId(publisherId);
        order.setRunnerId(runnerId);
        order.setServiceType(serviceType);
        order.setPickupAddress(pickupAddress);
        order.setDeliveryAddress(deliveryAddress);
        order.setPickupTimeText(pickupTimeText);
        order.setDetailContent(detailContent);
        order.setRemark(remark);
        order.setBaseFee(baseFee);
        order.setUrgentFlag(urgent ? 1 : 0);
        order.setFragileFlag(fragile ? 1 : 0);
        order.setUrgentFee(urgent ? new BigDecimal("2.00") : BigDecimal.ZERO);
        order.setFragileFee(fragile ? new BigDecimal("3.00") : BigDecimal.ZERO);
        order.setTotalFee(order.getBaseFee().add(order.getUrgentFee()).add(order.getFragileFee()));
        order.setStatus(status);
        order.setAcceptDeadline(acceptDeadline);
        order.setAcceptedAt(acceptedAt);
        order.setPickedUpAt(pickedUpAt);
        order.setDeliveredAt(deliveredAt);
        order.setCompletedAt(completedAt);
        order.setCancelledAt(cancelledAt);
        order.setCancelReason(cancelReason);
        order.setPublicVisible(publicVisible);
        errandOrderMapper.insert(order);
    }

    private User findUser(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    private void ensureSeedBeautyGoods() {
        if (!tableExists("beauty_good")) {
            return;
        }
        User user1 = findUser("18800000001");
        User user5 = findUser("18800000005");
        if (user1 == null || user5 == null) {
            return;
        }

        createBeautyGoodIfAbsent(
            5001L,
            user1,
            "MAKEUP",
            "轻透持妆气垫粉底",
            new BigDecimal("69.00"),
            "粉质细腻，上脸轻薄不闷，适合早八赶时间快速拍开。",
            "适合早八上课、日常通勤和社团外出等需要快速完成底妆的学生场景。",
            "这款气垫更偏轻薄自然路线，适合学生党日常通勤妆。",
            "宿舍使用体验比较友好，开盖即用，不需要另外调和。",
            "如果是明显干皮，上妆前要先把保湿做好。",
            List.of("混油皮", "自然妆感"),
            List.of(
                "{\"id\":\"r-1\",\"user\":\"大二通勤党\",\"content\":\"遮瑕够日常，拍开很快。\"}",
                "{\"id\":\"r-2\",\"user\":\"混油皮测评\",\"content\":\"午后补一次就够，不会假面。\"}"
            ),
            List.of(
                "https://images.unsplash.com/photo-1631214540242-6f6f7a0f7d6a?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1596704017254-9a8859e7751d?auto=format&fit=crop&w=900&q=80"
            ),
            "https://images.unsplash.com/photo-1631214540242-6f6f7a0f7d6a?auto=format&fit=crop&w=900&q=80"
        );

        createBeautyGoodIfAbsent(
            5002L,
            user1,
            "MAKEUP",
            "奶杏豆沙镜面唇釉",
            new BigDecimal("39.00"),
            "颜色温柔显白，成膜快，不挑场景，日常和约会都能搭。",
            "适合课堂通勤、社团活动和轻约会等需要自然提气色的日常妆容。",
            "整体颜色偏温柔奶杏豆沙调，适合学生党日常低门槛使用。",
            "宿舍日常随手补涂很方便，搭配简单底妆就能撑起完整感。",
            "如果嘴唇起皮明显，先做好润唇打底再上色。",
            List.of("黄皮友好", "提气色"),
            List.of(
                "{\"id\":\"r-5\",\"user\":\"黄皮唇妆控\",\"content\":\"颜色很日常，素颜薄涂也不会突兀。\"}",
                "{\"id\":\"r-6\",\"user\":\"宿舍试色组\",\"content\":\"镜面感很漂亮，拍照显得嘴巴状态特别好。\"}"
            ),
            List.of(
                "https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1616394584738-fc6e612e71b9?auto=format&fit=crop&w=900&q=80"
            ),
            "https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=900&q=80"
        );

        createBeautyGoodIfAbsent(
            5003L,
            user1,
            "MAKEUP",
            "三色修容高光盘",
            new BigDecimal("48.00"),
            "一盘满足鼻影、高光和下颌修饰，粉质细腻不容易结块。",
            "适合通勤妆、活动妆和拍照前临时补轮廓，让面部更立体。",
            "这一盘更适合刚入门的学生党做基础轮廓修饰，颜色不会太脏。",
            "宿舍灯光下也比较容易控制用量，早上快速修容不容易翻车。",
            "刷子沾粉后先抖掉浮粉，再轻扫边缘，避免一开始就把颜色压得太实。",
            List.of("新手适用", "通勤轮廓"),
            List.of(
                "{\"id\":\"r-7\",\"user\":\"新手修容组\",\"content\":\"粉质比较柔和，不容易一下下手太重。\"}"
            ),
            List.of(
                "https://images.unsplash.com/photo-1526045478516-99145907023c?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1583241475880-083f84372725?auto=format&fit=crop&w=900&q=80"
            ),
            "https://images.unsplash.com/photo-1526045478516-99145907023c?auto=format&fit=crop&w=900&q=80"
        );

        createBeautyGoodIfAbsent(
            5004L,
            user1,
            "MAKEUP",
            "新手顺滑眉笔套装",
            new BigDecimal("29.00"),
            "笔芯软硬适中，附带眉刷，新手不容易一下画太重。",
            "适合早八淡妆、新手入门妆和日常快速整理眉形。",
            "属于比较稳妥的入门型眉笔，颜色柔和，适合快速修整眉形。",
            "日常放在宿舍桌面或者化妆包里都很方便，早上简单画几笔就能精神不少。",
            "眉头不要一次下笔过重，建议先画眉尾再把余粉轻扫到眉头。",
            List.of("扁平眉头", "自然毛流"),
            List.of(
                "{\"id\":\"r-8\",\"user\":\"眉毛手残党\",\"content\":\"颜色自然，附带的眉刷很实用。\"}"
            ),
            List.of(
                "https://images.unsplash.com/photo-1625093742435-6fa192b6fb10?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1596704017254-9a8859e7751d?auto=format&fit=crop&w=900&q=80"
            ),
            "https://images.unsplash.com/photo-1625093742435-6fa192b6fb10?auto=format&fit=crop&w=900&q=80"
        );

        createBeautyGoodIfAbsent(
            5007L,
            user1,
            "ACCESSORY",
            "便携补妆镜小夜灯款",
            new BigDecimal("32.00"),
            "镜面清晰，自带柔光灯，图书馆和宿舍补妆都很方便。",
            "适合宿舍补妆、图书馆整理妆面和外出旅行随身携带。",
            "对经常在外补妆的学生党来说很实用，便携性强。",
            "放在书包里不占太多地方，课间和外出时都能快速拿出来补妆。",
            "出门携带时建议放进软袋里，避免和钥匙等硬物一起磕碰镜面。",
            List.of("宿舍补妆", "便携外出"),
            List.of(
                "{\"id\":\"r-3\",\"user\":\"补妆焦虑患者\",\"content\":\"灯光很实用，晚上回宿舍也能看清底妆状态。\"}"
            ),
            List.of(
                "https://images.unsplash.com/photo-1583241801160-61f4f315b5a2?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?auto=format&fit=crop&w=900&q=80"
            ),
            "https://images.unsplash.com/photo-1583241801160-61f4f315b5a2?auto=format&fit=crop&w=900&q=80"
        );

        createBeautyGoodIfAbsent(
            5006L,
            user1,
            "ACCESSORY",
            "柔软抓粉腮红刷",
            new BigDecimal("19.00"),
            "刷毛细软不扎脸，抓粉力适中，适合新手慢慢叠色。",
            "适合腮红晕染、高光辅助和新手建立基础刷具习惯。",
            "这类基础刷具对学生党更实用，价格不高但能明显提升上妆均匀度。",
            "日常拿来扫腮红和高光都够用，宿舍化妆时不用准备太多复杂工具。",
            "第一次使用前先轻洗一次并晾干，可以减少浮毛，也更卫生。",
            List.of("敏感肌可用", "均匀晕染"),
            List.of(
                "{\"id\":\"r-9\",\"user\":\"新手刷具党\",\"content\":\"刷毛很软，晕染腮红不容易一坨堆在脸上。\"}"
            ),
            List.of(
                "https://images.unsplash.com/photo-1590156205333-1b5d7d7357df?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1512496015851-a90fb38ba796?auto=format&fit=crop&w=900&q=80"
            ),
            "https://images.unsplash.com/photo-1590156205333-1b5d7d7357df?auto=format&fit=crop&w=900&q=80"
        );

        createBeautyGoodIfAbsent(
            5005L,
            user1,
            "CARE",
            "桌面分格化妆收纳盒",
            new BigDecimal("25.00"),
            "把底妆、唇妆、眉笔按格收好，桌面看起来更整洁，拿取也方便。",
            "适合宿舍桌面收纳、化妆台整理和高频单品集中摆放。",
            "虽然不是彩妆单品，但对学生宿舍场景非常友好。",
            "日常把底妆、眉笔和唇妆按格摆好，早上出门补妆和拿取都更顺手。",
            "如果放在靠窗高温位置，建议不要长期暴晒，避免塑料材质老化变形。",
            List.of("宿舍友好", "防尘整理"),
            List.of(
                "{\"id\":\"r-4\",\"user\":\"宿舍收纳组\",\"content\":\"容量比想象中大，基础通勤妆单品一盒就能装下。\"}"
            ),
            List.of(
                "https://images.unsplash.com/photo-1612817159949-195b6eb9e31a?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?auto=format&fit=crop&w=900&q=80"
            ),
            "https://images.unsplash.com/photo-1612817159949-195b6eb9e31a?auto=format&fit=crop&w=900&q=80"
        );

        createPendingBeautyGoodIfAbsent(
            5012L,
            user1,
            "MAKEUP",
            "待审核演示奶雾唇泥",
            new BigDecimal("27.90"),
            "用于演示前台发布后待审核、后台审核后再公开展示的完整闭环。",
            "提交后会先在“我的发布”里显示审核中，后台通过后再进入首页公开展示。",
            "https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=900&q=80"
        );

        createBeautyGoodIfAbsent(
            5008L,
            user5,
            "CARE",
            "积雪草舒缓补水面膜",
            new BigDecimal("36.00"),
            "膜布服帖，精华量足，晒后和熬夜后都适合拿来快速补水舒缓。",
            "适合军训晒后、换季泛红和宿舍空调房干燥时做基础舒缓补水。",
            "属于学生党常见的低门槛舒缓面膜，适合囤在宿舍应急。",
            "晚上洗漱后敷 10 分钟就够，后续简单叠加乳液会更舒服。",
            "如果皮肤本身破损明显，先避开破损区域使用。",
            List.of("换季舒缓", "晒后补水"),
            List.of(
                "{\"id\":\"r-10\",\"user\":\"军训补水组\",\"content\":\"晒后敷着很舒服，第二天脸没那么干。\"}",
                "{\"id\":\"r-11\",\"user\":\"宿舍护肤党\",\"content\":\"精华不黏，秋冬在空调房也够用。\"}"
            ),
            List.of(
                "https://images.unsplash.com/photo-1625772452859-1c03d5bf1137?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=900&q=80"
            ),
            "https://images.unsplash.com/photo-1625772452859-1c03d5bf1137?auto=format&fit=crop&w=900&q=80"
        );

        createBeautyGoodIfAbsent(
            5009L,
            user5,
            "CARE",
            "清爽控油洁面泡沫",
            new BigDecimal("42.00"),
            "起泡快、清洁力温和，适合油皮和混油皮日常晨晚洁面。",
            "适合早八前快速洗脸、运动后清洁和夏季出油明显时使用。",
            "更偏日常温和清洁路线，不会像强清洁型那样洗完绷脸。",
            "放在宿舍洗手台很好用，按压两泵就能覆盖全脸。",
            "如果是明显敏感干皮，不建议过度清洁，一天 1 到 2 次即可。",
            List.of("混油皮", "夏季清洁"),
            List.of(
                "{\"id\":\"r-12\",\"user\":\"晨间洁面组\",\"content\":\"早上洗完很清爽，但不会拔干。\"}"
            ),
            List.of(
                "https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1617897903246-719242758050?auto=format&fit=crop&w=900&q=80"
            ),
            "https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=900&q=80"
        );

        createBeautyGoodIfAbsent(
            5010L,
            user5,
            "MAKEUP",
            "细闪卧蚕提亮笔",
            new BigDecimal("24.90"),
            "珠光细腻不夸张，轻轻一画就能让眼下更有精神。",
            "适合课堂淡妆、拍照妆和想快速提气色的日常眼部点缀。",
            "对新手来说比较友好，随手提亮卧蚕也不容易显脏。",
            "早上化妆时间紧的时候，卧蚕和眼头点一下就能提升完整度。",
            "如果眼下卡纹明显，先少量上妆，再用指腹轻拍开。",
            List.of("新手眼妆", "自然提亮"),
            List.of(
                "{\"id\":\"r-13\",\"user\":\"淡妆通勤党\",\"content\":\"闪得很细，不会像大亮片那样突兀。\"}"
            ),
            List.of(
                "https://images.unsplash.com/photo-1512496015851-a90fb38ba796?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1526045478516-99145907023c?auto=format&fit=crop&w=900&q=80"
            ),
            "https://images.unsplash.com/photo-1512496015851-a90fb38ba796?auto=format&fit=crop&w=900&q=80"
        );

        createBeautyGoodIfAbsent(
            5011L,
            user1,
            "ACCESSORY",
            "宿舍桌面亚克力口红收纳架",
            new BigDecimal("28.80"),
            "透明分格设计，常用唇釉和口红能一眼看到，桌面更整齐。",
            "适合宿舍梳妆区、社团化妆间和日常高频彩妆集中收纳。",
            "对口红比较多的学生党很友好，拿取快也方便整理。",
            "把常用色号分区摆开，早上赶课时挑色更快。",
            "建议定期擦拭灰尘，避免透明材质长期堆积指纹和粉尘。",
            List.of("桌面收纳", "透明分格"),
            List.of(
                "{\"id\":\"r-14\",\"user\":\"彩妆整理组\",\"content\":\"唇釉竖着摆很稳，看着也整洁很多。\"}"
            ),
            List.of(
                "https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1612817159949-195b6eb9e31a?auto=format&fit=crop&w=900&q=80"
            ),
            "https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=900&q=80"
        );
    }

    private void ensureSeedBeautyAppeals() {
        if (!tableExists("beauty_appeal")) {
            return;
        }
        createBeautyAppeal(9601L, 5002L, "奶杏豆沙镜面唇釉", "MAKEUP", "描述不符", "收到后色号与详情页差异较大，实际偏橘棕，不符合宣传图展示。", "苏同学", "默认演示用户", "18800004061", "PENDING", "已提交开箱实拍图、下单截图和色号对比图。", new BigDecimal("39.00"), null, null, LocalDateTime.now().minusDays(1).withHour(11).withMinute(10));
        createBeautyAppeal(9602L, 5005L, "桌面分格化妆收纳盒", "CARE", "过敏反馈", "使用后出现明显泛红和刺痛，希望平台介入核查并处理退款。", "周同学", "默认演示用户", "18800004062", "PROCESSING", "已提交皮肤状态照片、包装图、聊天沟通记录。", new BigDecimal("25.00"), "已受理，正在核对商品批次与买卖双方沟通记录。", LocalDateTime.now().minusHours(12), LocalDateTime.now().minusDays(2).withHour(18).withMinute(40));
        createBeautyAppeal(9603L, 5006L, "柔软抓粉腮红刷", "ACCESSORY", "商品破损", "快递到手后发现刷柄有裂痕，外包装也有明显挤压痕迹。", "林同学", "默认演示用户", "18800004063", "RESOLVED", "提供破损照片、外包装照片及签收时间截图。", new BigDecimal("19.00"), "核实属物流破损，平台已判定退款并提醒卖家优化包装。", LocalDateTime.now().minusDays(3).withHour(18).withMinute(20), LocalDateTime.now().minusDays(3).withHour(15).withMinute(30));
        createBeautyAppeal(9604L, 5007L, "便携补妆镜小夜灯款", "ACCESSORY", "假货投诉", "收到商品材质与原品牌图明显不符，怀疑并非正品同款。", "赵同学", "默认演示用户", "18800004064", "REJECTED", "仅提交单张商品图，未补充订单凭证与品牌对比说明。", new BigDecimal("32.00"), "现有凭证不足，暂不支持假货判定，建议补充原订单截图后重提。", LocalDateTime.now().minusDays(2).withHour(10).withMinute(10), LocalDateTime.now().minusDays(4).withHour(20).withMinute(15));
        createBeautyAppeal(9605L, 5008L, "积雪草舒缓补水面膜", "CARE", "物流超时", "下单后长时间未发货，已经超过约定寄出时间，希望平台协助催发或退款。", "何同学", "晚风同学", "18800004065", "PENDING", "已上传订单截图与卖家承诺发货时间截图。", new BigDecimal("36.00"), null, null, LocalDateTime.now().minusHours(7));
        createBeautyAppeal(9606L, 5010L, "细闪卧蚕提亮笔", "MAKEUP", "描述不符", "商品实际偏大亮片，上眼和详情页展示差异较大，不适合日常通勤。", "孟同学", "晚风同学", "18800004066", "PROCESSING", "已上传试色图、自然光照片和开箱视频截图。", new BigDecimal("24.90"), "已联系卖家补充批次信息，等待进一步核验。", LocalDateTime.now().minusHours(5), LocalDateTime.now().minusDays(1).withHour(14).withMinute(25));
        createBeautyAppeal(9607L, 5009L, "清爽控油洁面泡沫", "CARE", "售后退货", "使用一周后与个人肤质不适配，希望按未过量使用申请退货退款。", "唐同学", "晚风同学", "18800004067", "RESOLVED", "已上传瓶身余量照片、订单截图和沟通记录。", new BigDecimal("42.00"), "平台判定符合协商退货条件，已完成退款处理。", LocalDateTime.now().minusHours(18), LocalDateTime.now().minusDays(2).withHour(16).withMinute(5));
    }

    private void createBeautyAppeal(Long id,
                                    Long goodId,
                                    String goodTitle,
                                    String category,
                                    String issueType,
                                    String reason,
                                    String buyerName,
                                    String sellerName,
                                    String contactPhone,
                                    String status,
                                    String proofSummary,
                                    BigDecimal refundAmount,
                                    String handleRemark,
                                    LocalDateTime handledAt,
                                    LocalDateTime createdAt) {
        BeautyAppeal exists = beautyAppealMapper.selectById(id);
        if (exists != null) {
            return;
        }
        BeautyAppeal appeal = new BeautyAppeal();
        appeal.setId(id);
        appeal.setGoodId(goodId);
        appeal.setGoodTitle(goodTitle);
        appeal.setCategory(category);
        appeal.setIssueType(issueType);
        appeal.setReason(reason);
        appeal.setBuyerName(buyerName);
        appeal.setSellerName(sellerName);
        appeal.setContactPhone(contactPhone);
        appeal.setStatus(status);
        appeal.setProofSummary(proofSummary);
        appeal.setRefundAmount(refundAmount);
        appeal.setHandleRemark(handleRemark);
        appeal.setHandledAt(handledAt);
        appeal.setCreatedAt(createdAt);
        beautyAppealMapper.insert(appeal);
    }

    private void createBeautyGoodIfAbsent(Long id,
                                          User creator,
                                          String category,
                                          String title,
                                          BigDecimal price,
                                          String summary,
                                          String sceneText,
                                          String evaluation,
                                          String dormExperience,
                                          String avoidanceGuide,
                                          List<String> skinTags,
                                          List<String> reviewJsonItems,
                                          List<String> gallery,
                                          String imageUrl) {
        BeautyGood exists = beautyGoodMapper.selectById(id);
        if (exists != null) {
            return;
        }
        BeautyGood good = new BeautyGood();
        good.setId(id);
        good.setCategory(category);
        good.setTitle(title);
        good.setPrice(price);
        good.setSummary(summary);
        good.setUsageGuide(summary);
        good.setSceneText(sceneText);
        good.setEvaluation(evaluation);
        good.setDormExperience(dormExperience);
        good.setAvoidanceGuide(avoidanceGuide);
        good.setSkinTagsJson(toJsonArray(skinTags));
        good.setReviewJson("[" + String.join(",", reviewJsonItems) + "]");
        good.setGalleryJson(toJsonArray(gallery));
        good.setImageUrl(imageUrl);
        good.setCreatorId(creator.getId());
        good.setCreatorNickname(creator.getNickname());
        good.setStatus("APPROVED");
        good.setSourceType("SYSTEM");
        good.setFavoriteCount(0);
        good.setViewCount(0);
        good.setPublishedAt(LocalDateTime.now());
        beautyGoodMapper.insert(good);
    }

    private void createPendingBeautyGoodIfAbsent(Long id,
                                                 User creator,
                                                 String category,
                                                 String title,
                                                 BigDecimal price,
                                                 String summary,
                                                 String sceneText,
                                                 String imageUrl) {
        BeautyGood exists = beautyGoodMapper.selectById(id);
        if (exists != null) {
            return;
        }
        BeautyGood good = new BeautyGood();
        good.setId(id);
        good.setCategory(category);
        good.setTitle(title);
        good.setPrice(price);
        good.setSummary(summary);
        good.setUsageGuide(summary);
        good.setSceneText(sceneText);
        good.setEvaluation("该内容为待审核演示数据，用于验证后台审核闭环。");
        good.setDormExperience("待审核阶段仅在个人中心可见，首页不会公开展示。");
        good.setAvoidanceGuide("请在后台审核通过后再对外展示。");
        good.setSkinTagsJson(toJsonArray(List.of("用户发布", "审核中")));
        good.setReviewJson("[]");
        good.setGalleryJson(toJsonArray(List.of(imageUrl)));
        good.setImageUrl(imageUrl);
        good.setCreatorId(creator.getId());
        good.setCreatorNickname(creator.getNickname());
        good.setStatus("PENDING");
        good.setSourceType("USER");
        good.setFavoriteCount(0);
        good.setViewCount(0);
        beautyGoodMapper.insert(good);
    }

    private String toJsonArray(List<String> values) {
        return "[" + values.stream().map(value -> "\"" + value.replace("\"", "\\\"") + "\"").reduce((a, b) -> a + "," + b).orElse("") + "]";
    }

    private void ensureSeedJobsAdmin() {
        if (!tableExists("jobs_account")) {
            return;
        }
        createJobsAccount(7001L, "林晓婷", "STUDENT", "18810000001", "ACTIVE", true, true, true, 1, 0, 96, "近期履约稳定，状态正常。");
        createJobsAccount(7002L, "陈泽宇", "STUDENT", "18810000002", "LIMITED", false, true, true, 2, 2, 58, "近期爽约与恶意举报较多，限制热门岗位报名。");
        createJobsAccount(7003L, "西街咖啡小站", "EMPLOYER", "18810000003", "ACTIVE", true, true, true, 1, 1, 84, "资质通过，可正常发布校外兼职岗位。");
        createJobsAccount(7004L, "海棠打印社", "EMPLOYER", "18810000004", "BANNED", false, false, false, 3, 3, 41, "多次出现薪资争议与违规收费，已封禁主体。");
        createJobsAccount(7005L, "校园新媒体工作室", "EMPLOYER", "18810000005", "ACTIVE", true, true, true, 0, 1, 88, "岗位需求清晰，处于常规观察中。");
        createJobsAccount(7006L, "周子涵", "STUDENT", "18810000006", "ACTIVE", true, true, true, 0, 0, 92, "技能互助表现良好。");
        createJobsAccount(7007L, "苏雨萌", "STUDENT", "18810000007", "ACTIVE", true, true, true, 1, 0, 90, "文案与办公软件接单稳定。");
        createJobsAccount(7008L, "南门轻食店", "EMPLOYER", "18810000008", "ACTIVE", true, true, true, 0, 0, 86, "新入驻商家，已补充校外资质。");

        createJobsMerchant(7101L, 7003L, "刘店长", "西街咖啡小站", "18810000003", "营业执照", "学校西门 1 公里", "https://images.unsplash.com/photo-1556740749-887f6717d7e4?auto=format&fit=crop&w=900&q=80", "APPROVED", LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(4), "资质照片清晰，允许发岗。");
        createJobsMerchant(7102L, 7004L, "王主管", "海棠打印社", "18810000004", "营业执照", "学校南门 2 公里", "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?auto=format&fit=crop&w=900&q=80", "REJECTED", LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(3), "门店资质与经营主体信息不一致，请补充后重提。");
        createJobsMerchant(7103L, 7005L, "顾老师", "校园新媒体工作室", "18810000005", "工作室资质", "学校北门 1 公里", "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?auto=format&fit=crop&w=900&q=80", "PENDING", LocalDateTime.now().minusDays(1), null, null);
        createJobsMerchant(7104L, 7008L, "黄店长", "南门轻食店", "18810000008", "营业执照", "学校南门 1 公里", "https://images.unsplash.com/photo-1556740749-887f6717d7e4?auto=format&fit=crop&w=900&q=80", "APPROVED", LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), "资料齐全，允许发布线下岗位。");

        createJobsPost(7201L, 7001L, "STUDENT", "林晓婷", "18810000001", "急需 PPT 美化与排版协助", "PPT&文档服务", "PPT", "ONLINE", "校内", new BigDecimal("0.30"), "图书馆自习区", new BigDecimal("80.00"), "按次结算", "今晚 20:00 前交付", 1, 3, "APPROVED", true, List.of("技能互助", "热门需求"), "需要将课程汇报 PPT 调整为简洁统一的学术风格，控制在 18 页内。", "熟悉答辩类 PPT 信息层级、图表优化和字体规范。", LocalDateTime.now().minusDays(2), "内容清晰，可正常展示。");
        createJobsPost(7202L, 7003L, "EMPLOYER", "西街咖啡小站", "18810000003", "周末门店兼职咖啡助手", "校内兼职", "OFFLINE_PARTTIME", "OFFLINE", "1公里", new BigDecimal("0.80"), "西门商业街", new BigDecimal("18.00"), "按小时结算", "周六周日 10:00-18:00", 3, 8, "APPROVED", true, List.of("商家发岗", "高报名"), "负责点单、简单备料与打包，工作节奏适中，适合周末兼职。", "需有基础服务意识，能适应高峰期站班。", LocalDateTime.now().minusDays(3), "资质已通过，岗位可公开展示。");
        createJobsPost(7203L, 7004L, "EMPLOYER", "海棠打印社", "18810000004", "校外传单派发兼职", "假期兼职", "HOLIDAY_JOB", "OFFLINE", "3公里", new BigDecimal("4.60"), "城北商圈", new BigDecimal("120.00"), "日结", "暑期 09:00-18:00", 2, 2, "BLOCKED", false, List.of("超出固定区间", "中介风险", "违规扣费"), "要求到校外商圈连续发单，另收取制服押金与培训费用。", "需先缴纳物料押金后安排岗位。", LocalDateTime.now().minusDays(1), "岗位超出固定范围且含收费风险，已拦截。");
        createJobsPost(7204L, 7006L, "STUDENT", "周子涵", "18810000006", "高数一对一家教辅导", "家教辅导", "TUTOR", "OFFLINE", "校内", new BigDecimal("90.00"), "教学楼 A 区", new BigDecimal("90.00"), "按次结算", "每周三晚 19:00-21:00", 1, 4, "APPROVED", true, List.of("待核验"), "针对高数期末冲刺，提供一对一题型讲解和错题复盘。", "希望有家教经历，讲解耐心清晰。", null, null);
        createJobsPost(7205L, 7005L, "EMPLOYER", "校园新媒体工作室", "18810000005", "短视频脚本与文案实习", "文案代写", "COPYWRITING", "ONLINE", "校内", new BigDecimal("1.20"), "线上协作", new BigDecimal("120.00"), "按稿结算", "每周交付 3 条脚本", 2, 6, "OFFLINE", false, List.of("举报处理中"), "需要完成校园活动短视频脚本策划与发布文案，风格轻松真实。", "有新媒体脚本经验，了解学生内容表达。", LocalDateTime.now().minusHours(18), "关联举报处理中，已先行下架复核。");
        createJobsPost(7206L, 7007L, "STUDENT", "苏雨萌", "18810000007", "论文格式与参考文献整理", "PPT&文档服务", "DOC_EDIT", "ONLINE", "校内", new BigDecimal("0.20"), "线上交付", new BigDecimal("45.00"), "按次结算", "48 小时内完成", 1, 2, "APPROVED", true, List.of("线上技能"), "帮助整理论文目录、页码、脚注和参考文献格式，适合期末论文提交前冲刺修改。", "需提前提供学校格式要求和论文当前版本。", LocalDateTime.now().minusDays(1), "技能型需求正常展示。");
        createJobsPost(7207L, 7008L, "EMPLOYER", "南门轻食店", "18810000008", "晚班备餐与打包兼职", "校内兼职", "OFFLINE_PARTTIME", "OFFLINE", "1公里", new BigDecimal("0.90"), "学校南门轻食店", new BigDecimal("20.00"), "按小时结算", "周一到周五 17:30-21:00", 2, 5, "APPROVED", true, List.of("门店兼职", "餐饮岗"), "协助晚高峰备餐、打包和前台整理，工作内容清晰，离校近。", "需要能适应晚高峰节奏，做事细致。", LocalDateTime.now().minusHours(10), "商家资质通过，岗位正常展示。");
        createJobsPost(7208L, 7006L, "STUDENT", "周子涵", "18810000006", "活动主持稿与串词代写", "文案代写", "COPYWRITING", "ONLINE", "校内", new BigDecimal("0.10"), "线上协作", new BigDecimal("60.00"), "按稿结算", "本周五前交稿", 1, 1, "APPROVED", true, List.of("校园活动"), "为学院晚会和社团活动撰写主持串词、开场词和流程衔接文案。", "需提前明确活动主题和节目单。", LocalDateTime.now().minusHours(14), "内容明确，可正常公开。");

        createJobsReport(7301L, 7205L, 7002L, 7005L, "短视频脚本与文案实习", "信息不符", "陈泽宇", "校园新媒体工作室", "EMPLOYER", "PENDING", "实际沟通后发现工作量远超原描述，且结算标准模糊。", null, null, null);
        createJobsReport(7302L, 7202L, 7001L, 7003L, "周末门店兼职咖啡助手", "薪资拖欠", "林晓婷", "西街咖啡小站", "EMPLOYER", "RESOLVED", "兼职结束后商家拖延结算，已超过约定时间。", "平台已联系商家补齐欠付薪资，并保留后续观察。", "商家已完成补发，平台继续跟进履约信用。", LocalDateTime.now().minusHours(12));
        createJobsReport(7303L, 7201L, 7006L, 7001L, "急需 PPT 美化与排版协助", "恶意刁难", "周子涵", "林晓婷", "STUDENT", "CLOSED", "需求方多次临时追加内容，沟通态度较差。", "平台已协调需求范围，双方确认结单。", "工单已完结，结果已同步记录。", LocalDateTime.now().minusHours(6));
        createJobsReport(7304L, 7207L, 7007L, 7008L, "晚班备餐与打包兼职", "不合理安排", "苏雨萌", "南门轻食店", "EMPLOYER", "PROCESSING", "沟通时临时增加闭店清洁工作，但岗位描述中未提前说明。", "平台已联系商家确认岗位职责边界。", "处理中，待补充双方沟通截图。", LocalDateTime.now().minusHours(3));
        createJobsReport(7305L, 7206L, 7002L, 7007L, "论文格式与参考文献整理", "信息不符", "陈泽宇", "苏雨萌", "STUDENT", "PENDING", "需求描述写 48 小时交付，但沟通中对方要求 24 小时内完成全部修改。", null, null, null);

        createJobsSkillTag(7401L, "PPT制作", "技能服务", true, 28);
        createJobsSkillTag(7402L, "文档排版", "技能服务", true, 19);
        createJobsSkillTag(7403L, "家教辅导", "教育支持", true, 23);
        createJobsSkillTag(7404L, "新媒体文案", "内容创作", true, 17);
        createJobsSkillTag(7405L, "办公软件", "办公支持", true, 14);
        createJobsSkillTag(7406L, "活动协助", "校园服务", false, 6);
        createJobsSkillTag(7407L, "主持串词", "内容创作", true, 9);
        createJobsSkillTag(7408L, "轻食备餐", "线下岗位", true, 11);
        createJobsSkillTag(7409L, "数据录入", "办公支持", true, 8);
    }

    private void createJobsAccount(Long id,
                                   String name,
                                   String role,
                                   String phone,
                                   String status,
                                   boolean publishEnabled,
                                   boolean applyEnabled,
                                   boolean acceptEnabled,
                                   int reportCount,
                                   int disputeCount,
                                   int creditScore,
                                   String latestRemark) {
        JobsAccount exists = jobsAccountMapper.selectById(id);
        if (exists != null) {
            return;
        }
        JobsAccount account = new JobsAccount();
        account.setId(id);
        account.setName(name);
        account.setRole(role);
        account.setPhone(phone);
        account.setStatus(status);
        account.setPublishEnabled(publishEnabled ? 1 : 0);
        account.setApplyEnabled(applyEnabled ? 1 : 0);
        account.setAcceptEnabled(acceptEnabled ? 1 : 0);
        account.setReportCount(reportCount);
        account.setDisputeCount(disputeCount);
        account.setCreditScore(creditScore);
        account.setLatestRemark(latestRemark);
        jobsAccountMapper.insert(account);
    }

    private void createJobsMerchant(Long id,
                                    Long accountId,
                                    String applicantName,
                                    String businessName,
                                    String phone,
                                    String identityType,
                                    String area,
                                    String licenseImage,
                                    String status,
                                    LocalDateTime submittedAt,
                                    LocalDateTime reviewedAt,
                                    String reviewRemark) {
        JobsMerchantQualification exists = jobsMerchantQualificationMapper.selectById(id);
        if (exists != null) {
            return;
        }
        JobsMerchantQualification merchant = new JobsMerchantQualification();
        merchant.setId(id);
        merchant.setAccountId(accountId);
        merchant.setApplicantName(applicantName);
        merchant.setBusinessName(businessName);
        merchant.setPhone(phone);
        merchant.setIdentityType(identityType);
        merchant.setArea(area);
        merchant.setLicenseImage(licenseImage);
        merchant.setStatus(status);
        merchant.setSubmittedAt(submittedAt);
        merchant.setReviewedAt(reviewedAt);
        merchant.setReviewRemark(reviewRemark);
        jobsMerchantQualificationMapper.insert(merchant);
    }

    private void createJobsPost(Long id,
                                Long accountId,
                                String role,
                                String publisherName,
                                String publisherPhone,
                                String title,
                                String category,
                                String jobType,
                                String workMode,
                                String distanceRange,
                                BigDecimal actualDistanceKm,
                                String area,
                                BigDecimal salary,
                                String settlement,
                                String schedule,
                                int headCount,
                                int applicantCount,
                                String status,
                                boolean publicVisible,
                                List<String> riskTags,
                                String description,
                                String requirement,
                                LocalDateTime reviewedAt,
                                String reviewRemark) {
        JobsPost exists = jobsPostMapper.selectById(id);
        if (exists != null) {
            return;
        }
        JobsPost post = new JobsPost();
        post.setId(id);
        post.setAccountId(accountId);
        post.setRole(role);
        post.setPublisherName(publisherName);
        post.setPublisherPhone(publisherPhone);
        post.setTitle(title);
        post.setCategory(category);
        post.setJobType(jobType);
        post.setWorkMode(workMode);
        post.setDistanceRange(distanceRange);
        post.setActualDistanceKm(actualDistanceKm);
        post.setArea(area);
        post.setSalary(salary);
        post.setSettlement(settlement);
        post.setSchedule(schedule);
        post.setHeadCount(headCount);
        post.setApplicantCount(applicantCount);
        post.setStatus(status);
        post.setPublicVisible(publicVisible ? 1 : 0);
        post.setRiskTagsJson(toJsonArray(riskTags));
        post.setDescription(description);
        post.setRequirement(requirement);
        post.setReviewedAt(reviewedAt);
        post.setReviewRemark(reviewRemark);
        jobsPostMapper.insert(post);
    }

    private void createJobsReport(Long id,
                                  Long postId,
                                  Long reporterAccountId,
                                  Long targetAccountId,
                                  String postTitle,
                                  String reportType,
                                  String reporterName,
                                  String targetName,
                                  String targetRole,
                                  String status,
                                  String description,
                                  String handleRemark,
                                  String publicResult,
                                  LocalDateTime handledAt) {
        JobsReport exists = jobsReportMapper.selectById(id);
        if (exists != null) {
            return;
        }
        JobsReport report = new JobsReport();
        report.setId(id);
        report.setPostId(postId);
        report.setReporterAccountId(reporterAccountId);
        report.setTargetAccountId(targetAccountId);
        report.setPostTitle(postTitle);
        report.setReportType(reportType);
        report.setReporterName(reporterName);
        report.setTargetName(targetName);
        report.setTargetRole(targetRole);
        report.setStatus(status);
        report.setDescription(description);
        report.setHandleRemark(handleRemark);
        report.setPublicResult(publicResult);
        report.setHandledAt(handledAt);
        jobsReportMapper.insert(report);
    }

    private void createJobsSkillTag(Long id, String label, String category, boolean enabled, int usageCount) {
        JobsSkillTag exists = jobsSkillTagMapper.selectById(id);
        if (exists != null) {
            return;
        }
        JobsSkillTag tag = new JobsSkillTag();
        tag.setId(id);
        tag.setLabel(label);
        tag.setCategory(category);
        tag.setEnabled(enabled ? 1 : 0);
        tag.setUsageCount(usageCount);
        jobsSkillTagMapper.insert(tag);
    }

    private void ensureSeedPartnerAdmin() {
        if (!tableExists("partner_demand")) {
            return;
        }
        createPartnerDemand(9101L, "周三晚自习自律搭子", "学习搭子", "图书馆二楼靠窗自习区",
            "2026-06-11 19:00 - 21:30", "林小满", 31L, "18800001031",
            "想找一位周三一起晚自习的搭子，主要互相监督复习高数和英语，不闲聊太多，结束后可以互相检查打卡。",
            List.of("晚自习", "高数", "英语复习"), "APPROVED", List.of(), 6, LocalDateTime.now().minusDays(2), "内容合规，允许展示");
        createPartnerDemand(9102L, "周末羽毛球双打搭子", "运动搭子", "南区体育馆二楼羽毛球场",
            "2026-06-14 15:00 - 17:00", "周星河", 32L, "18800001032",
            "周末想约一位球龄一年左右的同学一起打羽毛球，偏娱乐局，希望能稳定配合双打。",
            List.of("羽毛球", "双打", "周末运动"), "APPROVED", List.of("频繁索要押金截图"), 12, LocalDateTime.now().minusDays(3), "已通过常规内容审核");
        createPartnerDemand(9103L, "饭搭子一起打卡新食堂", "饭搭子", "新食堂一层门口",
            "2026-06-10 11:40 - 12:30", "陈芊芊", 33L, "18800001033",
            "中午想去新食堂试新窗口，想找一位饭搭子一起排队聊天，吃完各自回去上课。",
            List.of("新食堂", "中午约饭", "轻松社交"), "APPROVED", List.of(), 9, LocalDateTime.now().minusDays(2), "内容正常");
        createPartnerDemand(9104L, "演唱会校内拼车搭子", "出行搭子", "东门地铁站集合",
            "2026-06-15 17:30 - 23:30", "许知远", 34L, "18800001034",
            "找两位同校同去演唱会的搭子，回程一起拼车，希望提前沟通费用和路线。",
            List.of("拼车", "演唱会", "夜间返校"), "OFFLINE", List.of("线下交易风险"), 9, LocalDateTime.now().minusDays(1), "因涉及站外转账引导，已下架观察");
        createPartnerDemand(9105L, "考公晨读监督搭子", "自律搭子", "一教门口集合后去空教室",
            "2026-06-09 06:50 - 08:00", "宋以宁", 35L, "18800001035",
            "需要一位早起晨读互相监督的搭子，坚持 21 天，要求能按时打卡。",
            List.of("晨读", "考公", "21 天打卡"), "REJECTED", List.of("资料搬运嫌疑"), 4, LocalDateTime.now().minusDays(2), "文案存在站外引流描述，已驳回");
        createPartnerDemand(9111L, "周五摄影散步搭子", "探店搭子", "湖心广场与银杏大道",
            "2026-06-13 17:30 - 19:00", "沈书意", 36L, "18800001036",
            "想找一位放学后一起散步拍照的搭子，主要拍校园黄昏和树影，节奏轻松。",
            List.of("散步拍照", "黄昏", "轻社交"), "APPROVED", List.of(), 5, LocalDateTime.now().minusDays(1), "内容正常，允许展示");
        createPartnerDemand(9112L, "英语口语晨练搭子", "学习搭子", "外语楼前草坪",
            "2026-06-12 07:10 - 07:50", "顾念安", 37L, "18800001037",
            "想每天早上练 30 分钟口语表达和自我介绍，互相纠音，要求能坚持一周以上。",
            List.of("英语口语", "晨练", "互相纠音"), "APPROVED", List.of(), 7, LocalDateTime.now().minusDays(1), "需求清晰，可正常展示");
        createPartnerDemand(9113L, "周末短途骑行搭子", "运动搭子", "北门共享单车点集合",
            "2026-06-14 08:30 - 11:00", "秦一川", 38L, "18800001038",
            "周末想绕学校周边绿道骑行，节奏中等，希望有安全意识和守时习惯。",
            List.of("骑行", "周末", "中等强度"), "APPROVED", List.of("需线下安全提醒"), 6, LocalDateTime.now().minusHours(20), "已提示注意骑行安全");
        createPartnerDemand(9114L, "展览拼车往返搭子", "出行搭子", "南门公交站",
            "2026-06-15 13:00 - 18:30", "陆清禾", 39L, "18800001039",
            "去市区看毕业展，想找两位同校搭子一起往返，优先公共交通，不接受私下收款。",
            List.of("展览", "周末出行", "往返同行"), "APPROVED", List.of(), 3, LocalDateTime.now().minusHours(16), "内容合规");

        createPartnerReport(8801L, 9102L, "周末羽毛球双打搭子", "虚假邀约",
            "发布后多次临时改时间，还要求先支付场地押金，疑似虚假组织。", "王同学", 51L, "18800002051",
            "PENDING", "partner-9102-51", LocalDateTime.now().minusHours(8), LocalDateTime.now().plusHours(4),
            "已提交聊天截图 3 张，包含押金要求与多次改约记录。", null, null);
        createPartnerReport(8802L, 9104L, "演唱会校内拼车搭子", "线下违规交易",
            "对方引导私下转账订车并要求脱离平台沟通，存在资金风险。", "刘同学", 52L, "18800002052",
            "PROCESSING", "partner-9104-52", LocalDateTime.now().minusHours(18), LocalDateTime.now().minusHours(2),
            "已补充转账二维码截图和语音转文字记录。", "已进入人工复核，正在核查线下交易证据", LocalDateTime.now().minusHours(6));
        createPartnerReport(8803L, 9103L, "饭搭子一起打卡新食堂", "恶意骚扰",
            "报名后反复私聊与搭子需求无关的话题，影响正常沟通。", "赵同学", 53L, "18800002053",
            "RESOLVED", "partner-9103-53", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusHours(6),
            "聊天记录包含连续骚扰内容，已核查属实。", "已核实存在骚扰消息，完成提醒并清理相关会话", LocalDateTime.now().minusHours(20));
        createPartnerReport(8804L, 9105L, "考公晨读监督搭子", "违规广告",
            "内容含站外群二维码导流，疑似广告推广并非真实找搭子。", "马同学", 54L, "18800002054",
            "REJECTED", null, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
            "仅提供单张截图，无法完整判断外部导流链路。", "现有凭证不足，已驳回；但内容审核已单独拦截该帖子", LocalDateTime.now().minusDays(2).plusHours(2));
        createPartnerReport(8805L, 9113L, "周末短途骑行搭子", "安全风险", "对方在聊天里提出无头盔夜骑，存在安全隐患。", "胡同学", 55L, "18800002055",
            "PROCESSING", "partner-9113-55", LocalDateTime.now().minusHours(5), LocalDateTime.now().plusHours(8),
            "已提交聊天截图与活动安排说明。", "已提醒发布者补充安全说明，等待进一步核查。", LocalDateTime.now().minusHours(1));
        createPartnerReport(8806L, 9111L, "周五摄影散步搭子", "信息不符", "实际沟通后对方改为商业约拍收费，与原搭子需求不符。", "彭同学", 56L, "18800002056",
            "PENDING", "partner-9111-56", LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(10),
            "提供收费聊天记录与原需求截图。", null, null);

        createPartnerConversation("partner-9101-61", 9101L, "周三晚自习自律搭子", "学习搭子", "谢知夏", 61L,
            "18800003061", "ACTIVE", "MEDIUM", List.of("新发布高活跃"), 2,
            "我可以 18:50 到图书馆门口，先一起选位置。", LocalDateTime.now().minusHours(9), null);
        createPartnerConversation("partner-9102-51", 9102L, "周末羽毛球双打搭子", "运动搭子", "王同学", 51L,
            "18800002051", "PENDING_REVIEW", "HIGH", List.of("频繁索要押金截图"), 3,
            "如果不先转场地押金，这周就不带你一起打了。", LocalDateTime.now().minusHours(10), "会话出现押金要求，已进入人工复核");
        createPartnerConversation("partner-9103-53", 9103L, "饭搭子一起打卡新食堂", "饭搭子", "赵同学", 53L,
            "18800002053", "ACTIVE", "LOW", List.of(), 0,
            "那我们中午 11:40 在食堂门口碰面。", LocalDateTime.now().minusDays(1), null);
        createPartnerConversation("partner-9104-52", 9104L, "演唱会校内拼车搭子", "出行搭子", "刘同学", 52L,
            "18800002052", "CLOSED", "HIGH", List.of("已触发举报工单"), 0,
            "加我微信发定位，车费先转我，平台里不方便说。", LocalDateTime.now().minusHours(20), "因线下交易风险已关闭该会话");
        createPartnerConversation("partner-9111-56", 9111L, "周五摄影散步搭子", "探店搭子", "彭同学", 56L,
            "18800002056", "ACTIVE", "MEDIUM", List.of("收费争议待核查"), 1,
            "如果要我带相机的话要另外算拍摄费用。", LocalDateTime.now().minusHours(2), "需核查是否偏离原搭子需求");
        createPartnerConversation("partner-9113-55", 9113L, "周末短途骑行搭子", "运动搭子", "胡同学", 55L,
            "18800002055", "PENDING_REVIEW", "MEDIUM", List.of("安全提醒"), 2,
            "晚上骑也行，不戴头盔拍照更好看。", LocalDateTime.now().minusHours(4), "涉及安全风险用语，待人工复核");

        createPartnerConversationMessage("partner-9101-61", "林小满", "发布者", "TEXT", "我想找晚自习互相监督的搭子，你时间合适吗？", LocalDateTime.now().minusHours(12));
        createPartnerConversationMessage("partner-9101-61", "谢知夏", "报名者", "TEXT", "可以，我这周三晚上有空，想一起复习高数。", LocalDateTime.now().minusHours(11));
        createPartnerConversationMessage("partner-9101-61", "谢知夏", "报名者", "TEXT", "我可以 18:50 到图书馆门口，先一起选位置。", LocalDateTime.now().minusHours(9));

        createPartnerConversationMessage("partner-9102-51", "周星河", "发布者", "TEXT", "球馆比较抢手，先把押金转给我我去定场。", LocalDateTime.now().minusHours(11));
        createPartnerConversationMessage("partner-9102-51", "王同学", "报名者", "TEXT", "为什么要先私下转账，平台内不是可以沟通好吗？", LocalDateTime.now().minusHours(10));
        createPartnerConversationMessage("partner-9102-51", "周星河", "发布者", "TEXT", "如果不先转场地押金，这周就不带你一起打了。", LocalDateTime.now().minusHours(10));

        createPartnerConversationMessage("partner-9103-53", "陈芊芊", "发布者", "TEXT", "我想中午去新食堂吃饭，要不要一起？", LocalDateTime.now().minusDays(1).plusHours(1));
        createPartnerConversationMessage("partner-9103-53", "赵同学", "报名者", "TEXT", "可以，我正好也想去看看新窗口。", LocalDateTime.now().minusDays(1).plusHours(1).plusMinutes(2));
        createPartnerConversationMessage("partner-9103-53", "陈芊芊", "发布者", "TEXT", "那我们中午 11:40 在食堂门口碰面。", LocalDateTime.now().minusDays(1).plusHours(1).plusMinutes(30));

        createPartnerConversationMessage("partner-9104-52", "许知远", "发布者", "TEXT", "拼车的话你先转我车费，我统一订车。", LocalDateTime.now().minusHours(21));
        createPartnerConversationMessage("partner-9104-52", "刘同学", "报名者", "TEXT", "可以走平台内确认吗？", LocalDateTime.now().minusHours(20).plusMinutes(2));
        createPartnerConversationMessage("partner-9104-52", "许知远", "发布者", "TEXT", "加我微信发定位，车费先转我，平台里不方便说。", LocalDateTime.now().minusHours(20));
        createPartnerConversationMessage("partner-9111-56", "沈书意", "发布者", "TEXT", "我想周五傍晚去湖心广场拍照散步，你有空吗？", LocalDateTime.now().minusHours(3));
        createPartnerConversationMessage("partner-9111-56", "彭同学", "报名者", "TEXT", "可以，不过如果要我带相机帮拍的话要另外算拍摄费用。", LocalDateTime.now().minusHours(2));
        createPartnerConversationMessage("partner-9113-55", "秦一川", "发布者", "TEXT", "周末骑行主要走绿道，记得带水和头盔。", LocalDateTime.now().minusHours(5));
        createPartnerConversationMessage("partner-9113-55", "胡同学", "报名者", "TEXT", "晚上骑也行，不戴头盔拍照更好看。", LocalDateTime.now().minusHours(4));
    }

    private void createPartnerDemand(Long id,
                                     String title,
                                     String type,
                                     String location,
                                     String schedule,
                                     String publisherName,
                                     Long publisherUserId,
                                     String publisherPhone,
                                     String description,
                                     List<String> needTags,
                                     String status,
                                     List<String> baseRiskTags,
                                     int applyCount,
                                     LocalDateTime reviewedAt,
                                     String reviewRemark) {
        if (partnerDemandMapper.selectById(id) != null) {
            return;
        }
        PartnerDemand demand = new PartnerDemand();
        demand.setId(id);
        demand.setTitle(title);
        demand.setType(type);
        demand.setLocation(location);
        demand.setSchedule(schedule);
        demand.setPublisherName(publisherName);
        demand.setPublisherUserId(publisherUserId);
        demand.setPublisherPhone(publisherPhone);
        demand.setDescription(description);
        demand.setNeedTagsJson(toJsonArray(needTags));
        demand.setStatus(status);
        demand.setBaseRiskTagsJson(toJsonArray(baseRiskTags));
        demand.setApplyCount(applyCount);
        demand.setReviewedAt(reviewedAt);
        demand.setReviewRemark(reviewRemark);
        partnerDemandMapper.insert(demand);
    }

    private void createPartnerReport(Long id,
                                     Long targetId,
                                     String targetTitle,
                                     String reportType,
                                     String description,
                                     String reporterName,
                                     Long reporterUserId,
                                     String contactPhone,
                                     String status,
                                     String conversationId,
                                     LocalDateTime createdAt,
                                     LocalDateTime deadlineAt,
                                     String evidenceSummary,
                                     String handleRemark,
                                     LocalDateTime handledAt) {
        if (partnerReportMapper.selectById(id) != null) {
            return;
        }
        PartnerReport report = new PartnerReport();
        report.setId(id);
        report.setModule("PARTNER");
        report.setTargetType("DEMAND");
        report.setTargetId(targetId);
        report.setTargetTitle(targetTitle);
        report.setReportType(reportType);
        report.setDescription(description);
        report.setReporterName(reporterName);
        report.setReporterUserId(reporterUserId);
        report.setContactPhone(contactPhone);
        report.setStatus(status);
        report.setConversationId(conversationId);
        report.setDeadlineAt(deadlineAt);
        report.setEvidenceSummary(evidenceSummary);
        report.setHandleRemark(handleRemark);
        report.setHandledAt(handledAt);
        report.setCreatedAt(createdAt);
        report.setUpdatedAt(createdAt);
        report.setDeleted(0);
        partnerReportMapper.insert(report);
    }

    private void createPartnerConversation(String id,
                                           Long demandId,
                                           String demandTitle,
                                           String demandType,
                                           String counterpartyName,
                                           Long counterpartyUserId,
                                           String counterpartyPhone,
                                           String status,
                                           String riskLevel,
                                           List<String> baseRiskTags,
                                           int unreadCount,
                                           String lastMessage,
                                           LocalDateTime lastMessageAt,
                                           String reviewRemark) {
        if (partnerConversationMapper.selectById(id) != null) {
            return;
        }
        PartnerConversation conversation = new PartnerConversation();
        conversation.setId(id);
        conversation.setDemandId(demandId);
        conversation.setDemandTitle(demandTitle);
        conversation.setDemandType(demandType);
        conversation.setCounterpartyName(counterpartyName);
        conversation.setCounterpartyUserId(counterpartyUserId);
        conversation.setCounterpartyPhone(counterpartyPhone);
        conversation.setStatus(status);
        conversation.setRiskLevel(riskLevel);
        conversation.setBaseRiskTagsJson(toJsonArray(baseRiskTags));
        conversation.setReviewRemark(reviewRemark);
        conversation.setUnreadCount(unreadCount);
        conversation.setLastMessage(lastMessage);
        conversation.setLastMessageAt(lastMessageAt);
        partnerConversationMapper.insert(conversation);
    }

    private void createPartnerConversationMessage(String conversationId,
                                                  String senderName,
                                                  String senderRole,
                                                  String type,
                                                  String content,
                                                  LocalDateTime createdAt) {
        Long exists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM partner_conversation_message WHERE conversation_id = ? AND sender_name = ? AND sender_role = ? AND type = ? AND content = ? AND deleted = 0",
            Long.class,
            conversationId,
            senderName,
            senderRole,
            type,
            content
        );
        if (exists != null && exists > 0) {
            return;
        }
        PartnerConversationMessage message = new PartnerConversationMessage();
        message.setConversationId(conversationId);
        message.setSenderName(senderName);
        message.setSenderRole(senderRole);
        message.setType(type);
        message.setContent(content);
        message.setCreatedAt(createdAt);
        message.setUpdatedAt(createdAt);
        message.setDeleted(0);
        partnerConversationMessageMapper.insert(message);
    }

    private void normalizeSeedPresentationData() {
        User primaryDemoUser = findUser("18800000001");
        if (primaryDemoUser != null) {
            primaryDemoUser.setNickname("江闻晴");
            userMapper.updateById(primaryDemoUser);
            if (tableExists("user_profile")) {
                jdbcTemplate.update(
                    "UPDATE user_profile SET bio = ? WHERE user_id = ? AND deleted = 0",
                    "常用全模块演示账号，覆盖跑腿、美妆、找搭子与勤工助学场景。",
                    primaryDemoUser.getId()
                );
            }
            if (tableExists("beauty_good")) {
                jdbcTemplate.update(
                    "UPDATE beauty_good SET creator_nickname = ? WHERE creator_id = ? AND deleted = 0",
                    "江闻晴",
                    primaryDemoUser.getId()
                );
            }
            if (tableExists("beauty_appeal")) {
                jdbcTemplate.update(
                    "UPDATE beauty_appeal SET seller_name = ? WHERE seller_name = ? AND deleted = 0",
                    "江闻晴",
                    "默认演示用户"
                );
            }
            if (tableExists("partner_demand")) {
                jdbcTemplate.update(
                    "UPDATE partner_demand SET publisher_name = ?, review_remark = ? WHERE publisher_user_id = ? AND deleted = 0",
                    "江闻晴",
                    "前台发布成功，已按正式演示数据公开展示。",
                    primaryDemoUser.getId()
                );
            }
            if (tableExists("partner_conversation")) {
                jdbcTemplate.update(
                    "UPDATE partner_conversation SET last_message = REPLACE(last_message, '默认演示用户', ?) WHERE last_message LIKE '%默认演示用户%' AND deleted = 0",
                    "江闻晴"
                );
            }
            if (tableExists("partner_conversation_message")) {
                jdbcTemplate.update(
                    "UPDATE partner_conversation_message SET content = REPLACE(content, '默认演示用户', ?) WHERE content LIKE '%默认演示用户%' AND deleted = 0",
                    "江闻晴"
                );
            }
        }

        if (tableExists("beauty_good")) {
            polishBeautyGood(5008L, "雾感柔焦气垫粉底", "轻薄柔焦、补妆方便，适合学生党日常通勤底妆。", "适合早八通勤、课堂淡妆和社团活动前快速补妆。", "妆感偏自然，遮瑕够日常，不容易显得底妆厚重。", "放在宿舍和书包里都方便，课间补妆速度很快。", "建议先少量拍开，再根据局部状态叠加。");
            polishBeautyGood(5009L, "奶茶杏色丝绒唇釉", "颜色温柔日常，薄涂显气色，厚涂更有奶茶杏调层次。", "适合课堂通勤、约饭和拍照时做自然提气色唇妆。", "整体更偏柔雾奶茶调，对黄皮和学生日常妆都比较友好。", "放在宿舍桌面或随身化妆包里补涂都很顺手。", "上唇前先简单润唇，妆感会更平整。");
            polishBeautyGood(5010L, "抽屉式桌面刷具收纳盒", "分区清晰，适合放常用刷具、眉笔和小型彩妆单品。", "适合宿舍书桌、化妆角和高频单品日常整理收纳。", "收纳逻辑直观，适合学生宿舍桌面保持整洁。", "把常用工具分区摆放后，早上出门拿取会快很多。", "下单前先确认尺寸是否适合自己的桌面空间。");
            polishBeautyGood(5011L, "柔雾杏粉单色腮红", "颜色清透自然，轻扫就能让脸部气色更柔和。", "适合日常淡妆、课堂通勤和拍照前快速提气色。", "粉质偏细，新手叠加也不容易一块一块上脸。", "宿舍光线下一样比较好控制用量，不容易失手。", "建议少量多次叠加，边缘再用刷具轻轻晕开。");
            polishBeautyGood(5012L, "奶雾豆沙唇泥", "豆沙色调自然柔和，适合作为学生党日常雾面唇妆选择。", "审核通过后适合通勤、约饭和校园活动等日常场景使用。", "当前处于审核中，等待管理员核验订单凭证和商品实拍后公开展示。", "审核通过后会展示在首页和个人发布列表中。", "请保证订单截图与实拍图清晰完整，便于审核通过。");
            jdbcTemplate.update("UPDATE beauty_good SET status = 'PENDING' WHERE id = 5012 AND deleted = 0");
        }

        if (tableExists("partner_demand")) {
            polishPartnerDemand(9106L, "周中晚饭拼桌搭子", "MEAL", "二食堂一楼", "2026-06-08 晚上 18:00 - 20:00", "想找一位下课后一起吃晚饭的同学，边吃边聊课程安排，节奏轻松。");
            polishPartnerDemand(9107L, "二食堂晚饭搭子", "MEAL", "二食堂一楼", "今晚 18:30 - 20:00", "想约一位同学一起去二食堂吃晚饭，顺便交流本周课程和社团安排。");
            polishPartnerDemand(9108L, "图书馆复习搭子", "STUDY", "图书馆四楼", "明晚 19:00 - 21:00", "想找一位安静复习的同学一起在图书馆刷题、整理重点，互相监督效率。");
        }

        if (tableExists("partner_conversation")) {
            polishPartnerConversation("partner-9107-18", "二食堂晚饭搭子", "何同学", "江闻晴已同意申请，已匹配成功", null);
            polishPartnerConversation("partner-9108-19", "图书馆复习搭子", "陆同学", "江闻晴已同意申请，已匹配成功", null);
        }

        if (tableExists("partner_conversation_message")) {
            replacePartnerMessage("partner-9107-18", "这里是申请人补充的真实联调消息。", "我 18:40 左右能到二食堂，到时候我们在一楼饮品区见面。");
            replacePartnerMessage("partner-9108-19", "回归消息", "我会带着高数和英语笔记过去，到时候可以先各自复习再互相抽查。");
            replacePartnerMessage("partner-9107-18", "默认演示用户已同意申请，已匹配成功", "江闻晴已同意申请，已匹配成功");
            replacePartnerMessage("partner-9108-19", "默认演示用户已同意申请，已匹配成功", "江闻晴已同意申请，已匹配成功");
        }
    }

    private void polishBeautyGood(Long id,
                                  String title,
                                  String summary,
                                  String sceneText,
                                  String evaluation,
                                  String dormExperience,
                                  String avoidanceGuide) {
        jdbcTemplate.update(
            "UPDATE beauty_good SET title = ?, summary = ?, usage_guide = ?, scene_text = ?, evaluation = ?, dorm_experience = ?, avoidance_guide = ? WHERE id = ? AND deleted = 0",
            title,
            summary,
            summary,
            sceneText,
            evaluation,
            dormExperience,
            avoidanceGuide,
            id
        );
    }

    private void polishPartnerDemand(Long id,
                                     String title,
                                     String type,
                                     String location,
                                     String schedule,
                                     String description) {
        jdbcTemplate.update(
            "UPDATE partner_demand SET title = ?, type = ?, location = ?, schedule = ?, description = ? WHERE id = ? AND deleted = 0",
            title,
            type,
            location,
            schedule,
            description,
            id
        );
    }

    private void polishPartnerConversation(String id,
                                           String demandTitle,
                                           String counterpartyName,
                                           String lastMessage,
                                           String reviewRemark) {
        jdbcTemplate.update(
            "UPDATE partner_conversation SET demand_title = ?, counterparty_name = ?, last_message = ?, review_remark = ? WHERE id = ? AND deleted = 0",
            demandTitle,
            counterpartyName,
            lastMessage,
            reviewRemark,
            id
        );
    }

    private void replacePartnerMessage(String conversationId, String oldContent, String newContent) {
        jdbcTemplate.update(
            "UPDATE partner_conversation_message SET content = ? WHERE conversation_id = ? AND content = ? AND deleted = 0",
            newContent,
            conversationId,
            oldContent
        );
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = UPPER(?)",
            Integer.class,
            tableName
        );
        return count != null && count > 0;
    }
}
