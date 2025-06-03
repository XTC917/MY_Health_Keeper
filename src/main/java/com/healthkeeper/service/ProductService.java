package com.healthkeeper.service;

import com.healthkeeper.dto.ProductDTO;
import com.healthkeeper.entity.Product;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.ProductRepository;
import com.healthkeeper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, String username) {
        try {
            System.out.println("Creating product with DTO: " + productDTO);
            System.out.println("Seller username: " + username);
            
            // 获取卖家信息
            User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            System.out.println("Found seller: " + seller);
            
            // 创建商品实体
            Product product = new Product();
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());
            product.setStock(productDTO.getStock());
            product.setCategory(productDTO.getCategory());
            product.setBrand(productDTO.getBrand());
            product.setImageUrl(productDTO.getImageUrl());
            product.setSeller(seller);
            
            // 保存商品
            Product savedProduct = productRepository.save(product);
            System.out.println("Product saved successfully: " + savedProduct);
            
            // 转换为DTO并返回
            return convertToDTO(savedProduct);
        } catch (Exception e) {
            System.err.println("Error in createProduct service: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsBySeller(User seller) {
        return productRepository.findBySeller(seller)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCategory(product.getCategory());
        dto.setBrand(product.getBrand());
        dto.setImageUrl(product.getImageUrl());
        dto.setSellerId(product.getSeller().getId());
        dto.setSellerName(product.getSeller().getUsername());
        return dto;
    }

    @Transactional
    public void deleteProduct(Long id, String username) {
        try {
            System.out.println("Deleting product with ID: " + id);
            System.out.println("Requested by user: " + username);
            
            // 获取商品
            Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
            
            // 验证用户权限
            if (!product.getSeller().getUsername().equals(username)) {
                throw new RuntimeException("无权删除此商品");
            }
            
            // 检查商品是否被订单引用
            if (!product.getOrderItems().isEmpty()) {
                // 如果商品被订单引用，则将其标记为不可用而不是删除
                product.setIsActive(false);
                product.setIsAvailable(false);
                productRepository.save(product);
                System.out.println("Product marked as inactive due to existing orders");
            } else {
                // 如果商品没有被订单引用，则直接删除
            productRepository.delete(product);
            System.out.println("Product deleted successfully");
            }
        } catch (Exception e) {
            System.err.println("Error in deleteProduct service: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, String username) {
        try {
            System.out.println("Updating product - ID: " + id + ", User: " + username);
            
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            if (!product.getSeller().getId().equals(user.getId())) {
                throw new RuntimeException("无权修改此商品");
            }
            
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());
            product.setStock(productDTO.getStock());
            if (productDTO.getImageUrl() != null) {
                product.setImageUrl(productDTO.getImageUrl());
            }
            
            Product updatedProduct = productRepository.save(product);
            System.out.println("Product updated successfully: " + updatedProduct);
            
            return convertToDTO(updatedProduct);
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 