package com.billingsystem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class BillingSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillingSystemApplication.class, args);
        log.info("Billing System Application Started Successfully");
    }

    // @Bean
    // public CommandLineRunner initDefaultUser(UserRepository userRepository,
    // PasswordEncoder passwordEncoder) {
    // return args -> {
    // if (!userRepository.existsByUsername("admin")) {
    // User admin = new User();
    // admin.setUsername("admin");
    // admin.setEmail("admin@billingsystem.com");
    // admin.setPassword(passwordEncoder.encode("admin123"));
    // admin.setRole("ADMIN");
    // admin.setActive(true);
    // admin.setCreatedAt(System.currentTimeMillis());
    // admin.setUpdatedAt(System.currentTimeMillis());
    // userRepository.save(admin);
    // System.out.println("✓ Default admin user created: admin / admin123");
    // }
    // };
    // }
}
