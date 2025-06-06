package com.healthkeeper.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Long sellerId;
    private String sellerName;
    private Boolean isActive;
    private String category;
    private String brand;
} 