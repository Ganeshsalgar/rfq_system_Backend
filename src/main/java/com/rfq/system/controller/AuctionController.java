package com.rfq.system.controller;

import com.rfq.system.dto.request.PlaceBidRequest;
import com.rfq.system.dto.response.ApiResponse;
import com.rfq.system.dto.response.BidResponse;
import com.rfq.system.dto.response.SupplierRankingResponse;
import com.rfq.system.service.ActivityLogService;
import com.rfq.system.service.AuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auction")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final ActivityLogService activityLogService;

    // Only SUPPLIER can place a bid
    @PostMapping("/bid")
    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BidResponse>> placeBid(@Valid @RequestBody PlaceBidRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(auctionService.placeBid(request), "Bid placed successfully"));
    }

    // Both BUYER and SUPPLIER can view bids
    @GetMapping("/rfq/{rfqId}/bids")
    @PreAuthorize("hasAnyRole('BUYER', 'SUPPLIER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBids(@PathVariable Long rfqId) {
        return ResponseEntity.ok(ApiResponse.success(
                auctionService.getBidsByRfqId(rfqId), "Bids fetched successfully"));
    }

    // Both BUYER and SUPPLIER can view rankings
    @GetMapping("/rfq/{rfqId}/rankings")
    @PreAuthorize("hasAnyRole('BUYER', 'SUPPLIER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SupplierRankingResponse>>> getRankings(@PathVariable Long rfqId) {
        return ResponseEntity.ok(ApiResponse.success(
                activityLogService.getRankingsByRfqId(rfqId), "Rankings fetched successfully"));
    }
}
