package com.example.datn_sd28_2025.config;

import com.example.datn_sd28_2025.entity.NhanVien;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class JwtUserDetails implements UserDetails {

    private String username;
    private String password;
    private String role;
    private Integer id;
    private String hoTen;
    private String email;
    private Integer trangThai;
    private boolean enabled;

    public JwtUserDetails(NhanVien nhanVien) {
        this.username = nhanVien.getTaiKhoan() != null ? nhanVien.getTaiKhoan() : 
                       (nhanVien.getEmail() != null ? nhanVien.getEmail() : String.valueOf(nhanVien.getId()));
        this.password = nhanVien.getMatKhau();
        this.role = determineRoleByPosition(nhanVien.getChucVu());
        this.id = nhanVien.getId();
        this.hoTen = nhanVien.getHoTen();
        this.email = nhanVien.getEmail();
        this.trangThai = nhanVien.getTrangThai();
        this.enabled = nhanVien.getTrangThai() != null && nhanVien.getTrangThai() == 1;
    }
    
    private String determineRoleByPosition(String chucVu) {
        if (chucVu == null || chucVu.trim().isEmpty()) {
            return "STAFF";
        }
        
        String chucVuLower = chucVu.toLowerCase().trim();
        
        // Admin positions
        if (chucVuLower.contains("quản trị") || chucVuLower.contains("admin") || chucVuLower.contains("administrator")) {
            return "ADMIN";
        }
        
        // Manager positions
        if (chucVuLower.contains("quản lý") || chucVuLower.contains("manager") || 
            chucVuLower.contains("giám đốc") || chucVuLower.contains("director") || 
            chucVuLower.contains("trưởng phòng")) {
            return "MANAGER";
        }
        
        // Staff positions
        return "STAFF";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert chucVu to role format
        String roleName = "ROLE_" + role.toUpperCase();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // Getters for additional user info
    public Integer getId() {
        return id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Integer getTrangThai() {
        return trangThai;
    }
}
