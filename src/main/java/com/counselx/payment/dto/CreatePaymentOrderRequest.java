package com.counselx.payment.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record CreatePaymentOrderRequest(@NotBlank @Size(max=100) String studentId,@Size(max=100) String applicationId,@NotNull @DecimalMin("1.00") @Digits(integer=10,fraction=2) BigDecimal amount,@Size(max=120) String customerName,@NotBlank @Email @Size(max=180) String customerEmail,@NotBlank @Pattern(regexp="\\d{10,15}") String customerPhone,@Size(max=200) String orderNote){}
