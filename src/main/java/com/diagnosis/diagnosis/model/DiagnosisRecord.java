package com.diagnosis.diagnosis.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "diagnosis_records")
public class DiagnosisRecord {

    @Id
    private String id;
    private String symptoms;
    private LocalDateTime timestamp;
    private List<Condition> conditions;

    public DiagnosisRecord() {
    }

    public DiagnosisRecord(String id, String symptoms, LocalDateTime timestamp, List<Condition> conditions) {
        this.id = id;
        this.symptoms = symptoms;
        this.timestamp = timestamp;
        this.conditions = conditions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }
}












