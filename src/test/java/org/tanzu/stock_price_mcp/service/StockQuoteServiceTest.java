package org.tanzu.stock_price_mcp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tanzu.stock_price_mcp.model.AlphaVantageResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class StockQuoteServiceTest {

    @Mock
    private AlphaVantageService alphaVantageService;

    @Mock
    private Validator validator;

    private StockQuoteService stockQuoteService;

    @BeforeEach
    void setUp() {
        stockQuoteService = new StockQuoteService(alphaVantageService, validator);
    }

    @Test
    void testIsValidSymbol_ValidSymbol() {
        assertTrue(stockQuoteService.isValidSymbol("IBM"));
        assertTrue(stockQuoteService.isValidSymbol("AAPL"));
        assertTrue(stockQuoteService.isValidSymbol("MSFT"));
        assertTrue(stockQuoteService.isValidSymbol("A"));
        assertTrue(stockQuoteService.isValidSymbol("GOOGL"));
    }

    @Test
    void testIsValidSymbol_InvalidSymbol() {
        assertFalse(stockQuoteService.isValidSymbol(null));
        assertFalse(stockQuoteService.isValidSymbol(""));
        assertFalse(stockQuoteService.isValidSymbol("   "));
        assertFalse(stockQuoteService.isValidSymbol("123"));
        assertFalse(stockQuoteService.isValidSymbol("TOOLONG"));
        assertFalse(stockQuoteService.isValidSymbol("IBM123"));
        assertFalse(stockQuoteService.isValidSymbol("IBM-A"));
    }

    @Test
    void testGetStockQuote_Success() {
        // Arrange
        String symbol = "IBM";
        AlphaVantageResponse mockResponse = new AlphaVantageResponse();
        mockResponse.setData("test", "data");

        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(alphaVantageService.getGlobalQuote(anyString())).thenReturn(Mono.just(mockResponse));

        // Act & Assert
        StepVerifier.create(stockQuoteService.getStockQuote(symbol))
                .expectNext(mockResponse)
                .verifyComplete();
    }

    @Test
    void testGetStockQuote_InvalidSymbol() {
        // Arrange - Mock the validator to return validation errors
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid symbol format");
        when(validator.validate(any())).thenReturn(Set.of(violation));

        // Act & Assert - Invalid symbol should be caught by validation
        StepVerifier.create(stockQuoteService.getStockQuote("INVALID123"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testGetStockData_Success() {
        // Arrange
        String function = "TIME_SERIES_DAILY";
        String symbol = "IBM";
        AlphaVantageResponse mockResponse = new AlphaVantageResponse();
        mockResponse.setData("test", "data");

        when(alphaVantageService.callAlphaVantageFunction(anyString(), anyString(), any()))
                .thenReturn(Mono.just(mockResponse));

        // Act & Assert
        StepVerifier.create(stockQuoteService.getStockData(function, symbol))
                .expectNext(mockResponse)
                .verifyComplete();
    }

    @Test
    void testGetStockData_InvalidSymbol() {
        // Act & Assert
        StepVerifier.create(stockQuoteService.getStockData("TIME_SERIES_DAILY", "INVALID123"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}