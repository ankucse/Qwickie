package com.qwickie.controller;

import com.qwickie.dto.OrderRequest;
import com.qwickie.dto.OrderResponse;
import com.qwickie.dto.OrderStatusUpdateRequest;
import com.qwickie.service.OrderService;
import com.qwickie.service.SseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final SseService sseService;

    public OrderController(OrderService orderService, SseService sseService) {
        this.orderService = orderService;
        this.sseService = sseService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request, Authentication auth) {
        return ResponseEntity.ok(orderService.placeOrder(request, auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getCustomerOrders(auth.getName()));
    }

    @GetMapping(value = "/{id}/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrderUpdates(@PathVariable Long id) {
        return sseService.subscribe(id);
    }

    @PostMapping("/{id}/not-received")
    public ResponseEntity<OrderResponse> reportNotReceived(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.notReceived(id, auth.getName()));
    }

    // Partner endpoints
    @GetMapping("/available")
    public ResponseEntity<List<OrderResponse>> getAvailableOrders() {
        return ResponseEntity.ok(orderService.getAvailableOrders());
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.acceptOrder(id, auth.getName()));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @RequestBody OrderStatusUpdateRequest request, Authentication auth) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.getStatus(), auth.getName()));
    }
}
