package com.diagnosis.diagnosis.service;

import com.diagnosis.ai.service.GroqAiService;
import com.diagnosis.diagnosis.dto.DiagnoseResponse;
import com.diagnosis.diagnosis.model.Condition;
import com.diagnosis.diagnosis.model.DiagnosisRecord;
import com.diagnosis.diagnosis.repository.DiagnosisRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiagnosisService {

    private final GroqAiService groqAiService;
    private final DiagnosisRepository diagnosisRepository;

    public DiagnosisService(GroqAiService groqAiService, DiagnosisRepository diagnosisRepository) {
        this.groqAiService = groqAiService;
        this.diagnosisRepository = diagnosisRepository;
    }

    public DiagnoseResponse diagnose(String symptoms) {
        List<Condition> conditions = groqAiService.diagnose(symptoms);

        DiagnosisRecord record = new DiagnosisRecord();
        record.setSymptoms(symptoms);
        record.setTimestamp(LocalDateTime.now());
        record.setConditions(conditions);

        DiagnosisRecord savedRecord = diagnosisRepository.save(record);

        return new DiagnoseResponse(
                savedRecord.getId(),
                savedRecord.getSymptoms(),
                savedRecord.getTimestamp(),
                savedRecord.getConditions()
        );
    }
}












