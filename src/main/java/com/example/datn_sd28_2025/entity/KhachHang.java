package com.example.datn_sd28_2025.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
<<<<<<< HEAD
import jakarta.validation.constraints.*;
=======
>>>>>>> origin/Huan
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "khach_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_khach_hang", unique = true, length = 50)
    private String maKhachHang;

<<<<<<< HEAD
    @Column(name = "ho_ten", nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 255, message = "Họ tên phải có từ 2 đến 255 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]+$", 
             message = "Họ tên chỉ được chứa chữ cái và khoảng trắng")
    private String hoTen;

    @Column(name = "so_dien_thoai", length = 15)
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại phải bắt đầu bằng 0 và có 10 chữ số")
    private String soDienThoai;


    @Column(name = "ngay_sinh")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh", length = 10, columnDefinition = "NVARCHAR(10)")
    @Pattern(regexp = "^(Nam|Nữ|Khác|)$", message = "Giới tính phải là Nam, Nữ hoặc Khác")
    private String gioiTinh;

    @Column(name = "email", length = 255)
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được quá 255 ký tự")
    private String email;

    // Address information is now managed through user_dia_chi table
    // No direct dia_chi field in khach_hang table
    
    @Column(name = "tai_khoan", length = 100)
    private String taiKhoan;
    
    @Column(name = "mat_khau", length = 255)
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu phải có ít nhất 6 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,}$", 
             message = "Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ hoa, chữ thường và số")
    private String matKhau;

=======
    @Column(name = "ho_ten", nullable = false, length = 255)
    private String hoTen;

    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Column(name = "tai_khoan", length = 100)
    private String taiKhoan;

    @Column(name = "mat_khau", length = 255)
    private String matKhau;


    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @Column(name = "email", length = 255)
    private String email;

>>>>>>> origin/Huan
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "nguoi_tao", length = 255)
    private String nguoiTao;

    @Column(name = "nguoi_cap_nhat", length = 255)
    private String nguoiCapNhat;

<<<<<<< HEAD
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
=======
    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
>>>>>>> origin/Huan
    @JsonIgnore
    private List<HoaDon> hoaDons;




    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<KhachHangGiamGia> khachHangGiamGias;
<<<<<<< HEAD

    // Relationship with UserDiaChi for address management
    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserDiaChi> userDiaChis;
=======
>>>>>>> origin/Huan
}
