package com.example.datn_sd28_2025.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.datn_sd28_2025.repository")
@EnableTransactionManagement
public class JpaConfig {
    // JPA configuration is handled by Spring Boot auto-configuration
    // This class just enables JPA repositories and transaction management
}