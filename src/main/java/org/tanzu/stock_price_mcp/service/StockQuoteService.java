package org.tanzu.stock_price_mcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tanzu.stock_price_mcp.model.AlphaVantageResponse;
import org.tanzu.stock_price_mcp.model.StockQuoteRequest;
import reactor.core.publisher.Mono;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;

@Service
public class StockQuoteService {

    private static final Logger logger = LoggerFactory.getLogger(StockQuoteService.class);

    private final AlphaVantageService alphaVantageService;
    private final Validator validator;

    @Autowired
    public StockQuoteService(AlphaVantageService alphaVantageService, Validator validator) {
        this.alphaVantageService = alphaVantageService;
        this.validator = validator;
    }

    /**
     * Get stock quote for a given symbol
     * @param symbol Stock symbol
     * @return Mono containing the raw AlphaVantage response
     */
    public Mono<AlphaVantageResponse> getStockQuote(String symbol) {
        logger.info("Processing stock quote request for symbol: {}", symbol);

        // Create and validate request
        StockQuoteRequest request = new StockQuoteRequest(symbol);
        Set<ConstraintViolation<StockQuoteRequest>> violations = validator.validate(request);
        
        if (!violations.isEmpty()) {
            String errorMessage = violations.iterator().next().getMessage();
            logger.warn("Invalid stock quote request: {}", errorMessage);
            return Mono.error(new IllegalArgumentException(errorMessage));
        }

        return alphaVantageService.getGlobalQuote(request.getSymbol())
                .doOnSuccess(response -> logger.info("Successfully processed stock quote for symbol: {}", symbol))
                .doOnError(error -> logger.error("Failed to process stock quote for symbol: {}", symbol, error));
    }

    /**
     * Validate stock symbol format
     * @param symbol Stock symbol to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        
        String normalizedSymbol = symbol.trim();
        // Must be 1-5 letters only, no numbers or special characters
        return normalizedSymbol.matches("^[A-Za-z]{1,5}$");
    }

    /**
     * Extensible method for future stock data functions
     * @param function AlphaVantage function name
     * @param symbol Stock symbol
     * @return Mono containing the raw AlphaVantage response
     */
    public Mono<AlphaVantageResponse> getStockData(String function, String symbol) {
        logger.info("Processing stock data request for function: {} and symbol: {}", function, symbol);

        if (!isValidSymbol(symbol)) {
            return Mono.error(new IllegalArgumentException("Invalid stock symbol format"));
        }

        return alphaVantageService.callAlphaVantageFunction(function, symbol, null)
                .doOnSuccess(response -> logger.info("Successfully processed stock data for function: {} and symbol: {}", function, symbol))
                .doOnError(error -> logger.error("Failed to process stock data for function: {} and symbol: {}", function, symbol, error));
    }
}