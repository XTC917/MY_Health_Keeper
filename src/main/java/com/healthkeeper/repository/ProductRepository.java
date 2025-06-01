package com.healthkeeper.repository;

import com.healthkeeper.entity.Product;
import com.healthkeeper.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySeller(User seller);
    Page<Product> findByCategory(String category, Pageable pageable);
    List<Product> findByIsAvailableTrue();
    Page<Product> findByNameContaining(String name, Pageable pageable);
    List<Product> findByBrand(String brand);
    Page<Product> findByNameContainingAndCategory(String name, String category, Pageable pageable);
    
    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findDistinctCategories();
    
    List<Product> findTop5ByOrderByCreatedAtDesc();

    List<Product> findByIsActiveTrue();
} 