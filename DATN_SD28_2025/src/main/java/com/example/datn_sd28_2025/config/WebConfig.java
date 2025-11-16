package com.example.datn_sd28_2025.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get absolute path to uploads directory
        String uploadsPath = Paths.get("uploads").toAbsolutePath().toString().replace("\\", "/");
        
        // Serve static files from uploads directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath + "/")
                .setCachePeriod(3600); // Cache for 1 hour

        // Serve avatar images specifically
        String avatarPath = Paths.get("uploads/avatar").toAbsolutePath().toString().replace("\\", "/");
        registry.addResourceHandler("/uploads/avatar/**")
                .addResourceLocations("file:" + avatarPath + "/")
                .setCachePeriod(3600);
        
        System.out.println("📁 Static resources configured:");
        System.out.println("  - /uploads/** -> file:" + uploadsPath + "/");
        System.out.println("  - /uploads/avatar/** -> file:" + avatarPath + "/");
    }
}