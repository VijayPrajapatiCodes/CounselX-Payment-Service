package com.counselx.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "payment_orders",
        indexes = {
                @Index(name = "idx_payment_student", columnList = "student_id"),
                @Index(name = "idx_payment_application", columnList = "application_id"),
                @Index(name = "idx_payment_status", columnList = "status")
        }
)
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 45)
    private String orderId;

    @Column(name = "cf_order_id", length = 64)
    private String cfOrderId;

    @Column(name = "payment_session_id", length = 1000)
    private String paymentSessionId;

    @Column(name = "student_id", nullable = false, length = 100)
    private String studentId;

    @Column(name = "application_id", length = 100)
    private String applicationId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(nullable = false, length = 40)
    private String status = "CREATING";

    @Column(name = "payment_id", length = 64)
    private String paymentId;

    @Column(name = "payment_status", length = 40)
    private String paymentStatus;

    @Column(name = "customer_name", length = 120)
    private String customerName;

    @Column(name = "customer_email", length = 180)
    private String customerEmail;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String v) { orderId = v; }
    public String getCfOrderId() { return cfOrderId; }
    public void setCfOrderId(String v) { cfOrderId = v; }
    public String getPaymentSessionId() { return paymentSessionId; }
    public void setPaymentSessionId(String v) { paymentSessionId = v; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String v) { studentId = v; }
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String v) { applicationId = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { amount = v; }
    public String getCurrency() { return currency; }
    public void setCurrency(String v) { currency = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String v) { paymentId = v; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String v) { paymentStatus = v; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String v) { customerName = v; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String v) { customerEmail = v; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String v) { customerPhone = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
