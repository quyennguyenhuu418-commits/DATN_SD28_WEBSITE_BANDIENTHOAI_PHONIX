package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Gpu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GpuRepository extends JpaRepository<Gpu, Integer> {
    Optional<Gpu> findByMaGpu(String maGpu);
    
    @Query("SELECT g FROM Gpu g WHERE g.trangThai = 1")
    List<Gpu> findAllActive();
    
    @Query("SELECT g FROM Gpu g WHERE g.trangThai = 1")
    Page<Gpu> findAllActive(Pageable pageable);
    
    @Query("SELECT g FROM Gpu g WHERE g.tenGpu LIKE %:ten%")
    List<Gpu> findByTenGpuContaining(@Param("ten") String ten);
    
    @Query("SELECT g FROM Gpu g WHERE g.maGpu LIKE %:ma%")
    List<Gpu> findByMaGpuContaining(@Param("ma") String ma);
}
