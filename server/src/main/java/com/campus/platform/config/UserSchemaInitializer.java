package com.campus.platform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@RequiredArgsConstructor
public class UserSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureColumn("user", "report_restricted", "ALTER TABLE `user` ADD COLUMN `report_restricted` TINYINT NOT NULL DEFAULT 0");
        ensureColumn("user_profile", "publish_role", "ALTER TABLE `user_profile` ADD COLUMN `publish_role` VARCHAR(20) NOT NULL DEFAULT 'STUDENT'");
        ensureColumn("jobs_merchant_qualification", "license_file_id", "ALTER TABLE `jobs_merchant_qualification` ADD COLUMN `license_file_id` BIGINT DEFAULT NULL");
        ensureColumn("jobs_post", "job_type", "ALTER TABLE `jobs_post` ADD COLUMN `job_type` VARCHAR(30) DEFAULT NULL");
        ensureColumn("jobs_post", "head_count", "ALTER TABLE `jobs_post` ADD COLUMN `head_count` INT NOT NULL DEFAULT 1");
        ensureColumn("jobs_post", "public_visible", "ALTER TABLE `jobs_post` ADD COLUMN `public_visible` TINYINT NOT NULL DEFAULT 0");
        ensureColumn("conversation_member", "hidden", "ALTER TABLE `conversation_member` ADD COLUMN `hidden` TINYINT NOT NULL DEFAULT 0");
    }

    private void ensureColumn(String tableName, String columnName, String alterSql) {
        if (!tableExists(tableName)) {
            return;
        }
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = UPPER(?) AND UPPER(COLUMN_NAME) = UPPER(?)",
            Integer.class,
            tableName,
            columnName
        );
        boolean columnExists = count != null && count > 0;
        if (!columnExists) {
            jdbcTemplate.execute(alterSql);
        }
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
