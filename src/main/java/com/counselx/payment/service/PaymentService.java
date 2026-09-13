package com.counselx.payment.service;

import com.counselx.payment.config.CashfreeProperties;
import com.counselx.payment.dto.*;
import com.counselx.payment.entity.PaymentOrder;
import com.counselx.payment.entity.WebhookEvent;
import com.counselx.payment.repository.PaymentOrderRepository;
import com.counselx.payment.repository.WebhookEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class PaymentService {

    private final PaymentOrderRepository orders;
    private final WebhookEventRepository events;
    private final CashfreeClient cashfree;
    private final CashfreeProperties props;
    private final ObjectMapper mapper;

    public PaymentService(
            PaymentOrderRepository orders,
            WebhookEventRepository events,
            CashfreeClient cashfree,
            CashfreeProperties props,
            ObjectMapper mapper) {
        this.orders = orders;
        this.events = events;
        this.cashfree = cashfree;
        this.props = props;
        this.mapper = mapper;
    }

    @Transactional
    public PaymentOrderResponse createOrder(CreatePaymentOrderRequest r) {
        String clean = r.studentId().replaceAll("[^A-Za-z0-9]", "");
        if (clean.isBlank()) {
            throw new IllegalArgumentException("Invalid studentId");
        }

        String orderId = "CX-" + clean.substring(0, Math.min(12, clean.length())) + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        PaymentOrder p = new PaymentOrder();
        p.setOrderId(orderId);
        p.setStudentId(r.studentId());
        p.setApplicationId(r.applicationId());
        p.setAmount(r.amount());
        p.setCurrency("INR");
        p.setStatus("CREATING");
        p.setCustomerName(r.customerName());
        p.setCustomerEmail(r.customerEmail());
        p.setCustomerPhone(r.customerPhone());
        orders.save(p);

        var customer = new CashfreeCreateOrderRequest.CashfreeCustomerDetails(
                r.studentId(), r.customerName(), r.customerEmail(), r.customerPhone());

        var meta = new CashfreeCreateOrderRequest.CashfreeOrderMeta(
                appendQuery(props.returnUrl(), "order_id", orderId),
                blankToNull(props.notifyUrl()));

        var req = new CashfreeCreateOrderRequest(
                r.amount(),
                "INR",
                customer,
                orderId,
                meta,
                (r.orderNote() == null || r.orderNote().isBlank()) ? "CounselX payment" : r.orderNote(),
                Map.of(
                        "student_id", r.studentId(),
                        "application_id", r.applicationId() == null ? "" : r.applicationId()
                )
        );

        var cf = cashfree.createOrder(req);
        p.setCfOrderId(cf.cf_order_id());
        p.setPaymentSessionId(cf.payment_session_id());
        p.setStatus(cf.order_status());
        orders.save(p);

        return response(p);
    }

    @Transactional
    public PaymentOrderResponse sync(String id) {
        PaymentOrder p = find(id);
        Map remote = cashfree.getOrder(id);
        if (remote.get("order_status") != null) {
            p.setStatus(String.valueOf(remote.get("order_status")));
            orders.save(p);
        }
        return response(p);
    }

    public Object payments(String id) {
        find(id);
        return cashfree.getPayments(id);
    }

    @Transactional
    public void webhook(String raw, String sig, String ts, String idem, String eventType) {
        verifyOrThrow(raw, sig, ts);

        String key = (idem == null || idem.isBlank())
                ? UUID.randomUUID().toString()
                : idem;

        if (events.existsByIdempotencyKey(key)) return;

        try {
            JsonNode n = mapper.readTree(raw);
            String orderId = n.path("data").path("order").path("order_id").asText(null);
            JsonNode pay = n.path("data").path("payment");
            String ps = pay.path("payment_status").asText(null);
            String pid = pay.path("cf_payment_id").asText(null);

            if (orderId != null) {
                orders.findByOrderId(orderId).ifPresent(p -> {
                    if (ps != null) {
                        p.setPaymentStatus(ps);
                        p.setStatus(switch (ps.toUpperCase()) {
                            case "SUCCESS" -> "PAID";
                            case "FAILED" -> "FAILED";
                            case "USER_DROPPED" -> "USER_DROPPED";
                            default -> p.getStatus();
                        });
                    }
                    if (pid != null) p.setPaymentId(pid);
                    orders.save(p);
                });
            }

            saveWebhookEvent(key, eventType, orderId);
        } catch (Exception ex) {
            if (ex instanceof IllegalArgumentException iae) throw iae;
            throw new IllegalArgumentException("Invalid webhook JSON", ex);
        }
    }

    private void verifyOrThrow(String raw, String sig, String ts) {
        if (sig == null || ts == null) {
            throw new IllegalArgumentException("Missing Cashfree webhook signature");
        }
        if (!verify(raw, sig, ts)) {
            throw new IllegalArgumentException("Invalid Cashfree webhook signature");
        }
    }

    private void saveWebhookEvent(String key, String eventType, String orderId) {
        WebhookEvent e = new WebhookEvent();
        e.setIdempotencyKey(key);
        e.setEventType(eventType);
        e.setOrderId(orderId);
        e.setReceivedAt(OffsetDateTime.now());
        events.save(e);
    }

    private boolean verify(String raw, String sig, String ts) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    props.clientSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));
            String exp = Base64.getEncoder().encodeToString(
                    mac.doFinal((ts + raw).getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(
                    exp.getBytes(StandardCharsets.UTF_8),
                    sig.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Webhook signature verification failed", e);
        }
    }

    private PaymentOrder find(String id) {
        return orders.findByOrderId(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment order not found: " + id));
    }

    private PaymentOrderResponse response(PaymentOrder p) {
        return new PaymentOrderResponse(
                p.getOrderId(),
                p.getCfOrderId(),
                p.getPaymentSessionId(),
                p.getStudentId(),
                p.getApplicationId(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus(),
                p.getPaymentId(),
                p.getPaymentStatus(),
                p.getCustomerEmail(),
                p.getCustomerPhone()
        );
    }



    private String appendQuery(String base, String key, String value) {
        if (base == null || base.isBlank()) return null;
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + key + "=" + value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
