package com.cpb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "provenance_record")
public class ProvenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String value;

    @Column(nullable = false)
    private String sourceType; // RESUME or CSV

    @Column(nullable = false)
    private String extractionMethod; // REGEX, PDF_TEXT_EXTRACTION, CSV_MAPPING

    private Double confidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_profile_id", nullable = false)
    @JsonIgnore
    private CandidateProfile candidateProfile;

    public ProvenanceRecord() {}

    public ProvenanceRecord(String fieldName, String value, String sourceType,
                            String extractionMethod, Double confidence) {
        this.fieldName = fieldName;
        this.value = value;
        this.sourceType = sourceType;
        this.extractionMethod = extractionMethod;
        this.confidence = confidence;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getExtractionMethod() { return extractionMethod; }
    public void setExtractionMethod(String extractionMethod) { this.extractionMethod = extractionMethod; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public CandidateProfile getCandidateProfile() { return candidateProfile; }
    public void setCandidateProfile(CandidateProfile candidateProfile) { this.candidateProfile = candidateProfile; }
}
