package com.healthkeeper.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private LocalDateTime createTime;
    private UserDto user;

    @Data
    public static class UserDto {
        private Long id;
        private String username;
    }
}
