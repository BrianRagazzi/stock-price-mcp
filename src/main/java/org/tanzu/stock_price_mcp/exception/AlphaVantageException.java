package org.tanzu.stock_price_mcp.exception;

public class AlphaVantageException extends RuntimeException {

    private final String errorCode;

    public AlphaVantageException(String message) {
        super(message);
        this.errorCode = "ALPHAVANTAGE_ERROR";
    }

    public AlphaVantageException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AlphaVantageException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ALPHAVANTAGE_ERROR";
    }

    public AlphaVantageException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}