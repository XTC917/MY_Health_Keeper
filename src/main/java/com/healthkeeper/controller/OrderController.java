package com.healthkeeper.controller;

import com.healthkeeper.dto.OrderDTO;
import com.healthkeeper.dto.ErrorResponse;
import com.healthkeeper.entity.User;
import com.healthkeeper.entity.Order;
import com.healthkeeper.security.services.UserDetailsImpl;
import com.healthkeeper.service.OrderService;
import com.healthkeeper.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/checkout")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderData, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            log.info("Received checkout request from user: {}", user.getUsername());
            OrderDTO order;
            if (orderData != null && orderData.containsKey("items")) {
                // 直接创建订单
                order = orderService.createOrder(user, orderData);
            } else {
                // 从购物车创建订单
                order = orderService.createOrderFromCart(user);
            }
            log.info("Successfully created order: {}", order.getId());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to create order: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getMyOrders(Authentication authentication) {
        try {
            log.info("Getting orders for user: {}", authentication.getName());
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            log.info("User details: {}", userDetails);
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> {
                        log.error("User not found with id: {}", userDetails.getId());
                        return new RuntimeException("User not found");
                    });
            log.info("Found user: {}", user.getUsername());
            List<OrderDTO> orders = orderService.getOrdersByUser(user);
            log.info("Found {} orders for user: {}", orders.size(), user.getUsername());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting orders for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ArrayList<>());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Getting order by id: {} for user: {}", id, authentication.getName());
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> {
                        log.error("User not found with id: {}", userDetails.getId());
                        return new RuntimeException("User not found");
                    });
            log.info("Found user: {}", user.getUsername());
            OrderDTO order = orderService.getOrderById(id, user);
            log.info("Found order: {} for user: {}", order.getId(), user.getUsername());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error getting order by id: {} for user: {}", id, authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to get order: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {
        try {
            log.info("Updating order status - Order ID: {}, Status: {}, User: {}", 
                    id, status, authentication.getName());
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            OrderDTO updatedOrder = orderService.updateOrderStatus(
                    id, 
                    Order.OrderStatus.valueOf(status.toUpperCase()),
                    user
            );
            
            log.info("Successfully updated order status - Order ID: {}, Status: {}", 
                    id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            log.error("Error updating order status - Order ID: {}, Status: {}, User: {}", 
                    id, status, authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to update order status: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Deleting order - Order ID: {}, User: {}", id, authentication.getName());
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            orderService.deleteOrder(id, user);
            
            log.info("Successfully deleted order - Order ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting order - Order ID: {}, User: {}", id, authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to delete order: " + e.getMessage()));
        }
    }

    @GetMapping("/seller")
    public ResponseEntity<?> getSellerOrders(Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            log.info("Getting seller orders for user: {}", user.getUsername());
            
            List<OrderDTO> orders = orderService.getSellerOrders(user);
            log.info("Found {} orders for seller", orders.size());
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting seller orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to get seller orders: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<?> shipOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            log.info("Received shipping request for order: {}", id);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String trackingNumber = request.get("trackingNumber");
            if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
                throw new RuntimeException("Tracking number is required");
            }
            
            OrderDTO updatedOrder = orderService.shipOrder(id, trackingNumber, user);
            log.info("Successfully processed shipping request for order: {}", id);
            
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            log.error("Error processing shipping request for order: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to ship order: " + e.getMessage()));
        }
    }
} 