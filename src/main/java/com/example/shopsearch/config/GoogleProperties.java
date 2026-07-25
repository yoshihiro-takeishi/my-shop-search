package com.example.shopsearch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "google.places")
@Data
public class GoogleProperties {
    /**
     * Google Places APIのキー
     */
    private String apiKey;
}