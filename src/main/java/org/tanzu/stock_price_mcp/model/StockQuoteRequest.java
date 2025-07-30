package org.tanzu.stock_price_mcp.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class StockQuoteRequest {

    @NotBlank(message = "Stock symbol is required")
    @Pattern(regexp = "^[A-Z]{1,5}$", message = "Stock symbol must be 1-5 uppercase letters")
    private String symbol;

    public StockQuoteRequest() {
    }

    public StockQuoteRequest(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol != null ? symbol.toUpperCase() : null;
    }

    @Override
    public String toString() {
        return "StockQuoteRequest{" +
                "symbol='" + symbol + '\'' +
                '}';
    }
}