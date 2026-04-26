package com.rfq.system.config;

import com.rfq.system.entity.User;
import com.rfq.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin();
    }

    private void seedAdmin() {
        if (userRepository.findByUsername("admin").isPresent()) {
            log.info("Admin user already exists — skipping seed");
            return;
        }
        User admin = User.builder()
                .username("admin")
                .email("admin@rfqsystem.com")
                .password(passwordEncoder.encode("admin123"))
                .role("ADMIN")
                .companyName("RFQ System")
                .build();
        userRepository.save(admin);
        log.info("✅ Admin user created — username: admin | password: admin123");
    }
}
