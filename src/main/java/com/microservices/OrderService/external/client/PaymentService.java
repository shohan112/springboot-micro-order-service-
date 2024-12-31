package com.microservices.OrderService.external.client;

import com.microservices.OrderService.external.request.PaymentRequest;
import com.microservices.OrderService.external.response.PaymentResponse;
import com.microservices.OrderService.model.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentService {

    @PostMapping("/payment/request")
    Long createPayment(@RequestBody PaymentRequest paymentRequest);

    @GetMapping("/payment/details/{orderId}")
    PaymentResponse paymentDetailsByOrderId(@PathVariable Long orderId);
}
