package com.rfq.system.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class SupplierRankingResponse {
    private Long supplierId;
    private String supplierName;
    private String companyName;
    private Integer rankPosition;
    private String rankLabel; // L1, L2, L3...
    private Double bestAmount;
    private LocalDateTime updatedAt;
}
