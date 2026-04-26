package com.rfq.system.dto.response;

import com.rfq.system.enums.ActivityType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class ActivityLogResponse {
    private Long id;
    private Long rfqId;
    private ActivityType activityType;
    private String description;
    private String actorName;
    private LocalDateTime previousCloseTime;
    private LocalDateTime newCloseTime;
    private LocalDateTime createdAt;
}
