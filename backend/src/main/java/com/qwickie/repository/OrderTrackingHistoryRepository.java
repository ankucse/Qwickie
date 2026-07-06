package com.qwickie.repository;

import com.qwickie.model.OrderTrackingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * @author Ankit Sinha
 */
public interface OrderTrackingHistoryRepository extends JpaRepository<OrderTrackingHistory, Long> {
    List<OrderTrackingHistory> findByOrderIdOrderByTimestampAsc(Long orderId);
}
