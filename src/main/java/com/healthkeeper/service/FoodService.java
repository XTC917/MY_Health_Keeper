package com.healthkeeper.service;

import com.healthkeeper.model.Food;
import com.healthkeeper.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodService {
    @Autowired
    private FoodRepository foodRepository;

    public List<Food> searchFood(String query, String category) {
        if (category != null && !category.isEmpty()) {
            return foodRepository.searchByNameAndCategory(query, category);
        }
        return foodRepository.searchByName(query);
    }

    public List<Food> getFoodsByCategory(String category) {
        return foodRepository.findByCategory(category);
    }
} 