package com.counselx.payment.dto;

import java.math.BigDecimal;

public record PaymentOrderResponse(
        String orderId,
        String cfOrderId,
        String paymentSessionId,
        String studentId,
        String applicationId,
        BigDecimal amount,
        String currency,
        String orderStatus,
        String paymentId,
        String paymentStatus,
        String customerEmail,
        String customerPhone
) {
}
