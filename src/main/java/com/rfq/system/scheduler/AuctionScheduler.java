package com.rfq.system.scheduler;

import com.rfq.system.entity.Rfq;
import com.rfq.system.enums.AuctionStatus;
import com.rfq.system.repository.RfqRepository;
import com.rfq.system.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionScheduler {

    private final RfqRepository rfqRepository;
    private final AuctionService auctionService;

    /**
     * Activates PENDING auctions whose start time has arrived.
     * Runs every 30 seconds.
     */
    @Scheduled(fixedDelay = 30_000)
    public void activatePendingAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Rfq> toActivate = rfqRepository.findPendingToActivate(AuctionStatus.PENDING, now);
        toActivate.forEach(rfq -> {
            rfq.setStatus(AuctionStatus.ACTIVE);
            rfqRepository.save(rfq);
            log.info("Activated RFQ: {}", rfq.getId());
        });
    }

    /**
     * Closes auctions that have passed their bid close time.
     * Force-closes auctions that have passed their forced close time.
     * Runs every 30 seconds.
     */
    @Scheduled(fixedDelay = 30_000)
    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Rfq> activeAuctions = rfqRepository.findActiveAuctionsToClose(now);

        for (Rfq rfq : activeAuctions) {
            boolean isForced = !now.isBefore(rfq.getForcedBidCloseTime());
            auctionService.closeAuction(rfq, isForced);
        }
    }
}
