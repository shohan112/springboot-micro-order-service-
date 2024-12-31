package com.microservices.OrderService.service;

import com.microservices.OrderService.entity.Order;
import com.microservices.OrderService.exception.CustomException;
import com.microservices.OrderService.external.client.PaymentService;
import com.microservices.OrderService.external.client.ProductService;
import com.microservices.OrderService.external.request.PaymentRequest;
import com.microservices.OrderService.external.response.PaymentResponse;
import com.microservices.OrderService.model.OrderRequest;
import com.microservices.OrderService.model.OrderResponse;
import com.microservices.OrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Placing order: {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Placing order with product ID: {}", orderRequest.getProductId());
        Order order = Order.builder()
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .orderDate(Instant.now())
                .orderStatus("CREATED")
                .amount(orderRequest.getAmount())
                .build();
        order = orderRepository.save(order);

        log.info("calling payment service to complete the payment");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .amount(orderRequest.getAmount())
                .paymentModeEnum(orderRequest.getPaymentModeEnum())
                .amount(orderRequest.getAmount())
                .build();

        String orderStatus = null;
        try{
            paymentService.createPayment(paymentRequest);
            log.info("payment service completed the payment, changing order status to PLACED");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Error during payment. changing order status to FAILED ", e);
            orderStatus = "FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        log.info("order service placed the order");

        log.info("Placed order with order ID: {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse orderDetails(Long orderId) {
        log.info("Getting order details by order ID: {}", orderId);
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new CustomException("Order ID not found.","NOT_FOUND",404));

        log.info("Invoking product service to fetch the product details retrieved from DB");

//        calling product service using REST templete

        OrderResponse.ProductDetails productResponse = restTemplate.getForObject(
                "http://PRODUCT-SERVICE/product/"+order.getProductId(), OrderResponse.ProductDetails.class
        );
        log.info("Product response: {}", productResponse);
        if (productResponse == null) {
            throw new CustomException("Product details not found.", "NOT_FOUND", 404);
        }

        OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails.builder()
                .productId(productResponse.getProductId())
                .productName(productResponse.getProductName())
                .productPrice(productResponse.getProductPrice())
                .quantity(productResponse.getQuantity())
                .build();

//        calling payment service using FeignClient

        PaymentResponse paymentResponse = paymentService.paymentDetailsByOrderId(orderId);

        OrderResponse.PaymentDetails paymentDetails = OrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.getPaymentId())
                .orderId(paymentResponse.getOrderId())
                .paymentMode(paymentResponse.getPaymentMode())
                .paymentDate(paymentResponse.getPaymentDate())
                .paymentStatus(paymentResponse.getPaymentStatus())
                .amount(paymentResponse.getAmount())
                .build();


        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();
        log.info("order details retrieved from DB");
        return orderResponse;
    }


}
