package com.rfq.system.dto.response;

import com.rfq.system.enums.AuctionStatus;
import com.rfq.system.enums.ExtensionTriggerType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class RfqResponse {
    private Long id;
    private String rfqName;
    private String referenceId;
    private String buyerName;
    private LocalDateTime bidStartTime;
    private LocalDateTime bidCloseTime;
    private LocalDateTime forcedBidCloseTime;
    private LocalDateTime pickupServiceDate;
    private AuctionStatus status;
    private Double currentLowestBid;
    private LocalDateTime createdAt;

    // Auction config
    private Integer triggerWindowMinutes;
    private Integer extensionDurationMinutes;
    private ExtensionTriggerType extensionTriggerType;
}
