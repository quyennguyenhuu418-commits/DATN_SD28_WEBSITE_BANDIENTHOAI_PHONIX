package com.example.datn_sd28_2025.config;

import com.example.datn_sd28_2025.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Sử dụng CustomPasswordEncoder để hỗ trợ cả {noop} và {bcrypt} prefix
        return new CustomPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 🚫 Disable CSRF (stateless API)
                .csrf(csrf -> csrf.disable())
                // 🌐 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 🔒 Stateless session (JWT or OAuth2)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // ⚙️ Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/uploads/**",
                                "/api/auth/**",
                                "/api/customer/auth/login",
                                "/api/customer/auth/register",
                                "/api/customer/auth/forgot-password",
                                "/api/customer/auth/verify-otp",
                                "/api/customer/auth/reset-password",
                                "/api/customer/auth/google-login",
                                "/api/customer/auth/google-url",
                                "/api/customer/auth/test-register",
                                "/api/customer/auth/check-email",
                                "/api/customer/auth/check-username",
                                "/api/customer/auth/check-phone",
                                "/api/public/**",
                                "/api/san-pham/**",
                                "/api/san-pham-pos/**",
                                "/api/danh-muc/**",
                                "/api/hang/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/customer/auth/update-profile",
                                "/api/customer/auth/me",
                                "/api/customer/auth/change-password"
                        ).authenticated()
                        .anyRequest().permitAll()
                )
                // 🔐 Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 🌍 CORS configuration
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
