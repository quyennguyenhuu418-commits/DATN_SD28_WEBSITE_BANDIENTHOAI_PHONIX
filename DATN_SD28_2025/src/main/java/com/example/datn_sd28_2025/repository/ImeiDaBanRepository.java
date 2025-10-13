package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.ImeiDaBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImeiDaBanRepository extends JpaRepository<ImeiDaBan, Integer> {
    
    @Query("SELECT i FROM ImeiDaBan i WHERE i.hoaDonChiTiet.id = :idHoaDonChiTiet")
    List<ImeiDaBan> findByIdHoaDonChiTiet(@Param("idHoaDonChiTiet") Integer idHoaDonChiTiet);
    
    @Query("SELECT i FROM ImeiDaBan i WHERE i.imei = :imei")
    ImeiDaBan findByImei(@Param("imei") String imei);
    
    @Query("SELECT i FROM ImeiDaBan i WHERE i.trangThai = 1")
    List<ImeiDaBan> findAllActive();
}
