# Logic Tính Toán Trả Hàng Chi Tiết

## Ví dụ cụ thể từ người dùng:

**Đơn ban đầu:**
- Tổng đơn: 1,000,000 VNĐ
- Khuyến mãi: 10% (≥500K, tối đa 800K) → Giảm 100,000 VNĐ
- Voucher: 5% (≥200K, tối đa 400K) → Giảm 45,000 VNĐ (tính trên 900K)
- **Tổng tiền khách phải trả: 855,000 VNĐ**
- Khách đã trả: 600,000 VNĐ
- **Khách còn nợ: 255,000 VNĐ**
- Đơn còn lại: 400,000 VNĐ (sau khi trả một phần)

## Bước 1: Tính lại giảm giá cho đơn còn lại

### Khuyến mãi 10%:
- Đơn còn lại: 400,000 VNĐ < 500,000 VNĐ
- **→ KHÔNG còn áp dụng được khuyến mãi 10%**
- **→ Phải trừ lại phần khuyến mãi đã áp dụng trên phần trả**

### Voucher 5%:
- Đơn còn lại: 400,000 VNĐ ≥ 200,000 VNĐ
- **→ Vẫn áp dụng được voucher 5%**
- Giảm giá mới: 5% × 400,000 = 20,000 VNĐ (≤ 400,000)

## Bước 2: Tính phần khuyến mãi đã áp dụng trên sản phẩm trả

**Tỷ lệ sản phẩm trả = 600,000 / 1,000,000 = 60%**

### Khuyến mãi 10% đã áp dụng:
- Tổng khuyến mãi ban đầu: 100,000 VNĐ
- Phần khuyến mãi trên sản phẩm trả: 100,000 × 60% = **60,000 VNĐ**
- → Phải trừ lại: **60,000 VNĐ**

### Voucher 5% đã áp dụng:
- Tổng voucher ban đầu: 45,000 VNĐ (tính trên 900K)
- Phần voucher trên sản phẩm trả: 45,000 × 60% = **27,000 VNĐ**
- → Phải trừ lại: **27,000 VNĐ**

**Tổng phải trừ lại: 60,000 + 27,000 = 87,000 VNĐ**

## Bước 3: Tính tiền hoàn lại

### Cách tính đúng:

**Giá gốc sản phẩm trả:** 600,000 VNĐ

**Trừ lại phần khuyến mãi đã áp dụng:**
- Khuyến mãi 10%: -60,000 VNĐ
- Voucher 5%: -27,000 VNĐ
- **Tổng trừ lại: -87,000 VNĐ**

**Áp dụng voucher mới cho đơn còn lại:**
- Voucher 5% mới: +20,000 VNĐ (nếu áp dụng cho đơn còn lại)

**Tiền hoàn lại = Giá gốc - Trừ lại giảm giá + Giảm giá mới (nếu có)**
- **Tiền hoàn lại = 600,000 - 87,000 = 513,000 VNĐ**

### Hoặc nếu không áp dụng voucher mới:

**Tiền hoàn lại = 600,000 - 87,000 = 513,000 VNĐ**

## Bước 4: Tính số tiền cuối cùng

**Khách còn nợ:** 255,000 VNĐ
**Tiền hoàn lại:** 513,000 VNĐ
**→ Cửa hàng phải trả lại:** 513,000 - 255,000 = **258,000 VNĐ**

## Logic Code Cần Implement:

```java
// 1. Lấy thông tin hóa đơn gốc
HoaDon originalInvoice = ...;
BigDecimal originalTotal = originalInvoice.getTongTien(); // 1,000,000
BigDecimal originalDiscount = originalInvoice.getTongTienSauGiam(); // 855,000
BigDecimal originalPromotionDiscount = originalTotal.subtract(originalDiscount);
// = 145,000 (100,000 khuyến mãi + 45,000 voucher)

// 2. Tính tỷ lệ sản phẩm trả
BigDecimal returnedAmount = calculateReturnedAmount(items); // 600,000
BigDecimal returnRatio = returnedAmount.divide(originalTotal); // 0.6 (60%)

// 3. Tính phần giảm giá đã áp dụng trên sản phẩm trả
BigDecimal promotionOnReturned = originalPromotionDiscount.multiply(returnRatio);
// = 145,000 × 0.6 = 87,000

// 4. Kiểm tra điều kiện khuyến mãi cho đơn còn lại
BigDecimal remainingAmount = originalTotal.subtract(returnedAmount); // 400,000
boolean canApplyPromotion = checkPromotionCondition(remainingAmount); // false (< 500K)
boolean canApplyVoucher = checkVoucherCondition(remainingAmount); // true (≥ 200K)

// 5. Tính giảm giá mới cho đơn còn lại (nếu có)
BigDecimal newDiscount = BigDecimal.ZERO;
if (canApplyVoucher) {
    BigDecimal voucherDiscount = calculateVoucherDiscount(remainingAmount);
    newDiscount = newDiscount.add(voucherDiscount); // 20,000
}

// 6. Tính tiền hoàn lại
BigDecimal refundAmount = returnedAmount
    .subtract(promotionOnReturned)  // 600,000 - 87,000 = 513,000
    .add(newDiscount);              // + 20,000 = 533,000 (nếu áp dụng voucher mới)

// 7. Tính số tiền cuối cùng
BigDecimal customerAlreadyPaid = getCustomerPaidAmount(); // 600,000
BigDecimal customerShouldPay = originalDiscount; // 855,000
BigDecimal customerOwed = customerShouldPay.subtract(customerAlreadyPaid); // 255,000
BigDecimal finalAmount = refundAmount.subtract(customerOwed); // 533,000 - 255,000 = 278,000
// → Cửa hàng phải trả lại 278,000 VNĐ
```

## Vấn đề trong logic người dùng mô tả:

Người dùng tính:
```
900 - 600 = 300
400 × 5% = 20
900 - 660 = 540
400 - 40 = 360 → Khách nợ 40K
400 - 20 = 380 → 20K khách được
540 - 40 + 20 = 520
```

**Phân tích:**
- 900K: Có vẻ là `tongTienSauGiam` = 1,000,000 - 100,000 = 900,000? (thiếu voucher 45K)
- 660K: Không rõ nguồn gốc
- 540K: Không rõ nguồn gốc
- 40K: Không rõ nguồn gốc

**Kết luận:** Logic người dùng mô tả không rõ ràng và có vẻ có lỗi. Cần làm rõ với người dùng trước khi implement.

