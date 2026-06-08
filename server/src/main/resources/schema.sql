CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `phone` VARCHAR(20) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(64) NOT NULL,
  `avatar_url` VARCHAR(255) DEFAULT NULL,
  `role` VARCHAR(20) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `report_restricted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `gender` VARCHAR(16) DEFAULT NULL,
  `contact_phone` VARCHAR(64) DEFAULT NULL,
  `bio` VARCHAR(255) DEFAULT NULL,
  `publish_role` VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_profile_user_id` (`user_id`),
  CONSTRAINT `fk_profile_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `file_asset` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `original_name` VARCHAR(255) NOT NULL,
  `stored_name` VARCHAR(255) NOT NULL,
  `relative_path` VARCHAR(255) NOT NULL,
  `content_type` VARCHAR(100) DEFAULT NULL,
  `biz_type` VARCHAR(50) DEFAULT NULL,
  `uploader_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_file_asset_uploader_id` (`uploader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `real_name_verification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `real_name` VARCHAR(64) NOT NULL,
  `id_card_no` VARCHAR(32) NOT NULL,
  `front_file_id` BIGINT NOT NULL,
  `back_file_id` BIGINT NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `reject_reason` VARCHAR(255) DEFAULT NULL,
  `reviewed_by` BIGINT DEFAULT NULL,
  `reviewed_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_verification_user_id` (`user_id`),
  KEY `idx_verification_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(20) NOT NULL,
  `title` VARCHAR(100) DEFAULT NULL,
  `last_message_id` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_conversation_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `conversation_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `conversation_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `unread_count` INT NOT NULL DEFAULT 0,
  `last_read_message_id` BIGINT DEFAULT NULL,
  `hidden` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_conv_user` (`conversation_id`, `user_id`),
  KEY `idx_member_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `conversation_id` BIGINT NOT NULL,
  `sender_id` BIGINT NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `content` TEXT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_message_conv_id` (`conversation_id`),
  KEY `idx_message_sender_id` (`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `report_ticket` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `module` VARCHAR(30) NOT NULL,
  `target_type` VARCHAR(30) NOT NULL,
  `target_id` BIGINT DEFAULT NULL,
  `report_type` VARCHAR(50) NOT NULL,
  `description` VARCHAR(500) NOT NULL,
  `contact_phone` VARCHAR(64) DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL,
  `submitter_id` BIGINT NOT NULL,
  `handle_remark` VARCHAR(255) DEFAULT NULL,
  `handled_by` BIGINT DEFAULT NULL,
  `handled_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_report_submitter_id` (`submitter_id`),
  KEY `idx_report_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `report_attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_id` BIGINT NOT NULL,
  `file_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_report_attachment_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `admin_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `operator_id` BIGINT NOT NULL,
  `operation_type` VARCHAR(50) NOT NULL,
  `target_type` VARCHAR(50) NOT NULL,
  `target_id` BIGINT DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_admin_log_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `errand_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(40) NOT NULL,
  `publisher_id` BIGINT NOT NULL,
  `runner_id` BIGINT DEFAULT NULL,
  `service_type` VARCHAR(20) NOT NULL,
  `pickup_address` VARCHAR(255) NOT NULL,
  `delivery_address` VARCHAR(255) NOT NULL,
  `pickup_time_text` VARCHAR(100) DEFAULT NULL,
  `detail_content` VARCHAR(500) NOT NULL,
  `remark` VARCHAR(500) DEFAULT NULL,
  `base_fee` DECIMAL(10,2) NOT NULL,
  `urgent_fee` DECIMAL(10,2) NOT NULL DEFAULT 0,
  `fragile_fee` DECIMAL(10,2) NOT NULL DEFAULT 0,
  `total_fee` DECIMAL(10,2) NOT NULL,
  `urgent_flag` TINYINT NOT NULL DEFAULT 0,
  `fragile_flag` TINYINT NOT NULL DEFAULT 0,
  `status` VARCHAR(20) NOT NULL,
  `accept_deadline` DATETIME NOT NULL,
  `accepted_at` DATETIME DEFAULT NULL,
  `picked_up_at` DATETIME DEFAULT NULL,
  `delivered_at` DATETIME DEFAULT NULL,
  `completed_at` DATETIME DEFAULT NULL,
  `cancelled_at` DATETIME DEFAULT NULL,
  `cancel_reason` VARCHAR(255) DEFAULT NULL,
  `public_visible` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_errand_order_no` (`order_no`),
  KEY `idx_errand_order_publisher_id` (`publisher_id`),
  KEY `idx_errand_order_runner_id` (`runner_id`),
  KEY `idx_errand_order_status` (`status`),
  KEY `idx_errand_order_accept_deadline` (`accept_deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `errand_order_attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `file_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_errand_order_attachment_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `errand_order_chat` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `conversation_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_errand_order_chat_order_id` (`order_id`),
  UNIQUE KEY `uk_errand_order_chat_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `errand_rule_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(40) NOT NULL,
  `urgent_fee` DECIMAL(10,2) NOT NULL,
  `fragile_fee` DECIMAL(10,2) NOT NULL,
  `publish_limit_per_user` INT NOT NULL,
  `accept_limit_per_user` INT NOT NULL,
  `auto_expire_hours` INT NOT NULL,
  `min_base_fee` DECIMAL(10,2) NOT NULL,
  `max_base_fee` DECIMAL(10,2) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_errand_rule_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `beauty_good` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `category` VARCHAR(30) NOT NULL,
  `title` VARCHAR(120) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `summary` VARCHAR(500) NOT NULL,
  `usage_guide` VARCHAR(1000) DEFAULT NULL,
  `scene_text` VARCHAR(255) DEFAULT NULL,
  `evaluation` TEXT DEFAULT NULL,
  `dorm_experience` TEXT DEFAULT NULL,
  `avoidance_guide` TEXT DEFAULT NULL,
  `skin_tags_json` TEXT DEFAULT NULL,
  `review_json` TEXT DEFAULT NULL,
  `gallery_json` TEXT DEFAULT NULL,
  `image_url` VARCHAR(255) DEFAULT NULL,
  `product_image_file_id` BIGINT DEFAULT NULL,
  `receipt_file_id` BIGINT DEFAULT NULL,
  `creator_id` BIGINT NOT NULL,
  `creator_nickname` VARCHAR(64) DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL,
  `source_type` VARCHAR(20) NOT NULL DEFAULT 'USER',
  `reject_reason` VARCHAR(255) DEFAULT NULL,
  `reviewed_by` BIGINT DEFAULT NULL,
  `reviewed_at` DATETIME DEFAULT NULL,
  `favorite_count` INT NOT NULL DEFAULT 0,
  `view_count` INT NOT NULL DEFAULT 0,
  `published_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_beauty_good_category` (`category`),
  KEY `idx_beauty_good_status` (`status`),
  KEY `idx_beauty_good_creator_id` (`creator_id`),
  KEY `idx_beauty_good_published_at` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `beauty_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `good_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_beauty_favorite_good_user` (`good_id`, `user_id`),
  KEY `idx_beauty_favorite_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `beauty_appeal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `good_id` BIGINT NOT NULL,
  `good_title` VARCHAR(120) NOT NULL,
  `category` VARCHAR(30) NOT NULL,
  `issue_type` VARCHAR(50) NOT NULL,
  `reason` VARCHAR(500) NOT NULL,
  `buyer_name` VARCHAR(64) NOT NULL,
  `seller_name` VARCHAR(64) NOT NULL,
  `contact_phone` VARCHAR(20) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `proof_summary` VARCHAR(500) NOT NULL,
  `refund_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `handle_remark` VARCHAR(255) DEFAULT NULL,
  `handled_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_beauty_appeal_good_id` (`good_id`),
  KEY `idx_beauty_appeal_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `jobs_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `role` VARCHAR(20) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `publish_enabled` TINYINT NOT NULL DEFAULT 1,
  `apply_enabled` TINYINT NOT NULL DEFAULT 1,
  `accept_enabled` TINYINT NOT NULL DEFAULT 1,
  `report_count` INT NOT NULL DEFAULT 0,
  `dispute_count` INT NOT NULL DEFAULT 0,
  `credit_score` INT NOT NULL DEFAULT 100,
  `latest_remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_jobs_account_role` (`role`),
  KEY `idx_jobs_account_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `jobs_merchant_qualification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `account_id` BIGINT DEFAULT NULL,
  `applicant_name` VARCHAR(64) NOT NULL,
  `business_name` VARCHAR(128) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `identity_type` VARCHAR(32) NOT NULL,
  `area` VARCHAR(64) NOT NULL,
  `license_file_id` BIGINT DEFAULT NULL,
  `license_image` VARCHAR(255) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `submitted_at` DATETIME NOT NULL,
  `reviewed_at` DATETIME DEFAULT NULL,
  `review_remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_jobs_merchant_account_id` (`account_id`),
  KEY `idx_jobs_merchant_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `jobs_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `account_id` BIGINT NOT NULL,
  `role` VARCHAR(20) NOT NULL,
  `publisher_name` VARCHAR(64) NOT NULL,
  `publisher_phone` VARCHAR(20) NOT NULL,
  `title` VARCHAR(120) NOT NULL,
  `category` VARCHAR(64) NOT NULL,
  `job_type` VARCHAR(30) DEFAULT NULL,
  `work_mode` VARCHAR(20) NOT NULL,
  `distance_range` VARCHAR(16) NOT NULL,
  `actual_distance_km` DECIMAL(10,2) NOT NULL DEFAULT 0,
  `area` VARCHAR(64) NOT NULL,
  `salary` DECIMAL(10,2) NOT NULL DEFAULT 0,
  `settlement` VARCHAR(32) NOT NULL,
  `schedule` VARCHAR(120) NOT NULL,
  `head_count` INT NOT NULL DEFAULT 1,
  `applicant_count` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(20) NOT NULL,
  `public_visible` TINYINT NOT NULL DEFAULT 0,
  `risk_tags_json` TEXT DEFAULT NULL,
  `description` VARCHAR(1000) NOT NULL,
  `requirement` VARCHAR(1000) NOT NULL,
  `reviewed_at` DATETIME DEFAULT NULL,
  `review_remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_jobs_post_account_id` (`account_id`),
  KEY `idx_jobs_post_role` (`role`),
  KEY `idx_jobs_post_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `jobs_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `reporter_account_id` BIGINT DEFAULT NULL,
  `target_account_id` BIGINT DEFAULT NULL,
  `post_title` VARCHAR(120) NOT NULL,
  `report_type` VARCHAR(64) NOT NULL,
  `reporter_name` VARCHAR(64) NOT NULL,
  `target_name` VARCHAR(64) NOT NULL,
  `target_role` VARCHAR(20) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `description` VARCHAR(1000) NOT NULL,
  `handle_remark` VARCHAR(255) DEFAULT NULL,
  `public_result` VARCHAR(255) DEFAULT NULL,
  `handled_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_jobs_report_post_id` (`post_id`),
  KEY `idx_jobs_report_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `jobs_skill_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `label` VARCHAR(64) NOT NULL,
  `category` VARCHAR(64) NOT NULL,
  `enabled` TINYINT NOT NULL DEFAULT 1,
  `usage_count` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_jobs_skill_tag_category` (`category`),
  KEY `idx_jobs_skill_tag_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `jobs_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_jobs_favorite_post_user` (`post_id`, `user_id`),
  KEY `idx_jobs_favorite_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `jobs_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `applicant_id` BIGINT NOT NULL,
  `publisher_id` BIGINT NOT NULL,
  `title` VARCHAR(120) NOT NULL,
  `location` VARCHAR(120) DEFAULT NULL,
  `time_text` VARCHAR(120) DEFAULT NULL,
  `salary_text` VARCHAR(64) DEFAULT NULL,
  `publisher_name` VARCHAR(64) DEFAULT NULL,
  `publisher_phone` VARCHAR(20) DEFAULT NULL,
  `role_type` VARCHAR(20) NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `conversation_id` VARCHAR(64) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_jobs_application_post_user` (`post_id`, `applicant_id`),
  KEY `idx_jobs_application_applicant_id` (`applicant_id`),
  KEY `idx_jobs_application_publisher_id` (`publisher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `jobs_post_chat` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `conversation_id` BIGINT NOT NULL,
  `applicant_id` BIGINT NOT NULL,
  `publisher_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_jobs_post_chat_post_applicant` (`post_id`, `applicant_id`),
  UNIQUE KEY `uk_jobs_post_chat_conversation` (`conversation_id`),
  KEY `idx_jobs_post_chat_publisher` (`publisher_id`),
  KEY `idx_jobs_post_chat_applicant` (`applicant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `jobs_resume` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `real_name` VARCHAR(64) DEFAULT NULL,
  `major` VARCHAR(128) DEFAULT NULL,
  `grade` VARCHAR(64) DEFAULT NULL,
  `skills` TEXT DEFAULT NULL,
  `available_time` VARCHAR(255) DEFAULT NULL,
  `contact_phone` VARCHAR(64) DEFAULT NULL,
  `introduction` TEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_jobs_resume_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `partner_demand` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(120) NOT NULL,
  `type` VARCHAR(40) NOT NULL,
  `location` VARCHAR(255) NOT NULL,
  `schedule` VARCHAR(120) NOT NULL,
  `publisher_name` VARCHAR(64) NOT NULL,
  `publisher_user_id` BIGINT NOT NULL,
  `publisher_phone` VARCHAR(32) DEFAULT NULL,
  `description` VARCHAR(1000) NOT NULL,
  `need_tags_json` TEXT DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL,
  `base_risk_tags_json` TEXT DEFAULT NULL,
  `apply_count` INT NOT NULL DEFAULT 0,
  `reviewed_at` DATETIME DEFAULT NULL,
  `review_remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_partner_demand_status` (`status`),
  KEY `idx_partner_demand_publisher_user_id` (`publisher_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `partner_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `module` VARCHAR(20) NOT NULL DEFAULT 'PARTNER',
  `target_type` VARCHAR(20) NOT NULL DEFAULT 'DEMAND',
  `target_id` BIGINT NOT NULL,
  `target_title` VARCHAR(120) NOT NULL,
  `report_type` VARCHAR(50) NOT NULL,
  `description` VARCHAR(1000) NOT NULL,
  `reporter_name` VARCHAR(64) NOT NULL,
  `reporter_user_id` BIGINT NOT NULL,
  `contact_phone` VARCHAR(32) DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL,
  `conversation_id` VARCHAR(64) DEFAULT NULL,
  `handle_remark` VARCHAR(255) DEFAULT NULL,
  `handled_at` DATETIME DEFAULT NULL,
  `deadline_at` DATETIME DEFAULT NULL,
  `evidence_summary` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_partner_report_status` (`status`),
  KEY `idx_partner_report_target_id` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `partner_conversation` (
  `id` VARCHAR(64) NOT NULL,
  `demand_id` BIGINT NOT NULL,
  `demand_title` VARCHAR(120) NOT NULL,
  `demand_type` VARCHAR(40) NOT NULL,
  `counterparty_name` VARCHAR(64) NOT NULL,
  `counterparty_user_id` BIGINT NOT NULL,
  `counterparty_phone` VARCHAR(32) DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL,
  `risk_level` VARCHAR(20) NOT NULL,
  `base_risk_tags_json` TEXT DEFAULT NULL,
  `review_remark` VARCHAR(255) DEFAULT NULL,
  `unread_count` INT NOT NULL DEFAULT 0,
  `last_message` VARCHAR(1000) DEFAULT NULL,
  `last_message_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_partner_conversation_demand_id` (`demand_id`),
  KEY `idx_partner_conversation_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `partner_conversation_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `conversation_id` VARCHAR(64) NOT NULL,
  `sender_name` VARCHAR(64) NOT NULL,
  `sender_role` VARCHAR(30) NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `content` VARCHAR(2000) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_partner_message_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
