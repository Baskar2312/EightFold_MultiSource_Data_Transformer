package com.cpb.service;

import com.cpb.dto.CandidateProfileResponse;
import com.cpb.dto.ProjectionConfig;
import com.cpb.dto.ProjectionField;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectionService {

    public Map<String, Object> project(CandidateProfileResponse profile, ProjectionConfig config) {
        Map<String, Object> output = new LinkedHashMap<>();

        if (config == null || config.getFields() == null || config.getFields().isEmpty()) {
            return defaultProjection(profile);
        }

        for (ProjectionField field : config.getFields()) {
            if (field == null || isBlank(field.getPath()) || isBlank(field.getFrom())) {
                continue;
            }

            Object value = resolveValue(profile, field.getFrom());
            if (value == null && "omit".equalsIgnoreCase(config.getOnMissing())) {
                continue;
            }
            output.put(field.getPath(), value);
        }

        if (Boolean.TRUE.equals(config.getIncludeConfidence())) {
            output.put("overall_confidence", profile.getOverallConfidence());
        }

        return output;
    }

    private Map<String, Object> defaultProjection(CandidateProfileResponse profile) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("candidate_name", profile.getFullName());
        output.put("primary_email", first(profile.getEmails()));
        output.put("primary_phone", first(profile.getPhones()));
        output.put("headline", profile.getHeadline());
        output.put("skills", skillNames(profile));
        output.put("overall_confidence", profile.getOverallConfidence());
        return output;
    }

    private Object resolveValue(CandidateProfileResponse profile, String from) {
        switch (from) {
            case "candidateId": return profile.getCandidateId();
            case "fullName": return profile.getFullName();
            case "headline": return profile.getHeadline();
            case "emails": return profile.getEmails();
            case "emails[0]": return first(profile.getEmails());
            case "phones": return profile.getPhones();
            case "phones[0]": return first(profile.getPhones());
            case "skills": return profile.getSkills();
            case "skills[].name": return skillNames(profile);
            case "education": return profile.getEducation();
            case "experience": return profile.getExperience();
            case "overallConfidence": return profile.getOverallConfidence();
            case "provenance": return profile.getProvenance();
            case "conflicts": return profile.getConflicts();
            case "createdAt": return profile.getCreatedAt();
            default: return null;
        }
    }

    private Object first(List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private List<String> skillNames(CandidateProfileResponse profile) {
        if (profile.getSkills() == null) {
            return List.of();
        }
        return profile.getSkills().stream()
                .map(CandidateProfileResponse.SkillDto::getName)
                .collect(Collectors.toList());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
