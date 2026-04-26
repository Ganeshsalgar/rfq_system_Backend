package com.rfq.system.service;

import com.rfq.system.dto.response.ActivityLogResponse;
import com.rfq.system.dto.response.SupplierRankingResponse;
import com.rfq.system.entity.ActivityLog;
import com.rfq.system.entity.SupplierRanking;
import com.rfq.system.repository.ActivityLogRepository;
import com.rfq.system.repository.SupplierRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final SupplierRankingRepository supplierRankingRepository;

    public List<ActivityLogResponse> getLogsByRfqId(Long rfqId) {
        return activityLogRepository.findByRfqIdOrderByCreatedAtAsc(rfqId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<SupplierRankingResponse> getRankingsByRfqId(Long rfqId) {
        return supplierRankingRepository.findByRfqIdOrderByRankPositionAsc(rfqId).stream()
                .map(this::toRankingResponse)
                .collect(Collectors.toList());
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .rfqId(log.getRfq().getId())
                .activityType(log.getActivityType())
                .description(log.getDescription())
                .actorName(log.getActorName())
                .previousCloseTime(log.getPreviousCloseTime())
                .newCloseTime(log.getNewCloseTime())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private SupplierRankingResponse toRankingResponse(SupplierRanking sr) {
        return SupplierRankingResponse.builder()
                .supplierId(sr.getSupplier().getId())
                .supplierName(sr.getSupplier().getUsername())
                .companyName(sr.getSupplier().getCompanyName())
                .rankPosition(sr.getRankPosition())
                .rankLabel("L" + sr.getRankPosition())
                .bestAmount(sr.getBestAmount())
                .updatedAt(sr.getUpdatedAt())
                .build();
    }
}
