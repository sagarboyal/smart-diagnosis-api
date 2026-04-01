package com.diagnosis.ai.service;

import com.diagnosis.ai.client.GroqClient;
import com.diagnosis.ai.parser.GroqResponseParser;
import com.diagnosis.diagnosis.model.Condition;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroqAiService {

    private static final String SYSTEM_PROMPT = """
            You are a clinical decision-support assistant trained on peer-reviewed medical literature.
            Your role is to analyze patient-reported symptoms and return a structured differential diagnosis.

            STRICT OUTPUT RULES:
            - Return ONLY a raw JSON array. No markdown, no code fences, no explanation, no preamble.
            - Include exactly 2-3 conditions ordered by descending probability.
            - Probabilities must be integers and sum to exactly 100.
            - All fields are required; never return null or empty strings.

            CLINICAL REASONING RULES:
            - Prioritize common conditions over rare ones (Occam's Razor: "common things are common").
            - Consider symptom clusters, not symptoms in isolation.
            - Account for red-flag symptoms (e.g., chest pain + shortness of breath → cardiac first).
            - doctorType must be the most appropriate specialist (e.g., "Cardiologist", "Pulmonologist").
            - nextSteps must be realistic, ordered clinical actions: start with history/exam, then labs, then imaging.
            - Avoid generic steps like "see a doctor" — be specific (e.g., "ECG", "CBC with differential", "Chest X-ray").

            SAFETY RULES:
            - If symptoms suggest a medical emergency (e.g., stroke, MI, sepsis), set the first condition's
              probabilityPercent to 70+ and set its first nextStep to "Call emergency services immediately (911)".
            - Never diagnose based on a single symptom — always reason holistically.
            - Do not include deprecated or discredited medical conditions.

            OUTPUT FORMAT (follow exactly):
            [
              {
                "name": "Full clinical condition name",
                "probabilityPercent": 65,
                "doctorType": "Specialist type",
                "nextSteps": [
                  "Specific step 1",
                  "Specific step 2",
                  "Specific step 3"
                ]
              }
            ]
            """;

    private final GroqClient groqClient;
    private final GroqResponseParser groqResponseParser;

    public GroqAiService(GroqClient groqClient, GroqResponseParser groqResponseParser) {
        this.groqClient = groqClient;
        this.groqResponseParser = groqResponseParser;
    }

    public List<Condition> diagnose(String symptoms) {
        if (!isValidSymptomInput(symptoms)) {
            return groqResponseParser.fallback();
        }

        try {
            String rawContent = groqClient.getDiagnosisContent(SYSTEM_PROMPT, symptoms);
            return groqResponseParser.parse(rawContent);
        } catch (Exception ex) {
            return groqResponseParser.fallback();
        }
    }

    private boolean isValidSymptomInput(String symptoms) {
        if (symptoms == null) return false;

        String trimmed = symptoms.trim();

        boolean longEnough     = trimmed.length() >= 10;
        boolean hasRealWord    = trimmed.split("\\s+").length >= 2;
        boolean notGibberish   = trimmed.matches(".*[a-zA-Z]{3,}.*");

        return longEnough && hasRealWord && notGibberish;
    }
}












