package org.tanzu.stock_price_mcp.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphaVantageResponse {

    private Map<String, Object> data = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }

    @JsonAnySetter
    public void setData(String key, Object value) {
        this.data.put(key, value);
    }

    public boolean hasError() {
        return data.containsKey("Error Message") || 
               data.containsKey("Note") ||
               data.containsKey("Information");
    }

    public String getErrorMessage() {
        if (data.containsKey("Error Message")) {
            return (String) data.get("Error Message");
        }
        if (data.containsKey("Note")) {
            return (String) data.get("Note");
        }
        if (data.containsKey("Information")) {
            return (String) data.get("Information");
        }
        return null;
    }

    @Override
    public String toString() {
        return "AlphaVantageResponse{" +
                "data=" + data +
                '}';
    }
}