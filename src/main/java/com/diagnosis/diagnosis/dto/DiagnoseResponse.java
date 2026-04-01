package com.diagnosis.diagnosis.dto;

import com.diagnosis.diagnosis.model.Condition;

import java.time.LocalDateTime;
import java.util.List;

public record DiagnoseResponse(
        String id,
        String symptoms,
        LocalDateTime timestamp,
        List<Condition> conditions
) {
}












