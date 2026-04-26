package com.rfq.system.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlaceBidRequest {

    @NotNull(message = "RFQ ID is required")
    private Long rfqId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private String carrierName;

    @NotNull(message = "Freight charges are required")
    @Positive(message = "Freight charges must be positive")
    private Double freightCharges;

    private Double originCharges;

    private Double destinationCharges;

    private Integer transitTimeDays;

    private LocalDateTime quoteValidityDate;

    private String remarks;
}
