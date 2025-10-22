package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.CameraSau;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CameraSauRepository extends JpaRepository<CameraSau, Integer> {
    Optional<CameraSau> findByMaCamera(String maCamera);
    
    @Query("SELECT c FROM CameraSau c WHERE c.trangThai = 1")
    List<CameraSau> findAllActive();
    
    @Query("SELECT c FROM CameraSau c WHERE c.trangThai = 1")
    Page<CameraSau> findAllActive(Pageable pageable);
    
    @Query("SELECT c FROM CameraSau c WHERE c.thongSo LIKE %:thongSo%")
    List<CameraSau> findByThongSoContaining(@Param("thongSo") String thongSo);
    
    @Query("SELECT c FROM CameraSau c WHERE c.maCamera LIKE %:ma%")
    List<CameraSau> findByMaCameraContaining(@Param("ma") String ma);
}
