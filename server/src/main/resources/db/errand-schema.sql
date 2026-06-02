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
