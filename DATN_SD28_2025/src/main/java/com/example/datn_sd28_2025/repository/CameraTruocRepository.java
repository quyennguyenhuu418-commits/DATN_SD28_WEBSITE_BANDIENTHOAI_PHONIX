package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.CameraTruoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CameraTruocRepository extends JpaRepository<CameraTruoc, Integer> {
    Optional<CameraTruoc> findByMaCamera(String maCamera);
    
    @Query("SELECT c FROM CameraTruoc c WHERE c.trangThai = 1")
    List<CameraTruoc> findAllActive();
    
    @Query("SELECT c FROM CameraTruoc c WHERE c.trangThai = 1")
    Page<CameraTruoc> findAllActive(Pageable pageable);
    
    @Query("SELECT c FROM CameraTruoc c WHERE c.thongSo LIKE %:thongSo%")
    List<CameraTruoc> findByThongSoContaining(@Param("thongSo") String thongSo);
    
    @Query("SELECT c FROM CameraTruoc c WHERE c.maCamera LIKE %:ma%")
    List<CameraTruoc> findByMaCameraContaining(@Param("ma") String ma);
}
