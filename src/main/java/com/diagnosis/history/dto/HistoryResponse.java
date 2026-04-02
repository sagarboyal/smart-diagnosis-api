package com.diagnosis.history.dto;

import com.diagnosis.diagnosis.dto.DiagnoseResponse;

import java.util.List;

public record HistoryResponse(
        long total,
        List<DiagnoseResponse> records
) {
}












