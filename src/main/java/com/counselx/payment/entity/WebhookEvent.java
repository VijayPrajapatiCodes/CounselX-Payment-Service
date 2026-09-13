package com.counselx.payment.entity;
import jakarta.persistence.*; import java.time.OffsetDateTime;
@Entity @Table(name="payment_webhook_events",uniqueConstraints=@UniqueConstraint(name="uk_webhook_idempotency",columnNames="idempotency_key"))
public class WebhookEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="idempotency_key",nullable=false,unique=true,length=200) private String idempotencyKey;
 @Column(name="event_type",length=100) private String eventType; @Column(name="order_id",length=100) private String orderId; @Column(name="received_at",nullable=false) private OffsetDateTime receivedAt;
 public String getIdempotencyKey(){return idempotencyKey;} public void setIdempotencyKey(String v){idempotencyKey=v;} public String getEventType(){return eventType;} public void setEventType(String v){eventType=v;} public String getOrderId(){return orderId;} public void setOrderId(String v){orderId=v;} public void setReceivedAt(OffsetDateTime v){receivedAt=v;}
}
