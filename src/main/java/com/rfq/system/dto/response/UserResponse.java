package com.rfq.system.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String companyName;
    private LocalDateTime createdAt;
}
