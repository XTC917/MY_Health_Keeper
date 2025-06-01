package com.healthkeeper.controller;

import com.healthkeeper.dto.CartDTO;
import com.healthkeeper.dto.ErrorResponse;
import com.healthkeeper.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = {
    "http://localhost:8080",
    "https://my-health-keeper.vercel.app",
    "https://6186-2001-da8-201d-1113-00-f435.ngrok-free.app"
}, allowCredentials = "true")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<?> getCart(Authentication authentication) {
        try {
            log.info("Getting cart for user: {}", authentication.getName());
            return ResponseEntity.ok(cartService.getCart(authentication.getName()));
        } catch (Exception e) {
            log.error("Error getting cart for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to get cart: " + e.getMessage()));
        }
    }

    @PostMapping("/items")
    public ResponseEntity<?> addToCart(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            int quantity = Integer.parseInt(request.get("quantity").toString());
            
            log.info("Adding item to cart - Product ID: {}, Quantity: {}, User: {}", 
                    productId, quantity, authentication.getName());
            CartDTO cart = cartService.addItem(authentication.getName(), productId, quantity);
            log.info("Successfully added item to cart - Product ID: {}, Quantity: {}, User: {}", 
                    productId, quantity, authentication.getName());
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            log.error("Business error adding item to cart - User: {}, Error: {}", 
                    authentication.getName(), e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error adding item to cart - User: {}", 
                    authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to add item to cart: " + e.getMessage()));
        }
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateItemQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        try {
            log.info("Updating cart item quantity - Item ID: {}, Quantity: {}, User: {}", 
                    itemId, quantity, authentication.getName());
            CartDTO cart = cartService.updateItemQuantity(authentication.getName(), itemId, quantity);
            log.info("Successfully updated cart item quantity - Item ID: {}, Quantity: {}, User: {}", 
                    itemId, quantity, authentication.getName());
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            log.error("Business error updating cart item quantity - Item ID: {}, Quantity: {}, User: {}, Error: {}", 
                    itemId, quantity, authentication.getName(), e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating cart item quantity - Item ID: {}, Quantity: {}, User: {}", 
                    itemId, quantity, authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to update cart item quantity: " + e.getMessage()));
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        try {
            log.info("Removing item from cart - Item ID: {}, User: {}", 
                    itemId, authentication.getName());
            CartDTO cart = cartService.removeItem(authentication.getName(), itemId);
            log.info("Successfully removed item from cart - Item ID: {}, User: {}", 
                    itemId, authentication.getName());
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            log.error("Business error removing item from cart - Item ID: {}, User: {}, Error: {}", 
                    itemId, authentication.getName(), e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error removing item from cart - User: {}, Item ID: {}", authentication.getName(), itemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to remove item from cart: " + e.getMessage()));
        }
    }
} 