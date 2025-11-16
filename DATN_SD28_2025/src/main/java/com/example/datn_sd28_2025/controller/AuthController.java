package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.config.JwtUserDetails;
import com.example.datn_sd28_2025.dto.JwtRequest;
import com.example.datn_sd28_2025.entity.NhanVien;
import com.example.datn_sd28_2025.repository.NhanVienRepository;
import com.example.datn_sd28_2025.service.EmailService;
import com.example.datn_sd28_2025.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${google.oauth.token-uri}")
    private String tokenUri;

    @Value("${google.oauth.user-info-uri}")
    private String userInfoUri;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private EmailService emailService;

    private final RestTemplate restTemplate = new RestTemplate();

    // Lưu trữ OTP tạm thời (email -> OTP và thời gian hết hạn) cho admin/staff
    private final Map<String, OtpInfo> otpStorage = new ConcurrentHashMap<>();
    
    // Class để lưu thông tin OTP
    private static class OtpInfo {
        String otp;
        long expiryTime;
        String resetToken;
        
        OtpInfo(String otp, long expiryTime, String resetToken) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.resetToken = resetToken;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    /**
     * Đăng nhập cho admin/staff
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody(required = false) JwtRequest request) {
        try {
            System.out.println("=== LOGIN REQUEST ===");
            System.out.println("Request object: " + request);
            
            // Kiểm tra request null
            if (request == null) {
                System.out.println("ERROR: Request is null");
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Request body không được để trống"));
            }
            
            String username = request.getUsername();
            String password = request.getPassword();
            
            System.out.println("Username: " + username);
            System.out.println("Password: " + (password != null ? "***" : "null"));

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản không được để trống"));
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mật khẩu không được để trống"));
            }

            // Tìm nhân viên theo tài khoản hoặc email
            Optional<NhanVien> nhanVienOpt;
            if (username.contains("@")) {
                nhanVienOpt = nhanVienRepository.findByEmail(username);
            } else {
                nhanVienOpt = nhanVienRepository.findByTaiKhoan(username);
            }

            if (nhanVienOpt.isEmpty()) {
                System.out.println("ERROR: Không tìm thấy nhân viên với username: " + username);
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản hoặc mật khẩu không đúng"));
            }

            NhanVien nhanVien = nhanVienOpt.get();
            System.out.println("Found employee: " + nhanVien.getHoTen() + " (ID: " + nhanVien.getId() + ")");

            // Kiểm tra trạng thái tài khoản
            if (nhanVien.getTrangThai() == null || nhanVien.getTrangThai() != 1) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản đã bị khóa"));
            }

            // Kiểm tra mật khẩu
            if (nhanVien.getMatKhau() == null || !passwordEncoder.matches(password, nhanVien.getMatKhau())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản hoặc mật khẩu không đúng"));
            }

            // Tạo JWT token
            JwtUserDetails userDetails = new JwtUserDetails(nhanVien);
            String role = userDetails.getRole() != null ? userDetails.getRole() : "STAFF";
            String usernameForToken = nhanVien.getTaiKhoan() != null ? nhanVien.getTaiKhoan() : 
                                     (nhanVien.getEmail() != null ? nhanVien.getEmail() : String.valueOf(nhanVien.getId()));
            String token = jwtUtil.generateToken(usernameForToken, role);

            // Trả về thông tin nhân viên và token
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", nhanVien.getId());
            userMap.put("maNhanVien", nhanVien.getMaNhanVien() != null ? nhanVien.getMaNhanVien() : "");
            userMap.put("hoTen", nhanVien.getHoTen() != null ? nhanVien.getHoTen() : "");
            userMap.put("email", nhanVien.getEmail() != null ? nhanVien.getEmail() : "");
            userMap.put("soDienThoai", nhanVien.getSoDienThoai() != null ? nhanVien.getSoDienThoai() : "");
            userMap.put("taiKhoan", nhanVien.getTaiKhoan() != null ? nhanVien.getTaiKhoan() : "");
            userMap.put("chucVu", nhanVien.getChucVu() != null ? nhanVien.getChucVu() : "");
            userMap.put("role", role);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đăng nhập thành công");
            response.put("token", token);
            response.put("user", userMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Lỗi không xác định";
            if (e.getCause() != null) {
                errorMessage += " - " + e.getCause().getMessage();
            }
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + errorMessage));
        }
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            // Lấy thông tin từ SecurityContext
            String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

            if (username == null || username.equals("anonymousUser")) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }

            // Tìm nhân viên theo tài khoản hoặc email (vì JWT token có thể chứa email)
            Optional<NhanVien> nhanVienOpt;
            if (username.contains("@")) {
                nhanVienOpt = nhanVienRepository.findByEmail(username);
            } else {
                nhanVienOpt = nhanVienRepository.findByTaiKhoan(username);
            }
            
            // Nếu không tìm thấy, thử tìm theo cách khác
            if (nhanVienOpt.isEmpty() && !username.contains("@")) {
                nhanVienOpt = nhanVienRepository.findByEmail(username);
            }
            
            if (nhanVienOpt.isEmpty()) {
                System.out.println("ERROR: Không tìm thấy nhân viên với username: " + username);
                return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Không tìm thấy thông tin người dùng"));
            }

            NhanVien nhanVien = nhanVienOpt.get();
            JwtUserDetails userDetails = new JwtUserDetails(nhanVien);
            String role = userDetails.getRole() != null ? userDetails.getRole() : "STAFF";

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", nhanVien.getId());
            userMap.put("maNhanVien", nhanVien.getMaNhanVien() != null ? nhanVien.getMaNhanVien() : "");
            userMap.put("hoTen", nhanVien.getHoTen() != null ? nhanVien.getHoTen() : "");
            userMap.put("email", nhanVien.getEmail() != null ? nhanVien.getEmail() : "");
            userMap.put("soDienThoai", nhanVien.getSoDienThoai() != null ? nhanVien.getSoDienThoai() : "");
            userMap.put("taiKhoan", nhanVien.getTaiKhoan() != null ? nhanVien.getTaiKhoan() : "");
            userMap.put("chucVu", nhanVien.getChucVu() != null ? nhanVien.getChucVu() : "");
            userMap.put("role", role);

            return ResponseEntity.ok(userMap);

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Lỗi không xác định";
            if (e.getCause() != null) {
                errorMessage += " - " + e.getCause().getMessage();
            }
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + errorMessage));
        }
    }

    /**
     * Làm mới JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Authorization header không hợp lệ"));
            }

            String token = authHeader.substring(7);
            if (token.isBlank()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Token không được để trống"));
            }

            String username = jwtUtil.extractUsername(token);
            if (username == null || username.isBlank()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể trích xuất thông tin từ token"));
            }

            // Validate token (không cần check username vì đã extract được)
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Token không hợp lệ hoặc đã hết hạn"));
            }

            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByTaiKhoan(username);
            if (nhanVienOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Không tìm thấy thông tin người dùng"));
            }

            NhanVien nhanVien = nhanVienOpt.get();
            if (nhanVien.getTrangThai() == null || nhanVien.getTrangThai() != 1) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Tài khoản đã bị khóa"));
            }

            JwtUserDetails userDetails = new JwtUserDetails(nhanVien);
            String role = userDetails.getRole() != null ? userDetails.getRole() : "STAFF";
            String usernameForToken = nhanVien.getTaiKhoan() != null ? nhanVien.getTaiKhoan() : 
                                     (nhanVien.getEmail() != null ? nhanVien.getEmail() : String.valueOf(nhanVien.getId()));
            String newToken = jwtUtil.generateToken(usernameForToken, role);

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", nhanVien.getId());
            userMap.put("maNhanVien", nhanVien.getMaNhanVien() != null ? nhanVien.getMaNhanVien() : "");
            userMap.put("hoTen", nhanVien.getHoTen() != null ? nhanVien.getHoTen() : "");
            userMap.put("email", nhanVien.getEmail() != null ? nhanVien.getEmail() : "");
            userMap.put("soDienThoai", nhanVien.getSoDienThoai() != null ? nhanVien.getSoDienThoai() : "");
            userMap.put("taiKhoan", nhanVien.getTaiKhoan() != null ? nhanVien.getTaiKhoan() : "");
            userMap.put("chucVu", nhanVien.getChucVu() != null ? nhanVien.getChucVu() : "");
            userMap.put("role", role);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Làm mới token thành công");
            response.put("token", newToken);
            response.put("user", userMap);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Lỗi không xác định";
            if (e.getCause() != null) {
                errorMessage += " - " + e.getCause().getMessage();
            }
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + errorMessage));
        }
    }

    /**
     * Đăng xuất
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("success", true, "message", "Đăng xuất thành công"));
    }

    // XOÁ TOÀN BỘ endpoint @GetMapping("/google/callback") khỏi controller này. Đăng nhập Google chỉ được thực hiện qua POST /api/customer/auth/google-login!

    @GetMapping("/google/login")
    public ResponseEntity<?> redirectToGoogle() {
        // URL để frontend gọi và redirect người dùng đến trang đăng nhập Google
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=openid%20profile%20email"
                + "&access_type=offline";
        return ResponseEntity.ok(Map.of("login_url", googleAuthUrl));
    }

    /**
     * Gửi OTP về email cho chức năng quên mật khẩu (admin/staff)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email không được để trống"));
            }

            // Kiểm tra email có tồn tại không
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByEmail(email);
            if (nhanVienOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email không tồn tại trong hệ thống"));
            }

            NhanVien nhanVien = nhanVienOpt.get();

            // Kiểm tra tài khoản có mật khẩu không
            if (nhanVien.getMatKhau() == null || nhanVien.getMatKhau().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản này chưa có mật khẩu. Vui lòng liên hệ quản trị viên."));
            }

            // Kiểm tra trạng thái tài khoản
            if (nhanVien.getTrangThai() == null || nhanVien.getTrangThai() != 1) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản đã bị khóa"));
            }

            // Tạo OTP 5 chữ số
            Random random = new Random();
            int otp = 10000 + random.nextInt(90000); // Tạo số từ 10000 đến 99999
            String otpCode = String.valueOf(otp);
            
            // Tạo JWT token cho reset password
            String resetToken = jwtUtil.generatePasswordResetToken(email);
            
            // Lưu OTP vào cache (hiệu lực 5 phút)
            long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 phút
            otpStorage.put(email, new OtpInfo(otpCode, expiryTime, resetToken));
            
            // Gửi email OTP
            emailService.sendOtpEmail(email, otpCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mã OTP đã được gửi đến email của bạn");
            response.put("resetToken", resetToken); // Trả về token để frontend có thể dùng sau khi verify OTP
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Xác thực OTP (admin/staff)
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email không được để trống"));
            }
            
            if (otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mã OTP không được để trống"));
            }
            
            // Lấy thông tin OTP từ storage
            OtpInfo otpInfo = otpStorage.get(email);
            
            if (otpInfo == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mã OTP không hợp lệ hoặc đã hết hạn"));
            }
            
            if (otpInfo.isExpired()) {
                otpStorage.remove(email);
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mã OTP đã hết hạn"));
            }
            
            if (!otpInfo.otp.equals(otp)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mã OTP không đúng"));
            }
            
            // OTP hợp lệ, trả về resetToken
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xác thực OTP thành công");
            response.put("resetToken", otpInfo.resetToken);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Đặt lại mật khẩu (admin/staff)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String resetToken = request.get("resetToken");
            String newPassword = request.get("newPassword");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email không được để trống"));
            }
            
            if (resetToken == null || resetToken.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Reset token không được để trống"));
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mật khẩu mới không được để trống"));
            }
            
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mật khẩu phải có ít nhất 6 ký tự"));
            }
            
            // Validate reset token
            if (!jwtUtil.validatePasswordResetToken(resetToken)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Reset token không hợp lệ hoặc đã hết hạn"));
            }
            
            // Kiểm tra email trong token có khớp với email được gửi lên không
            String tokenEmail = jwtUtil.extractUsername(resetToken);
            if (!email.equals(tokenEmail)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Reset token không khớp với email"));
            }
            
            // Tìm nhân viên
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByEmail(email);
            if (nhanVienOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email không tồn tại trong hệ thống"));
            }
            
            NhanVien nhanVien = nhanVienOpt.get();
            
            // Mã hóa mật khẩu mới
            String encodedPassword = passwordEncoder.encode(newPassword);
            
            // Cập nhật mật khẩu
            nhanVien.setMatKhau(encodedPassword);
            // Có thể cập nhật ngày cập nhật nếu có field này
            // nhanVien.setNgayCapNhat(LocalDateTime.now());
            
            nhanVienRepository.save(nhanVien);
            
            // Xóa OTP đã sử dụng
            otpStorage.remove(email);
            
            // Gửi email thông báo đặt lại mật khẩu thành công
            try {
                emailService.sendPasswordResetSuccessEmail(email, nhanVien.getHoTen() != null ? nhanVien.getHoTen() : "Nhân viên");
            } catch (Exception e) {
                System.err.println("Không thể gửi email thông báo: " + e.getMessage());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đặt lại mật khẩu thành công");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }
}
