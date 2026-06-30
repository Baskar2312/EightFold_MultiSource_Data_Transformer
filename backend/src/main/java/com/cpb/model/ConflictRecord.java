package com.cpb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "conflict_record")
public class ConflictRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String resumeValue;

    @Column(columnDefinition = "TEXT")
    private String csvValue;

    @Column(columnDefinition = "TEXT")
    private String selectedValue;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_profile_id", nullable = false)
    @JsonIgnore
    private CandidateProfile candidateProfile;

    public ConflictRecord() {}

    public ConflictRecord(String fieldName, String resumeValue, String csvValue,
                          String selectedValue, String reason) {
        this.fieldName = fieldName;
        this.resumeValue = resumeValue;
        this.csvValue = csvValue;
        this.selectedValue = selectedValue;
        this.reason = reason;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getResumeValue() { return resumeValue; }
    public void setResumeValue(String resumeValue) { this.resumeValue = resumeValue; }

    public String getCsvValue() { return csvValue; }
    public void setCsvValue(String csvValue) { this.csvValue = csvValue; }

    public String getSelectedValue() { return selectedValue; }
    public void setSelectedValue(String selectedValue) { this.selectedValue = selectedValue; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public CandidateProfile getCandidateProfile() { return candidateProfile; }
    public void setCandidateProfile(CandidateProfile candidateProfile) { this.candidateProfile = candidateProfile; }
}
