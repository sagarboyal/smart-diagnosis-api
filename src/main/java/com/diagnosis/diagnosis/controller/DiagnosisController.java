package com.diagnosis.diagnosis.controller;

import com.diagnosis.diagnosis.dto.DiagnoseRequest;
import com.diagnosis.diagnosis.dto.DiagnoseResponse;
import com.diagnosis.diagnosis.service.DiagnosisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping("/diagnose")
    @ResponseStatus(HttpStatus.CREATED)
    public DiagnoseResponse diagnose(@Valid @RequestBody DiagnoseRequest request) {
        return diagnosisService.diagnose(request.symptoms());
    }
}












