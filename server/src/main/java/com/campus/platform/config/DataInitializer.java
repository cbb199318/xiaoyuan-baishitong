package com.campus.platform.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.platform.entity.ErrandOrder;
import com.campus.platform.entity.User;
import com.campus.platform.entity.UserProfile;
import com.campus.platform.enums.ErrandOrderStatus;
import com.campus.platform.enums.ErrandServiceType;
import com.campus.platform.enums.RoleType;
import com.campus.platform.enums.UserStatus;
import com.campus.platform.mapper.ErrandOrderMapper;
import com.campus.platform.mapper.UserMapper;
import com.campus.platform.mapper.UserProfileMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final ErrandOrderMapper errandOrderMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureUser("18800000000", "admin123", "系统管理员", RoleType.ADMIN.name(), "默认管理员账号");
        ensureUser("18800000001", "user123", "默认演示用户", RoleType.USER.name(), "默认用户账号");
        ensureUser("18800000002", "user123", "南苑同学", RoleType.USER.name(), "演示跑腿发单用户");
        ensureUser("18800000003", "user123", "北苑同学", RoleType.USER.name(), "演示跑腿发单用户");
        ensureUser("18800000004", "user123", "图书馆同学", RoleType.USER.name(), "演示跑腿发单用户");
        ensureSeedErrandOrders();
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
        userMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setContactPhone(user.getPhone());
        profile.setBio(bio);
        userProfileMapper.insert(profile);
    }

    private void ensureSeedErrandOrders() {
        Long count = errandOrderMapper.selectCount(new LambdaQueryWrapper<ErrandOrder>());
        if (count != null && count > 0) {
            return;
        }

        User user1 = findUser("18800000001");
        User user2 = findUser("18800000002");
        User user3 = findUser("18800000003");
        User user4 = findUser("18800000004");
        if (user1 == null || user2 == null || user3 == null || user4 == null) {
            return;
        }

        createPublishedOrder(
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
    }

    private void createPublishedOrder(Long publisherId,
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
        ErrandOrder order = new ErrandOrder();
        order.setOrderNo(generateOrderNo(orderIndex));
        order.setPublisherId(publisherId);
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
        order.setStatus(ErrandOrderStatus.PUBLISHED.name());
        order.setAcceptDeadline(LocalDateTime.now().plusHours(deadlineHours));
        order.setPublicVisible(1);
        errandOrderMapper.insert(order);
    }

    private User findUser(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    private String generateOrderNo(int orderIndex) {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return "ER" + datePart + String.format("%03d", orderIndex);
    }
}
