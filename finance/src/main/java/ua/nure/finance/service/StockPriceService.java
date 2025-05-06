package ua.nure.finance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class StockPriceService {

    private final RestTemplate restTemplate;
    private static final String API_KEY = "T2M766DBVKNDWLHP";

    public BigDecimal fetchCurrentPrice(String ticker) {
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + ticker + "&apikey=" + API_KEY;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> globalQuote = (Map<String, Object>) response.getBody().get("Global Quote");

            if (globalQuote != null && globalQuote.containsKey("05. price")) {
                String priceStr = (String) globalQuote.get("05. price");
                return new BigDecimal(priceStr);
            } else {
                System.err.println("Price not found in response for ticker: " + ticker);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch stock price for " + ticker + ": " + e.getMessage());
        }

        return null;
    }
}

