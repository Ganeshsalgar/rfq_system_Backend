package com.rfq.system.entity;

import com.rfq.system.enums.ActivityType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_log_rfq_id", columnList = "rfq_id"),
    @Index(name = "idx_log_created_at", columnList = "created_at")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 30)
    private ActivityType activityType;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "actor_name", length = 100)
    private String actorName;

    @Column(name = "previous_close_time")
    private LocalDateTime previousCloseTime;

    @Column(name = "new_close_time")
    private LocalDateTime newCloseTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { this.createdAt = LocalDateTime.now(); }
}
