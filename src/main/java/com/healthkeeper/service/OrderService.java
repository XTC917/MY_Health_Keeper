package com.healthkeeper.service;

import com.healthkeeper.dto.OrderDTO;
import com.healthkeeper.dto.OrderItemDTO;
import com.healthkeeper.dto.CartDTO;
import com.healthkeeper.dto.CartDTO.CartItemDTO;
import com.healthkeeper.entity.*;
import com.healthkeeper.repository.OrderRepository;
import com.healthkeeper.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Slf4j
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Transactional
    public OrderDTO createOrderFromCart(User user) {
        try {
            log.info("Creating order from cart for user: {}", user.getUsername());
            
            CartDTO cart = cartService.getCart(user.getUsername());
            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                log.error("Cart is empty for user: {}", user.getUsername());
                throw new RuntimeException("Cart is empty");
            }
            log.info("Retrieved cart with {} items", cart.getItems().size());
            
            Order order = new Order();
            order.setUser(user);
            order.setTotalAmount(cart.getTotalAmount());
            order.setStatus(Order.OrderStatus.PENDING);
            log.info("Created new order with total amount: {}", order.getTotalAmount());

            for (CartItemDTO item : cart.getItems()) {
                log.info("Processing cart item: productId={}, quantity={}", item.getProductId(), item.getQuantity());
                
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> {
                            log.error("Product not found: {}", item.getProductId());
                            return new RuntimeException("Product not found");
                        });
                log.info("Found product: {}", product.getName());
                
                if (product.getStock() < item.getQuantity()) {
                    log.error("Insufficient stock for product: {}. Available: {}, Requested: {}", 
                            product.getId(), product.getStock(), item.getQuantity());
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }
                
                orderItem.setProduct(product);
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPrice(item.getPrice());
                orderItem.setSubtotal(item.getSubtotal());
                order.getItems().add(orderItem);
                log.info("Added order item: quantity={}, price={}, subtotal={}", 
                        orderItem.getQuantity(), orderItem.getPrice(), orderItem.getSubtotal());
            }

            Order savedOrder = orderRepository.save(order);
            log.info("Successfully saved order: {}", savedOrder.getId());
            
            return convertToDTO(savedOrder);
        } catch (Exception e) {
            log.error("Error creating order from cart", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUser(User user) {
        try {
            log.info("Getting orders for user: {}", user.getUsername());
            List<Order> orders = orderRepository.findByUser(user);
            log.info("Found {} orders in repository", orders.size());
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            log.info("Converted {} orders to DTOs", orderDTOs.size());
            return orderDTOs;
        } catch (Exception e) {
            log.error("Error getting orders for user: {}", user.getUsername(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id, User user) {
        try {
            log.info("Getting order by id: {} for user: {}", id, user.getUsername());
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Order not found with id: {}", id);
                        return new RuntimeException("Order not found");
                    });
            
            if (!order.getUser().getId().equals(user.getId())) {
                log.error("Unauthorized access attempt - Order: {}, User: {}", id, user.getUsername());
                throw new RuntimeException("Unauthorized access to order");
            }
            
            log.info("Found order: {} for user: {}", order.getId(), user.getUsername());
            return convertToDTO(order);
        } catch (Exception e) {
            log.error("Error getting order by id: {} for user: {}", id, user.getUsername(), e);
            throw e;
        }
    }

    @Transactional
    public OrderDTO createOrder(User user, Map<String, Object> orderData) {
        try {
            log.info("Creating direct order for user: {}", user.getUsername());
            
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
            if (items == null || items.isEmpty()) {
                log.error("No items provided for order");
                throw new RuntimeException("No items provided for order");
            }
            
            Order order = new Order();
            order.setUser(user);
            order.setStatus(Order.OrderStatus.PENDING);
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            // 设置收货地址
            if (orderData.containsKey("shippingAddress")) {
                order.setShippingAddress((String) orderData.get("shippingAddress"));
            }
            
            for (Map<String, Object> item : items) {
                Long productId = Long.valueOf(item.get("productId").toString());
                int quantity = Integer.parseInt(item.get("quantity").toString());
                
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> {
                            log.error("Product not found: {}", productId);
                            return new RuntimeException("Product not found");
                        });
                
                if (product.getStock() < quantity) {
                    log.error("Insufficient stock for product: {}. Available: {}, Requested: {}", 
                            product.getId(), product.getStock(), quantity);
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }
                
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(quantity);
                orderItem.setPrice(product.getPrice());
                orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
                order.getItems().add(orderItem);
                
                totalAmount = totalAmount.add(orderItem.getSubtotal());
            }
            
            order.setTotalAmount(totalAmount);
            Order savedOrder = orderRepository.save(order);
            log.info("Successfully saved direct order: {}", savedOrder.getId());
            
            return convertToDTO(savedOrder);
        } catch (Exception e) {
            log.error("Error creating direct order", e);
            throw e;
        }
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus newStatus, User user) {
        try {
            log.info("Updating order status - Order ID: {}, New Status: {}, User: {}", 
                    orderId, newStatus, user.getUsername());
            
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found: {}", orderId);
                        return new RuntimeException("Order not found");
                    });
            
            if (!order.getUser().getId().equals(user.getId())) {
                log.error("Unauthorized access attempt - Order: {}, User: {}", orderId, user.getUsername());
                throw new RuntimeException("Unauthorized access to order");
            }
            
            if (newStatus == Order.OrderStatus.PAID) {
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    int newStock = product.getStock() - item.getQuantity();
                    if (newStock < 0) {
                        throw new RuntimeException("Insufficient stock for product: " + product.getName());
                    }
                    product.setStock(newStock);
                    productRepository.save(product);
                    log.info("Updated stock for product: {}, New stock: {}", product.getId(), newStock);
                }
            }
            
            order.setStatus(newStatus);
            Order updatedOrder = orderRepository.save(order);
            log.info("Successfully updated order status - Order ID: {}, New Status: {}", 
                    orderId, newStatus);
            
            return convertToDTO(updatedOrder);
        } catch (Exception e) {
            log.error("Error updating order status - Order ID: {}, New Status: {}, User: {}", 
                    orderId, newStatus, user.getUsername(), e);
            throw e;
        }
    }

    @Transactional
    public void deleteOrder(Long orderId, User user) {
        try {
            log.info("Deleting order - Order ID: {}, User: {}", orderId, user.getUsername());
            
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found: {}", orderId);
                        return new RuntimeException("Order not found");
                    });
            
            if (!order.getUser().getId().equals(user.getId())) {
                log.error("Unauthorized access attempt - Order: {}, User: {}", orderId, user.getUsername());
                throw new RuntimeException("Unauthorized access to order");
            }
            
            orderRepository.delete(order);
            log.info("Successfully deleted order - Order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Error deleting order - Order ID: {}, User: {}", orderId, user.getUsername(), e);
            throw e;
        }
    }

    @Transactional
    public OrderDTO shipOrder(Long orderId, String trackingNumber, User seller) {
        try {
            log.info("Shipping order - Order ID: {}, Tracking Number: {}, Seller: {}", 
                    orderId, trackingNumber, seller.getUsername());
            
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found: {}", orderId);
                        return new RuntimeException("Order not found");
                    });
            
            // 验证订单中的商品是否属于该卖家
            boolean isSeller = order.getItems().stream()
                    .anyMatch(item -> item.getProduct().getSeller().getId().equals(seller.getId()));
            
            if (!isSeller) {
                log.error("Unauthorized shipping attempt - Order: {}, Seller: {}", orderId, seller.getUsername());
                throw new RuntimeException("Unauthorized shipping attempt");
            }
            
            if (order.getStatus() != Order.OrderStatus.PAID) {
                throw new RuntimeException("Order is not in PAID status");
            }
            
            order.setStatus(Order.OrderStatus.SHIPPED);
            order.setTrackingNumber(trackingNumber);
            order.setShippedAt(LocalDateTime.now());
            
            Order updatedOrder = orderRepository.save(order);
            log.info("Successfully shipped order - Order ID: {}, Tracking Number: {}", 
                    orderId, trackingNumber);
            
            return convertToDTO(updatedOrder);
        } catch (Exception e) {
            log.error("Error shipping order - Order ID: {}, Seller: {}", orderId, seller.getUsername(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getSellerOrders(User seller) {
        try {
            log.info("Getting orders for seller: {}", seller.getUsername());
            List<Order> orders = orderRepository.findByItems_Product_Seller(seller);
            log.info("Found {} orders for seller", orders.size());
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            log.info("Converted {} orders to DTOs", orderDTOs.size());
            return orderDTOs;
        } catch (Exception e) {
            log.error("Error getting orders for seller: {}", seller.getUsername(), e);
            throw e;
        }
    }

    @Transactional
    public OrderDTO confirmOrderReceipt(Long orderId, User user) {
        try {
            log.info("Confirming order receipt - Order ID: {}, User: {}", orderId, user.getUsername());
            
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found: {}", orderId);
                        return new RuntimeException("Order not found");
                    });
            
            if (!order.getUser().getId().equals(user.getId())) {
                log.error("Unauthorized confirmation attempt - Order: {}, User: {}", orderId, user.getUsername());
                throw new RuntimeException("Unauthorized confirmation attempt");
            }
            
            if (order.getStatus() != Order.OrderStatus.SHIPPED) {
                throw new RuntimeException("Order must be in SHIPPED status to confirm receipt");
            }
            
            order.setStatus(Order.OrderStatus.DELIVERED);
            Order updatedOrder = orderRepository.save(order);
            log.info("Successfully confirmed order receipt - Order ID: {}", orderId);
            
            return convertToDTO(updatedOrder);
        } catch (Exception e) {
            log.error("Error confirming order receipt - Order ID: {}, User: {}", orderId, user.getUsername(), e);
            throw e;
        }
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setTrackingNumber(order.getTrackingNumber());
        dto.setShippedAt(order.getShippedAt());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setItems(new ArrayList<>());

        for (OrderItem item : order.getItems()) {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductName(item.getProduct().getName());
            itemDTO.setProductPrice(item.getPrice());
            itemDTO.setProductImage(item.getProduct().getImageUrl());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setSubtotal(item.getSubtotal());
            dto.getItems().add(itemDTO);
        }

        return dto;
    }
} 