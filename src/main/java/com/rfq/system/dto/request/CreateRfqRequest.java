package com.rfq.system.dto.request;

import com.rfq.system.enums.ExtensionTriggerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateRfqRequest {

    @NotBlank(message = "RFQ name is required")
    private String rfqName;

    private String referenceId;

    @NotNull(message = "Buyer ID is required")
    private Long buyerId;

    @NotNull(message = "Bid start time is required")
    private LocalDateTime bidStartTime;

    @NotNull(message = "Bid close time is required")
    private LocalDateTime bidCloseTime;

    @NotNull(message = "Forced bid close time is required")
    private LocalDateTime forcedBidCloseTime;

    private LocalDateTime pickupServiceDate;

    // Auction Config
    @NotNull(message = "Trigger window minutes is required")
    @Positive(message = "Trigger window must be positive")
    private Integer triggerWindowMinutes;

    @NotNull(message = "Extension duration minutes is required")
    @Positive(message = "Extension duration must be positive")
    private Integer extensionDurationMinutes;

    @NotNull(message = "Extension trigger type is required")
    private ExtensionTriggerType extensionTriggerType;
}
