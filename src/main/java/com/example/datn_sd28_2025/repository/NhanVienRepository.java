package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    
    @Query("SELECT n FROM NhanVien n WHERE n.maNhanVien = :maNhanVien")
    Optional<NhanVien> findByMaNhanVien(@Param("maNhanVien") String maNhanVien);
    
    @Query("SELECT n FROM NhanVien n WHERE n.taiKhoan = :taiKhoan")
    Optional<NhanVien> findByTaiKhoan(@Param("taiKhoan") String taiKhoan);
    
    @Query("SELECT n FROM NhanVien n WHERE n.email = :email")
    Optional<NhanVien> findByEmail(@Param("email") String email);
    
    @Query("SELECT n FROM NhanVien n WHERE n.soDienThoai = :soDienThoai")
    Optional<NhanVien> findBySoDienThoai(@Param("soDienThoai") String soDienThoai);
    
    @Query("SELECT n FROM NhanVien n WHERE n.trangThai = 1")
    List<NhanVien> findAllActive();
    
    @Query("SELECT n FROM NhanVien n WHERE n.hoTen LIKE %:ten%")
    List<NhanVien> findByHoTenContaining(@Param("ten") String ten);
    
    @Query("SELECT n FROM NhanVien n WHERE n.chucVu = :chucVu")
    List<NhanVien> findByChucVu(@Param("chucVu") String chucVu);
<<<<<<< HEAD
    
    @Query("SELECT n FROM NhanVien n WHERE n.cccd = :cccd")
    Optional<NhanVien> findByCccd(@Param("cccd") String cccd);
    
    @Query("SELECT COUNT(n) > 0 FROM NhanVien n WHERE n.cccd = :cccd")
    boolean existsByCccd(@Param("cccd") String cccd);
    
    @Query(value = "SELECT ma_nhan_vien FROM nhan_vien WHERE ma_nhan_vien LIKE 'NV%' AND ISNUMERIC(SUBSTRING(ma_nhan_vien, 3, LEN(ma_nhan_vien))) = 1 ORDER BY CAST(SUBSTRING(ma_nhan_vien, 3, LEN(ma_nhan_vien)) AS BIGINT) DESC", nativeQuery = true)
    List<String> findAllMaNhanVienOrderByNumber();
}
=======
}
>>>>>>> origin/Huan
