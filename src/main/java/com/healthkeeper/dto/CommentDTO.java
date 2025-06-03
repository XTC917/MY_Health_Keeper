package com.healthkeeper.dto;


import lombok.Data;

@Data
public class CommentDTO {
    private Long momentId;
    private String content;
    private Long parentId;
}
