package com.example.datn_sd28_2025.dto;

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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVienDTO {
    private Integer id;
<<<<<<< HEAD

    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(max = 50, message = "Mã nhân viên không được quá 50 ký tự")
    private String maNhanVien;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255, message = "Họ tên không được quá 255 ký tự")
    private String hoTen;

    @Size(max = 20, message = "Số căn cước không được quá 20 ký tự")
    private String cccd;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String soDienThoai;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate ngaySinh;

    @NotBlank(message = "Tài khoản không được để trống")
    @Size(min = 3, max = 100, message = "Tài khoản phải có từ 3-100 ký tự")
    private String taiKhoan;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu phải có từ 6-255 ký tự")
    private String matKhau;

    @NotBlank(message = "Giới tính không được để trống")
    @Pattern(regexp = "^(Nam|Nữ)$", message = "Giới tính phải là Nam hoặc Nữ")
    private String gioiTinh;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 1000, message = "Địa chỉ không được quá 1000 ký tự")
    private String diaChi;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email không được quá 255 ký tự")
    private String email;

    @NotBlank(message = "Chức vụ không được để trống")
    @Size(max = 100, message = "Chức vụ không được quá 100 ký tự")
    private String chucVu;

=======
    private String maNhanVien;
    private String hoTen;
    private String soDienThoai;
    private LocalDate ngaySinh;
    private String taiKhoan;
    private String matKhau;
    private String gioiTinh;
    private String diaChi;
    private String email;
    private String chucVu;
>>>>>>> origin/Huan
    private String anhDaiDien;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer trangThai;
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/Huan
