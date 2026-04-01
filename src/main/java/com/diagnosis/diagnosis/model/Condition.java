package com.diagnosis.diagnosis.model;

import java.util.List;

public record Condition(
        String name,
        int probabilityPercent,
        String doctorType,
        List<String> nextSteps
) {
}












