-- Contact Group
CREATE TABLE IF NOT EXISTS `contact_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    `owner_id` BIGINT,
    `name` VARCHAR(255),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`owner_id`) REFERENCES `account` (`id`)
    ) ENGINE=InnoDB;

-- Contact Group Emails
CREATE TABLE IF NOT EXISTS `contact_group_emails` (
    `contact_group_id` BIGINT NOT NULL,
    `email` VARCHAR(255),
    PRIMARY KEY (`contact_group_id`, `email`),
    FOREIGN KEY (`contact_group_id`) REFERENCES `contact_group` (`id`)
    ) ENGINE=InnoDB;

-- Email Recipient
CREATE TABLE IF NOT EXISTS `email_recipient` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `email_template_id` BIGINT,
    `email` VARCHAR(255),
    `type` ENUM('BCC','CC','TO'),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`email_template_id`) REFERENCES `email_template` (`id`)
    ) ENGINE=InnoDB;

-- Email Template
CREATE TABLE IF NOT EXISTS `email_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT,
    `body` TEXT,
    `subject` VARCHAR(255),
    `template_name` VARCHAR(255),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`member_id`) REFERENCES `account` (`id`)
    ) ENGINE=InnoDB;

-- FCM Token
CREATE TABLE IF NOT EXISTS `fcm_token` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT,
    `fcm_token` VARCHAR(255),
    `machine_uuid` VARCHAR(255),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`member_id`) REFERENCES `account` (`id`)
    ) ENGINE=InnoDB;

-- Account
CREATE TABLE IF NOT EXISTS `account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `is_primary` BIT NOT NULL,
    `access_token_fetched_at` DATETIME(6),
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    `super_account_id` BIGINT,
    `access_token` VARCHAR(255),
    `display_name` VARCHAR(255),
    `email` VARCHAR(255),
    `google_provider_id` VARCHAR(255),
    `profile_image_url` VARCHAR(255),
    `refresh_token` VARCHAR(255),
    `uid` VARCHAR(255),
    `role` ENUM('ROLE_ADMIN','ROLE_USER'),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`super_account_id`) REFERENCES `super_account` (`id`)
    ) ENGINE=InnoDB;

-- Account Contact Group
CREATE TABLE IF NOT EXISTS `member_contact_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `contact_group_id` BIGINT,
    `member_id` BIGINT,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`contact_group_id`) REFERENCES `contact_group` (`id`),
    FOREIGN KEY (`member_id`) REFERENCES `account` (`id`)
    ) ENGINE=InnoDB;

-- Pub Sub History
CREATE TABLE IF NOT EXISTS `pub_sub_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `history_id` DECIMAL(38,0),
    `member_id` BIGINT,
    PRIMARY KEY (`id`),
    UNIQUE (`member_id`),
    FOREIGN KEY (`member_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- Signature
CREATE TABLE IF NOT EXISTS `signature` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    `owner_id` BIGINT,
    `name` VARCHAR(255),
    `content` TINYTEXT,
    `type` ENUM('MEMBER','TEAM'),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`owner_id`) REFERENCES `account` (`id`)
    ) ENGINE=InnoDB;

-- Super Account
CREATE TABLE IF NOT EXISTS `super_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB;

-- Super Account Account UIDs
CREATE TABLE IF NOT EXISTS `super_account_member_uids` (
    `super_account_id` BIGINT NOT NULL,
    `member_uid` VARCHAR(255),
    PRIMARY KEY (`super_account_id`, `member_uid`),
    FOREIGN KEY (`super_account_id`) REFERENCES `super_account` (`id`)
    ) ENGINE=InnoDB;

-- Team
CREATE TABLE IF NOT EXISTS `team` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    `creator_id` BIGINT,
    `name` VARCHAR(255),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`creator_id`) REFERENCES `account` (`id`)
    ) ENGINE=InnoDB;

-- Team Invitation
CREATE TABLE IF NOT EXISTS `team_invitation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `deleted` BIT NOT NULL,
    `expires_at` DATETIME(6),
    `sent_at` DATETIME(6),
    `inviter_id` BIGINT,
    `team_id` BIGINT,
    `invitee_email` VARCHAR(255),
    `token` VARCHAR(255),
    `role` ENUM('ADMIN','EDITOR','PUBLIC_VIEWER','VIEWER'),
    `status` ENUM('ACCEPTED','EXPIRED','PENDING','REJECTED'),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`inviter_id`) REFERENCES `account` (`id`),
    FOREIGN KEY (`team_id`) REFERENCES `team` (`id`)
    ) ENGINE=InnoDB;

-- Team Account
CREATE TABLE IF NOT EXISTS `team_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT,
    `team_id` BIGINT,
    `role` ENUM('ADMIN','EDITOR','PUBLIC_VIEWER','VIEWER'),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`member_id`) REFERENCES `account` (`id`),
    FOREIGN KEY (`team_id`) REFERENCES `team` (`id`)
    ) ENGINE=InnoDB;

-- User Sidebar Config
CREATE TABLE IF NOT EXISTS `user_sidebar_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT,
    `sidebar_config` TEXT,
    PRIMARY KEY (`id`),
    UNIQUE (`member_id`),
    FOREIGN KEY (`member_id`) REFERENCES `account` (`id`)
    ) ENGINE=InnoDB;
