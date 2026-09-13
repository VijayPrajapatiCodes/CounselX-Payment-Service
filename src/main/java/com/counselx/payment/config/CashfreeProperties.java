package com.counselx.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cashfree")
public record CashfreeProperties(
        String baseUrl,
        String apiVersion,
        String clientId,
        String clientSecret,
        String returnUrl,
        String notifyUrl
) {
}
