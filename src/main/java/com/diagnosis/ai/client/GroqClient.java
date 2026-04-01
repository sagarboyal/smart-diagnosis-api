package com.diagnosis.ai.client;

import com.diagnosis.ai.config.GroqProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Component
public class GroqClient {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);
    private static final String CHAT_ENDPOINT = "/openai/v1/chat/completions";
    private static final String FALLBACK_MODEL = "llama3-70b-8192";

    private final RestClient groqRestClient;
    private final GroqProperties groqProperties;

    public GroqClient(RestClient groqRestClient, GroqProperties groqProperties) {
        this.groqRestClient = groqRestClient;
        this.groqProperties = groqProperties;
    }

    public String getDiagnosisContent(String systemPrompt, String symptoms) {
        validateApiKey();

        try {
            log.info("Calling Groq with model: {}", groqProperties.getModel());
            return callGroq(groqProperties.getModel(), systemPrompt, symptoms);

        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 429) {
                log.warn("Primary model '{}' rate limited. Retrying with fallback '{}'",
                        groqProperties.getModel(), FALLBACK_MODEL);
                return callGroq(FALLBACK_MODEL, systemPrompt, symptoms);
            }

            if (ex.getStatusCode().value() == 400) {
                log.error("Bad request to Groq API: {}", ex.getResponseBodyAsString());
                throw new IllegalStateException("Invalid request sent to Groq: " + ex.getResponseBodyAsString());
            }

            if (ex.getStatusCode().value() == 401) {
                log.error("Groq API key is invalid or expired.");
                throw new IllegalStateException("Groq API key is invalid or expired.");
            }

            log.error("Groq API error [{}]: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Groq API call failed with status: " + ex.getStatusCode());
        }
    }

    private String callGroq(String model, String systemPrompt, String symptoms) {
        Map<String, Object> payload = buildPayload(model, systemPrompt, symptoms);

        JsonNode response = groqRestClient.post()
                .uri(CHAT_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqProperties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new RestClientResponseException(
                            "Groq error: " + res.getStatusCode(),
                            res.getStatusCode().value(),
                            res.getStatusText(),
                            res.getHeaders(),
                            null, null
                    );
                })
                .body(JsonNode.class);

        return extractContent(response, model);
    }

    private Map<String, Object> buildPayload(String model, String systemPrompt, String symptoms) {
        return Map.of(
                "model",       model,
                "max_tokens",  groqProperties.getMaxTokens(),
                "temperature", groqProperties.getTemperature(),
                "messages", List.of(
                        Map.of("role", "system",  "content", systemPrompt),
                        Map.of("role", "user",    "content", "Patient symptoms: " + symptoms)
                )
        );
    }

    private String extractContent(JsonNode response, String model) {
        if (response == null) {
            throw new IllegalStateException("Groq returned null response for model: " + model);
        }

        JsonNode usage = response.get("usage");
        if (usage != null) {
            log.info("Groq token usage — prompt: {}, completion: {}, total: {}",
                    usage.path("prompt_tokens").asInt(),
                    usage.path("completion_tokens").asInt(),
                    usage.path("total_tokens").asInt());
        }

        JsonNode contentNode = response.at("/choices/0/message/content");

        if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
            String finishReason = response.at("/choices/0/finish_reason").asText("unknown");
            log.error("Empty content from Groq. finish_reason: {}", finishReason);

            if ("length".equals(finishReason)) {
                throw new IllegalStateException(
                        "Groq response was cut off — increase max_tokens in config (current: "
                                + groqProperties.getMaxTokens() + ")"
                );
            }

            throw new IllegalStateException("Groq response has no content. finish_reason: " + finishReason);
        }

        String content = contentNode.asText();
        log.debug("Groq raw response: {}", content);
        return content;
    }

    private void validateApiKey() {
        if (groqProperties.getApiKey() == null || groqProperties.getApiKey().isBlank()) {
            throw new IllegalStateException(
                    "GROQ_API_KEY is missing. Set it in application.yml or as an environment variable."
            );
        }
    }
}