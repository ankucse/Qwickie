package com.qwickie.controller;

import com.qwickie.dto.OrderRequest;
import com.qwickie.dto.OrderResponse;
import com.qwickie.dto.OrderStatusUpdateRequest;
import com.qwickie.service.OrderService;
import com.qwickie.service.SseService;
import com.qwickie.service.OrderWorkflowOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Controller for managing customer orders and partner delivery flows.
 * This class acts as the API gateway, mapping HTTP requests to internal business logic.
 * State-changing operations (placing orders, accepting orders, status updates) are 
 * delegated to the {@link OrderWorkflowOrchestrator} to handle complex business rules, 
 * auditing, and real-time SSE broadcasts. 
 * Simple data retrieval (getting an order) remains directly tied to the {@link OrderService}.
 *
 * @author Ankit Sinha
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderWorkflowOrchestrator orderOrchestrator;
    private final SseService sseService;

    public OrderController(OrderService orderService, OrderWorkflowOrchestrator orderOrchestrator, SseService sseService) {
        this.orderService = orderService;
        this.orderOrchestrator = orderOrchestrator;
        this.sseService = sseService;
    }

    /**
     * Customer endpoint: Places a new delivery order.
     * The orchestrator handles persisting the order and triggering a global SSE broadcast 
     * to notify all active delivery partners.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request, Authentication auth) {
        return ResponseEntity.ok(orderOrchestrator.processNewOrder(request, auth.getName()));
    }

    /**
     * Retrieves specific order details by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    /**
     * Customer endpoint: Fetches all past and current orders for the logged-in customer.
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getCustomerOrders(auth.getName()));
    }

    /**
     * Client endpoint: Establishes an SSE (Server-Sent Events) connection for a SPECIFIC order.
     * The customer's frontend listens to this stream to watch real-time status transitions.
     */
    @GetMapping(value = "/{id}/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrderUpdates(@PathVariable Long id) {
        return sseService.subscribe(id);
    }

    /**
     * Partner endpoint: Establishes a GLOBAL SSE connection for the Partner Dashboard.
     * Pushes real-time alerts when new orders arrive or when an order is accepted by another rider.
     */
    @GetMapping(value = "/available/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPartnerUpdates() {
        return sseService.subscribeToPartnerStream();
    }

    /**
     * Customer endpoint: Disputes an order delivery (e.g. Rider marked delivered, but it wasn't).
     */
    @PostMapping("/{id}/not-received")
    public ResponseEntity<OrderResponse> reportNotReceived(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderOrchestrator.handleOrderDispute(id, auth.getName()));
    }

    // ==========================================
    // Partner (Rider) Endpoints
    // ==========================================

    /**
     * Partner endpoint: Fetches the initial list of all currently available (unassigned) orders.
     */
    @GetMapping("/available")
    public ResponseEntity<List<OrderResponse>> getAvailableOrders() {
        return ResponseEntity.ok(orderService.getAvailableOrders());
    }

    /**
     * Partner endpoint: A rider claims an available order.
     * The orchestrator assigns the rider and broadcasts to other riders that the order is gone.
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderOrchestrator.assignRiderToOrder(id, auth.getName()));
    }

    /**
     * Partner endpoint: Rider updates the order lifecycle (e.g., AT_STORE, EN_ROUTE, DELIVERED).
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @RequestBody OrderStatusUpdateRequest request, Authentication auth) {
        return ResponseEntity.ok(orderOrchestrator.transitionOrderStatus(id, request.getStatus(), auth.getName()));
    }
}
