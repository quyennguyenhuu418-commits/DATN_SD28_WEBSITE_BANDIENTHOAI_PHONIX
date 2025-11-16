package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Sim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimRepository extends JpaRepository<Sim, Integer> {
    @Query("SELECT s FROM Sim s WHERE s.trangThai = 1")
    List<Sim> findAllActive();
}
