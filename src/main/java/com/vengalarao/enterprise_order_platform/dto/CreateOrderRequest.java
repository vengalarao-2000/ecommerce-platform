package com.vengalarao.enterprise_order_platform.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull (message = "Customer id is required")
    private Long customerId;
    
    @NotEmpty (message = "Order must contain at least one item")
    @Valid //Valid is important here because Spring may validate the List but not each item in List without this.
    private List<CreateOrderItemRequest> items;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long customerId, List<CreateOrderItemRequest> items) {
        this.customerId = customerId;
        this.items = items;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<CreateOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CreateOrderItemRequest> items) {
        this.items = items;
    }

    

    
    
}
