package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.config.GoogleOAuthConfig;
import com.example.datn_sd28_2025.entity.KhachHang;
import com.example.datn_sd28_2025.repository.KhachHangRepository;
import com.example.datn_sd28_2025.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class GoogleOAuthService {

    @Autowired
    private GoogleOAuthConfig googleOAuthConfig;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Lưu trữ OTP tạm thời (email -> OTP và thời gian hết hạn)
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
     * Xác thực Google token và lấy thông tin người dùng
     */
    public Map<String, Object> authenticateGoogleUser(String code) {
        try {
            // 1. Trao đổi code để lấy access token
            String accessToken = getAccessToken(code);
            if (accessToken == null) {
                throw new RuntimeException("Không thể lấy access token từ Google");
            }

            // 2. Lấy thông tin người dùng từ Google
            Map<String, Object> userInfo = getUserInfoFromGoogle(accessToken);
            if (userInfo == null) {
                throw new RuntimeException("Không thể lấy thông tin người dùng từ Google");
            }

            // 3. Tìm hoặc tạo tài khoản khách hàng
            KhachHang customer = findOrCreateCustomer(userInfo);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("customer", customer);
            result.put("userInfo", userInfo);
            result.put("accessToken", accessToken);

            return result;

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName() + ": Lỗi không xác định";
            error.put("error", errorMessage);
            e.printStackTrace(); // Log for debugging
            return error;
        }
    }

    /**
     * Lấy access token từ Google (log chi tiết thông số)
     */
    private String getAccessToken(String code) {
        try {
            String tokenUrl = "https://oauth2.googleapis.com/token";

            String clientId = googleOAuthConfig.getClientId();
            String clientSecret = googleOAuthConfig.getClientSecret();
            String redirectUri = "http://localhost:5173/google-callback"; // Luôn dùng frontend URL

            System.out.println("GOOGLE OAUTH - CODE: " + code);
            System.out.println("GOOGLE OAUTH - CLIENT_ID: " + clientId);
            System.out.println("GOOGLE OAUTH - CLIENT_SECRET: " + clientSecret);
            System.out.println("GOOGLE OAUTH - REDIRECT_URI: " + redirectUri);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("code", code);
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("redirect_uri", redirectUri);
            body.add("grant_type", "authorization_code");

            System.out.println("GOOGLE OAUTH - REQUEST BODY: " + body.toString());
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {});

            System.out.println("GOOGLE OAUTH - TOKEN RESPONSE: " + response);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.get("access_token") != null) {
                return (String) responseBody.get("access_token");
            }
            return null;
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            System.out.println("GOOGLE OAUTH - ERROR: " + ex.getMessage());
            String responseBody = ex.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isBlank()) {
                System.out.println("GOOGLE OAUTH - ERROR BODY: " + responseBody);
            }
            return null;
        } catch (Exception e) {
            System.out.println("GOOGLE OAUTH - ERROR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy thông tin người dùng từ Google
     */
    private Map<String, Object> getUserInfoFromGoogle(String accessToken) {
        try {
            String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    userInfoUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>() {});

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tìm hoặc tạo tài khoản khách hàng
     */
    private KhachHang findOrCreateCustomer(Map<String, Object> userInfo) {
        String googleId = (String) userInfo.get("id");
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");

        // Tìm khách hàng theo Google ID
        KhachHang existingCustomer = khachHangRepository.findByGoogleId(googleId);
        if (existingCustomer != null) {
            return existingCustomer;
        }

        // Tìm khách hàng theo email
        KhachHang customerByEmail = khachHangRepository.findByEmail(email);
        if (customerByEmail != null) {
            // Cập nhật Google ID cho khách hàng hiện có
            if (customerByEmail.getId() != null) {
                KhachHang updatedCustomer = KhachHang.builder()
                        .id(customerByEmail.getId())
                        .maKhachHang(customerByEmail.getMaKhachHang())
                        .hoTen(customerByEmail.getHoTen())
                        .soDienThoai(customerByEmail.getSoDienThoai())
                        .taiKhoan(customerByEmail.getTaiKhoan())
                        .matKhau(customerByEmail.getMatKhau())
                        .ngaySinh(customerByEmail.getNgaySinh())
                        .gioiTinh(customerByEmail.getGioiTinh())
                        .email(customerByEmail.getEmail())
                        .googleId(googleId) // Cập nhật Google ID
                        .diaChi(customerByEmail.getDiaChi())
                        .ngayTao(customerByEmail.getNgayTao())
                        .ngayCapNhat(LocalDateTime.now()) // Cập nhật thời gian
                        .trangThai(customerByEmail.getTrangThai())
                        .nguoiTao(customerByEmail.getNguoiTao())
                        .nguoiCapNhat("GOOGLE_OAUTH")
                        .build();
                return khachHangRepository.save(updatedCustomer);
            }
            // Fallback: try to set directly if builder fails
            customerByEmail.setGoogleId(googleId);
            customerByEmail.setNgayCapNhat(LocalDateTime.now());
            return khachHangRepository.save(customerByEmail);
        }

        // Tạo khách hàng mới
        String maKhachHang = "KH" + System.currentTimeMillis();
        String taiKhoan = email != null ? email : "google_" + googleId;

        KhachHang newCustomer = KhachHang.builder()
                .maKhachHang(maKhachHang)
                .hoTen(name != null ? name : "Khách hàng Google")
                .email(email)
                .googleId(googleId)
                .taiKhoan(taiKhoan)
                .matKhau(null) // Không có mật khẩu cho tài khoản Google
                .trangThai(1) // Hoạt động
                .ngayTao(LocalDateTime.now())
                .ngayCapNhat(LocalDateTime.now())
                .nguoiTao("GOOGLE_OAUTH")
                .nguoiCapNhat("GOOGLE_OAUTH")
                .build();

        return khachHangRepository.save(newCustomer);
    }

    /**
     * Xác thực Google token (cho trường hợp frontend gửi token trực tiếp)
     */
    public boolean validateGoogleToken(String token) {
        try {
            Map<String, Object> userInfo = getUserInfoFromGoogle(token);
            return userInfo != null && userInfo.get("id") != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lấy thông tin người dùng từ Google token
     */
    public Map<String, Object> getGoogleUserInfo(String token) {
        return getUserInfoFromGoogle(token);
    }

    /**
     * Lấy Client ID
     */
    public String getClientId() {
        return googleOAuthConfig.getClientId();
    }

    /**
     * Lấy Redirect URI (luôn trả về frontend URL)
     */
    public String getRedirectUri() {
        return "http://localhost:5173/google-callback";
    }

    /**
     * Gửi OTP về email cho chức năng quên mật khẩu
     * @param email Email của khách hàng
     * @return Map chứa kết quả (success, message)
     */
    public Map<String, Object> sendForgotPasswordOTP(String email) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra email có tồn tại không
            KhachHang customer = khachHangRepository.findByEmail(email);
            if (customer == null) {
                result.put("success", false);
                result.put("message", "Email không tồn tại trong hệ thống");
                return result;
            }

            // Kiểm tra tài khoản có mật khẩu không (tài khoản Google không có mật khẩu)
            if (customer.getMatKhau() == null || customer.getMatKhau().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Tài khoản này được đăng nhập bằng Google. Vui lòng sử dụng chức năng đăng nhập Google.");
                return result;
            }

            // Tạo OTP 5 chữ số
            String otp = generateOTP();
            
            // Tạo JWT token cho reset password
            String resetToken = jwtUtil.generatePasswordResetToken(email);
            
            // Lưu OTP vào cache (hiệu lực 5 phút)
            long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 phút
            otpStorage.put(email, new OtpInfo(otp, expiryTime, resetToken));
            
            // Gửi email OTP
            emailService.sendOtpEmail(email, otp);
            
            result.put("success", true);
            result.put("message", "Mã OTP đã được gửi đến email của bạn");
            result.put("resetToken", resetToken); // Trả về token để frontend có thể dùng sau khi verify OTP
            
            return result;
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    /**
     * Xác thực OTP
     * @param email Email của khách hàng
     * @param otp Mã OTP cần xác thực
     * @return Map chứa kết quả (success, message, resetToken)
     */
    public Map<String, Object> verifyOTP(String email, String otp) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Kiểm tra email có trong cache không
            OtpInfo otpInfo = otpStorage.get(email);
            if (otpInfo == null) {
                result.put("success", false);
                result.put("message", "Mã OTP không hợp lệ hoặc đã hết hạn. Vui lòng gửi lại mã OTP.");
                return result;
            }

            // Kiểm tra OTP đã hết hạn chưa
            if (otpInfo.isExpired()) {
                otpStorage.remove(email);
                result.put("success", false);
                result.put("message", "Mã OTP đã hết hạn. Vui lòng gửi lại mã OTP.");
                return result;
            }

            // Kiểm tra OTP có đúng không
            if (!otpInfo.otp.equals(otp)) {
                result.put("success", false);
                result.put("message", "Mã OTP không đúng. Vui lòng thử lại.");
                return result;
            }

            // OTP hợp lệ, xóa khỏi cache và trả về reset token
            String resetToken = otpInfo.resetToken;
            otpStorage.remove(email);
            
            result.put("success", true);
            result.put("message", "Xác thực OTP thành công");
            result.put("resetToken", resetToken);
            
            return result;
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    /**
     * Đặt lại mật khẩu với JWT token
     * @param resetToken JWT token từ bước verify OTP
     * @param newPassword Mật khẩu mới
     * @return Map chứa kết quả (success, message)
     */
    public Map<String, Object> resetPassword(String resetToken, String newPassword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Xác thực token
            if (!jwtUtil.validatePasswordResetToken(resetToken)) {
                result.put("success", false);
                result.put("message", "Token không hợp lệ hoặc đã hết hạn. Vui lòng thực hiện lại quy trình quên mật khẩu.");
                return result;
            }

            // Lấy email từ token
            String email = jwtUtil.extractUsername(resetToken);
            
            // Tìm khách hàng theo email
            KhachHang customer = khachHangRepository.findByEmail(email);
            if (customer == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy tài khoản");
                return result;
            }

            // Kiểm tra mật khẩu mới không được rỗng
            if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 6) {
                result.put("success", false);
                result.put("message", "Mật khẩu phải có ít nhất 6 ký tự");
                return result;
            }

            // Mã hóa mật khẩu mới
            String encodedPassword = passwordEncoder.encode(newPassword);
            
            // Cập nhật mật khẩu
            customer.setMatKhau(encodedPassword);
            customer.setNgayCapNhat(LocalDateTime.now());
            customer.setNguoiCapNhat("PASSWORD_RESET");
            
            khachHangRepository.save(customer);
            
            // Gửi email thông báo đặt lại mật khẩu thành công
            try {
                emailService.sendPasswordResetSuccessEmail(email, customer.getHoTen());
            } catch (Exception e) {
                System.err.println("Không thể gửi email thông báo: " + e.getMessage());
            }
            
            result.put("success", true);
            result.put("message", "Đặt lại mật khẩu thành công");
            
            return result;
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    /**
     * Tạo mã OTP ngẫu nhiên 5 chữ số
     */
    private String generateOTP() {
        Random random = new Random();
        int otp = 10000 + random.nextInt(90000); // Tạo số từ 10000 đến 99999
        return String.valueOf(otp);
    }

    /**
     * Xóa OTP đã hết hạn (cleanup task có thể gọi định kỳ)
     */
    public void cleanupExpiredOTPs() {
        otpStorage.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}