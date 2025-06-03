package com.healthkeeper.service;

import com.healthkeeper.config.SupabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class SupabaseStorageService {
    
    @Autowired
    private SupabaseConfig supabaseConfig;
    
    private final RestTemplate restTemplate;

    public SupabaseStorageService() {
        this.restTemplate = new RestTemplate();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        byte[] fileBytes = file.getBytes();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("apikey", supabaseConfig.getKey());
        headers.set("Authorization", "Bearer " + supabaseConfig.getKey());
        headers.set("x-upsert", "true");

        String uploadUrl = String.format("%s/storage/v1/object/%s/%s", 
            supabaseConfig.getUrl(), 
            supabaseConfig.getBucketName(), 
            fileName);
        
        log.info("Uploading file to URL: {}", uploadUrl);
        
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            log.info("Upload response status: {}", response.getStatusCode());
            log.info("Upload response body: {}", response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK) {
                String fileUrl = String.format("%s/storage/v1/object/public/%s/%s", 
                    supabaseConfig.getUrl(),
                    supabaseConfig.getBucketName(), 
                    fileName);
                log.info("File uploaded successfully. URL: {}", fileUrl);
                return fileUrl;
            } else {
                String errorMsg = String.format("Failed to upload file. Status: %s, Body: %s", 
                    response.getStatusCode(), response.getBody());
                log.error(errorMsg);
                throw new IOException(errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = String.format("Failed to upload file to Supabase: %s", e.getMessage());
            log.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        }
    }

    public void deleteFile(String fileUrl) throws IOException {
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseConfig.getKey());
            headers.set("Authorization", "Bearer " + supabaseConfig.getKey());
            
            String deleteUrl = String.format("%s/storage/v1/object/%s/%s", 
                supabaseConfig.getUrl(),
                supabaseConfig.getBucketName(), 
                fileName);
            
            log.info("Deleting file from URL: {}", deleteUrl);
            
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                requestEntity,
                String.class
            );
            
            log.info("Delete response status: {}", response.getStatusCode());
            log.info("Delete response body: {}", response.getBody());
            
            if (response.getStatusCode() != HttpStatus.OK) {
                String errorMsg = String.format("Failed to delete file. Status: %s, Body: %s", 
                    response.getStatusCode(), response.getBody());
                log.error(errorMsg);
                throw new IOException(errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = String.format("Failed to delete file from Supabase: %s", e.getMessage());
            log.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }
} 