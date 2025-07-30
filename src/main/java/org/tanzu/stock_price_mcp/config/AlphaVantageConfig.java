package org.tanzu.stock_price_mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "alphavantage.api")
@Validated
public class AlphaVantageConfig {

    @NotBlank(message = "AlphaVantage API key is required")
    private String key;

    @NotBlank(message = "AlphaVantage API base URL is required")
    private String baseUrl;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}