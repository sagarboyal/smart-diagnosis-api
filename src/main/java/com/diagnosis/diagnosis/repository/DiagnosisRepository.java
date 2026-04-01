package com.diagnosis.diagnosis.repository;

import com.diagnosis.diagnosis.model.DiagnosisRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DiagnosisRepository extends MongoRepository<DiagnosisRecord, String> {

    List<DiagnosisRecord> findAllByOrderByTimestampDesc();

    Page<DiagnosisRecord> findAllByOrderByTimestampDesc(Pageable pageable);
}












