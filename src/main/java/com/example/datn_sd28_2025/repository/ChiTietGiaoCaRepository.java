package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.ChiTietGiaoCa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietGiaoCaRepository extends JpaRepository<ChiTietGiaoCa, Integer> {
    
    List<ChiTietGiaoCa> findByGiaoCaId(Integer giaoCaId);
    
    List<ChiTietGiaoCa> findByLoaiChiTiet(String loaiChiTiet);
    
    List<ChiTietGiaoCa> findByGiaoCaIdAndLoaiChiTiet(Integer giaoCaId, String loaiChiTiet);
    
    List<ChiTietGiaoCa> findByTrangThai(Integer trangThai);
    
    @Query("SELECT ctgc FROM ChiTietGiaoCa ctgc WHERE ctgc.giaoCaId = :giaoCaId AND ctgc.trangThai = 1 ORDER BY ctgc.thoiGian DESC")
    List<ChiTietGiaoCa> findActiveByGiaoCaId(@Param("giaoCaId") Integer giaoCaId);
    
    @Query("SELECT ctgc FROM ChiTietGiaoCa ctgc WHERE ctgc.giaoCaId = :giaoCaId AND ctgc.loaiChiTiet = :loaiChiTiet AND ctgc.trangThai = 1 ORDER BY ctgc.thoiGian DESC")
    List<ChiTietGiaoCa> findActiveByGiaoCaIdAndLoaiChiTiet(@Param("giaoCaId") Integer giaoCaId, 
                                                           @Param("loaiChiTiet") String loaiChiTiet);
    
    @Query("SELECT ctgc FROM ChiTietGiaoCa ctgc WHERE ctgc.giaoCa.phanCa.nhanVienId = :nhanVienId AND ctgc.loaiChiTiet = :loaiChiTiet ORDER BY ctgc.thoiGian DESC")
    List<ChiTietGiaoCa> findByNhanVienIdAndLoaiChiTiet(@Param("nhanVienId") Integer nhanVienId, 
                                                       @Param("loaiChiTiet") String loaiChiTiet);
    
    @Query("SELECT ctgc FROM ChiTietGiaoCa ctgc WHERE ctgc.giaoCa.phanCa.caId = :caId AND ctgc.loaiChiTiet = :loaiChiTiet ORDER BY ctgc.thoiGian DESC")
    List<ChiTietGiaoCa> findByCaIdAndLoaiChiTiet(@Param("caId") Integer caId, 
                                                 @Param("loaiChiTiet") String loaiChiTiet);
    
    @Query("SELECT SUM(ctgc.giaTri) FROM ChiTietGiaoCa ctgc WHERE ctgc.giaoCaId = :giaoCaId AND ctgc.loaiChiTiet = :loaiChiTiet AND ctgc.trangThai = 1")
    java.math.BigDecimal getTotalValueByGiaoCaIdAndLoaiChiTiet(@Param("giaoCaId") Integer giaoCaId, 
                                                              @Param("loaiChiTiet") String loaiChiTiet);
    
    @Query("SELECT SUM(ctgc.soLuong) FROM ChiTietGiaoCa ctgc WHERE ctgc.giaoCaId = :giaoCaId AND ctgc.loaiChiTiet = :loaiChiTiet AND ctgc.trangThai = 1")
    Long getTotalQuantityByGiaoCaIdAndLoaiChiTiet(@Param("giaoCaId") Integer giaoCaId, 
                                                  @Param("loaiChiTiet") String loaiChiTiet);
}

























