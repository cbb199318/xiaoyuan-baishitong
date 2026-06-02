CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `phone` VARCHAR(20),
  `password` VARCHAR(255),
  `nickname` VARCHAR(64),
  `avatar_url` VARCHAR(255),
  `role` VARCHAR(20),
  `status` VARCHAR(20),
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);

CREATE TABLE IF NOT EXISTS `user_profile` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT,
  `gender` VARCHAR(16),
  `contact_phone` VARCHAR(64),
  `bio` VARCHAR(255),
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);

CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `type` VARCHAR(20),
  `title` VARCHAR(100),
  `last_message_id` BIGINT,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);

CREATE TABLE IF NOT EXISTS `conversation_member` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `conversation_id` BIGINT,
  `user_id` BIGINT,
  `unread_count` INT,
  `last_read_message_id` BIGINT,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);

CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `conversation_id` BIGINT,
  `sender_id` BIGINT,
  `type` VARCHAR(20),
  `content` CLOB,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);

CREATE TABLE IF NOT EXISTS `file_asset` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `original_name` VARCHAR(255),
  `stored_name` VARCHAR(255),
  `relative_path` VARCHAR(255),
  `content_type` VARCHAR(100),
  `biz_type` VARCHAR(50),
  `uploader_id` BIGINT,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);

CREATE TABLE IF NOT EXISTS `real_name_verification` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT,
  `real_name` VARCHAR(64),
  `id_card_no` VARCHAR(32),
  `front_file_id` BIGINT,
  `back_file_id` BIGINT,
  `status` VARCHAR(20),
  `reject_reason` VARCHAR(255),
  `reviewed_by` BIGINT,
  `reviewed_at` TIMESTAMP,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);

CREATE TABLE IF NOT EXISTS `report_ticket` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `module` VARCHAR(30),
  `target_type` VARCHAR(30),
  `target_id` BIGINT,
  `report_type` VARCHAR(50),
  `description` VARCHAR(500),
  `contact_phone` VARCHAR(64),
  `status` VARCHAR(20),
  `submitter_id` BIGINT,
  `handle_remark` VARCHAR(255),
  `handled_by` BIGINT,
  `handled_at` TIMESTAMP,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);

CREATE TABLE IF NOT EXISTS `report_attachment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `report_id` BIGINT,
  `file_id` BIGINT,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);

CREATE TABLE IF NOT EXISTS `admin_operation_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `operator_id` BIGINT,
  `operation_type` VARCHAR(50),
  `target_type` VARCHAR(50),
  `target_id` BIGINT,
  `remark` VARCHAR(255),
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);

CREATE TABLE IF NOT EXISTS `errand_order` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `order_no` VARCHAR(40),
  `publisher_id` BIGINT,
  `runner_id` BIGINT,
  `service_type` VARCHAR(20),
  `pickup_address` VARCHAR(255),
  `delivery_address` VARCHAR(255),
  `pickup_time_text` VARCHAR(100),
  `detail_content` VARCHAR(500),
  `remark` VARCHAR(500),
  `base_fee` DECIMAL(10,2),
  `urgent_fee` DECIMAL(10,2),
  `fragile_fee` DECIMAL(10,2),
  `total_fee` DECIMAL(10,2),
  `urgent_flag` TINYINT,
  `fragile_flag` TINYINT,
  `status` VARCHAR(20),
  `accept_deadline` TIMESTAMP,
  `accepted_at` TIMESTAMP,
  `picked_up_at` TIMESTAMP,
  `delivered_at` TIMESTAMP,
  `completed_at` TIMESTAMP,
  `cancelled_at` TIMESTAMP,
  `cancel_reason` VARCHAR(255),
  `public_visible` TINYINT,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);

CREATE TABLE IF NOT EXISTS `errand_order_attachment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `order_id` BIGINT,
  `file_id` BIGINT,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);

CREATE TABLE IF NOT EXISTS `errand_order_chat` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `order_id` BIGINT,
  `conversation_id` BIGINT,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  `deleted` TINYINT
);
