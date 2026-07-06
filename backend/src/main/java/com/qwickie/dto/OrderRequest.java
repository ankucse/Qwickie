package com.qwickie.dto;

import java.math.BigDecimal;

/**
 * @author Ankit Sinha
 */
public class OrderRequest {
    private BigDecimal totalAmount;
    private String pincode;
    private String deliveryAddress;

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}
