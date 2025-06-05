package com.healthkeeper.controller;

import com.healthkeeper.service.DietPlanService;
import com.healthkeeper.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DietController {
    @Autowired
    private FoodService foodService;

    @Autowired
    private DietPlanService dietPlanService;

    @GetMapping("/food/search")
    public ResponseEntity<?> searchFood(
            @RequestParam String query,
            @RequestParam(required = false) String category) {
        try {
            List<?> results = foodService.searchFood(query, category);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/diet/plan")
    public ResponseEntity<?> generateDietPlan(
            @RequestBody Map<String, Object> request) {
        try {
            int targetCalories = (int) request.get("targetCalories");
            @SuppressWarnings("unchecked")
            List<String> preferences = (List<String>) request.get("preferences");
            
            Map<String, Object> plan = dietPlanService.generateDietPlan(targetCalories, preferences);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
} 