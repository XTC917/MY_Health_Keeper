package com.healthkeeper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    // 移除重复的CORS配置，使用WebSecurityConfig中的配置
    // 这个类保留为空，以便将来可能的扩展
} 