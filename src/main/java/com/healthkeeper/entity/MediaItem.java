package com.healthkeeper.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@AllArgsConstructor // 添加全参构造函数
@NoArgsConstructor  // 确保无参构造函数存在（JPA 需要）
public class MediaItem {
    private String type; // image/video
    private String url;  // 文件路径
}