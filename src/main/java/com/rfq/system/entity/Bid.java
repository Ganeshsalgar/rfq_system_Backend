package com.rfq.system.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids", indexes = {
    @Index(name = "idx_bid_rfq_id", columnList = "rfq_id"),
    @Index(name = "idx_bid_supplier_id", columnList = "supplier_id"),
    @Index(name = "idx_bid_total_amount", columnList = "total_amount")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private User supplier;

    @Column(name = "carrier_name", length = 100)
    private String carrierName;

    @Column(name = "freight_charges", nullable = false)
    private Double freightCharges;

    @Column(name = "origin_charges")
    private Double originCharges;

    @Column(name = "destination_charges")
    private Double destinationCharges;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "transit_time_days")
    private Integer transitTimeDays;

    @Column(name = "quote_validity_date")
    private LocalDateTime quoteValidityDate;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @PrePersist
    void prePersist() {
        this.bidTime = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }
}
