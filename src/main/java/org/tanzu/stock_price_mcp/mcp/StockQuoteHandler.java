package org.tanzu.stock_price_mcp.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tanzu.stock_price_mcp.model.AlphaVantageResponse;
import org.tanzu.stock_price_mcp.service.StockQuoteService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mcp")
public class StockQuoteHandler {

    private static final Logger logger = LoggerFactory.getLogger(StockQuoteHandler.class);

    private final StockQuoteService stockQuoteService;

    @Autowired
    public StockQuoteHandler(StockQuoteService stockQuoteService) {
        this.stockQuoteService = stockQuoteService;
    }

    /**
     * List available tools endpoint for MCP protocol
     */
    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> listTools() {
        logger.info("MCP tools list requested");

        Map<String, Object> response = Map.of(
            "tools", List.of(
                Map.of(
                    "name", "quote",
                    "description", "Get real-time stock quote information for a given stock symbol using AlphaVantage API",
                    "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "symbol", Map.of(
                                "type", "string",
                                "description", "Stock ticker symbol (e.g., 'IBM', 'AAPL', 'MSFT'). Must be 1-5 uppercase letters."
                            )
                        ),
                        "required", List.of("symbol")
                    )
                ),
                Map.of(
                    "name", "validate_symbol",
                    "description", "Validate if a stock symbol has the correct format",
                    "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "symbol", Map.of(
                                "type", "string",
                                "description", "Stock ticker symbol to validate"
                            )
                        ),
                        "required", List.of("symbol")
                    )
                )
            )
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Call tool endpoint for MCP protocol
     */
    @PostMapping("/tools/call")
    public ResponseEntity<Map<String, Object>> callTool(@RequestBody Map<String, Object> request) {
        String toolName = (String) request.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) request.get("arguments");

        logger.info("MCP tool '{}' called with arguments: {}", toolName, arguments);

        Map<String, Object> result;
        switch (toolName) {
            case "quote":
                result = handleQuoteTool(arguments);
                break;
            case "validate_symbol":
                result = handleValidateSymbolTool(arguments);
                break;
            default:
                logger.warn("Unknown tool called: {}", toolName);
                result = Map.of(
                    "error", "Unknown tool",
                    "message", "Tool '" + toolName + "' is not supported",
                    "tool", toolName
                );
        }

        Map<String, Object> response = Map.of(
            "content", List.of(
                Map.of(
                    "type", "text",
                    "text", result
                )
            )
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Direct quote endpoint for easier testing
     */
    @GetMapping("/quote/{symbol}")
    public ResponseEntity<Map<String, Object>> getQuote(@PathVariable String symbol) {
        logger.info("Direct quote request for symbol: {}", symbol);
        
        Map<String, Object> result = handleQuoteTool(Map.of("symbol", symbol));
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> handleQuoteTool(Map<String, Object> arguments) {
        String symbol = (String) arguments.get("symbol");
        
        if (symbol == null || symbol.trim().isEmpty()) {
            return Map.of(
                "error", "Missing symbol",
                "message", "Symbol parameter is required"
            );
        }

        try {
            // Validate symbol
            if (!stockQuoteService.isValidSymbol(symbol)) {
                logger.warn("Invalid symbol format provided: {}", symbol);
                return Map.of(
                    "error", "Invalid symbol format",
                    "message", "Stock symbol must be 1-5 uppercase letters (e.g., 'IBM', 'AAPL')",
                    "symbol", symbol
                );
            }

            // Get stock quote synchronously (blocking for MCP tool)
            AlphaVantageResponse response = stockQuoteService.getStockQuote(symbol).block();
            
            if (response == null) {
                logger.error("Received null response for symbol: {}", symbol);
                return Map.of(
                    "error", "No data received",
                    "message", "Failed to retrieve stock quote data",
                    "symbol", symbol
                );
            }

            logger.info("Successfully retrieved stock quote for symbol: {}", symbol);
            
            // Return the raw AlphaVantage response as requested
            return response.getData();

        } catch (Exception e) {
            logger.error("Error retrieving stock quote for symbol: {}", symbol, e);
            return Map.of(
                "error", "Failed to retrieve stock quote",
                "message", e.getMessage(),
                "symbol", symbol
            );
        }
    }

    private Map<String, Object> handleValidateSymbolTool(Map<String, Object> arguments) {
        String symbol = (String) arguments.get("symbol");
        
        if (symbol == null) {
            return Map.of(
                "error", "Missing symbol",
                "message", "Symbol parameter is required"
            );
        }

        logger.info("Validating symbol: {}", symbol);

        boolean isValid = stockQuoteService.isValidSymbol(symbol);
        
        return Map.of(
            "symbol", symbol,
            "valid", isValid,
            "message", isValid ? "Valid stock symbol format" : "Invalid stock symbol format. Must be 1-5 uppercase letters."
        );
    }
}