package com.example.datn_sd28_2025.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // Set max file size (5MB)
        factory.setMaxFileSize(DataSize.ofMegabytes(5));

        // Set max request size (10MB)
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));

        // Set file size threshold (1MB)
        factory.setFileSizeThreshold(DataSize.ofMegabytes(1));

        return factory.createMultipartConfig();
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
