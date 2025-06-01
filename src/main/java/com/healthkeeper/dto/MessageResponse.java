package com.healthkeeper.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageResponse {
    private String message;
    private Long userId;

    public MessageResponse(String message) {
        this.message = message;
        this.userId = null;
    }

    public MessageResponse(String message, Long userId) {
        this.message = message;
        this.userId = userId;
    }
} 