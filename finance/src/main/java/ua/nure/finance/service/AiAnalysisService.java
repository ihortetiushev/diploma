package ua.nure.finance.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AiAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AiAnalysisService.class);
    private final RestClient restClient;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${finance.ai.base-url:}")
    private String baseUrl;

    @Value("${finance.ai.api-key:}")
    private String apiKey;

    @Value("${finance.ai.model:}")
    private String model;

    public AiAnalysisService(RestClient.Builder restClientBuilder,
                             com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String analyze(String prompt, LocalDate from, LocalDate to,
                          List<MonthlySummary> monthlySummaries,
                          BigDecimal totalIncome, BigDecimal totalExpenses) {
        if (baseUrl.isBlank() || apiKey.isBlank() || model.isBlank()) {
            throw new AiAnalysisException(
                    "AI API не налаштовано. Вкажіть AI_API_BASE_URL, AI_API_KEY та AI_MODEL.");
        }

        String context = """
                Період: %s — %s
                Місячні підсумки (суми у основній валюті):
                %s
                Разом доходи: %s
                Разом витрати: %s
                """.formatted(from, to, monthlySummaries.stream()
                .map(summary -> "%s: доходи %s, витрати %s".formatted(
                        summary.month(), summary.income(), summary.expenses()))
                .reduce((left, right) -> left + "\n" + right).orElse("Немає транзакцій"),
                totalIncome, totalExpenses);
        Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "Ти помічник з аналізу особистих фінансів. Відповідай українською. "
                                        + "Аналізуй лише надані агреговані дані та не вигадуй відсутню інформацію."),
                        Map.of("role", "user", "content", context + "\nЗапит користувача:\n" + prompt)));

        try {
            JsonNode response = restClient.post()
                    .uri(baseUrl.replaceAll("/+$", "") + "/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode content = response == null ? null
                    : response.path("choices").path(0).path("message").path("content");
            if (content == null || !content.isTextual() || content.asText().isBlank()) {
                throw new AiAnalysisException("AI API повернув порожню або некоректну відповідь.");
            }
            return content.asText();
        } catch (HttpStatusCodeException exception) {
            String details = extractErrorMessage(exception.getResponseBodyAsString());
            logger.warn("AI API returned HTTP {}", exception.getStatusCode());
            throw new AiAnalysisException("AI API повернув HTTP %s: %s"
                    .formatted(exception.getStatusCode().value(), details));
        } catch (RestClientException exception) {
            logger.warn("AI API request failed", exception);
            throw new AiAnalysisException("Не вдалося з’єднатися з AI API. Перевірте адресу API та мережу.");
        }
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode error = objectMapper.readTree(responseBody).path("error");
            String message = error.path("message").asText();
            if (!message.isBlank()) {
                return message.substring(0, Math.min(message.length(), 500));
            }
        } catch (Exception exception) {
            logger.debug("Could not parse AI API error response", exception);
        }
        return "Перевірте API-ключ, модель та квоту.";
    }

    public record MonthlySummary(String month, BigDecimal income, BigDecimal expenses) {
    }

    public static class AiAnalysisException extends RuntimeException {
        public AiAnalysisException(String message) {
            super(message);
        }
    }
}
