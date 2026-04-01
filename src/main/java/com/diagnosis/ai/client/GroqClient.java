package com.diagnosis.ai.client;

import com.diagnosis.ai.config.GroqProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GroqClient {

    private final RestClient groqRestClient;
    private final GroqProperties groqProperties;

    public GroqClient(RestClient groqRestClient, GroqProperties groqProperties) {
        this.groqRestClient = groqRestClient;
        this.groqProperties = groqProperties;
    }

    public String getDiagnosisContent(String systemPrompt, String symptoms) {
        if (groqProperties.getApiKey() == null || groqProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("GROQ_API_KEY is missing.");
        }

        Map<String, Object> payload = Map.of(
                "model", groqProperties.getModel(),
                "max_tokens", groqProperties.getMaxTokens(),
                "temperature", groqProperties.getTemperature(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", "Symptoms: " + symptoms)
                )
        );

        JsonNode response = groqRestClient.post()
                .uri("/openai/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqProperties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("Groq response is empty.");
        }

        JsonNode contentNode = response.at("/choices/0/message/content");
        if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
            throw new IllegalStateException("Groq response did not include message content.");
        }

        return contentNode.asText();
    }
}












