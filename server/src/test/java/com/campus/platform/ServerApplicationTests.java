package com.campus.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.platform.common.BusinessException;
import com.campus.platform.dto.ErrandOrderCreateRequest;
import com.campus.platform.dto.RegisterRequest;
import com.campus.platform.service.AuthService;
import com.campus.platform.service.ErrandOrderService;
import com.campus.platform.vo.AuthLoginVO;
import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:campus;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.sql.init.mode=always"
})
class ServerApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuthService authService;

    @Autowired
    private ErrandOrderService errandOrderService;

	@Test
	void contextLoads() {
	}

    @Test
    void errandCoreTablesShouldExistAfterStartup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        assertThat(tableExists(jdbcTemplate, "ERRAND_ORDER")).isTrue();
        assertThat(tableExists(jdbcTemplate, "ERRAND_ORDER_ATTACHMENT")).isTrue();
        assertThat(tableExists(jdbcTemplate, "ERRAND_ORDER_CHAT")).isTrue();
    }

    @Test
    void nonParticipantShouldNotAccessAcceptedOrderDetailOrConversation() {
        Long publisherId = registerUser(uniquePhone("13")).getUser().getUserId();
        Long runnerId = registerUser(uniquePhone("14")).getUser().getUserId();
        Long outsiderId = registerUser(uniquePhone("15")).getUser().getUserId();

        var created = errandOrderService.create(publisherId, buildCreateRequest());
        errandOrderService.accept(created.getId(), runnerId);

        assertThatThrownBy(() -> errandOrderService.detail(created.getId(), outsiderId))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getCode())
            .isEqualTo(403);

        assertThatThrownBy(() -> errandOrderService.getConversation(created.getId(), outsiderId))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getCode())
            .isEqualTo(403);
    }

    @Test
    void adminShouldAccessNonPublicOrderDetail() {
        Long publisherId = registerUser(uniquePhone("17")).getUser().getUserId();
        Long runnerId = registerUser(uniquePhone("18")).getUser().getUserId();

        var created = errandOrderService.create(publisherId, buildCreateRequest());
        errandOrderService.accept(created.getId(), runnerId);

        assertThat(errandOrderService.adminDetail(created.getId()).getId()).isEqualTo(created.getId());
        assertThat(errandOrderService.adminDetail(created.getId()).getStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    void adminSearchShouldSupportPublisherPhone() {
        String publisherPhone = uniquePhone("16");
        Long publisherId = registerUser(publisherPhone).getUser().getUserId();
        errandOrderService.create(publisherId, buildCreateRequest());

        Page<?> page = errandOrderService.adminPage(publisherPhone, "", 1, 20);
        assertThat(page.getTotal()).isGreaterThanOrEqualTo(1);
    }

    private AuthLoginVO registerUser(String phone) {
        RegisterRequest request = new RegisterRequest();
        request.setPhone(phone);
        request.setPassword("abc12345");
        request.setConfirmPassword("abc12345");
        return authService.register(request);
    }

    private ErrandOrderCreateRequest buildCreateRequest() {
        ErrandOrderCreateRequest request = new ErrandOrderCreateRequest();
        request.setServiceType("PICKUP");
        request.setPickupAddress("一食堂快递点");
        request.setDeliveryAddress("3号宿舍楼");
        request.setPickupTimeText("今晚8点前");
        request.setDetailContent("帮忙取一个快递");
        request.setRemark("到楼下联系");
        request.setBaseFee(new BigDecimal("5.50"));
        request.setUrgentFlag(true);
        request.setFragileFlag(false);
        request.setAttachmentFileIds(List.of());
        return request;
    }

    private String uniquePhone(String prefix) {
        String seed = String.valueOf(System.nanoTime());
        return prefix + seed.substring(Math.max(0, seed.length() - 9));
    }

    private boolean tableExists(JdbcTemplate jdbcTemplate, String tableName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?",
            Integer.class,
            tableName
        );
        return count != null && count > 0;
    }

}
