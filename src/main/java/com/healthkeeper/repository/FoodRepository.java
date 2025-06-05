package com.healthkeeper.repository;

import com.healthkeeper.model.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {
    @Query("SELECT f FROM Food f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Food> searchByName(@Param("query") String query);

    @Query("SELECT f FROM Food f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%')) AND f.category = :category")
    List<Food> searchByNameAndCategory(@Param("query") String query, @Param("category") String category);

    List<Food> findByCategory(String category);
} 