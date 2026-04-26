package com.rfq.system.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class BidResponse {
    private Long id;
    private Long rfqId;
    private Long supplierId;
    private String supplierName;
    private String carrierName;
    private Double freightCharges;
    private Double originCharges;
    private Double destinationCharges;
    private Double totalAmount;
    private Integer transitTimeDays;
    private LocalDateTime quoteValidityDate;
    private String remarks;
    private LocalDateTime bidTime;
    private Integer rankPosition;
}
