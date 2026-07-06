package com.qwickie.service;

import com.qwickie.dto.OrderRequest;
import com.qwickie.dto.OrderResponse;
import com.qwickie.model.Order;
import com.qwickie.model.OrderTrackingHistory;
import com.qwickie.repository.OrderTrackingHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrator pattern implementation to manage business logic flows.
 * This class coordinates the interactions between core domain services (OrderService),
 * auditing services (OrderTrackingHistoryRepository), and real-time event 
 * streaming services (SseService).
 * 
 * By extracting workflow coordination out of the basic CRUD services, we achieve a 
 * cleaner architecture where services only handle data persistence, and the orchestrator
 * manages the 'how' and 'when' of system transitions.
 * 
 * @author Ankit Sinha
 */
@Service
public class OrderWorkflowOrchestrator {

    private final OrderService orderService;
    private final OrderTrackingHistoryRepository historyRepository;
    private final SseService sseService;

    public OrderWorkflowOrchestrator(OrderService orderService, 
                                     OrderTrackingHistoryRepository historyRepository, 
                                     SseService sseService) {
        this.orderService = orderService;
        this.historyRepository = historyRepository;
        this.sseService = sseService;
    }

    /**
     * Coordinates the placement of a new order.
     * 1. Persists the core order via OrderService.
     * 2. Logs the creation event in the tracking history.
     * 3. Broadcasts the new order to all active partner dashboards globally via SSE.
     */
    @Transactional
    public OrderResponse processNewOrder(OrderRequest request, String customerUsername) {
        // 1. Delegate core database creation to the domain service
        Order order = orderService.createOrder(request, customerUsername);
        
        // 2. Audit the state transition
        OrderTrackingHistory history = new OrderTrackingHistory(order, order.getStatus(), "Order Placed");
        historyRepository.save(history);

        // 3. Coordinate real-time broadcast to all riders
        OrderResponse response = new OrderResponse(order);
        sseService.broadcastToPartners("NEW_ORDER", response);

        return response;
    }

    /**
     * Coordinates a rider accepting an order.
     * 1. Updates the order's state and assigns the rider.
     * 2. Logs the acceptance event.
     * 3. Notifies the specific customer of the status change.
     * 4. Broadcasts to all other riders that this order is no longer available.
     */
    @Transactional
    public OrderResponse assignRiderToOrder(Long orderId, String riderUsername) {
        Order order = orderService.assignRider(orderId, riderUsername);

        historyRepository.save(new OrderTrackingHistory(order, order.getStatus(), "Accepted by " + riderUsername));
        
        // Notify the specific customer viewing this order's tracking page
        sseService.sendOrderStatusUpdate(orderId, order.getStatus().name());
        
        // Notify all riders so their dashboard instantly grays out the "Accept" button
        sseService.broadcastToPartners("ORDER_ACCEPTED", orderId);

        return new OrderResponse(order);
    }

    /**
     * Coordinates a status transition (e.g., En Route, Delivered) initiated by a rider.
     * 1. Updates the core order state.
     * 2. Logs the audit history.
     * 3. Streams the real-time update directly to the customer.
     */
    @Transactional
    public OrderResponse transitionOrderStatus(Long orderId, Order.OrderStatus newStatus, String riderUsername) {
        Order order = orderService.changeOrderStatus(orderId, newStatus, riderUsername);

        historyRepository.save(new OrderTrackingHistory(order, order.getStatus(), "Status updated"));
        
        sseService.sendOrderStatusUpdate(orderId, order.getStatus().name());

        return new OrderResponse(order);
    }

    /**
     * Coordinates the workflow for when a customer reports an order as not received.
     * Routs the order to support and notifies relevant parties.
     */
    @Transactional
    public OrderResponse handleOrderDispute(Long orderId, String customerUsername) {
        Order order = orderService.markAsNotReceived(orderId, customerUsername);

        historyRepository.save(new OrderTrackingHistory(order, order.getStatus(), "Customer reported not received"));
        
        sseService.sendOrderStatusUpdate(orderId, order.getStatus().name());

        return new OrderResponse(order);
    }
}
