package com.cpb.service;

import com.cpb.model.ProvenanceRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates provenance records for every extracted field,
 * tracking source and extraction method.
 */
@Service
public class ProvenanceService {

    public ProvenanceRecord createRecord(String fieldName, String value,
                                          String sourceType, String extractionMethod,
                                          Double confidence) {
        return new ProvenanceRecord(fieldName, value, sourceType, extractionMethod, confidence);
    }

    /**
     * Create provenance records for a list of values from a single source.
     */
    public List<ProvenanceRecord> createRecords(String fieldName, List<String> values,
                                                  String sourceType, String extractionMethod,
                                                  Double confidence) {
        List<ProvenanceRecord> records = new ArrayList<>();
        if (values == null) return records;
        for (String value : values) {
            records.add(createRecord(fieldName, value, sourceType, extractionMethod, confidence));
        }
        return records;
    }
}
