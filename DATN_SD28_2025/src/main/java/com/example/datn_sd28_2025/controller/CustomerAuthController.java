package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.CustomerLoginRequest;
import com.example.datn_sd28_2025.dto.CustomerRegisterRequest;
import com.example.datn_sd28_2025.dto.SimpleRegisterRequest;
import com.example.datn_sd28_2025.entity.KhachHang;
import com.example.datn_sd28_2025.service.CustomerAuthService;
import com.example.datn_sd28_2025.service.GoogleOAuthService;
import com.example.datn_sd28_2025.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/customer/auth")
@CrossOrigin(origins = "*")
public class CustomerAuthController {

    @Autowired
    private CustomerAuthService customerAuthService;

    // @Autowired
    // private DiaChiKhachHangService diaChiKhachHangService; // TẠM THỜI BỎ

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Đăng nhập bằng JWT với tài khoản và mật khẩu
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CustomerLoginRequest request) {
        try {
            // Xác thực thông tin đăng nhập
            KhachHang customer = customerAuthService.authenticate(request.getTaiKhoan(), request.getMatKhau());
            
            if (customer == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản hoặc mật khẩu không đúng"));
            }

            // Kiểm tra trạng thái tài khoản
            if (customer.getTrangThai() != 1) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản đã bị khóa"));
            }

            // Tạo JWT token - dùng email làm username (vì email được dùng để đăng nhập)
            String token = jwtUtil.generateToken(customer.getEmail() != null ? customer.getEmail() : customer.getTaiKhoan(), "CUSTOMER");

            // Trả về thông tin khách hàng và token
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đăng nhập thành công");
            response.put("token", token);
            response.put("user", Map.of(
                "id", customer.getId(),
                "maKhachHang", customer.getMaKhachHang(),
                "hoTen", customer.getHoTen(),
                "email", customer.getEmail(),
                "soDienThoai", customer.getSoDienThoai(),
                "taiKhoan", customer.getTaiKhoan(),
                "role", "CUSTOMER"
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Đăng ký tài khoản khách hàng mới
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CustomerRegisterRequest request) {
        try {
            System.out.println("=== DEBUG REGISTER ===");
            System.out.println("Request: " + request);
            System.out.println("TaiKhoan: " + request.getTaiKhoan());
            System.out.println("Email: " + request.getEmail());
            System.out.println("SoDienThoai: " + request.getSoDienThoai());
            
            // Kiểm tra tài khoản đã tồn tại
            boolean taiKhoanExists = customerAuthService.existsByTaiKhoan(request.getTaiKhoan());
            System.out.println("TaiKhoan exists: " + taiKhoanExists);
            if (taiKhoanExists) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản đã tồn tại"));
            }

            // Kiểm tra email đã tồn tại
            boolean emailExists = customerAuthService.existsByEmail(request.getEmail());
            System.out.println("Email exists: " + emailExists);
            if (emailExists) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email đã được sử dụng"));
            }

            // Kiểm tra số điện thoại đã tồn tại
            boolean phoneExists = customerAuthService.existsBySoDienThoai(request.getSoDienThoai());
            System.out.println("Phone exists: " + phoneExists);
            if (phoneExists) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Số điện thoại đã được sử dụng"));
            }

            // Tạo khách hàng mới
            KhachHang newCustomer = customerAuthService.register(request);

            // Tạo JWT token
            String token = jwtUtil.generateToken(newCustomer.getTaiKhoan(), "CUSTOMER");

            // Trả về thông tin khách hàng và token
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đăng ký thành công");
            response.put("token", token);
            response.put("user", Map.of(
                "id", newCustomer.getId(),
                "maKhachHang", newCustomer.getMaKhachHang(),
                "hoTen", newCustomer.getHoTen(),
                "email", newCustomer.getEmail(),
                "soDienThoai", newCustomer.getSoDienThoai(),
                "taiKhoan", newCustomer.getTaiKhoan(),
                "role", "CUSTOMER"
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Làm mới JWT token cho khách hàng
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

            if (!jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Token không hợp lệ hoặc đã hết hạn"));
            }

            // Tìm customer theo email hoặc tài khoản (vì JWT token có thể chứa email)
            KhachHang customer = customerAuthService.findByEmail(username);
            if (customer == null) {
                customer = customerAuthService.findByTaiKhoan(username);
            }
            if (customer == null) {
                return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng"));
            }

            if (customer.getTrangThai() != null && customer.getTrangThai() != 1) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Tài khoản đã bị khóa"));
            }

            // Tạo token mới với email (hoặc tài khoản nếu không có email)
            String newToken = jwtUtil.generateToken(customer.getEmail() != null ? customer.getEmail() : customer.getTaiKhoan(), "CUSTOMER");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Làm mới token thành công");
            response.put("token", newToken);
            response.put("user", Map.of(
                    "id", customer.getId(),
                    "maKhachHang", customer.getMaKhachHang(),
                    "hoTen", customer.getHoTen(),
                    "email", customer.getEmail(),
                    "soDienThoai", customer.getSoDienThoai(),
                    "taiKhoan", customer.getTaiKhoan(),
                    "role", "CUSTOMER"
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Đăng nhập bằng Google OAuth với code
     */
    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Code không được để trống"));
            }

            // Xác thực Google OAuth với code
            Map<String, Object> result = customerAuthService.authenticateWithGoogleCode(code);
            
            if (!(Boolean) result.get("success")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", result.get("error")));
            }

            KhachHang customer = (KhachHang) result.get("customer");
            
            // Kiểm tra trạng thái tài khoản
            if (customer.getTrangThai() != 1) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản đã bị khóa"));
            }

            // Tạo JWT token - dùng email làm username (vì email được dùng để đăng nhập)
            String token = jwtUtil.generateToken(customer.getEmail() != null ? customer.getEmail() : customer.getTaiKhoan(), "CUSTOMER");

            // Trả về thông tin khách hàng và token
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", customer.getId());
            userMap.put("maKhachHang", customer.getMaKhachHang() != null ? customer.getMaKhachHang() : "");
            userMap.put("hoTen", customer.getHoTen() != null ? customer.getHoTen() : "");
            userMap.put("email", customer.getEmail() != null ? customer.getEmail() : "");
            userMap.put("soDienThoai", customer.getSoDienThoai() != null ? customer.getSoDienThoai() : "");
            userMap.put("taiKhoan", customer.getTaiKhoan() != null ? customer.getTaiKhoan() : "");
            userMap.put("googleId", customer.getGoogleId() != null ? customer.getGoogleId() : "");
            userMap.put("role", "CUSTOMER");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đăng nhập Google thành công");
            response.put("token", token);
            response.put("user", userMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Lỗi không xác định";
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + errorMessage));
        }
    }

    /**
     * Lấy Google OAuth URL để redirect user
     */
    @GetMapping("/google-url")
    public ResponseEntity<?> getGoogleOAuthUrl() {
        try {
            String googleUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=" + googleOAuthService.getClientId() +
                    "&redirect_uri=" + java.net.URLEncoder.encode(googleOAuthService.getRedirectUri(), java.nio.charset.StandardCharsets.UTF_8) +
                    "&response_type=code" +
                    "&scope=openid%20profile%20email" +
                    "&access_type=offline";
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "googleUrl", googleUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // XÓA ENDPOINT CALLBACK - Google sẽ redirect về frontend, không phải backend

    /**
     * Đăng xuất
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Với JWT, logout chỉ cần xóa token ở client
        return ResponseEntity.ok(Map.of("success", true, "message", "Đăng xuất thành công"));
    }

    /**
     * Kiểm tra email có tồn tại không
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            boolean exists = customerAuthService.existsByEmail(email);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra tài khoản có tồn tại không
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String taiKhoan) {
        try {
            boolean exists = customerAuthService.existsByTaiKhoan(taiKhoan);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra số điện thoại có tồn tại không
     */
    @GetMapping("/check-phone")
    public ResponseEntity<?> checkPhone(@RequestParam String soDienThoai) {
        try {
            boolean exists = customerAuthService.existsBySoDienThoai(soDienThoai);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Test endpoint đơn giản
     */
    @PostMapping("/test-register")
    public ResponseEntity<?> testRegister(@RequestBody SimpleRegisterRequest request) {
        try {
            // Kiểm tra tài khoản đã tồn tại
            if (customerAuthService.existsByTaiKhoan(request.getTaiKhoan())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản đã tồn tại"));
            }

            // Kiểm tra email đã tồn tại
            if (customerAuthService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email đã được sử dụng"));
            }

            // Kiểm tra số điện thoại đã tồn tại
            if (customerAuthService.existsBySoDienThoai(request.getSoDienThoai())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Số điện thoại đã được sử dụng"));
            }

            // Tạo khách hàng mới
            CustomerRegisterRequest registerRequest = new CustomerRegisterRequest();
            registerRequest.setHoTen(request.getHoTen());
            registerRequest.setSoDienThoai(request.getSoDienThoai());
            registerRequest.setTaiKhoan(request.getTaiKhoan());
            registerRequest.setMatKhau(request.getMatKhau());
            registerRequest.setNgaySinh(request.getNgaySinh());
            registerRequest.setGioiTinh(request.getGioiTinh());
            registerRequest.setEmail(request.getEmail());

            KhachHang newCustomer = customerAuthService.register(registerRequest);

            // Tạo JWT token
            String token = jwtUtil.generateToken(newCustomer.getTaiKhoan(), "CUSTOMER");

            // Trả về thông tin khách hàng và token
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đăng ký thành công");
            response.put("token", token);
            response.put("user", Map.of(
                "id", newCustomer.getId(),
                "maKhachHang", newCustomer.getMaKhachHang(),
                "hoTen", newCustomer.getHoTen(),
                "email", newCustomer.getEmail(),
                "soDienThoai", newCustomer.getSoDienThoai(),
                "taiKhoan", newCustomer.getTaiKhoan(),
                "role", "CUSTOMER"
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin khách hàng hiện tại
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentCustomer() {
        try {
            // Lấy thông tin từ SecurityContext
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            
            if (username == null || username.equals("anonymousUser")) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            // Tìm khách hàng theo email hoặc tài khoản (vì JWT token có thể chứa email)
            KhachHang customer = customerAuthService.findByEmail(username);
            if (customer == null) {
                customer = customerAuthService.findByTaiKhoan(username);
            }
            if (customer == null) {
                return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng"));
            }
            
            // Trả về customer object trực tiếp (không wrap trong response)
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật thông tin khách hàng
     */
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updateData) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (username == null || username.equals("anonymousUser")) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            KhachHang customer = customerAuthService.findByTaiKhoan(username);
            if (customer == null) {
                return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng"));
            }

            // Cập nhật thông tin
            if (updateData.containsKey("hoTen")) {
                customer.setHoTen((String) updateData.get("hoTen"));
            }
            if (updateData.containsKey("soDienThoai")) {
                String newPhone = (String) updateData.get("soDienThoai");
                // Kiểm tra trùng số điện thoại
                if (newPhone != null && !Objects.equals(customer.getSoDienThoai(), newPhone)
                        && customerAuthService.existsBySoDienThoai(newPhone)) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Số điện thoại đã được sử dụng"));
                }
                customer.setSoDienThoai(newPhone);
            }
            if (updateData.containsKey("email")) {
                String newEmail = (String) updateData.get("email");
                // Kiểm tra trùng email
                if (newEmail != null && !Objects.equals(customer.getEmail(), newEmail)
                        && customerAuthService.existsByEmail(newEmail)) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Email đã được sử dụng"));
                }
                customer.setEmail(newEmail);
            }
            if (updateData.containsKey("gioiTinh")) {
                customer.setGioiTinh((String) updateData.get("gioiTinh"));
            }
            if (updateData.containsKey("ngaySinh")) {
                String ngaySinhStr = (String) updateData.get("ngaySinh");
                if (ngaySinhStr != null && !ngaySinhStr.isEmpty()) {
                    try {
                        java.time.LocalDate ngaySinh = java.time.LocalDate.parse(ngaySinhStr);
                        customer.setNgaySinh(ngaySinh);
                    } catch (Exception e) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Định dạng ngày sinh không hợp lệ"));
                    }
                }
            }

            // Lưu thay đổi
            KhachHang updatedCustomer = customerAuthService.updateCustomer(customer);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật thông tin thành công",
                "customer", updatedCustomer
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách địa chỉ của khách hàng (TẠM THỜI BỎ)
     */
    @GetMapping("/addresses")
    public ResponseEntity<?> getAddresses() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "addresses", new java.util.ArrayList<>()
        ));
    }

    /**
     * Thêm địa chỉ mới (TẠM THỜI BỎ)
     */
    @PostMapping("/addresses")
    public ResponseEntity<?> addAddress(@RequestBody Map<String, Object> addressData) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Chức năng địa chỉ tạm thời bị vô hiệu hóa"
        ));
    }

    /**
     * Cập nhật địa chỉ
     */
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<?> updateAddress(@PathVariable Long addressId, @RequestBody Map<String, Object> addressData) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Chức năng địa chỉ tạm thời bị vô hiệu hóa"
        ));
    }

    /**
     * Xóa địa chỉ
     */
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Chức năng địa chỉ tạm thời bị vô hiệu hóa"
        ));
    }

    /**
     * Đặt địa chỉ làm mặc định
     */
    @PutMapping("/addresses/{addressId}/set-default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long addressId) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Chức năng địa chỉ tạm thời bị vô hiệu hóa"
        ));
    }

    /**
     * Test endpoint đơn giản
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of("message", "Backend is running with new code", "timestamp", System.currentTimeMillis()));
    }

    /**
     * Gửi OTP cho chức năng quên mật khẩu
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email không được để trống"));
            }

            Map<String, Object> result = googleOAuthService.sendForgotPasswordOTP(email);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Xác thực OTP
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

            Map<String, Object> result = googleOAuthService.verifyOTP(email, otp);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Đặt lại mật khẩu với JWT token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String resetToken = request.get("resetToken");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");
            
            if (resetToken == null || resetToken.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Token không được để trống"));
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mật khẩu mới không được để trống"));
            }
            
            if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mật khẩu xác nhận không khớp"));
            }

            Map<String, Object> result = googleOAuthService.resetPassword(resetToken, newPassword);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Xử lý validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.badRequest()
            .body(Map.of("success", false, "message", "Dữ liệu không hợp lệ", "errors", errors));
    }
}
