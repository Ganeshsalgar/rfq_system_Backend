package com.rfq.system.entity;

import com.rfq.system.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rfq", indexes = {
    @Index(name = "idx_rfq_status", columnList = "status"),
    @Index(name = "idx_rfq_close_time", columnList = "bid_close_time")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rfq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rfq_name", nullable = false, length = 200)
    private String rfqName;

    @Column(name = "reference_id", unique = true, length = 50)
    private String referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Column(name = "bid_start_time", nullable = false)
    private LocalDateTime bidStartTime;

    @Column(name = "bid_close_time", nullable = false)
    private LocalDateTime bidCloseTime;

    @Column(name = "forced_bid_close_time", nullable = false)
    private LocalDateTime forcedBidCloseTime;

    @Column(name = "pickup_service_date")
    private LocalDateTime pickupServiceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuctionStatus status;

    @Column(name = "current_lowest_bid")
    private Double currentLowestBid;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = AuctionStatus.PENDING;
    }

    @PreUpdate
    void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
