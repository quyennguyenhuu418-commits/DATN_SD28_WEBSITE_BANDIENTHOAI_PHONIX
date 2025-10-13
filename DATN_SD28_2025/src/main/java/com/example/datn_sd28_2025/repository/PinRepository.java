package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Pin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PinRepository extends JpaRepository<Pin, Integer> {
    Optional<Pin> findByMaPin(String maPin);
    List<Pin> findByTrangThai(Integer trangThai);
    Page<Pin> findByTrangThai(Integer trangThai, Pageable pageable);
}
