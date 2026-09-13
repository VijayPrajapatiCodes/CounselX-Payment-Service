package com.counselx.payment.dto;
import java.math.BigDecimal; import java.util.Map;
public record CashfreeCreateOrderRequest(BigDecimal order_amount,String order_currency,CashfreeCustomerDetails customer_details,String order_id,CashfreeOrderMeta order_meta,String order_note,Map<String,String> order_tags){
 public record CashfreeCustomerDetails(String customer_id,String customer_name,String customer_email,String customer_phone){}
 public record CashfreeOrderMeta(String return_url,String notify_url){}
}
