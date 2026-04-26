package com.rfq.system.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_rankings", indexes = {
    @Index(name = "idx_ranking_rfq_id", columnList = "rfq_id"),
    @Index(name = "idx_ranking_rank", columnList = "rank_position")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupplierRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private User supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "best_bid_id")
    private Bid bestBid;

    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition; // 1 = L1, 2 = L2, etc.

    @Column(name = "best_amount", nullable = false)
    private Double bestAmount;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
