package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nhan_vien")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(max = 50, message = "Mã nhân viên không được quá 50 ký tự")
    @Column(name = "ma_nhan_vien", unique = true, length = 50)
    private String maNhanVien;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255, message = "Họ tên không được quá 255 ký tự")
    @Column(name = "ho_ten", length = 255, columnDefinition = "NVARCHAR(255)")
    private String hoTen;

    @Size(max = 20, message = "Số căn cước không được quá 20 ký tự")
    @Column(name = "cccd", length = 20, unique = true)
    private String cccd;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @NotBlank(message = "Tài khoản không được để trống")
    @Size(min = 3, max = 100, message = "Tài khoản phải có từ 3-100 ký tự")
    @Column(name = "tai_khoan", length = 100)
    private String taiKhoan;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu phải có từ 6-255 ký tự")
    @Column(name = "mat_khau", length = 255)
    private String matKhau;

    @NotBlank(message = "Giới tính không được để trống")
    @Pattern(regexp = "^(Nam|Nữ)$", message = "Giới tính phải là Nam hoặc Nữ")
    @Column(name = "gioi_tinh", length = 10, columnDefinition = "NVARCHAR(10)")
    private String gioiTinh;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 1000, message = "Địa chỉ không được quá 1000 ký tự")
    @Column(name = "dia_chi", columnDefinition = "NVARCHAR(MAX)")
    private String diaChi;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email không được quá 255 ký tự")
    @Column(name = "email", length = 255)
    private String email;

    @NotBlank(message = "Chức vụ không được để trống")
    @Size(max = 100, message = "Chức vụ không được quá 100 ký tự")
    @Column(name = "chuc_vu", length = 100, columnDefinition = "NVARCHAR(100)")
    private String chucVu;

    @Column(name = "anh_dai_dien", columnDefinition = "NVARCHAR(MAX)")
    private String anhDaiDien;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;
}