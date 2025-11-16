package com.example.datn_sd28_2025.util;

import com.example.datn_sd28_2025.config.JwtUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class để lấy thông tin user hiện tại từ Security Context
 */
public class SecurityUtil {
    
    /**
     * Lấy tên người dùng hiện tại (hoTen hoặc username)
     * @return tên người dùng, hoặc "System" nếu không có authentication
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
            JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
            // Ưu tiên hoTen, nếu không có thì dùng username
            String hoTen = userDetails.getHoTen();
            if (hoTen != null && !hoTen.trim().isEmpty()) {
                return hoTen;
            }
            return userDetails.getUsername();
        }
        
        return "System";
    }
    
    /**
     * Lấy ID của user hiện tại
     * @return ID của user, hoặc null nếu không có authentication
     */
    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
            JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
            return userDetails.getId();
        }
        
        return null;
    }
    
    /**
     * Kiểm tra xem có user đang đăng nhập không
     * @return true nếu có user đăng nhập, false nếu không
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               authentication.getPrincipal() instanceof JwtUserDetails;
    }
}





