package com.qwickie.dto;

import com.qwickie.model.Order.OrderStatus;

public class OrderStatusUpdateRequest {
    private OrderStatus status;
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
