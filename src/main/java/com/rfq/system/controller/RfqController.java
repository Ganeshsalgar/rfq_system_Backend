package com.rfq.system.controller;

import com.rfq.system.dto.request.CreateRfqRequest;
import com.rfq.system.dto.response.ApiResponse;
import com.rfq.system.dto.response.BidResponse;
import com.rfq.system.dto.response.RfqResponse;
import com.rfq.system.dto.response.SupplierRankingResponse;
import com.rfq.system.service.ActivityLogService;
import com.rfq.system.service.AuctionService;
import com.rfq.system.service.RfqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rfq")
@RequiredArgsConstructor
public class RfqController {

    private final RfqService rfqService;
    private final AuctionService auctionService;
    private final ActivityLogService activityLogService;

    // BUYER and ADMIN can create an RFQ
    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RfqResponse>> createRfq(@Valid @RequestBody CreateRfqRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(rfqService.createRfq(request), "RFQ created successfully"));
    }

    // Both BUYER and SUPPLIER can view RFQ list
    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER', 'SUPPLIER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RfqResponse>>> getAllRfqs() {
        return ResponseEntity.ok(ApiResponse.success(rfqService.getAllRfqs(), "RFQs fetched successfully"));
    }

    // Both BUYER and SUPPLIER can view RFQ details
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUYER', 'SUPPLIER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RfqResponse>> getRfq(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rfqService.getRfqById(id), "RFQ fetched successfully"));
    }

    // Both BUYER and SUPPLIER can view bids
    @GetMapping("/{id}/bids")
    @PreAuthorize("hasAnyRole('BUYER', 'SUPPLIER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBids(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(auctionService.getBidsByRfqId(id), "Bids fetched successfully"));
    }

    // Both BUYER and SUPPLIER can view rankings
    @GetMapping("/{id}/rankings")
    @PreAuthorize("hasAnyRole('BUYER', 'SUPPLIER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SupplierRankingResponse>>> getRankings(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(activityLogService.getRankingsByRfqId(id), "Rankings fetched successfully"));
    }
}
