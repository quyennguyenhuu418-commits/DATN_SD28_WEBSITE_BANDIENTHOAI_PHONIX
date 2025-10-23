package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {
    Optional<ChiTietSanPham> findByMaCtsp(String maCtsp);
    boolean existsByMaCtsp(String maCtsp);
    
    List<ChiTietSanPham> findBySanPham(SanPham sanPham);
    
<<<<<<< HEAD
    @Query("SELECT c FROM ChiTietSanPham c LEFT JOIN FETCH c.ram LEFT JOIN FETCH c.rom LEFT JOIN FETCH c.mauSac WHERE c.sanPham.id = :sanPhamId AND c.trangThai = 1")
=======
    @Query("SELECT c FROM ChiTietSanPham c WHERE c.sanPham.id = :sanPhamId AND c.trangThai = 1")
>>>>>>> origin/Huan
    List<ChiTietSanPham> findBySanPhamIdAndTrangThaiTrue(@Param("sanPhamId") Integer sanPhamId);
    
    @Query("SELECT c FROM ChiTietSanPham c WHERE c.trangThai = 1")
    List<ChiTietSanPham> findAvailableProducts();
    
    @Modifying
    @Query("DELETE FROM ChiTietSanPham c WHERE c.maCtsp IS NULL")
    int deleteByMaCtspIsNull();
<<<<<<< HEAD
    
    // Methods for POS
    List<ChiTietSanPham> findByTrangThaiAndSoLuongGreaterThan(Integer trangThai, Integer soLuong);
    
    @Query("SELECT c FROM ChiTietSanPham c WHERE c.sanPham.hang.id = :hangId AND c.trangThai = :trangThai AND c.soLuong > :soLuong")
    List<ChiTietSanPham> findBySanPhamHangIdAndTrangThaiAndSoLuongGreaterThan(@Param("hangId") Integer hangId, 
                                                                              @Param("trangThai") Integer trangThai, 
                                                                              @Param("soLuong") Integer soLuong);
    
    @Query("SELECT c FROM ChiTietSanPham c WHERE " +
           "(c.sanPham.tenSanPham LIKE %:keyword% OR c.maCtsp LIKE %:keyword%) " +
           "AND c.trangThai = 1 AND c.soLuong > 0")
    List<ChiTietSanPham> searchByKeyword(@Param("keyword") String keyword);
    
    // Statistics methods
    long countBySoLuongGreaterThan(Integer soLuong);
=======
>>>>>>> origin/Huan
}
