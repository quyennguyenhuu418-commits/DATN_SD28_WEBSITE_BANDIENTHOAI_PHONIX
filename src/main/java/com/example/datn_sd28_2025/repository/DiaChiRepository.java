package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaChiRepository extends JpaRepository<DiaChi, Integer> {
    
    @Query("SELECT d FROM DiaChi d WHERE d.trangThai = 1")
    List<DiaChi> findAllActive();
    
    @Query("SELECT d FROM DiaChi d WHERE d.id = :id AND d.trangThai = 1")
    DiaChi findActiveById(@Param("id") Integer id);
}





