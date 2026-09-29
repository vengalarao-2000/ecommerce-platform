package com.vengalarao.enterprise_order_platform.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class UpdateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 150)
    private String name;
    
    @Size(max = 500, message = "Product description should not exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price can not be negative or zero")
    private BigDecimal price;

    @NotNull(message = "Stock Quanity is required")
    @PositiveOrZero(message = "Stock Quantity can not be negative")
    private Integer stockQuantity;

    //While creating a product we are setting the default as true, but authorized user must be able to activate/deactivate the product availability
    @NotNull(message = "Active must not be empty")
    private Boolean active;

    public UpdateProductRequest() {
    }

    public UpdateProductRequest(String name, String description, BigDecimal price, Integer stockQuantity, Boolean active) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    
    
    
    
}
