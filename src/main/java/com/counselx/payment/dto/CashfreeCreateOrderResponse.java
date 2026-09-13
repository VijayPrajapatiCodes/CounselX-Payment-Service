package com.counselx.payment.dto;
import java.math.BigDecimal;
public record CashfreeCreateOrderResponse(String cf_order_id,String order_id,String order_currency,BigDecimal order_amount,String order_status,String payment_session_id){}
