package com.rfq.system.repository;

import com.rfq.system.entity.Rfq;
import com.rfq.system.enums.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface RfqRepository extends JpaRepository<Rfq, Long> {

    List<Rfq> findAllByOrderByCreatedAtDesc();

    @Query("SELECT r FROM Rfq r WHERE r.status = :status AND r.bidStartTime <= :now")
    List<Rfq> findPendingToActivate(@Param("status") AuctionStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT r FROM Rfq r WHERE r.status = 'ACTIVE' AND r.bidCloseTime <= :now")
    List<Rfq> findActiveAuctionsToClose(@Param("now") LocalDateTime now);
}
