package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.config.JwtTokenUtil;
import com.example.datn_sd28_2025.config.JwtUserDetails;
import com.example.datn_sd28_2025.dto.JwtRequest;
import com.example.datn_sd28_2025.dto.JwtResponse;
import com.example.datn_sd28_2025.dto.UserInfo;
import com.example.datn_sd28_2025.dto.ChangePasswordRequest;
import com.example.datn_sd28_2025.dto.ForgotPasswordRequest;
import com.example.datn_sd28_2025.dto.ResetPasswordRequest;
import com.example.datn_sd28_2025.service.JwtUserDetailsService;
import com.example.datn_sd28_2025.service.UserService;
import com.example.datn_sd28_2025.service.InMemoryOtpService;
import com.example.datn_sd28_2025.service.EmailService;
import com.example.datn_sd28_2025.service.GiaoCaService;
import com.example.datn_sd28_2025.service.ChatService;
import com.example.datn_sd28_2025.repository.NhanVienRepository;
import com.example.datn_sd28_2025.dto.GiaoCaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private InMemoryOtpService inMemoryOtpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private GiaoCaService giaoCaService;

    @Autowired
    private ChatService chatService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        try {
            System.out.println("=== LOGIN DEBUG ===");
            System.out.println("Username: " + authenticationRequest.getUsername());
            System.out.println("Password length: " + (authenticationRequest.getPassword() != null ? authenticationRequest.getPassword().length() : "NULL"));
            
            authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

            final JwtUserDetails userDetails = (JwtUserDetails) userDetailsService
                    .loadUserByUsername(authenticationRequest.getUsername());

            final String token = jwtTokenUtil.generateToken(userDetails);

            // Create user info
            UserInfo userInfo = new UserInfo();
            userInfo.setId(userDetails.getId());
            userInfo.setUsername(userDetails.getUsername());
            userInfo.setHoTen(userDetails.getHoTen());
            userInfo.setEmail(userDetails.getEmail());
            userInfo.setRole(userDetails.getRole());

            System.out.println("Login successful for user: " + userInfo.getHoTen() + " (ID: " + userInfo.getId() + ")");
            
            // Auto-join staff to chat system only if not already joined
            try {
                if (userInfo.getRole().equals("STAFF") || userInfo.getRole().equals("MANAGER")) {
                    chatService.staffJoin(userInfo.getId().toString(), userInfo.getHoTen());
                    System.out.println("Staff auto-joined chat system: " + userInfo.getHoTen());
                }
            } catch (Exception e) {
                System.err.println("Failed to auto-join staff to chat: " + e.getMessage());
            }
            
            return ResponseEntity.ok(new JwtResponse(token, userInfo));
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Đăng nhập thất bại");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            // Validate Authorization header format
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Authorization header không hợp lệ"));
            }
            
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Validate JWT token format before processing
            if (!isValidJwtFormat(token)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token không đúng định dạng JWT"));
            }
            
            String username = jwtTokenUtil.getUsernameFromToken(token);
            
            JwtUserDetails userDetails = (JwtUserDetails) userDetailsService.loadUserByUsername(username);
            
            if (jwtTokenUtil.validateToken(token, userDetails)) {
                String newToken = jwtTokenUtil.generateToken(userDetails);
                
                UserInfo userInfo = new UserInfo();
                userInfo.setId(userDetails.getId());
                userInfo.setUsername(userDetails.getUsername());
                userInfo.setHoTen(userDetails.getHoTen());
                userInfo.setEmail(userDetails.getEmail());
                userInfo.setRole(userDetails.getRole());
                
                return ResponseEntity.ok(new JwtResponse(newToken, userInfo));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Token không hợp lệ"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không thể làm mới token: " + e.getMessage()));
        }
    }
    
    // Helper method to validate JWT format
    private boolean isValidJwtFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        long periodCount = token.chars().filter(ch -> ch == '.').count();
        return periodCount == 2;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication: " + (authentication != null ? "exists" : "null"));
            System.out.println("Principal: " + (authentication != null ? authentication.getPrincipal().getClass().getSimpleName() : "null"));
            
            if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
                JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
                
                UserInfo userInfo = new UserInfo();
                userInfo.setId(userDetails.getId());
                userInfo.setUsername(userDetails.getUsername());
                userInfo.setHoTen(userDetails.getHoTen());
                userInfo.setEmail(userDetails.getEmail());
                userInfo.setRole(userDetails.getRole());
                
                System.out.println("User info: " + userInfo.getUsername() + " - " + userInfo.getHoTen());
                return ResponseEntity.ok(userInfo);
            } else {
                System.out.println("No valid authentication found");
                return ResponseEntity.status(401).body(Map.of("error", "Không tìm thấy thông tin người dùng"));
            }
        } catch (Exception e) {
            System.out.println("Error in getCurrentUser: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi khi lấy thông tin người dùng: " + e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            // Validate input
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu hiện tại không được để trống"));
            }
            
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới không được để trống"));
            }
            
            if (request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới phải có ít nhất 6 ký tự"));
            }
            
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới và xác nhận mật khẩu không khớp"));
            }
            
            // Get current user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserDetails)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy thông tin người dùng"));
            }
            
            JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
            
            // Load user from database
            JwtUserDetails currentUser = (JwtUserDetails) userDetailsService.loadUserByUsername(userDetails.getUsername());
            
            // Verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu hiện tại không đúng"));
            }
            
            // Update password
            boolean success = userService.updatePassword(userDetails.getUsername(), request.getNewPassword());
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Không thể cập nhật mật khẩu"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không thể đổi mật khẩu: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            // Check if email exists
            var user = nhanVienRepository.findByEmail(request.getEmail());
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email không tồn tại trong hệ thống"));
            }
            
            // Generate and save OTP
            String otpCode = inMemoryOtpService.createOtp(request.getEmail());
            
            // Send OTP via email
            emailService.sendOtpEmail(request.getEmail(), otpCode);
            
            return ResponseEntity.ok(Map.of("message", "Mã OTP đã được gửi đến email của bạn"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            
            // Validate input
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email không được để trống"));
            }
            
            if (otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã OTP không được để trống"));
            }
            
            // Validate OTP
            if (inMemoryOtpService.validateOtp(email, otp)) {
                return ResponseEntity.ok(Map.of("message", "Mã OTP đã được xác thực thành công"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã OTP không đúng hoặc đã hết hạn"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra khi xác thực OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            // Validate input
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email không được để trống"));
            }
            
            // Check if email exists
            var user = nhanVienRepository.findByEmail(email);
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email không tồn tại trong hệ thống"));
            }
            
            // Generate and save new OTP
            String otpCode = inMemoryOtpService.createOtp(email);
            
            // Send OTP via email
            emailService.sendOtpEmail(email, otpCode);
            
            return ResponseEntity.ok(Map.of("message", "Mã OTP mới đã được gửi đến email của bạn"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            System.out.println("=== RESET PASSWORD DEBUG ===");
            System.out.println("Email: " + request.getEmail());
            System.out.println("OTP: " + request.getOtp());
            System.out.println("New Password: " + (request.getNewPassword() != null ? "***" : "NULL"));
            System.out.println("Confirm Password: " + (request.getConfirmPassword() != null ? "***" : "NULL"));
            
            // Validate input
            if (request.getNewPassword() == null || request.getConfirmPassword() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới và xác nhận mật khẩu không được để trống"));
            }
            
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới và xác nhận mật khẩu không khớp"));
            }
            
            // Validate and consume OTP
            if (!inMemoryOtpService.validateAndConsumeOtp(request.getEmail(), request.getOtp())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã OTP không hợp lệ hoặc đã hết hạn"));
            }
            
            // Update password
            boolean success = userService.updatePassword(request.getEmail(), request.getNewPassword());
            
            if (success) {
                // Send success email
                var user = nhanVienRepository.findByEmail(request.getEmail());
                if (user.isPresent()) {
                    emailService.sendPasswordResetSuccessEmail(request.getEmail(), user.get().getHoTen());
                }
                
                // Cleanup used OTPs
                inMemoryOtpService.cleanupUsedOtps(request.getEmail());
                
                return ResponseEntity.ok(Map.of("message", "Mật khẩu đã được đặt lại thành công"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Không thể cập nhật mật khẩu"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không thể đặt lại mật khẩu: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a stateless JWT system, logout is handled on the client side
        // by removing the token from storage
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("Tài khoản đã bị vô hiệu hóa", e);
        } catch (BadCredentialsException e) {
            throw new Exception("Tài khoản hoặc mật khẩu không đúng", e);
        }
    }

    @GetMapping("/pending-shift-handover")
    public ResponseEntity<?> getPendingShiftHandover(@RequestParam Integer nhanVienId) {
        try {
            System.out.println("=== AUTH CONTROLLER DEBUG ===");
            System.out.println("Received request for nhanVienId: " + nhanVienId);
            
            List<GiaoCaDTO> pendingGiaoCa = giaoCaService.getPendingGiaoCaForNhanVienId(nhanVienId);
            
            System.out.println("Returning " + pendingGiaoCa.size() + " pending giao ca");
            return ResponseEntity.ok(pendingGiaoCa);
        } catch (Exception e) {
            System.out.println("Error in getPendingShiftHandover: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không thể lấy thông tin giao ca");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/confirm-shift-handover")
    public ResponseEntity<?> confirmShiftHandover(@RequestBody Map<String, Object> request) {
        try {
            Integer giaoCaId = (Integer) request.get("giaoCaId");
            Integer nhanVienNhanId = (Integer) request.get("nhanVienNhanId");
            
            // Get the giao ca details
            Optional<GiaoCaDTO> giaoCaOpt = giaoCaService.getById(giaoCaId);
            if (giaoCaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Giao ca không tồn tại"));
            }
            
            GiaoCaDTO giaoCaDTO = giaoCaOpt.get();
            giaoCaDTO.setNhanVienNhanId(nhanVienNhanId);
            
            // Confirm the giao ca
            GiaoCaDTO confirmedGiaoCa = giaoCaService.confirmGiaoCa(giaoCaId, nhanVienNhanId, giaoCaDTO);
            
            return ResponseEntity.ok(Map.of(
                "message", "Xác nhận giao ca thành công",
                "giaoCa", confirmedGiaoCa
            ));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không thể xác nhận giao ca");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
