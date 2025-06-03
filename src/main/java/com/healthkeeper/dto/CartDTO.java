package com.healthkeeper.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CartDTO {
    private Long id;
    private List<CartItemDTO> items = new ArrayList<>();
    private BigDecimal totalAmount;

    @Data
    public static class CartItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
    }
} 