package com.example.datn_sd28_2025.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow both Admin FE (5173) and Website FE (5174)
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",      // Admin FE
                "http://127.0.0.1:5173",      // Admin FE (alternative)
                "http://localhost:5174",      // Website FE
                "http://127.0.0.1:5174",      // Website FE (alternative)
                "http://localhost:3000",      // Legacy port
                "http://127.0.0.1:3000"       // Legacy port (alternative)
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Cache preflight response for 1 hour
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}


