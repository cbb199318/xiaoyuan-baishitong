package com.campus.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class SchemaResourceTests {

    @Test
    void mainSchemaShouldContainErrandTables() throws IOException {
        String sql = new ClassPathResource("schema.sql").getContentAsString(StandardCharsets.UTF_8);

        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS `errand_order`"));
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS `errand_order_attachment`"));
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS `errand_order_chat`"));
    }

    @Test
    void applicationShouldAlwaysInitializeSchema() throws IOException {
        Properties properties = new Properties();
        properties.load(new ClassPathResource("application.properties").getInputStream());

        assertEquals("always", properties.getProperty("spring.sql.init.mode"));
        assertEquals("classpath:schema.sql", properties.getProperty("spring.sql.init.schema-locations"));
    }
}
