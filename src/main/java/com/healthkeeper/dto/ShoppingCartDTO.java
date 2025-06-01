package com.healthkeeper.dto;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

@Data
public class ShoppingCartDTO {
    private Long id;
    private Long userId;
    private List<ShoppingCartItemDTO> items;
    private BigDecimal totalAmount;
} 