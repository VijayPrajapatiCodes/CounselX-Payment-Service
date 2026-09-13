package com.counselx.payment;
import com.counselx.payment.config.CashfreeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CashfreeProperties.class)
public class PaymentServiceApplication { public static void main(String[] args){ SpringApplication.run(PaymentServiceApplication.class,args); } }
