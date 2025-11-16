package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.entity.NhanVien;
import com.example.datn_sd28_2025.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean updatePassword(String username, String newPassword) {
        try {
            Optional<NhanVien> userOpt = nhanVienRepository.findByTaiKhoan(username);
            if (userOpt.isEmpty()) {
                // Try to find by email
                userOpt = nhanVienRepository.findByEmail(username);
            }
            
            if (userOpt.isPresent()) {
                NhanVien user = userOpt.get();
                // Sử dụng {noop} prefix cho plain text password
                String plainPassword = "{noop}" + newPassword;
                user.setMatKhau(plainPassword);
                nhanVienRepository.save(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
