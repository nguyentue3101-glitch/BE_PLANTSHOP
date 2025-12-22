package com.example.backendplantshop.dto.request.vnpay;

import com.example.backendplantshop.enums.VNPayPaymentPurpose;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateVNPayPaymentRequest {
    @NotNull(message = "Mã đơn hàng không được để trống")
    private Integer orderId;
    
    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;
    
    private String orderInfo;
    
    private VNPayPaymentPurpose purpose;
    
    private String bankCode; // Mã ngân hàng (tùy chọn)
}

