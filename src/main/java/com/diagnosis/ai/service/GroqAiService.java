package com.diagnosis.ai.service;

import com.diagnosis.ai.client.GroqClient;
import com.diagnosis.ai.parser.GroqResponseParser;
import com.diagnosis.diagnosis.model.Condition;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroqAiService {

    private static final String SYSTEM_PROMPT = """
            You are a medical diagnosis assistant.
            Given symptoms, return ONLY a valid JSON array (no markdown, no explanation).
            Return exactly 2-3 conditions in this format:
            [
              {
                \"name\": \"condition name\",
                \"probabilityPercent\": 65,
                \"doctorType\": \"type of specialist\",
                \"nextSteps\": [\"test 1\", \"test 2\", \"test 3\"]
              }
            ]
            Probabilities must sum to 100. Return JSON only.
            """;

    private final GroqClient groqClient;
    private final GroqResponseParser groqResponseParser;

    public GroqAiService(GroqClient groqClient, GroqResponseParser groqResponseParser) {
        this.groqClient = groqClient;
        this.groqResponseParser = groqResponseParser;
    }

    public List<Condition> diagnose(String symptoms) {
        try {
            String rawContent = groqClient.getDiagnosisContent(SYSTEM_PROMPT, symptoms);
            return groqResponseParser.parse(rawContent);
        } catch (Exception ex) {
            return groqResponseParser.fallback();
        }
    }
}












