package com.healthkeeper.repository;

import com.healthkeeper.entity.Order;
import com.healthkeeper.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, String status);
    Page<Order> findByUser(User user, Pageable pageable);
    List<Order> findByItems_Product_Seller(User seller);
} 