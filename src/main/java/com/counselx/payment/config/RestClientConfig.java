package com.counselx.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

 @Bean
 public RestClient.Builder restClientBuilder() {
  return RestClient.builder();
 }

 @Bean
 public RestClient cashfreeRestClient(
         RestClient.Builder builder,
         CashfreeProperties properties) {

  return builder
          .baseUrl(properties.baseUrl())
          .defaultHeader("x-client-id", properties.clientId())
          .defaultHeader("x-client-secret", properties.clientSecret())
          .defaultHeader("x-api-version", properties.apiVersion())
          .defaultHeader("Content-Type", "application/json")
          .build();
 }
}