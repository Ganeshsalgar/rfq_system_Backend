-- ============================================================
-- RFQ SYSTEM WITH BRITISH AUCTION ENGINE
-- MySQL 8+ Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS rfq_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE rfq_db;

-- ============================================================
-- TABLE 1: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    role          ENUM('BUYER','SUPPLIER','ADMIN') NOT NULL,
    company_name  VARCHAR(150),
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users          PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email),

    INDEX idx_users_role      (role),
    INDEX idx_users_is_active (is_active)
) ENGINE=InnoDB;


-- ============================================================
-- TABLE 2: rfq
-- ============================================================
CREATE TABLE IF NOT EXISTS rfq (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    rfq_name              VARCHAR(200) NOT NULL,
    reference_id          VARCHAR(50),
    buyer_id              BIGINT       NOT NULL,
    bid_start_time        DATETIME     NOT NULL,
    bid_close_time        DATETIME     NOT NULL,
    forced_bid_close_time DATETIME     NOT NULL,
    pickup_service_date   DATETIME,
    status                ENUM('PENDING','ACTIVE','CLOSED','FORCE_CLOSED') NOT NULL DEFAULT 'PENDING',
    current_lowest_bid    DECIMAL(15,2),
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_rfq        PRIMARY KEY (id),
    CONSTRAINT uq_rfq_ref_id UNIQUE (reference_id),
    CONSTRAINT fk_rfq_buyer  FOREIGN KEY (buyer_id) REFERENCES users(id),
    CONSTRAINT chk_rfq_times CHECK (
        forced_bid_close_time > bid_close_time
        AND bid_close_time > bid_start_time
    ),

    INDEX idx_rfq_status         (status),
    INDEX idx_rfq_buyer_id       (buyer_id),
    INDEX idx_rfq_bid_close_time (bid_close_time),
    INDEX idx_rfq_created_at     (created_at)
) ENGINE=InnoDB;


-- ============================================================
-- TABLE 3: auction_config
-- British Auction configuration (1-to-1 with rfq)
-- ============================================================
CREATE TABLE IF NOT EXISTS auction_config (
    id                         BIGINT NOT NULL AUTO_INCREMENT,
    rfq_id                     BIGINT NOT NULL,
    trigger_window_minutes     INT    NOT NULL COMMENT 'X: minutes before close to monitor',
    extension_duration_minutes INT    NOT NULL COMMENT 'Y: minutes to extend when triggered',
    extension_trigger_type     ENUM('BID_RECEIVED','ANY_RANK_CHANGE','L1_RANK_CHANGE') NOT NULL,

    CONSTRAINT pk_auction_config     PRIMARY KEY (id),
    CONSTRAINT uq_auction_config_rfq UNIQUE (rfq_id),
    CONSTRAINT fk_auction_config_rfq FOREIGN KEY (rfq_id) REFERENCES rfq(id) ON DELETE CASCADE,
    CONSTRAINT chk_trigger_window    CHECK (trigger_window_minutes > 0),
    CONSTRAINT chk_extension_dur     CHECK (extension_duration_minutes > 0)
) ENGINE=InnoDB;


-- ============================================================
-- TABLE 4: bids
-- Each bid submitted by a supplier
-- ============================================================
CREATE TABLE IF NOT EXISTS bids (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    rfq_id       BIGINT       NOT NULL,
    supplier_id  BIGINT       NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL COMMENT 'freight + origin + destination',
    is_active    TINYINT(1)   NOT NULL DEFAULT 1,
    bid_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_bids          PRIMARY KEY (id),
    CONSTRAINT fk_bids_rfq      FOREIGN KEY (rfq_id)      REFERENCES rfq(id),
    CONSTRAINT fk_bids_supplier FOREIGN KEY (supplier_id) REFERENCES users(id),
    CONSTRAINT chk_bid_amount   CHECK (total_amount > 0),

    INDEX idx_bids_rfq_id       (rfq_id),
    INDEX idx_bids_supplier_id  (supplier_id),
    INDEX idx_bids_total_amount (total_amount),
    INDEX idx_bids_bid_time     (bid_time),
    INDEX idx_bids_rfq_active   (rfq_id, is_active)
) ENGINE=InnoDB;


-- ============================================================
-- TABLE 5: bid_quote_details
-- Detailed charge breakdown per bid (1-to-1 with bids)
-- ============================================================
CREATE TABLE IF NOT EXISTS bid_quote_details (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    bid_id              BIGINT        NOT NULL,
    carrier_name        VARCHAR(100),
    freight_charges     DECIMAL(15,2) NOT NULL,
    origin_charges      DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    destination_charges DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    transit_time_days   INT,
    quote_validity_date DATETIME,
    remarks             VARCHAR(500),

    CONSTRAINT pk_bid_quote_details    PRIMARY KEY (id),
    CONSTRAINT uq_bid_quote_bid_id     UNIQUE (bid_id),
    CONSTRAINT fk_bid_quote_bid        FOREIGN KEY (bid_id) REFERENCES bids(id) ON DELETE CASCADE,
    CONSTRAINT chk_freight_charges     CHECK (freight_charges >= 0),
    CONSTRAINT chk_origin_charges      CHECK (origin_charges >= 0),
    CONSTRAINT chk_destination_charges CHECK (destination_charges >= 0)
) ENGINE=InnoDB;


-- ============================================================
-- TABLE 6: supplier_rankings
-- Current L1/L2/L3 rankings per RFQ
-- ============================================================
CREATE TABLE IF NOT EXISTS supplier_rankings (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    rfq_id        BIGINT        NOT NULL,
    supplier_id   BIGINT        NOT NULL,
    best_bid_id   BIGINT,
    rank_position INT           NOT NULL COMMENT '1=L1, 2=L2, 3=L3',
    best_amount   DECIMAL(15,2) NOT NULL,
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_supplier_rankings         PRIMARY KEY (id),
    CONSTRAINT uq_supplier_rankings_rfq_sup UNIQUE (rfq_id, supplier_id),
    CONSTRAINT fk_rankings_rfq              FOREIGN KEY (rfq_id)      REFERENCES rfq(id),
    CONSTRAINT fk_rankings_supplier         FOREIGN KEY (supplier_id) REFERENCES users(id),
    CONSTRAINT fk_rankings_best_bid         FOREIGN KEY (best_bid_id) REFERENCES bids(id),
    CONSTRAINT chk_rank_position            CHECK (rank_position > 0),

    INDEX idx_rankings_rfq_id      (rfq_id),
    INDEX idx_rankings_rank_pos    (rfq_id, rank_position),
    INDEX idx_rankings_supplier_id (supplier_id)
) ENGINE=InnoDB;


-- ============================================================
-- TABLE 7: activity_logs
-- Audit trail for all auction events
-- ============================================================
CREATE TABLE IF NOT EXISTS activity_logs (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    rfq_id              BIGINT       NOT NULL,
    activity_type       ENUM('BID_PLACED','TIME_EXTENDED','AUCTION_CLOSED','AUCTION_FORCE_CLOSED') NOT NULL,
    description         VARCHAR(500) NOT NULL,
    actor_name          VARCHAR(100),
    previous_close_time DATETIME     COMMENT 'Only for TIME_EXTENDED events',
    new_close_time      DATETIME     COMMENT 'Only for TIME_EXTENDED events',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_activity_logs     PRIMARY KEY (id),
    CONSTRAINT fk_activity_logs_rfq FOREIGN KEY (rfq_id) REFERENCES rfq(id),

    INDEX idx_logs_rfq_id     (rfq_id),
    INDEX idx_logs_type       (activity_type),
    INDEX idx_logs_created_at (created_at)
) ENGINE=InnoDB;


-- ============================================================
-- VIEWS
-- ============================================================

-- Current L1 per RFQ
CREATE OR REPLACE VIEW v_rfq_l1 AS
SELECT
    sr.rfq_id,
    sr.supplier_id,
    u.username     AS supplier_name,
    u.company_name,
    sr.best_amount AS l1_amount,
    sr.best_bid_id,
    sr.updated_at
FROM supplier_rankings sr
JOIN users u ON u.id = sr.supplier_id
WHERE sr.rank_position = 1;


-- Full bid details with quote breakdown and rank
CREATE OR REPLACE VIEW v_bid_full AS
SELECT
    b.id            AS bid_id,
    b.rfq_id,
    b.supplier_id,
    u.username      AS supplier_name,
    u.company_name,
    b.total_amount,
    b.bid_time,
    b.is_active,
    bqd.carrier_name,
    bqd.freight_charges,
    bqd.origin_charges,
    bqd.destination_charges,
    bqd.transit_time_days,
    bqd.quote_validity_date,
    bqd.remarks,
    sr.rank_position
FROM bids b
JOIN  users u                    ON u.id      = b.supplier_id
LEFT JOIN bid_quote_details bqd  ON bqd.bid_id = b.id
LEFT JOIN supplier_rankings sr   ON sr.rfq_id  = b.rfq_id
                                AND sr.supplier_id = b.supplier_id;


-- ============================================================
-- SAMPLE DATA
-- ============================================================

INSERT INTO users (username, email, password, role, company_name) VALUES
('buyer_rahul',      'rahul@techcorp.com',          '$2a$10$placeholder', 'BUYER',    'Tech Corp India'),
('buyer_priya',      'priya@globalmart.com',         '$2a$10$placeholder', 'BUYER',    'Global Mart Ltd'),
('supplier_delhi',   'delhi@delhicargo.com',         '$2a$10$placeholder', 'SUPPLIER', 'Delhi Cargo Services'),
('supplier_mumbai',  'mumbai@mumbaifreight.com',     '$2a$10$placeholder', 'SUPPLIER', 'Mumbai Freight Co'),
('supplier_chennai', 'chennai@chennaishipping.com',  '$2a$10$placeholder', 'SUPPLIER', 'Chennai Shipping Ltd'),
('admin',            'admin@rfqsystem.com',          '$2a$10$placeholder', 'ADMIN',    'RFQ System');

INSERT INTO rfq (rfq_name, reference_id, buyer_id, bid_start_time, bid_close_time, forced_bid_close_time, pickup_service_date, status) VALUES
('Mumbai to Delhi Freight Q2 2026', 'RFQ-MUM-DEL-001', 1,
 '2026-04-26 00:00:00', '2026-04-28 23:50:00', '2026-04-29 23:59:00',
 '2026-05-05 09:00:00', 'ACTIVE');

INSERT INTO auction_config (rfq_id, trigger_window_minutes, extension_duration_minutes, extension_trigger_type) VALUES
(1, 10, 5, 'L1_RANK_CHANGE');

INSERT INTO bids (rfq_id, supplier_id, total_amount, bid_time) VALUES
(1, 3, 8900.00, '2026-04-27 10:00:00'),
(1, 4, 8300.00, '2026-04-27 11:00:00'),
(1, 5, 7700.00, '2026-04-27 12:00:00');

INSERT INTO bid_quote_details (bid_id, carrier_name, freight_charges, origin_charges, destination_charges, transit_time_days, remarks) VALUES
(1, 'Delhi Express',     8000.00, 500.00, 400.00, 3, 'Door to door delivery'),
(2, 'Mumbai Fast Track', 7500.00, 450.00, 350.00, 4, 'GPS tracked shipment'),
(3, 'Chennai Speedline', 7000.00, 400.00, 300.00, 5, 'Includes packaging');

INSERT INTO supplier_rankings (rfq_id, supplier_id, best_bid_id, rank_position, best_amount) VALUES
(1, 5, 3, 1, 7700.00),
(1, 4, 2, 2, 8300.00),
(1, 3, 1, 3, 8900.00);

INSERT INTO activity_logs (rfq_id, activity_type, description, actor_name) VALUES
(1, 'BID_PLACED', 'Bid placed by supplier_delhi with amount: 8900.00',   'supplier_delhi'),
(1, 'BID_PLACED', 'Bid placed by supplier_mumbai with amount: 8300.00',  'supplier_mumbai'),
(1, 'BID_PLACED', 'Bid placed by supplier_chennai with amount: 7700.00', 'supplier_chennai');
