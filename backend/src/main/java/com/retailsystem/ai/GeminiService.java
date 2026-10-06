package com.retailsystem.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Thin Gemini caller. The model only receives prompts that already contain
 * backend-computed FACTS — this class does not query the database.
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private static final String NOT_ENOUGH = "I don't have enough data to answer that.";

    @Value("${app.gemini.api-key:}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-3.6-flash}")
    private String model;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    public GeminiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generate(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key is not configured");
            return NOT_ENOUGH;
        }
        List<String> candidates = List.of(model, "gemini-3.6-flash", "gemini-2.5-flash");
        RestClientException last = null;
        for (String candidate : candidates.stream().distinct().toList()) {
            try {
                String text = callModel(candidate, prompt);
                if (text != null && !text.isBlank()) {
                    return text.trim();
                }
            } catch (RestClientException ex) {
                last = ex;
                log.warn("Gemini model {} failed: {}", candidate, ex.getMessage());
            } catch (Exception ex) {
                log.warn("Gemini parse failed for {}: {}", candidate, ex.getMessage());
            }
        }
        if (last != null) {
            log.warn("Gemini request failed: {}", last.getMessage());
        }
        return NOT_ENOUGH;
    }

    private String callModel(String modelName, String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + modelName + ":generateContent?key=" + apiKey;
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                    "generationConfig", Map.of(
                            "temperature", 0.2,
                            "maxOutputTokens", 2048
                    )
        );
        String raw = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        try {
            return extractText(raw);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String extractText(String raw) throws Exception {
        if (raw == null) {
            return null;
        }
        JsonNode root = objectMapper.readTree(raw);
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            return null;
        }
        StringBuilder combined = new StringBuilder();
        for (JsonNode part : parts) {
            if (part.has("text")) {
                combined.append(part.get("text").asText());
            }
        }
        return combined.toString();
    }
}
