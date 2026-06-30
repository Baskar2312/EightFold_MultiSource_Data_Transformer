package com.cpb.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CandidateProfileResponse {

    private Long candidateId;
    private String fullName;
    private List<String> emails;
    private List<String> phones;
    private String headline;
    private List<SkillDto> skills;
    private List<EducationDto> education;
    private List<ExperienceDto> experience;
    private Double overallConfidence;
    private List<ProvenanceDto> provenance;
    private List<ConflictDto> conflicts;
    private LocalDateTime createdAt;

    // --- Getters and Setters ---

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }

    public List<String> getPhones() { return phones; }
    public void setPhones(List<String> phones) { this.phones = phones; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public List<SkillDto> getSkills() { return skills; }
    public void setSkills(List<SkillDto> skills) { this.skills = skills; }

    public List<EducationDto> getEducation() { return education; }
    public void setEducation(List<EducationDto> education) { this.education = education; }

    public List<ExperienceDto> getExperience() { return experience; }
    public void setExperience(List<ExperienceDto> experience) { this.experience = experience; }

    public Double getOverallConfidence() { return overallConfidence; }
    public void setOverallConfidence(Double overallConfidence) { this.overallConfidence = overallConfidence; }

    public List<ProvenanceDto> getProvenance() { return provenance; }
    public void setProvenance(List<ProvenanceDto> provenance) { this.provenance = provenance; }

    public List<ConflictDto> getConflicts() { return conflicts; }
    public void setConflicts(List<ConflictDto> conflicts) { this.conflicts = conflicts; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // --- Inner DTOs ---

    public static class SkillDto {
        private String name;
        private Double confidence;
        private List<String> sources;

        public SkillDto() {}
        public SkillDto(String name, Double confidence, List<String> sources) {
            this.name = name;
            this.confidence = confidence;
            this.sources = sources;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        public List<String> getSources() { return sources; }
        public void setSources(List<String> sources) { this.sources = sources; }
    }

    public static class EducationDto {
        private String degree;
        private String institution;
        private String year;

        public EducationDto() {}
        public EducationDto(String degree, String institution, String year) {
            this.degree = degree;
            this.institution = institution;
            this.year = year;
        }

        public String getDegree() { return degree; }
        public void setDegree(String degree) { this.degree = degree; }
        public String getInstitution() { return institution; }
        public void setInstitution(String institution) { this.institution = institution; }
        public String getYear() { return year; }
        public void setYear(String year) { this.year = year; }
    }

    public static class ExperienceDto {
        private String description;

        public ExperienceDto() {}
        public ExperienceDto(String description) { this.description = description; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class ProvenanceDto {
        private String fieldName;
        private String value;
        private String sourceType;
        private String extractionMethod;
        private Double confidence;

        public ProvenanceDto() {}
        public ProvenanceDto(String fieldName, String value, String sourceType,
                             String extractionMethod, Double confidence) {
            this.fieldName = fieldName;
            this.value = value;
            this.sourceType = sourceType;
            this.extractionMethod = extractionMethod;
            this.confidence = confidence;
        }

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
    }

    public static class ConflictDto {
        private String fieldName;
        private String resumeValue;
        private String csvValue;
        private String selectedValue;
        private String reason;

        public ConflictDto() {}
        public ConflictDto(String fieldName, String resumeValue, String csvValue,
                           String selectedValue, String reason) {
            this.fieldName = fieldName;
            this.resumeValue = resumeValue;
            this.csvValue = csvValue;
            this.selectedValue = selectedValue;
            this.reason = reason;
        }

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
    }
}
