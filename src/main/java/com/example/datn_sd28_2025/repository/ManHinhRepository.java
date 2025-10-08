package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.ManHinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManHinhRepository extends JpaRepository<ManHinh, Integer> {
    Optional<ManHinh> findByMaManHinh(String maManHinh);
    
    @Query("SELECT m FROM ManHinh m WHERE m.trangThai = 1")
    List<ManHinh> findAllActive();
}
