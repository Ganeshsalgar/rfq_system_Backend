package com.rfq.system.controller;

import com.rfq.system.dto.response.ActivityLogResponse;
import com.rfq.system.dto.response.ApiResponse;
import com.rfq.system.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rfq/{rfqId}/logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    // Both BUYER and SUPPLIER can view activity logs
    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER', 'SUPPLIER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getLogs(@PathVariable Long rfqId) {
        return ResponseEntity.ok(ApiResponse.success(
                activityLogService.getLogsByRfqId(rfqId), "Activity logs fetched successfully"));
    }
}
