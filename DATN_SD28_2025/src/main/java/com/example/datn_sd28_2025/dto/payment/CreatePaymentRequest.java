package com.example.datn_sd28_2025.dto.payment;

public class CreatePaymentRequest {
    private long amount; // in VND
    private String orderInfo;
    private String bankCode; // optional for VNPay

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public String getOrderInfo() { return orderInfo; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
}






















