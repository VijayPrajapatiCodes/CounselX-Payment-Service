package com.counselx.payment.service;

import com.counselx.payment.dto.CashfreeCreateOrderRequest;
import com.counselx.payment.dto.CashfreeCreateOrderResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class CashfreeClient {

    private final RestClient client;

    public CashfreeClient(RestClient client) {
        this.client = client;
    }

    public CashfreeCreateOrderResponse createOrder(
            CashfreeCreateOrderRequest request) {

        try {
            System.out.println("========== CASHFREE CREATE ORDER ==========");
            System.out.println("Amount: " + request.order_amount());
            System.out.println("Currency: " + request.order_currency());
            System.out.println("Order ID: " + request.order_id());

            return client.post()
                    .uri("/orders")
                    .header("x-request-id", UUID.randomUUID().toString())
                    .header("x-idempotency-key", UUID.randomUUID().toString())
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String errorBody = new String(res.getBody().readAllBytes());

                        System.err.println("========== CASHFREE ERROR ==========");
                        System.err.println("HTTP Status: " + res.getStatusCode());
                        System.err.println("Response: " + errorBody);
                        System.err.println("====================================");

                        throw new IllegalArgumentException(
                                "Cashfree API error: " + res.getStatusCode()
                                        + " - " + errorBody);
                    })
                    .body(CashfreeCreateOrderResponse.class);

        } catch (Exception e) {
            System.err.println("Cashfree create order failed: " + e.getMessage());
            throw e;
        }
    }

    public Map getOrder(String id) {
        return client.get()
                .uri("/orders/{id}", id)
                .header("x-request-id", UUID.randomUUID().toString())
                .retrieve()
                .body(Map.class);
    }

    public Object getPayments(String id) {
        return client.get()
                .uri("/orders/{id}/payments", id)
                .header("x-request-id", UUID.randomUUID().toString())
                .retrieve()
                .body(Object.class);
    }
}
