package com.example.datn_sd28_2025.filter;

import com.example.datn_sd28_2025.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Validate token without username check (for flexibility)
                    if (jwtUtil.validateToken(token)) {
                        // Tạo authentication object
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "CUSTOMER"));
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                username, 
                                null, 
                                Collections.singletonList(authority)
                            );
                        
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                logger.error("JWT authentication failed: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

