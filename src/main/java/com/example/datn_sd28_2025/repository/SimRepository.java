package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Sim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimRepository extends JpaRepository<Sim, Integer> {
}
