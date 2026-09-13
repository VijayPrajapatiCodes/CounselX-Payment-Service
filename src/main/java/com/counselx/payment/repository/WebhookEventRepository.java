package com.counselx.payment.repository;
import com.counselx.payment.entity.WebhookEvent; import org.springframework.data.jpa.repository.JpaRepository;
public interface WebhookEventRepository extends JpaRepository<WebhookEvent,Long>{ boolean existsByIdempotencyKey(String key); }
