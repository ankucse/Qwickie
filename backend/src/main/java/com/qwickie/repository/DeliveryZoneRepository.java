package com.qwickie.repository;

import com.qwickie.model.DeliveryZone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {
    Optional<DeliveryZone> findByPincode(String pincode);
}
