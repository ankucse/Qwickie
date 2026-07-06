package com.qwickie.repository;

import com.qwickie.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * @author Ankit Sinha
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByCustomerId(Long customerId);
}
