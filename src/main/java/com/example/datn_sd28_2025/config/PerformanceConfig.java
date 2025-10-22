package com.example.datn_sd28_2025.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class PerformanceConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(performanceInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }

    @Bean
    public HandlerInterceptor performanceInterceptor() {
        return new HandlerInterceptor() {
            private final ConcurrentHashMap<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
            private final ConcurrentHashMap<String, AtomicLong> totalTime = new ConcurrentHashMap<>();

            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                long startTime = System.currentTimeMillis();
                request.setAttribute("startTime", startTime);
                return true;
            }

            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
                long startTime = (Long) request.getAttribute("startTime");
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                String endpoint = request.getRequestURI();
                
                // Update statistics
                requestCounts.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
                totalTime.computeIfAbsent(endpoint, k -> new AtomicLong(0)).addAndGet(duration);

                // Log slow requests (> 1000ms)
                if (duration > 1000) {
                    System.out.println("🐌 Slow request: " + endpoint + " took " + duration + "ms");
                }

                // Add performance headers
                response.setHeader("X-Response-Time", duration + "ms");
            }
        };
    }
}
