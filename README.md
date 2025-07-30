# Stock Price MCP Server

A Model Context Protocol (MCP) server built with Spring Boot and Spring AI that provides real-time stock quote information using the AlphaVantage API. This application is designed to run in Cloud Foundry and be consumed by LLMs through MCP protocol.

## Features

- **Real-time Stock Quotes**: Get current stock price information using AlphaVantage API
- **MCP Protocol Support**: Built with Spring AI MCP server framework
- **Cloud Foundry Ready**: Optimized for Cloud Foundry deployment
- **Extensible Architecture**: Designed to easily add more AlphaVantage API functions
- **Comprehensive Error Handling**: Robust error handling and validation
- **Health Monitoring**: Spring Actuator endpoints for monitoring

## Architecture

```
LLM Client → MCP Protocol → Spring AI MCP Server → AlphaVantage Service → AlphaVantage API
```

## Prerequisites

- Java 21+
- Maven 3.6+
- AlphaVantage API Key (get one at https://www.alphavantage.co/support/#api-key)
- Cloud Foundry CLI (for deployment)

## Local Development

### 1. Clone and Setup

```bash
git clone <repository-url>
cd stock-price-mcp
```

### 2. Configure API Key

Set your AlphaVantage API key as an environment variable:

```bash
export ALPHAVANTAGE_API_KEY=your-api-key-here
```

Or create a `.env` file:
```
ALPHAVANTAGE_API_KEY=your-api-key-here
```

### 3. Build and Run

```bash
# Build the application
mvn clean package

# Run locally
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Test the API

```bash
# Test direct quote endpoint
curl "http://localhost:8080/mcp/quote/IBM"

# Test MCP tools list
curl "http://localhost:8080/mcp/tools"

# Test MCP tool call
curl -X POST "http://localhost:8080/mcp/tools/call" \
  -H "Content-Type: application/json" \
  -d '{"name": "quote", "arguments": {"symbol": "IBM"}}'

# Health check
curl "http://localhost:8080/actuator/health"
```

## Cloud Foundry Deployment

### 1. Build the Application

```bash
mvn clean package
```

### 2. Set Environment Variables

Set the AlphaVantage API key in Cloud Foundry:

```bash
cf set-env stock-price-mcp ALPHAVANTAGE_API_KEY your-api-key-here
```

### 3. Deploy

```bash
cf push
```

The application will be deployed using the configuration in `manifest.yml`.

### 4. Verify Deployment

```bash
# Check application status
cf apps

# Check logs
cf logs stock-price-mcp --recent

# Test the deployed application
curl "https://your-app-url.cfapps.io/actuator/health"
curl "https://your-app-url.cfapps.io/mcp/quote/IBM"
```

## MCP Tools

The server exposes the following MCP tools:

### 1. quote

Get real-time stock quote information.

**Parameters:**
- `symbol` (required): Stock ticker symbol (e.g., 'IBM', 'AAPL', 'MSFT')

**Example:**
```json
{
  "name": "quote",
  "arguments": {
    "symbol": "IBM"
  }
}
```

### 2. validate_symbol

Validate if a stock symbol has the correct format.

**Parameters:**
- `symbol` (required): Stock ticker symbol to validate

**Example:**
```json
{
  "name": "validate_symbol",
  "arguments": {
    "symbol": "IBM"
  }
}
```

## API Endpoints

### MCP Protocol Endpoints

- `GET /mcp/tools` - List available MCP tools
- `POST /mcp/tools/call` - Call an MCP tool

### Direct API Endpoints (for testing)

- `GET /mcp/quote/{symbol}` - Get stock quote directly
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application information

## Configuration

### Environment Variables

- `ALPHAVANTAGE_API_KEY` - Your AlphaVantage API key (required)
- `PORT` - Server port (default: 8080)
- `SPRING_PROFILES_ACTIVE` - Active Spring profiles (use 'cloud' for CF deployment)

### Application Properties

Key configuration properties:

```properties
# AlphaVantage API
alphavantage.api.key=${ALPHAVANTAGE_API_KEY:demo}
alphavantage.api.base-url=https://www.alphavantage.co/query

# HTTP Client
spring.webflux.timeout.connect=10s
spring.webflux.timeout.read=30s

# MCP Server
spring.ai.mcp.server.enabled=true
```

## Testing

Run the test suite:

```bash
mvn test
```

## Extending the Application

To add new AlphaVantage API functions:

1. Add new tool definition in `StockQuoteHandler`
2. Implement the handler method
3. Add corresponding service method in `StockQuoteService`
4. Update `AlphaVantageService` if needed
5. Add tests

Example for adding TIME_SERIES_DAILY:

```java
// In StockQuoteHandler
Map.of(
    "name", "time_series_daily",
    "description", "Get daily time series data for a stock",
    "inputSchema", Map.of(
        "type", "object",
        "properties", Map.of(
            "symbol", Map.of("type", "string", "description", "Stock symbol")
        ),
        "required", List.of("symbol")
    )
)
```

## Troubleshooting

### Common Issues

1. **API Key Issues**
   - Ensure `ALPHAVANTAGE_API_KEY` is set correctly
   - Check API key validity at AlphaVantage website
   - Verify API rate limits

2. **Network Issues**
   - Check internet connectivity
   - Verify AlphaVantage API is accessible
   - Check firewall settings

3. **Cloud Foundry Deployment**
   - Ensure Java 21 buildpack is available
   - Check memory allocation in manifest.yml
   - Verify environment variables are set

### Logs

Check application logs for detailed error information:

```bash
# Local development
mvn spring-boot:run

# Cloud Foundry
cf logs stock-price-mcp --recent
```

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review application logs
3. Verify AlphaVantage API status
4. Check Cloud Foundry documentation for deployment issues