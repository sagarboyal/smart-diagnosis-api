package com.diagnosis.ai.parser;

import com.diagnosis.diagnosis.model.Condition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroqResponseParser {

    private final ObjectMapper objectMapper;

    public GroqResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Condition> parse(String rawContent) {
        try {
            String jsonPayload = extractJsonArray(rawContent);
            List<Condition> conditions = objectMapper.readValue(jsonPayload, new TypeReference<>() {
            });
            if (conditions.isEmpty()) {
                return fallback();
            }
            return normalize(conditions);
        } catch (Exception ex) {
            return fallback();
        }
    }

    public List<Condition> fallback() {
        return List.of(
                new Condition(
                        "Undifferentiated illness",
                        60,
                        "General Physician",
                        List.of("Clinical examination", "CBC test", "Monitor symptoms for 24 hours")
                ),
                new Condition(
                        "Viral infection",
                        40,
                        "Internal Medicine",
                        List.of("Hydration", "Fever monitoring", "Follow-up if symptoms worsen")
                )
        );
    }

    private String extractJsonArray(String rawContent) {
        int firstBracket = rawContent.indexOf('[');
        int lastBracket = rawContent.lastIndexOf(']');
        if (firstBracket < 0 || lastBracket <= firstBracket) {
            throw new IllegalArgumentException("No JSON array found in AI response.");
        }
        return rawContent.substring(firstBracket, lastBracket + 1);
    }

    private List<Condition> normalize(List<Condition> conditions) {
        int sum = conditions.stream()
                .mapToInt(condition -> Math.max(0, condition.probabilityPercent()))
                .sum();

        if (sum <= 0) {
            return fallback();
        }

        int runningTotal = 0;
        for (int i = 0; i < conditions.size(); i++) {
            Condition original = conditions.get(i);
            int value;
            if (i == conditions.size() - 1) {
                value = 100 - runningTotal;
            } else {
                value = (int) Math.round((original.probabilityPercent() * 100.0) / sum);
                runningTotal += value;
            }
            conditions.set(i, new Condition(
                    original.name(),
                    Math.max(0, value),
                    original.doctorType(),
                    original.nextSteps() == null ? List.of() : original.nextSteps()
            ));
        }

        return conditions;
    }
}












