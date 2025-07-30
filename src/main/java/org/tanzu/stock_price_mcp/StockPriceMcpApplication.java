package org.tanzu.stock_price_mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.tanzu.stock_price_mcp.config.AlphaVantageConfig;

@SpringBootApplication
@EnableConfigurationProperties(AlphaVantageConfig.class)
public class StockPriceMcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockPriceMcpApplication.class, args);
	}

}
