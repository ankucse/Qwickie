package com.qwickie.service;

import com.qwickie.dto.OrderRequest;
import com.qwickie.dto.OrderResponse;
import com.qwickie.exception.InvalidPincodeException;
import com.qwickie.exception.OrderStateException;
import com.qwickie.model.DeliveryZone;
import com.qwickie.model.Order;
import com.qwickie.model.OrderTrackingHistory;
import com.qwickie.model.User;
import com.qwickie.repository.DeliveryZoneRepository;
import com.qwickie.repository.OrderRepository;
import com.qwickie.repository.OrderTrackingHistoryRepository;
import com.qwickie.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderTrackingHistoryRepository historyRepository;
    private final DeliveryZoneRepository deliveryZoneRepository;
    private final UserRepository userRepository;
    private final SseService sseService;

    public OrderService(OrderRepository orderRepository, OrderTrackingHistoryRepository historyRepository, DeliveryZoneRepository deliveryZoneRepository, UserRepository userRepository, SseService sseService) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.deliveryZoneRepository = deliveryZoneRepository;
        this.userRepository = userRepository;
        this.sseService = sseService;
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String customerUsername) {
        // Expanded regex to cover Kolkata (700xxx), Howrah (711xxx), Hooghly (712xxx), 
        // South 24 Parganas (743xxx, 744xxx), etc.
        if (request.getPincode() == null || !request.getPincode().matches("^(700|711|712|743|744)\\d{3}$")) {
            throw new InvalidPincodeException("Delivery is only available in Kolkata and surrounding areas.");
        }
        
        // We relax the strict database verification to support the extended zones automatically
        // If the regex passes, we consider it deliverable.

        User customer = userRepository.findByUsername(customerUsername).orElseThrow();

        Order order = new Order();
        order.setCustomer(customer);
        order.setTotalAmount(request.getTotalAmount());
        order.setPincode(request.getPincode());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus(Order.OrderStatus.ORDER_RECEIVED);
        
        order = orderRepository.save(order);
        
        OrderTrackingHistory history = new OrderTrackingHistory(order, order.getStatus(), "Order Placed");
        historyRepository.save(history);

        OrderResponse response = new OrderResponse(order);
        sseService.broadcastToPartners("NEW_ORDER", response);

        return response;
    }

    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        return new OrderResponse(order);
    }

    public List<OrderResponse> getAvailableOrders() {
        return orderRepository.findByStatus(Order.OrderStatus.ORDER_RECEIVED).stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }
    
    public List<OrderResponse> getCustomerOrders(String username) {
        User customer = userRepository.findByUsername(username).orElseThrow();
        return orderRepository.findByCustomerId(customer.getId()).stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse acceptOrder(Long orderId, String riderUsername) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != Order.OrderStatus.ORDER_RECEIVED) {
            throw new OrderStateException("Order cannot be accepted in its current state.");
        }
        
        User rider = userRepository.findByUsername(riderUsername).orElseThrow();
        order.setRider(rider);
        order.setStatus(Order.OrderStatus.PARTNER_ACCEPTED);
        orderRepository.save(order);

        historyRepository.save(new OrderTrackingHistory(order, order.getStatus(), "Accepted by " + riderUsername));
        sseService.sendOrderStatusUpdate(orderId, order.getStatus().name());
        sseService.broadcastToPartners("ORDER_ACCEPTED", orderId);

        return new OrderResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, Order.OrderStatus newStatus, String riderUsername) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        
        if (order.getRider() == null || !order.getRider().getUsername().equals(riderUsername)) {
            throw new RuntimeException("Not authorized to update this order.");
        }

        if (order.getStatus() == Order.OrderStatus.DELIVERED || order.getStatus() == Order.OrderStatus.SUPPORT_ROUTED) {
            throw new OrderStateException("Order is in a terminal state.");
        }
        
        if (newStatus.ordinal() <= order.getStatus().ordinal()) {
            throw new OrderStateException("Cannot revert to a previous state.");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        historyRepository.save(new OrderTrackingHistory(order, order.getStatus(), "Status updated"));
        sseService.sendOrderStatusUpdate(orderId, order.getStatus().name());

        return new OrderResponse(order);
    }

    @Transactional
    public OrderResponse notReceived(Long orderId, String customerUsername) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (!order.getCustomer().getUsername().equals(customerUsername)) {
            throw new RuntimeException("Not authorized");
        }

        order.setStatus(Order.OrderStatus.SUPPORT_ROUTED);
        orderRepository.save(order);

        historyRepository.save(new OrderTrackingHistory(order, order.getStatus(), "Customer reported not received"));
        sseService.sendOrderStatusUpdate(orderId, order.getStatus().name());

        return new OrderResponse(order);
    }
}
