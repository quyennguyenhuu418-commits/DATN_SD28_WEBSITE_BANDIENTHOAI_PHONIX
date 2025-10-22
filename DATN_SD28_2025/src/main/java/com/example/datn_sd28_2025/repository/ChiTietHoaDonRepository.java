package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.ChiTietHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Integer> {
    
    List<ChiTietHoaDon> findByHoaDonId(Integer hoaDonId);
    
    List<ChiTietHoaDon> findBySanPhamId(Integer sanPhamId);
    
    @Query("SELECT c FROM ChiTietHoaDon c WHERE c.hoaDon.id = :hoaDonId")
    List<ChiTietHoaDon> findChiTietByHoaDonId(@Param("hoaDonId") Integer hoaDonId);
    
    @Query("SELECT SUM(c.soLuong) FROM ChiTietHoaDon c WHERE c.sanPhamId = :sanPhamId AND c.hoaDon.trangThai = 1")
    Integer sumSoLuongBySanPhamId(@Param("sanPhamId") Integer sanPhamId);
    
    // Note: ChiTietHoaDon doesn't have chiTietSanPham relationship, using HoaDonCt instead
    
    void deleteByHoaDonId(Integer hoaDonId);
}




