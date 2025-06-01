package com.healthkeeper.controller;

import com.healthkeeper.dto.ProductDTO;
import com.healthkeeper.dto.ErrorResponse;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.UserRepository;
import com.healthkeeper.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO, Authentication authentication) {
        try {
            System.out.println("Received product creation request: " + productDTO);
            ProductDTO createdProduct = productService.createProduct(productDTO, authentication.getName());
            System.out.println("Product created successfully: " + createdProduct);
            return ResponseEntity.ok(createdProduct);
        } catch (Exception e) {
            System.err.println("Error creating product: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ErrorResponse("创建商品失败: " + e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProductDTO>> getMyProducts(Authentication authentication) {
        try {
            System.out.println("Getting products for user: " + authentication.getName());
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            List<ProductDTO> products = productService.getProductsBySeller(user);
            System.out.println("Found " + products.size() + " products");
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            System.err.println("Error getting my products: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, Authentication authentication) {
        try {
            System.out.println("Deleting product with ID: " + id);
            productService.deleteProduct(id, authentication.getName());
            System.out.println("Product deleted successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ErrorResponse("删除商品失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO,
            Authentication authentication) {
        try {
            System.out.println("Updating product with ID: " + id);
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO, authentication.getName());
            System.out.println("Product updated successfully: " + updatedProduct);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ErrorResponse("更新商品失败: " + e.getMessage()));
        }
    }
} 