package com.healthkeeper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthkeeper.model.Food;
import com.healthkeeper.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DietPlanService {
    @Autowired
    private FoodRepository foodRepository;

    @Value("${openrouter.api.key}")
    private String openRouterApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> generateDietPlan(int targetCalories, List<String> preferences) {
        // 获取所有食物数据
        List<Food> allFoods = foodRepository.findAll();
        
        // 构建提示词
        String prompt = buildPrompt(targetCalories, preferences, allFoods);
        
        // 调用OpenRouter API
        String response = callOpenRouterAPI(prompt);
        
        // 解析响应并生成饮食计划
        return parseDietPlan(response, allFoods);
    }

    private String buildPrompt(int targetCalories, List<String> preferences, List<Food> foods) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下要求生成一份饮食计划：\n");
        prompt.append("目标热量：").append(targetCalories).append("千卡/天\n");
        prompt.append("饮食偏好：").append(String.join("、", preferences)).append("\n\n");
        
        prompt.append("可用的食物列表：\n");
        for (Food food : foods) {
            prompt.append(String.format("%s：%d千卡/100g，蛋白质%.1fg，脂肪%.1fg，碳水%.1fg\n",
                    food.getName(), food.getCalories().intValue(),
                    food.getProtein(), food.getFat(), food.getCarbs()));
        }
        
        prompt.append("\n请生成一份包含早餐、午餐、晚餐和加餐的饮食计划，每餐包含具体的食物和份量。");
        prompt.append("请确保总热量接近目标值，并且营养均衡。");
        prompt.append("请以JSON格式返回，格式如下：");
        prompt.append("""
            {
                "meals": [
                    {
                        "name": "早餐",
                        "foods": [
                            {
                                "name": "食物名称",
                                "amount": "份量",
                                "calories": 热量
                            }
                        ],
                        "totalCalories": 总热量
                    }
                ]
            }
            """);
        
        return prompt.toString();
    }

    private String callOpenRouterAPI(String prompt) {
        String url = "https://openrouter.ai/api/v1/chat/completions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openRouterApiKey);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "anthropic/claude-3-opus-20240229");
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        
        requestBody.put("messages", messages);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message2 = (Map<String, Object>) choice.get("message");
            return (String) message2.get("content");
        } catch (Exception e) {
            throw new RuntimeException("调用OpenRouter API失败", e);
        }
    }

    private Map<String, Object> parseDietPlan(String response, List<Food> foods) {
        try {
            // 提取JSON部分
            int startIndex = response.indexOf('{');
            int endIndex = response.lastIndexOf('}') + 1;
            String jsonStr = response.substring(startIndex, endIndex);
            
            // 解析JSON
            Map<String, Object> dietPlan = objectMapper.readValue(jsonStr, Map.class);
            
            // 验证和补充食物信息
            List<Map<String, Object>> meals = (List<Map<String, Object>>) dietPlan.get("meals");
            for (Map<String, Object> meal : meals) {
                List<Map<String, Object>> mealFoods = (List<Map<String, Object>>) meal.get("foods");
                for (Map<String, Object> food : mealFoods) {
                    String foodName = (String) food.get("name");
                    Optional<Food> foodInfo = foods.stream()
                            .filter(f -> f.getName().equals(foodName))
                            .findFirst();
                    
                    if (foodInfo.isPresent()) {
                        Food f = foodInfo.get();
                        food.put("protein", f.getProtein());
                        food.put("fat", f.getFat());
                        food.put("carbs", f.getCarbs());
                    }
                }
            }
            
            return dietPlan;
        } catch (Exception e) {
            throw new RuntimeException("解析饮食计划失败", e);
        }
    }
} 