package com.rfq.system.service;

import com.rfq.system.dto.request.CreateRfqRequest;
import com.rfq.system.dto.response.RfqResponse;
import com.rfq.system.entity.AuctionConfig;
import com.rfq.system.entity.Rfq;
import com.rfq.system.entity.User;
import com.rfq.system.enums.AuctionStatus;
import com.rfq.system.exception.AuctionException;
import com.rfq.system.exception.ResourceNotFoundException;
import com.rfq.system.repository.AuctionConfigRepository;
import com.rfq.system.repository.RfqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RfqService {

    private final RfqRepository rfqRepository;
    private final AuctionConfigRepository auctionConfigRepository;
    private final UserService userService;

    @Transactional
    public RfqResponse createRfq(CreateRfqRequest request) {
        if (!request.getForcedBidCloseTime().isAfter(request.getBidCloseTime())) {
            throw new AuctionException("Forced bid close time must be after bid close time");
        }
        if (!request.getBidCloseTime().isAfter(request.getBidStartTime())) {
            throw new AuctionException("Bid close time must be after bid start time");
        }

        User buyer = userService.getById(request.getBuyerId());

        Rfq rfq = Rfq.builder()
                .rfqName(request.getRfqName())
                .referenceId(request.getReferenceId())
                .buyer(buyer)
                .bidStartTime(request.getBidStartTime())
                .bidCloseTime(request.getBidCloseTime())
                .forcedBidCloseTime(request.getForcedBidCloseTime())
                .pickupServiceDate(request.getPickupServiceDate())
                .status(AuctionStatus.PENDING)
                .build();

        rfq = rfqRepository.save(rfq);

        AuctionConfig config = AuctionConfig.builder()
                .rfq(rfq)
                .triggerWindowMinutes(request.getTriggerWindowMinutes())
                .extensionDurationMinutes(request.getExtensionDurationMinutes())
                .extensionTriggerType(request.getExtensionTriggerType())
                .build();

        auctionConfigRepository.save(config);

        return toResponse(rfq, config);
    }

    public RfqResponse getRfqById(Long id) {
        Rfq rfq = findById(id);
        AuctionConfig config = auctionConfigRepository.findByRfqId(id).orElse(null);
        return toResponse(rfq, config);
    }

    public List<RfqResponse> getAllRfqs() {
        return rfqRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(rfq -> {
                    AuctionConfig config = auctionConfigRepository.findByRfqId(rfq.getId()).orElse(null);
                    return toResponse(rfq, config);
                })
                .collect(Collectors.toList());
    }

    public Rfq findById(Long id) {
        return rfqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found with id: " + id));
    }

    public Rfq save(Rfq rfq) {
        return rfqRepository.save(rfq);
    }

    public RfqResponse toResponse(Rfq rfq, AuctionConfig config) {
        RfqResponse.RfqResponseBuilder builder = RfqResponse.builder()
                .id(rfq.getId())
                .rfqName(rfq.getRfqName())
                .referenceId(rfq.getReferenceId())
                .buyerName(rfq.getBuyer().getUsername())
                .bidStartTime(rfq.getBidStartTime())
                .bidCloseTime(rfq.getBidCloseTime())
                .forcedBidCloseTime(rfq.getForcedBidCloseTime())
                .pickupServiceDate(rfq.getPickupServiceDate())
                .status(rfq.getStatus())
                .currentLowestBid(rfq.getCurrentLowestBid())
                .createdAt(rfq.getCreatedAt());

        if (config != null) {
            builder.triggerWindowMinutes(config.getTriggerWindowMinutes())
                   .extensionDurationMinutes(config.getExtensionDurationMinutes())
                   .extensionTriggerType(config.getExtensionTriggerType());
        }

        return builder.build();
    }
}
