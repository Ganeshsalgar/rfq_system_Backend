package com.rfq.system.entity;

import com.rfq.system.enums.ExtensionTriggerType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auction_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuctionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false, unique = true)
    private Rfq rfq;

    @Column(name = "trigger_window_minutes", nullable = false)
    private Integer triggerWindowMinutes; // X

    @Column(name = "extension_duration_minutes", nullable = false)
    private Integer extensionDurationMinutes; // Y

    @Enumerated(EnumType.STRING)
    @Column(name = "extension_trigger_type", nullable = false, length = 30)
    private ExtensionTriggerType extensionTriggerType;
}
