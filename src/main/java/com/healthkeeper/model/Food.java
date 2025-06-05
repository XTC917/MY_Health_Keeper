package com.healthkeeper.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "foods")
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Double calories; // 每100克的热量（千卡）

    @Column(nullable = false)
    private Double protein; // 每100克的蛋白质含量（克）

    @Column(nullable = false)
    private Double fat; // 每100克的脂肪含量（克）

    @Column(nullable = false)
    private Double carbs; // 每100克的碳水化合物含量（克）

    @Column
    private String description;

    @Column
    private String imageUrl;
} 