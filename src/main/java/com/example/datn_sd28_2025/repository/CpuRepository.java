package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Cpu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CpuRepository extends JpaRepository<Cpu, Integer> {
    Optional<Cpu> findByMaCpu(String maCpu);
    
    @Query("SELECT c FROM Cpu c WHERE c.trangThai = 1")
    List<Cpu> findAllActive();
    
    @Query("SELECT c FROM Cpu c WHERE c.trangThai = 1")
    Page<Cpu> findAllActive(Pageable pageable);
    
    @Query("SELECT c FROM Cpu c WHERE c.tenCpu LIKE %:ten%")
    List<Cpu> findByTenCpuContaining(@Param("ten") String ten);
    
    @Query("SELECT c FROM Cpu c WHERE c.maCpu LIKE %:ma%")
    List<Cpu> findByMaCpuContaining(@Param("ma") String ma);
}
