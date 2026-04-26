package com.rfq.system.repository;

import com.rfq.system.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    @Query("SELECT b FROM Bid b WHERE b.rfq.id = :rfqId ORDER BY b.totalAmount ASC")
    List<Bid> findByRfqIdOrderByTotalAmountAsc(@Param("rfqId") Long rfqId);

    @Query("SELECT b FROM Bid b WHERE b.rfq.id = :rfqId AND b.isActive = true ORDER BY b.totalAmount ASC")
    List<Bid> findActiveBidsByRfqIdSorted(@Param("rfqId") Long rfqId);

    @Query("SELECT MIN(b.totalAmount) FROM Bid b WHERE b.rfq.id = :rfqId AND b.isActive = true")
    Optional<Double> findLowestBidAmountByRfqId(@Param("rfqId") Long rfqId);

    @Query("SELECT b FROM Bid b WHERE b.rfq.id = :rfqId AND b.bidTime >= :since AND b.isActive = true")
    List<Bid> findBidsSince(@Param("rfqId") Long rfqId, @Param("since") LocalDateTime since);

    @Query("SELECT b FROM Bid b WHERE b.rfq.id = :rfqId AND b.supplier.id = :supplierId AND b.isActive = true ORDER BY b.totalAmount ASC")
    List<Bid> findActiveByRfqAndSupplier(@Param("rfqId") Long rfqId, @Param("supplierId") Long supplierId);
}
