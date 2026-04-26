package com.rfq.system.repository;

import com.rfq.system.entity.SupplierRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SupplierRankingRepository extends JpaRepository<SupplierRanking, Long> {

    List<SupplierRanking> findByRfqIdOrderByRankPositionAsc(Long rfqId);

    Optional<SupplierRanking> findByRfqIdAndSupplierId(Long rfqId, Long supplierId);

    @Query("SELECT sr FROM SupplierRanking sr WHERE sr.rfq.id = :rfqId AND sr.rankPosition = 1")
    Optional<SupplierRanking> findL1ByRfqId(@Param("rfqId") Long rfqId);

    void deleteByRfqId(Long rfqId);
}
