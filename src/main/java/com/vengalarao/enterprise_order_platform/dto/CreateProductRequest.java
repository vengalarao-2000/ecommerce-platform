package com.vengalarao.enterprise_order_platform.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class CreateProductRequest {

    @NotBlank (message = "Product name is required")
    @Size(max = 150,
        message = "Product name must not exceed 150 characters"
    )
    private String name;

    @Size(max = 500, message = "Product description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive (message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull (message = "Stock quantity is required")
    @PositiveOrZero (message = "Stock quanity cannot be negative")
    private Integer stockQuantity;

    public CreateProductRequest() {

    }

    public CreateProductRequest(String name, String description, BigDecimal price, Integer stockQuantity) {
        this.name = name;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.price = price;
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

    

}
