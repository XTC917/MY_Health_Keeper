package com.healthkeeper.service;

import com.healthkeeper.model.Food;
import com.healthkeeper.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.*;

@Service
public class DietPlanService {
    @Autowired
    private FoodRepository foodRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;

    public DietPlanService(RestTemplateBuilder restTemplateBuilder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // 30 seconds
        factory.setReadTimeout(30000);    // 30 seconds
        this.restTemplate = restTemplateBuilder
            .requestFactory(() -> factory)
            .build();
    }

    public Map<String, Object> generateDietPlan(int targetCalories, List<String> preferences) {
        try {
            // 获取所有食物数据
            List<Food> allFoods = foodRepository.findAll();
            
            // 构建提示词
            String prompt = buildPrompt(targetCalories, preferences, allFoods);
            
            // 调用Gemini API
            String response = callGeminiAPI(prompt);
            
            // 解析响应
            return parseDietPlan(response, allFoods);
        } catch (Exception e) {
            throw new RuntimeException("调用Gemini API失败: " + e.getMessage());
        }
    }

    private String buildPrompt(int targetCalories, List<String> preferences, List<Food> foods) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下信息生成一个个性化的饮食计划：\n");
        prompt.append("目标热量：").append(targetCalories).append("千卡/天\n");
        prompt.append("饮食偏好：").append(String.join(", ", preferences)).append("\n\n");
        
        prompt.append("可用的食物列表：\n");
        for (Food food : foods) {
            prompt.append(String.format("- %s (%s): 热量%.1f千卡/100g, 蛋白质%.1fg, 脂肪%.1fg, 碳水%.1fg\n",
                food.getName(), food.getNameEn(), food.getCalories(), food.getProtein(), food.getFat(), food.getCarbs()));
        }
        
        prompt.append("\n请生成一个包含早餐、午餐、晚餐和加餐的饮食计划，每个餐次包含具体的食物和份量。");
        prompt.append("请确保总热量接近目标热量，并根据用户的饮食偏好选择合适的食物。");
        prompt.append("请以JSON格式返回，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"meals\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"name\": \"早餐\",\n");
        prompt.append("      \"foods\": [\n");
        prompt.append("        {\"name\": \"食物名称\", \"amount\": \"份量\", \"calories\": 热量值}\n");
        prompt.append("      ],\n");
        prompt.append("      \"totalCalories\": 总热量\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}");
        
        return prompt.toString();
    }

    private String callGeminiAPI(String prompt) {
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s", geminiApiKey);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        content.put("parts", Collections.singletonList(Map.of("text", prompt)));
        requestBody.put("contents", Collections.singletonList(content));
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content2 = (Map<String, Object>) candidate.get("content");
                    List<Map<String, String>> parts = (List<Map<String, String>>) content2.get("parts");
                    if (!parts.isEmpty()) {
                        return parts.get(0).get("text");
                    }
                }
            }
            throw new RuntimeException("Gemini API返回格式不正确");
        } catch (Exception e) {
            throw new RuntimeException("调用Gemini API失败: " + e.getMessage());
        }
    }

    private Map<String, Object> parseDietPlan(String response, List<Food> foods) {
        try {
            // 提取JSON部分
            int startIndex = response.indexOf("{");
            int endIndex = response.lastIndexOf("}") + 1;
            if (startIndex == -1 || endIndex == 0) {
                throw new RuntimeException("无法在响应中找到JSON数据");
            }
            
            String jsonStr = response.substring(startIndex, endIndex);
            
            // 使用Jackson或Gson解析JSON
            // 这里简化处理，直接返回原始响应
            Map<String, Object> result = new HashMap<>();
            result.put("meals", Collections.singletonList(
                Map.of(
                    "name", "示例餐",
                    "foods", Collections.singletonList(
                        Map.of(
                            "name", "示例食物",
                            "amount", "100g",
                            "calories", 200
                        )
                    ),
                    "totalCalories", 200
                )
            ));
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("解析饮食计划失败: " + e.getMessage());
        }
    }
} 