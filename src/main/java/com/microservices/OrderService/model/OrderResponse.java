package com.microservices.OrderService.model;

import com.microservices.OrderService.external.response.PaymentResponse;
import lombok.*;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;
    private Instant orderDate;
    private String orderStatus;
    private Long amount;
    private ProductDetails productDetails;
    private PaymentDetails paymentDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductDetails {
        private Long productId;
        private String productName;
        private Long productPrice;
        private Long quantity;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PaymentDetails {
        private Long paymentId;
        private String paymentStatus;
        private PaymentModeEnum paymentMode;
        private Long amount;
        private Instant paymentDate;
        private Long orderId;
    }
}
