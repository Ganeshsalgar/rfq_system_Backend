package com.rfq.system.repository;

import com.rfq.system.entity.AuctionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuctionConfigRepository extends JpaRepository<AuctionConfig, Long> {
    Optional<AuctionConfig> findByRfqId(Long rfqId);
}
