package com.healthkeeper.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private List<OrderItemDTO> items;
    private BigDecimal totalAmount;
    private String status;
    private String trackingNumber;
    private LocalDateTime shippedAt;
    private LocalDateTime createdAt;
    private String shippingAddress;
} 