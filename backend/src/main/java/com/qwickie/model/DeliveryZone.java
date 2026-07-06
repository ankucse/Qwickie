package com.qwickie.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Delivery_Zones")
/**
 * @author Ankit Sinha
 */
public class DeliveryZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String pincode;

    @Column(nullable = false)
    private boolean isActive = true;

    public DeliveryZone() {}

    public DeliveryZone(String pincode) {
        this.pincode = pincode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
}
