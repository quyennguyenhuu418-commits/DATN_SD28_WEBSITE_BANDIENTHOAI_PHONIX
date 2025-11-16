package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.KhachHangGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KhachHangGiamGiaRepository extends JpaRepository<KhachHangGiamGia, Integer> {
    
    @Query("SELECT khgg FROM KhachHangGiamGia khgg LEFT JOIN FETCH khgg.khachHang LEFT JOIN FETCH khgg.phieuGiamGia WHERE khgg.phieuGiamGia.id = :phieuGiamGiaId")
    List<KhachHangGiamGia> findByPhieuGiamGiaId(@Param("phieuGiamGiaId") Integer phieuGiamGiaId);
    
    @Query("SELECT khgg FROM KhachHangGiamGia khgg WHERE khgg.khachHang.id = :khachHangId")
    List<KhachHangGiamGia> findByKhachHangId(@Param("khachHangId") Integer khachHangId);
    
    @Query("SELECT khgg FROM KhachHangGiamGia khgg WHERE khgg.phieuGiamGia.id = :phieuGiamGiaId AND khgg.khachHang.id = :khachHangId")
    KhachHangGiamGia findByPhieuGiamGiaIdAndKhachHangId(@Param("phieuGiamGiaId") Integer phieuGiamGiaId, @Param("khachHangId") Integer khachHangId);
    
    @Modifying
    @Query("DELETE FROM KhachHangGiamGia khgg WHERE khgg.phieuGiamGia.id = :phieuGiamGiaId")
    void deleteByPhieuGiamGiaId(@Param("phieuGiamGiaId") Integer phieuGiamGiaId);
    
    // Tạm thời comment các method sử dụng field daSuDung
    // @Query("SELECT khgg FROM KhachHangGiamGia khgg WHERE khgg.phieuGiamGia.id = :phieuGiamGiaId AND khgg.khachHang.id = :khachHangId AND khgg.daSuDung = true")
    // KhachHangGiamGia findUsedVoucherByCustomer(@Param("phieuGiamGiaId") Integer phieuGiamGiaId, @Param("khachHangId") Integer khachHangId);

    // @Query("SELECT COUNT(khgg) FROM KhachHangGiamGia khgg WHERE khgg.phieuGiamGia.id = :phieuGiamGiaId AND khgg.daSuDung = true")
    // Long countUsedVouchers(@Param("phieuGiamGiaId") Integer phieuGiamGiaId);
}
