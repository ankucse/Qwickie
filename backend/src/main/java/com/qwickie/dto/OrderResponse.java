package com.qwickie.dto;

import com.qwickie.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResponse {
    private Long id;
    private BigDecimal totalAmount;
    private String pincode;
    private String deliveryAddress;
    private Order.OrderStatus status;
    private LocalDateTime createdAt;
    
    public OrderResponse(Order order) {
        this.id = order.getId();
        this.totalAmount = order.getTotalAmount();
        this.pincode = order.getPincode();
        this.deliveryAddress = order.getDeliveryAddress();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
    }
    
    public Long getId() { return id; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPincode() { return pincode; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public Order.OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
