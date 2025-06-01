package com.healthkeeper.service;

import com.healthkeeper.dto.ShoppingCartDTO;
import com.healthkeeper.dto.ShoppingCartItemDTO;
import com.healthkeeper.entity.*;
import com.healthkeeper.repository.ProductRepository;
import com.healthkeeper.repository.ShoppingCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ShoppingCartService {
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public ShoppingCartDTO addToCart(Long productId, Integer quantity, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ShoppingCart cart = shoppingCartRepository.findByUser(user)
                .orElseGet(() -> {
                    ShoppingCart newCart = new ShoppingCart();
                    newCart.setUser(user);
                    return shoppingCartRepository.save(newCart);
                });

        Optional<ShoppingCartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            ShoppingCartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            ShoppingCartItem newItem = new ShoppingCartItem();
            newItem.setShoppingCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        ShoppingCart savedCart = shoppingCartRepository.save(cart);
        return convertToDTO(savedCart);
    }

    @Transactional
    public ShoppingCartDTO updateCartItemQuantity(Long itemId, Integer quantity, User user) {
        ShoppingCart cart = shoppingCartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        ShoppingCartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setQuantity(quantity);
        ShoppingCart savedCart = shoppingCartRepository.save(cart);
        return convertToDTO(savedCart);
    }

    @Transactional
    public ShoppingCartDTO removeFromCart(Long itemId, User user) {
        ShoppingCart cart = shoppingCartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        ShoppingCart savedCart = shoppingCartRepository.save(cart);
        return convertToDTO(savedCart);
    }

    @Transactional(readOnly = true)
    public ShoppingCartDTO getCart(User user) {
        return shoppingCartRepository.findByUser(user)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    private ShoppingCartDTO convertToDTO(ShoppingCart cart) {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ShoppingCartItem item : cart.getItems()) {
            ShoppingCartItemDTO itemDTO = new ShoppingCartItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductName(item.getProduct().getName());
            itemDTO.setProductPrice(item.getProduct().getPrice());
            itemDTO.setProductImage(item.getProduct().getImageUrl());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setSubtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            dto.getItems().add(itemDTO);
            totalAmount = totalAmount.add(itemDTO.getSubtotal());
        }
        dto.setTotalAmount(totalAmount);

        return dto;
    }
} 