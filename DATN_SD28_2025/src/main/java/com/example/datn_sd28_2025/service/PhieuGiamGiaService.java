package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.PhieuGiamGiaDTO;

import java.util.List;
import java.util.Optional;

public interface PhieuGiamGiaService {
    List<PhieuGiamGiaDTO> getAll();
    Optional<PhieuGiamGiaDTO> getById(Integer id);
    List<PhieuGiamGiaDTO> getActiveVouchers();
    Optional<PhieuGiamGiaDTO> getByCode(String code);
    List<PhieuGiamGiaDTO> searchByQuery(String query);
    PhieuGiamGiaDTO save(PhieuGiamGiaDTO phieuGiamGiaDTO);
    PhieuGiamGiaDTO update(Integer id, PhieuGiamGiaDTO phieuGiamGiaDTO);
    void delete(Integer id);
    PhieuGiamGiaDTO toggleStatus(Integer id);
    boolean isVoucherValid(String code, java.math.BigDecimal orderAmount);
    List<PhieuGiamGiaDTO> getVouchersByCustomer(Integer customerId);
    List<PhieuGiamGiaDTO> getUsedVouchersByCustomer(Integer customerId);
    boolean canCustomerUseVoucher(Integer customerId, String voucherCode);
    boolean markVoucherAsUsed(Integer customerId, String voucherCode, Double discountAmount);
    void decreaseUsage(Integer voucherId);
    boolean canWalkInCustomerUseVoucher(String voucherCode);
    boolean markVoucherAsUsedForWalkIn(String voucherCode);
    List<PhieuGiamGiaDTO> getAvailableVouchers(Integer customerId);
}
