package com.campus.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private File file = new File();
    private Errand errand = new Errand();

    @Data
    public static class Jwt {
        private String secret;
        private Integer expireDays;
    }

    @Data
    public static class File {
        private String storagePath;
        private String publicBaseUrl;
    }

    @Data
    public static class Errand {
        private java.math.BigDecimal urgentFee;
        private java.math.BigDecimal fragileFee;
    }
}
