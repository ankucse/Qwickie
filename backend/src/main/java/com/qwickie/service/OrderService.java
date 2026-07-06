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
/**
 * @author Ankit Sinha
 */
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

    /**
     * Core logic to create and save a new order entity.
     * @param request The order details (address, pincode, total)
     * @param customerUsername The username of the customer placing the order
     * @return The saved Order entity
     */
    @Transactional
    public Order createOrder(OrderRequest request, String customerUsername) {
        if (request.getPincode() == null || !request.getPincode().matches("^(700|711|712|743|744)\\d{3}$")) {
            throw new InvalidPincodeException("Delivery is only available in Kolkata and surrounding areas.");
        }

        User customer = userRepository.findByUsername(customerUsername).orElseThrow();

        Order order = new Order();
        order.setCustomer(customer);
        order.setTotalAmount(request.getTotalAmount());
        order.setPincode(request.getPincode());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus(Order.OrderStatus.ORDER_RECEIVED);
        
        return orderRepository.save(order);
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

    /**
     * Validates and assigns a rider to an order.
     */
    @Transactional
    public Order assignRider(Long orderId, String riderUsername) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != Order.OrderStatus.ORDER_RECEIVED) {
            throw new OrderStateException("Order cannot be accepted in its current state.");
        }
        
        User rider = userRepository.findByUsername(riderUsername).orElseThrow();
        order.setRider(rider);
        order.setStatus(Order.OrderStatus.PARTNER_ACCEPTED);
        return orderRepository.save(order);
    }

    /**
     * Updates an order's status, ensuring state constraints.
     */
    @Transactional
    public Order changeOrderStatus(Long orderId, Order.OrderStatus newStatus, String riderUsername) {
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
        return orderRepository.save(order);
    }

    /**
     * Marks an order as disputed by the customer.
     */
    @Transactional
    public Order markAsNotReceived(Long orderId, String customerUsername) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (!order.getCustomer().getUsername().equals(customerUsername)) {
            throw new RuntimeException("Not authorized");
        }

        order.setStatus(Order.OrderStatus.SUPPORT_ROUTED);
        return orderRepository.save(order);
    }
}
