package com.rfq.system.service;

import com.rfq.system.dto.request.PlaceBidRequest;
import com.rfq.system.dto.response.BidResponse;
import com.rfq.system.entity.*;
import com.rfq.system.enums.ActivityType;
import com.rfq.system.enums.AuctionStatus;
import com.rfq.system.enums.ExtensionTriggerType;
import com.rfq.system.exception.AuctionException;
import com.rfq.system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final RfqRepository rfqRepository;
    private final BidRepository bidRepository;
    private final AuctionConfigRepository auctionConfigRepository;
    private final SupplierRankingRepository supplierRankingRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public BidResponse placeBid(PlaceBidRequest request) {
        Rfq rfq = rfqRepository.findById(request.getRfqId())
                .orElseThrow(() -> new AuctionException("RFQ not found: " + request.getRfqId()));

        validateAuctionIsOpen(rfq);

        User supplier = userService.getById(request.getSupplierId());

        double totalAmount = calculateTotal(request);
        validateBidAmount(rfq, totalAmount);

        // Capture L1 before bid
        Optional<SupplierRanking> previousL1 = supplierRankingRepository.findL1ByRfqId(rfq.getId());
        Long previousL1SupplierId = previousL1.map(sr -> sr.getSupplier().getId()).orElse(null);

        Bid bid = Bid.builder()
                .rfq(rfq)
                .supplier(supplier)
                .carrierName(request.getCarrierName())
                .freightCharges(request.getFreightCharges())
                .originCharges(request.getOriginCharges() != null ? request.getOriginCharges() : 0.0)
                .destinationCharges(request.getDestinationCharges() != null ? request.getDestinationCharges() : 0.0)
                .totalAmount(totalAmount)
                .transitTimeDays(request.getTransitTimeDays())
                .quoteValidityDate(request.getQuoteValidityDate())
                .remarks(request.getRemarks())
                .isActive(true)
                .build();

        bid = bidRepository.save(bid);

        // Update rfq lowest bid
        rfq.setCurrentLowestBid(totalAmount < (rfq.getCurrentLowestBid() != null ? rfq.getCurrentLowestBid() : Double.MAX_VALUE)
                ? totalAmount : rfq.getCurrentLowestBid());

        // Log bid placement
        logActivity(rfq, ActivityType.BID_PLACED,
                "Bid placed by " + supplier.getUsername() + " with amount: " + totalAmount,
                supplier.getUsername(), null, null);

        // Update rankings and check for extension
        boolean rankChanged = updateRankings(rfq);
        boolean l1Changed = checkL1Changed(rfq, previousL1SupplierId);

        // Evaluate extension
        AuctionConfig config = auctionConfigRepository.findByRfqId(rfq.getId())
                .orElseThrow(() -> new AuctionException("Auction config not found for RFQ: " + rfq.getId()));

        evaluateExtension(rfq, config, bid.getBidTime(), rankChanged, l1Changed, supplier.getUsername());

        rfqRepository.save(rfq);

        // Notify via WebSocket
        notifyBidUpdate(rfq.getId(), bid);

        return toBidResponse(bid, getRankForSupplier(rfq.getId(), supplier.getId()));
    }

    private void validateAuctionIsOpen(Rfq rfq) {
        LocalDateTime now = LocalDateTime.now();
        if (rfq.getStatus() == AuctionStatus.PENDING) {
            throw new AuctionException("Auction has not started yet");
        }
        if (rfq.getStatus() == AuctionStatus.CLOSED || rfq.getStatus() == AuctionStatus.FORCE_CLOSED) {
            throw new AuctionException("Auction is already closed");
        }
        if (now.isAfter(rfq.getBidCloseTime())) {
            throw new AuctionException("Auction bidding period has ended");
        }
    }

    private void validateBidAmount(Rfq rfq, double totalAmount) {
        if (rfq.getCurrentLowestBid() != null && totalAmount >= rfq.getCurrentLowestBid()) {
            throw new AuctionException("Bid amount must be lower than current lowest bid: " + rfq.getCurrentLowestBid());
        }
    }

    private double calculateTotal(PlaceBidRequest request) {
        return (request.getFreightCharges() != null ? request.getFreightCharges() : 0)
             + (request.getOriginCharges() != null ? request.getOriginCharges() : 0)
             + (request.getDestinationCharges() != null ? request.getDestinationCharges() : 0);
    }

    /**
     * Rebuilds supplier rankings from scratch based on best (lowest) bid per supplier.
     * Returns true if any rank changed.
     */
    @Transactional
    public boolean updateRankings(Rfq rfq) {
        List<SupplierRanking> existingRankings = supplierRankingRepository.findByRfqIdOrderByRankPositionAsc(rfq.getId());

        // Get best bid per supplier sorted by amount
        List<Bid> sortedBids = bidRepository.findActiveBidsByRfqIdSorted(rfq.getId());

        // Build new rankings: one entry per supplier (their best bid)
        java.util.Map<Long, Bid> bestBidPerSupplier = new java.util.LinkedHashMap<>();
        for (Bid bid : sortedBids) {
            bestBidPerSupplier.putIfAbsent(bid.getSupplier().getId(), bid);
        }

        List<Long> supplierOrder = new java.util.ArrayList<>(bestBidPerSupplier.keySet());

        boolean rankChanged = false;

        // Check if order changed
        List<Long> previousOrder = existingRankings.stream()
                .map(sr -> sr.getSupplier().getId())
                .collect(Collectors.toList());

        if (!supplierOrder.equals(previousOrder)) {
            rankChanged = true;
        }

        // Delete and rebuild
        supplierRankingRepository.deleteByRfqId(rfq.getId());

        int rank = 1;
        for (Long supplierId : supplierOrder) {
            Bid best = bestBidPerSupplier.get(supplierId);
            SupplierRanking sr = SupplierRanking.builder()
                    .rfq(rfq)
                    .supplier(best.getSupplier())
                    .bestBid(best)
                    .rankPosition(rank++)
                    .bestAmount(best.getTotalAmount())
                    .build();
            supplierRankingRepository.save(sr);
        }

        return rankChanged;
    }

    private boolean checkL1Changed(Rfq rfq, Long previousL1SupplierId) {
        Optional<SupplierRanking> currentL1 = supplierRankingRepository.findL1ByRfqId(rfq.getId());
        if (currentL1.isEmpty()) return false;
        Long currentL1SupplierId = currentL1.get().getSupplier().getId();
        return !currentL1SupplierId.equals(previousL1SupplierId);
    }

    /**
     * Core British Auction extension logic.
     * Checks if bid was placed within trigger window and extends if conditions met.
     */
    private void evaluateExtension(Rfq rfq, AuctionConfig config, LocalDateTime bidTime,
                                   boolean rankChanged, boolean l1Changed, String actorName) {
        LocalDateTime closeTime = rfq.getBidCloseTime();
        LocalDateTime triggerWindowStart = closeTime.minusMinutes(config.getTriggerWindowMinutes());

        boolean withinWindow = !bidTime.isBefore(triggerWindowStart) && !bidTime.isAfter(closeTime);
        if (!withinWindow) return;

        boolean shouldExtend = switch (config.getExtensionTriggerType()) {
            case BID_RECEIVED -> true;
            case ANY_RANK_CHANGE -> rankChanged;
            case L1_RANK_CHANGE -> l1Changed;
            default -> false;
        };

        if (!shouldExtend) return;

        LocalDateTime newCloseTime = closeTime.plusMinutes(config.getExtensionDurationMinutes());

        // Never exceed forced close time
        if (newCloseTime.isAfter(rfq.getForcedBidCloseTime())) {
            newCloseTime = rfq.getForcedBidCloseTime();
        }

        // Only extend if there's actual time to add
        if (newCloseTime.isAfter(closeTime)) {
            String reason = buildExtensionReason(config.getExtensionTriggerType(), rankChanged, l1Changed);
            log.info("Extending RFQ {} close time from {} to {} | Reason: {}", rfq.getId(), closeTime, newCloseTime, reason);

            logActivity(rfq, ActivityType.TIME_EXTENDED,
                    "Auction extended. Reason: " + reason + ". New close time: " + newCloseTime,
                    actorName, closeTime, newCloseTime);

            rfq.setBidCloseTime(newCloseTime);
        }
    }

    private String buildExtensionReason(ExtensionTriggerType triggerType, boolean rankChanged, boolean l1Changed) {
        return switch (triggerType) {
            case BID_RECEIVED -> "New bid received within trigger window";
            case ANY_RANK_CHANGE -> rankChanged ? "Supplier rank changed within trigger window" : "No rank change";
            case L1_RANK_CHANGE -> l1Changed ? "L1 (lowest bidder) changed within trigger window" : "L1 unchanged";
            default -> "Unknown trigger";
        };
    }

    public List<BidResponse> getBidsByRfqId(Long rfqId) {
        List<Bid> bids = bidRepository.findActiveBidsByRfqIdSorted(rfqId);
        List<SupplierRanking> rankings = supplierRankingRepository.findByRfqIdOrderByRankPositionAsc(rfqId);

        java.util.Map<Long, Integer> supplierRankMap = rankings.stream()
                .collect(Collectors.toMap(sr -> sr.getSupplier().getId(), SupplierRanking::getRankPosition));

        return bids.stream()
                .map(bid -> toBidResponse(bid, supplierRankMap.get(bid.getSupplier().getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public void closeAuction(Rfq rfq, boolean forced) {
        AuctionStatus newStatus = forced ? AuctionStatus.FORCE_CLOSED : AuctionStatus.CLOSED;
        rfq.setStatus(newStatus);
        rfqRepository.save(rfq);

        ActivityType activityType = forced ? ActivityType.AUCTION_FORCE_CLOSED : ActivityType.AUCTION_CLOSED;
        String description = forced
                ? "Auction force closed at forced close time: " + rfq.getForcedBidCloseTime()
                : "Auction closed at bid close time: " + rfq.getBidCloseTime();

        logActivity(rfq, activityType, description, "SYSTEM", null, null);
        log.info("RFQ {} closed with status: {}", rfq.getId(), newStatus);
    }

    private void logActivity(Rfq rfq, ActivityType type, String description,
                              String actorName, LocalDateTime prevClose, LocalDateTime newClose) {
        ActivityLog log = ActivityLog.builder()
                .rfq(rfq)
                .activityType(type)
                .description(description)
                .actorName(actorName)
                .previousCloseTime(prevClose)
                .newCloseTime(newClose)
                .build();
        activityLogRepository.save(log);
    }

    private Integer getRankForSupplier(Long rfqId, Long supplierId) {
        return supplierRankingRepository.findByRfqIdAndSupplierId(rfqId, supplierId)
                .map(SupplierRanking::getRankPosition)
                .orElse(null);
    }

    private void notifyBidUpdate(Long rfqId, Bid bid) {
        try {
            messagingTemplate.convertAndSend("/topic/rfq/" + rfqId + "/bids",
                    toBidResponse(bid, getRankForSupplier(rfqId, bid.getSupplier().getId())));
        } catch (Exception e) {
            log.warn("WebSocket notification failed for RFQ {}: {}", rfqId, e.getMessage());
        }
    }

    private BidResponse toBidResponse(Bid bid, Integer rank) {
        return BidResponse.builder()
                .id(bid.getId())
                .rfqId(bid.getRfq().getId())
                .supplierId(bid.getSupplier().getId())
                .supplierName(bid.getSupplier().getUsername())
                .carrierName(bid.getCarrierName())
                .freightCharges(bid.getFreightCharges())
                .originCharges(bid.getOriginCharges())
                .destinationCharges(bid.getDestinationCharges())
                .totalAmount(bid.getTotalAmount())
                .transitTimeDays(bid.getTransitTimeDays())
                .quoteValidityDate(bid.getQuoteValidityDate())
                .remarks(bid.getRemarks())
                .bidTime(bid.getBidTime())
                .rankPosition(rank)
                .build();
    }
}
