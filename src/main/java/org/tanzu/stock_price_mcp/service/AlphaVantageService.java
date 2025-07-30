package org.tanzu.stock_price_mcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.tanzu.stock_price_mcp.config.AlphaVantageConfig;
import org.tanzu.stock_price_mcp.exception.AlphaVantageException;
import org.tanzu.stock_price_mcp.model.AlphaVantageResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class AlphaVantageService {

    private static final Logger logger = LoggerFactory.getLogger(AlphaVantageService.class);

    private final WebClient webClient;
    private final AlphaVantageConfig config;

    @Autowired
    public AlphaVantageService(WebClient webClient, AlphaVantageConfig config) {
        this.webClient = webClient;
        this.config = config;
    }

    /**
     * Get global quote for a stock symbol
     * @param symbol Stock symbol (e.g., "IBM", "AAPL")
     * @return AlphaVantageResponse containing the raw API response
     */
    public Mono<AlphaVantageResponse> getGlobalQuote(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Stock symbol cannot be null or empty"));
        }

        String normalizedSymbol = symbol.trim().toUpperCase();
        logger.info("Fetching global quote for symbol: {}", normalizedSymbol);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("www.alphavantage.co")
                        .path("/query")
                        .queryParam("function", "GLOBAL_QUOTE")
                        .queryParam("symbol", normalizedSymbol)
                        .queryParam("apikey", config.getKey())
                        .build())
                .retrieve()
                .bodyToMono(AlphaVantageResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> {
                    if (response != null && response.hasError()) {
                        String errorMsg = response.getErrorMessage();
                        logger.warn("AlphaVantage API returned error for symbol {}: {}", normalizedSymbol, errorMsg);
                        throw new AlphaVantageException(errorMsg, "API_ERROR");
                    }
                    logger.info("Successfully retrieved quote for symbol: {}", normalizedSymbol);
                })
                .doOnError(WebClientResponseException.class, ex -> {
                    logger.error("HTTP error calling AlphaVantage API for symbol {}: {} - {}", 
                            normalizedSymbol, ex.getStatusCode(), ex.getMessage());
                })
                .doOnError(Exception.class, ex -> {
                    if (!(ex instanceof WebClientResponseException)) {
                        logger.error("Error calling AlphaVantage API for symbol {}: {}", normalizedSymbol, ex.getMessage());
                    }
                })
                .onErrorMap(WebClientResponseException.class, ex -> 
                        new AlphaVantageException("HTTP error: " + ex.getStatusCode() + " - " + ex.getMessage(), 
                                "HTTP_ERROR", ex))
                .onErrorMap(Exception.class, ex -> {
                    if (ex instanceof AlphaVantageException) {
                        return ex;
                    }
                    return new AlphaVantageException("Failed to fetch quote for symbol: " + normalizedSymbol, 
                            "NETWORK_ERROR", ex);
                });
    }

    /**
     * Extensible method for future AlphaVantage functions
     * @param function AlphaVantage function name
     * @param symbol Stock symbol
     * @param additionalParams Additional query parameters
     * @return AlphaVantageResponse containing the raw API response
     */
    public Mono<AlphaVantageResponse> callAlphaVantageFunction(String function, String symbol, 
                                                               java.util.Map<String, String> additionalParams) {
        if (function == null || function.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Function cannot be null or empty"));
        }
        if (symbol == null || symbol.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Stock symbol cannot be null or empty"));
        }

        String normalizedSymbol = symbol.trim().toUpperCase();
        logger.info("Calling AlphaVantage function: {} for symbol: {}", function, normalizedSymbol);

        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .scheme("https")
                            .host("www.alphavantage.co")
                            .path("/query")
                            .queryParam("function", function)
                            .queryParam("symbol", normalizedSymbol)
                            .queryParam("apikey", config.getKey());
                    
                    if (additionalParams != null) {
                        additionalParams.forEach(builder::queryParam);
                    }
                    
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(AlphaVantageResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> {
                    if (response != null && response.hasError()) {
                        String errorMsg = response.getErrorMessage();
                        logger.warn("AlphaVantage API returned error for function {} and symbol {}: {}", 
                                function, normalizedSymbol, errorMsg);
                        throw new AlphaVantageException(errorMsg, "API_ERROR");
                    }
                    logger.info("Successfully called function {} for symbol: {}", function, normalizedSymbol);
                })
                .onErrorMap(WebClientResponseException.class, ex -> 
                        new AlphaVantageException("HTTP error: " + ex.getStatusCode() + " - " + ex.getMessage(), 
                                "HTTP_ERROR", ex))
                .onErrorMap(Exception.class, ex -> {
                    if (ex instanceof AlphaVantageException) {
                        return ex;
                    }
                    return new AlphaVantageException("Failed to call function " + function + " for symbol: " + normalizedSymbol, 
                            "NETWORK_ERROR", ex);
                });
    }
}