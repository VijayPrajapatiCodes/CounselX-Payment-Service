package com.counselx.payment.controller;

import com.counselx.payment.dto.CreatePaymentOrderRequest;
import com.counselx.payment.dto.PaymentOrderResponse;
import com.counselx.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    /**
     * Creates a Cashfree Payment Gateway order.
     * The response contains paymentSessionId which the frontend
     * passes to Cashfree Checkout.
     */
    @PostMapping("/orders")
    public ResponseEntity<PaymentOrderResponse> create(
            @Valid @RequestBody CreatePaymentOrderRequest request) {

        System.out.println("========== PAYMENT REQUEST ==========");
        System.out.println("studentId = " + request.studentId());
        System.out.println("applicationId = " + request.applicationId());
        System.out.println("amount = " + request.amount());
        System.out.println("customerName = " + request.customerName());
        System.out.println("customerEmail = " + request.customerEmail());
        System.out.println("customerPhone = " + request.customerPhone());
        System.out.println("orderNote = " + request.orderNote());
        System.out.println("=====================================");

        return ResponseEntity.ok(service.createOrder(request));
    }

    /**
     * Server-side order/status verification.
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentOrderResponse> get(
            @PathVariable String orderId) {
        return ResponseEntity.ok(service.sync(orderId));
    }

    /**
     * Fetch all payment attempts/transactions for an order.
     */
    @GetMapping("/orders/{orderId}/payments")
    public ResponseEntity<Object> payments(
            @PathVariable String orderId) {
        return ResponseEntity.ok(service.payments(orderId));
    }

    /**
     * Cashfree Payment Gateway webhook.
     * Keep this endpoint publicly reachable over HTTPS in production.
     */
    @PostMapping("/webhooks/cashfree")
    public ResponseEntity<Void> webhook(
            @RequestHeader(value = "x-webhook-signature", required = false) String signature,
            @RequestHeader(value = "x-webhook-timestamp", required = false) String timestamp,
            @RequestHeader(value = "x-idempotency-key", required = false) String idempotencyKey,
            @RequestHeader(value = "x-event-type", required = false) String eventType,
            @RequestBody String raw) {

        service.webhook(raw, signature, timestamp, idempotencyKey, eventType);
        return ResponseEntity.ok().build();
    }
}
