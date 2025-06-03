package com.healthkeeper.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupabaseConfig {
    @Value("${supabase.url}")
    private String url;

    @Value("${supabase.key}")
    private String key;

    @Value("${supabase.bucket-name:public}")
    private String bucketName;

    public String getUrl() {
        return url;
    }

    public String getKey() {
        return key;
    }

    public String getBucketName() {
        return bucketName;
    }
} 