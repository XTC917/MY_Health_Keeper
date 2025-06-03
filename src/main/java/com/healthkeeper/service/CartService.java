package com.healthkeeper.service;

import com.healthkeeper.dto.CartDTO;
import com.healthkeeper.entity.*;
import com.healthkeeper.repository.CartRepository;
import com.healthkeeper.repository.CartItemRepository;
import com.healthkeeper.repository.ProductRepository;
import com.healthkeeper.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;

    @Transactional
    public CartDTO getCart(String username) {
        log.info("Getting cart for user: {}", username);
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Cart cart = cartRepository.findByUser(user)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUser(user);
                        return cartRepository.save(newCart);
                    });
            
            log.info("Successfully retrieved cart for user: {}, Cart ID: {}", username, cart.getId());
            return convertToDTO(cart);
        } catch (Exception e) {
            log.error("Error getting cart for user: {}", username, e);
            throw e;
        }
    }

    @Transactional
    public CartDTO addItem(String username, Long productId, int quantity) {
        log.info("Adding item to cart - User: {}, Product ID: {}, Quantity: {}", username, productId, quantity);
        
        try {
            if (quantity <= 0) {
                throw new RuntimeException("Quantity must be greater than 0");
            }
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            log.info("Found user: {}", user.getId());
            
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            log.info("Found product: {}, Available: {}, Stock: {}, Price: {}", 
                    product.getId(), product.getIsAvailable(), product.getStock(), product.getPrice());
            
            if (!product.getIsAvailable()) {
                throw new RuntimeException("Product is not available");
            }
            
            if (product.getStock() < quantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
            }
            
            Cart cart = cartRepository.findByUser(user)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUser(user);
                        return cartRepository.save(newCart);
                    });
            log.info("Found or created cart: {}", cart.getId());
            
            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();
            
            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                int newQuantity = item.getQuantity() + quantity;
                if (newQuantity > product.getStock()) {
                    throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
                }
                item.setQuantity(newQuantity);
                item.setPrice(product.getPrice());
                cartItemRepository.save(item);
                log.info("Updated existing cart item: {}, New quantity: {}, Price: {}", 
                        item.getId(), newQuantity, item.getPrice());
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                newItem.setPrice(product.getPrice());
                cart.getItems().add(newItem);
                cartItemRepository.save(newItem);
                log.info("Created new cart item: {}, Quantity: {}, Price: {}", 
                        newItem.getId(), quantity, newItem.getPrice());
            }
            
            return convertToDTO(cart);
        } catch (Exception e) {
            log.error("Error adding item to cart - User: {}, Product ID: {}, Quantity: {}", 
                    username, productId, quantity, e);
            throw e;
        }
    }

    @Transactional
    public CartDTO updateItemQuantity(String username, Long itemId, Integer quantity) {
        log.info("Updating cart item quantity - User: {}, Item ID: {}, Quantity: {}", username, itemId, quantity);
        
        try {
            if (quantity <= 0) {
                throw new RuntimeException("Quantity must be greater than 0");
            }
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            CartItem item = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));
            
            if (!item.getCart().getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized access to cart item");
            }
            
            Product product = item.getProduct();
            if (!product.getIsAvailable()) {
                throw new RuntimeException("Product is not available");
            }
            
            if (product.getStock() < quantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
            }
            
            item.setQuantity(quantity);
            item.setPrice(product.getPrice());
            cartItemRepository.save(item);
            log.info("Successfully updated cart item quantity - Item ID: {}, New quantity: {}, Price: {}", 
                    itemId, quantity, item.getPrice());
            
            return convertToDTO(item.getCart());
        } catch (Exception e) {
            log.error("Error updating cart item quantity - User: {}, Item ID: {}, Quantity: {}", 
                    username, itemId, quantity, e);
            throw e;
        }
    }

    @Transactional
    public CartDTO removeItem(String username, Long itemId) {
        log.info("Removing item from cart - User: {}, Item ID: {}", username, itemId);
        
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            CartItem item = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));
            
            if (!item.getCart().getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized access to cart item");
            }
            
            Cart cart = item.getCart();
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            log.info("Successfully removed item from cart - Item ID: {}", itemId);
            
            return convertToDTO(cart);
        } catch (Exception e) {
            log.error("Error removing item from cart - User: {}, Item ID: {}", username, itemId, e);
            throw e;
        }
    }

    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                CartDTO.CartItemDTO itemDTO = new CartDTO.CartItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setProductId(item.getProduct().getId());
                itemDTO.setProductName(item.getProduct().getName());
                itemDTO.setProductImage(item.getProduct().getImageUrl());
                itemDTO.setPrice(item.getPrice());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                dto.getItems().add(itemDTO);
                totalAmount = totalAmount.add(itemDTO.getSubtotal());
            }
        }
        dto.setTotalAmount(totalAmount);
        
        return dto;
    }
} 