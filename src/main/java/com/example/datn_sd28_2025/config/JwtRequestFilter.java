package com.example.datn_sd28_2025.config;

import com.example.datn_sd28_2025.service.JwtUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    // Rate limiting for warning logs
    private long lastWarningTime = 0;
    private static final long WARNING_COOLDOWN = 5000; // 5 seconds

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;
        
        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            
            // Validate JWT token format before processing
            if (isValidJwtFormat(jwtToken)) {
                try {
                    username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                } catch (IllegalArgumentException e) {
                    logger.error("Unable to get JWT Token", e);
                } catch (ExpiredJwtException e) {
                    logger.error("JWT Token has expired", e);
                } catch (io.jsonwebtoken.MalformedJwtException e) {
                    logger.error("JWT Token is malformed", e);
                } catch (io.jsonwebtoken.SignatureException e) {
                    logger.error("JWT Token signature validation failed", e);
                } catch (io.jsonwebtoken.UnsupportedJwtException e) {
                    logger.error("JWT Token is unsupported", e);
                } catch (Exception e) {
                    logger.error("JWT Token validation failed", e);
                }
            } else {
                // Rate limit warning logs to avoid spam
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastWarningTime > WARNING_COOLDOWN) {
                    logger.warn("Invalid JWT token format - token must contain exactly 2 period characters. " +
                              "This warning will be suppressed for " + (WARNING_COOLDOWN / 1000) + " seconds to avoid log spam.");
                    lastWarningTime = currentTime;
                }
            }
        } else if (requestTokenHeader != null && !requestTokenHeader.trim().isEmpty()) {
            // Only log if there's actually a token header but it's not Bearer format
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastWarningTime > WARNING_COOLDOWN) {
                logger.warn("JWT Token does not begin with Bearer String. " +
                          "This warning will be suppressed for " + (WARNING_COOLDOWN / 1000) + " seconds to avoid log spam.");
                lastWarningTime = currentTime;
            }
        }

        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

            // if token is valid configure Spring Security to manually set authentication
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the
                // Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
    
    /**
     * Validates if the token has the correct JWT format (contains exactly 2 periods)
     * @param token the JWT token to validate
     * @return true if the token has valid JWT format, false otherwise
     */
    private boolean isValidJwtFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // Count the number of periods in the token
        long periodCount = token.chars().filter(ch -> ch == '.').count();
        
        // JWT tokens must have exactly 2 periods
        return periodCount == 2;
    }
}
