-- RFQ System Database Schema
-- Run this script to initialize the database manually (JPA ddl-auto=update handles it automatically)

CREATE DATABASE IF NOT EXISTS rfq_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rfq_db;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL COMMENT 'BUYER or SUPPLIER',
    company_name VARCHAR(150),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_role (role)
) ENGINE=InnoDB;

-- RFQ table
CREATE TABLE IF NOT EXISTS rfq (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rfq_name VARCHAR(200) NOT NULL,
    reference_id VARCHAR(50) UNIQUE,
    buyer_id BIGINT NOT NULL,
    bid_start_time DATETIME NOT NULL,
    bid_close_time DATETIME NOT NULL,
    forced_bid_close_time DATETIME NOT NULL,
    pickup_service_date DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, ACTIVE, CLOSED, FORCE_CLOSED',
    current_lowest_bid DECIMAL(15,2),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_rfq_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
    CONSTRAINT chk_rfq_times CHECK (forced_bid_close_time > bid_close_time AND bid_close_time > bid_start_time),
    INDEX idx_rfq_status (status),
    INDEX idx_rfq_close_time (bid_close_time)
) ENGINE=InnoDB;

-- Auction configuration table
CREATE TABLE IF NOT EXISTS auction_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rfq_id BIGINT NOT NULL UNIQUE,
    trigger_window_minutes INT NOT NULL COMMENT 'X: minutes before close to monitor',
    extension_duration_minutes INT NOT NULL COMMENT 'Y: minutes to extend when triggered',
    extension_trigger_type VARCHAR(30) NOT NULL COMMENT 'BID_RECEIVED, ANY_RANK_CHANGE, L1_RANK_CHANGE',
    CONSTRAINT fk_config_rfq FOREIGN KEY (rfq_id) REFERENCES rfq(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Bids table
CREATE TABLE IF NOT EXISTS bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rfq_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    carrier_name VARCHAR(100),
    freight_charges DECIMAL(15,2) NOT NULL,
    origin_charges DECIMAL(15,2) DEFAULT 0.00,
    destination_charges DECIMAL(15,2) DEFAULT 0.00,
    total_amount DECIMAL(15,2) NOT NULL,
    transit_time_days INT,
    quote_validity_date DATETIME,
    remarks VARCHAR(500),
    bid_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_bid_rfq FOREIGN KEY (rfq_id) REFERENCES rfq(id),
    CONSTRAINT fk_bid_supplier FOREIGN KEY (supplier_id) REFERENCES users(id),
    INDEX idx_bid_rfq_id (rfq_id),
    INDEX idx_bid_supplier_id (supplier_id),
    INDEX idx_bid_total_amount (total_amount),
    INDEX idx_bid_time (bid_time)
) ENGINE=InnoDB;

-- Supplier rankings table
CREATE TABLE IF NOT EXISTS supplier_rankings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rfq_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    best_bid_id BIGINT,
    rank_position INT NOT NULL COMMENT '1=L1, 2=L2, 3=L3...',
    best_amount DECIMAL(15,2) NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ranking_rfq FOREIGN KEY (rfq_id) REFERENCES rfq(id),
    CONSTRAINT fk_ranking_supplier FOREIGN KEY (supplier_id) REFERENCES users(id),
    CONSTRAINT fk_ranking_bid FOREIGN KEY (best_bid_id) REFERENCES bids(id),
    UNIQUE KEY uq_rfq_supplier (rfq_id, supplier_id),
    INDEX idx_ranking_rfq_id (rfq_id),
    INDEX idx_ranking_rank (rank_position)
) ENGINE=InnoDB;

-- Activity logs table
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rfq_id BIGINT NOT NULL,
    activity_type VARCHAR(30) NOT NULL COMMENT 'BID_PLACED, TIME_EXTENDED, AUCTION_CLOSED, AUCTION_FORCE_CLOSED',
    description VARCHAR(500) NOT NULL,
    actor_name VARCHAR(100),
    previous_close_time DATETIME,
    new_close_time DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_rfq FOREIGN KEY (rfq_id) REFERENCES rfq(id),
    INDEX idx_log_rfq_id (rfq_id),
    INDEX idx_log_created_at (created_at)
) ENGINE=InnoDB;
