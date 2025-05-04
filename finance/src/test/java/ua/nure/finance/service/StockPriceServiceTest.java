package ua.nure.finance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockPriceServiceTest {

    private RestTemplate restTemplate;
    private StockPriceService stockPriceService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        stockPriceService = new StockPriceService(restTemplate);
    }

    @Test
    void fetchCurrentPrice_shouldReturnPrice_whenValidResponse() {
        Map<String, Object> quote = Map.of("05. price", "123.45");
        Map<String, Object> responseBody = Map.of("Global Quote", quote);
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(responseBody);

        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        BigDecimal price = stockPriceService.fetchCurrentPrice("AAPL");

        assertNotNull(price);
        assertEquals(new BigDecimal("123.45"), price);
    }

    @Test
    void fetchCurrentPrice_shouldReturnNull_whenPriceMissing() {
        Map<String, Object> responseBody = Map.of("Global Quote", Map.of());
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(responseBody);

        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        BigDecimal price = stockPriceService.fetchCurrentPrice("AAPL");

        assertNull(price);
    }

    @Test
    void fetchCurrentPrice_shouldReturnNull_onException() {
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenThrow(new RuntimeException("API error"));

        BigDecimal price = stockPriceService.fetchCurrentPrice("AAPL");

        assertNull(price);
    }
}
