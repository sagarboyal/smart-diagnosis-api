package com.diagnosis.history.service;

import com.diagnosis.diagnosis.dto.DiagnoseResponse;
import com.diagnosis.diagnosis.model.DiagnosisRecord;
import com.diagnosis.diagnosis.repository.DiagnosisRepository;
import com.diagnosis.history.dto.HistoryResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {

    private final DiagnosisRepository diagnosisRepository;

    public HistoryService(DiagnosisRepository diagnosisRepository) {
        this.diagnosisRepository = diagnosisRepository;
    }

    public HistoryResponse getHistory(Integer limit) {
        List<DiagnosisRecord> records = limit == null
                ? diagnosisRepository.findAllByOrderByTimestampDesc()
                : diagnosisRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit)).getContent();

        List<DiagnoseResponse> responseRecords = records.stream()
                .map(this::mapRecord)
                .toList();

        return new HistoryResponse(diagnosisRepository.count(), responseRecords);
    }

    private DiagnoseResponse mapRecord(DiagnosisRecord record) {
        return new DiagnoseResponse(
                record.getId(),
                record.getSymptoms(),
                record.getTimestamp(),
                record.getConditions()
        );
    }
}












