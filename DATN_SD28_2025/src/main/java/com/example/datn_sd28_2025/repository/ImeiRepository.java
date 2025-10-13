package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Imei;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImeiRepository extends JpaRepository<Imei, Integer> {
    Optional<Imei> findByImei(String imei);

    @Query("SELECT i FROM Imei i WHERE i.chiTietSanPham.id = :chiTietId AND (:status IS NULL OR i.trangThai = :status)")
    List<Imei> findByChiTietAndStatus(@Param("chiTietId") Integer chiTietId, @Param("status") Integer status);

    @Query("SELECT COUNT(i) FROM Imei i WHERE i.chiTietSanPham.id = :chiTietId AND i.trangThai = 1")
    long countAvailableByChiTiet(@Param("chiTietId") Integer chiTietId);

    List<Imei> findByChiTietSanPhamId(Integer chiTietSanPhamId);

    @Query("SELECT i.imei FROM Imei i WHERE i.imei IN :imeis")
    List<String> findExistingImeis(@Param("imeis") List<String> imeis);
}


