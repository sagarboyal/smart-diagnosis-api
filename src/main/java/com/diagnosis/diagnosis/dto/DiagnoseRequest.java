package com.diagnosis.diagnosis.dto;

import jakarta.validation.constraints.NotBlank;

public record DiagnoseRequest(
        @NotBlank(message = "symptoms is required")
        String symptoms
) {
}












