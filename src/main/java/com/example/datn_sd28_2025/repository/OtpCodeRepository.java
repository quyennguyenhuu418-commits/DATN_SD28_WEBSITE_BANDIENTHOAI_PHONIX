package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    
    @Query("SELECT o FROM OtpCode o WHERE o.email = :email AND o.otpCode = :otpCode AND o.isUsed = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<OtpCode> findValidOtp(@Param("email") String email, @Param("otpCode") String otpCode, @Param("now") LocalDateTime now);
    
    @Query("SELECT o FROM OtpCode o WHERE o.email = :email AND o.isUsed = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    List<OtpCode> findActiveOtpsByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
    
    @Query("SELECT o FROM OtpCode o WHERE o.email = :email ORDER BY o.createdAt DESC")
    List<OtpCode> findAllByEmailOrderByCreatedAtDesc(@Param("email") String email);
    
    @Query("DELETE FROM OtpCode o WHERE o.email = :email AND o.isUsed = true")
    void deleteUsedOtpsByEmail(@Param("email") String email);
    
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
}
