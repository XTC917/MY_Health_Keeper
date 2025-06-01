package com.healthkeeper.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShoppingCartItemDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private String productImage;
    private Integer quantity;
    private BigDecimal subtotal;
} 