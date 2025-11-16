package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Ca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface CaRepository extends JpaRepository<Ca, Integer> {
    
    // Check if maCa already exists
    boolean existsByMaCa(String maCa);
    
    // Check if maCa exists for another ca (for update operations)
    boolean existsByMaCaAndIdNot(String maCa, Integer id);
    
    // Find active ca ordered by start time
    @Query("SELECT c FROM Ca c WHERE c.trangThai = 1 ORDER BY c.gioBatDau ASC")
    List<Ca> findActiveCaOrderByGioBatDau();
    
    // Find current active ca based on current time
    @Query("SELECT c FROM Ca c WHERE c.trangThai = 1 AND c.gioBatDau <= :currentTime AND c.gioKetThuc >= :currentTime ORDER BY c.gioBatDau ASC")
    List<Ca> findCurrentActiveCa(@Param("currentTime") LocalTime currentTime);
    
    // Search by tenCa containing and trangThai
    List<Ca> findByTenCaContainingAndTrangThai(String tenCa, Integer trangThai);
}
