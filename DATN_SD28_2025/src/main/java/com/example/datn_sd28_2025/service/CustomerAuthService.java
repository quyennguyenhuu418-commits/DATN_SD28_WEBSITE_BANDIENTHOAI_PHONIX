package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.CustomerRegisterRequest;
import com.example.datn_sd28_2025.entity.KhachHang;
import com.example.datn_sd28_2025.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomerAuthService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GoogleOAuthService googleOAuthService;

    /**
     * Xác thực khách hàng bằng email (hoặc tài khoản) và mật khẩu
     */
    public KhachHang authenticate(String emailOrTaiKhoan, String matKhau) {
        // Thử tìm theo email trước (vì email được dùng làm tài khoản)
        KhachHang customer = khachHangRepository.findByEmail(emailOrTaiKhoan);
        
        // Nếu không tìm thấy theo email, thử tìm theo tài khoản
        if (customer == null) {
            customer = khachHangRepository.findByTaiKhoan(emailOrTaiKhoan);
        }
        
        if (customer != null && customer.getMatKhau() != null && passwordEncoder.matches(matKhau, customer.getMatKhau())) {
            return customer;
        }
        
        return null;
    }

    /**
     * Đăng ký khách hàng mới
     */
    public KhachHang register(CustomerRegisterRequest request) {
        KhachHang customer = new KhachHang();
        
        // Tạo mã khách hàng tự động
        String maKhachHang = generateMaKhachHang();
        
        // Set properties manually (without Lombok)
        customer.setMaKhachHang(maKhachHang);
        customer.setHoTen(request.getHoTen());
        customer.setSoDienThoai(request.getSoDienThoai());
        // Dùng email làm tài khoản nếu không có tài khoản được cung cấp
        customer.setTaiKhoan(request.getTaiKhoan() != null && !request.getTaiKhoan().trim().isEmpty() 
            ? request.getTaiKhoan() 
            : request.getEmail());
        customer.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        // Convert String ngaySinh to LocalDate
        LocalDate ngaySinh = null;
        if (request.getNgaySinh() != null && !request.getNgaySinh().trim().isEmpty()) {
            try {
                ngaySinh = LocalDate.parse(request.getNgaySinh(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                // If parsing fails, try other common formats
                try {
                    ngaySinh = LocalDate.parse(request.getNgaySinh(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (Exception e2) {
                    // If all parsing fails, leave as null
                    ngaySinh = null;
                }
            }
        }
        customer.setNgaySinh(ngaySinh);
        customer.setGioiTinh(request.getGioiTinh());
        customer.setEmail(request.getEmail());
        customer.setNgayTao(LocalDateTime.now());
        customer.setTrangThai(1); // 1 = Active
        customer.setNguoiTao("SYSTEM");
        
        return khachHangRepository.save(customer);
    }

    /**
     * Cập nhật thông tin khách hàng
     */
    public KhachHang updateCustomer(KhachHang customer) {
        if (customer == null || customer.getId() == null) {
            throw new IllegalArgumentException("Khách hàng không hợp lệ");
        }
        
        // Cập nhật ngày cập nhật
        customer.setNgayCapNhat(java.time.LocalDateTime.now());
        
        return khachHangRepository.save(customer);
    }

    /**
     * Lưu khách hàng (cho Google OAuth)
     */
    public KhachHang saveCustomer(KhachHang customer) {
        return khachHangRepository.save(customer);
    }

    /**
     * Kiểm tra tài khoản đã tồn tại
     */
    public boolean existsByTaiKhoan(String taiKhoan) {
        if (taiKhoan == null || taiKhoan.trim().isEmpty()) {
            return false;
        }
        return khachHangRepository.findByTaiKhoan(taiKhoan) != null;
    }

    /**
     * Kiểm tra email đã tồn tại
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return khachHangRepository.findByEmail(email) != null;
    }

    /**
     * Kiểm tra số điện thoại đã tồn tại
     */
    public boolean existsBySoDienThoai(String soDienThoai) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            System.out.println("DEBUG: SoDienThoai is null or empty");
            return false;
        }
        
        System.out.println("DEBUG: Searching for phone: '" + soDienThoai + "'");
        
        // Thử query trực tiếp để debug
        try {
            var result = khachHangRepository.findBySoDienThoai(soDienThoai);
            boolean exists = result.isPresent();
            System.out.println("DEBUG: Query result for phone '" + soDienThoai + "': " + result);
            System.out.println("DEBUG: Phone exists: " + exists);
            
            if (exists) {
                var customer = result.get();
                System.out.println("DEBUG: Found customer - ID: " + customer.getId() + 
                                 ", Name: " + customer.getHoTen() + 
                                 ", Phone: " + customer.getSoDienThoai());
            }
            return exists;
        } catch (Exception e) {
            System.out.println("DEBUG: Error checking phone: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tìm khách hàng bằng tài khoản
     */
    public KhachHang findByTaiKhoan(String taiKhoan) {
        return khachHangRepository.findByTaiKhoan(taiKhoan);
    }

    /**
     * Tìm khách hàng bằng email
     */
    public KhachHang findByEmail(String email) {
        return khachHangRepository.findByEmail(email);
    }

    /**
     * Xác thực Google token
     */
    public boolean validateGoogleToken(String googleToken) {
        return googleOAuthService.validateGoogleToken(googleToken);
    }

    /**
     * Lấy thông tin user từ Google
     */
    public Map<String, Object> getGoogleUserInfo(String googleToken) {
        return googleOAuthService.getGoogleUserInfo(googleToken);
    }

    /**
     * Xác thực Google OAuth với code
     */
    public Map<String, Object> authenticateWithGoogleCode(String code) {
        return googleOAuthService.authenticateGoogleUser(code);
    }

    /**
     * Tạo khách hàng mới từ Google
     */
    public KhachHang createFromGoogle(String email, String name, String googleId) {
        KhachHang customer = new KhachHang();
        
        // Tạo mã khách hàng tự động
        String maKhachHang = generateMaKhachHang();
        
        // Set properties manually
        customer.setMaKhachHang(maKhachHang);
        customer.setHoTen(name);
        customer.setTaiKhoan(email); // Sử dụng email làm tài khoản
        customer.setMatKhau(null); // Không có mật khẩu cho tài khoản Google
        customer.setEmail(email);
        customer.setGoogleId(googleId);
        customer.setNgayTao(LocalDateTime.now());
        customer.setTrangThai(1); // 1 = Active
        customer.setNguoiTao("GOOGLE_OAUTH");
        
        return khachHangRepository.save(customer);
    }

    /**
     * Lấy tất cả khách hàng (debug)
     */
    public List<KhachHang> getAllCustomers() {
        return khachHangRepository.findAll();
    }

    /**
     * Tạo mã khách hàng tự động
     */
    private String generateMaKhachHang() {
        // Lấy số lượng khách hàng hiện tại
        long count = khachHangRepository.count();
        return String.format("KH%06d", count + 1);
    }
}