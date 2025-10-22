package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.config.JwtUserDetails;
import com.example.datn_sd28_2025.entity.NhanVien;
import com.example.datn_sd28_2025.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<NhanVien> nhanVien;
        
        // Try to find by email first, then by taiKhoan
        if (username.contains("@")) {
            nhanVien = nhanVienRepository.findByEmail(username);
        } else {
            nhanVien = nhanVienRepository.findByTaiKhoan(username);
        }
        
        if (nhanVien.isEmpty()) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng với tài khoản: " + username);
        }

        NhanVien user = nhanVien.get();
        
        // Check if user is active
        if (user.getTrangThai() == null || user.getTrangThai() != 1) {
            throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa: " + username);
        }

        return new JwtUserDetails(user);
    }
}
