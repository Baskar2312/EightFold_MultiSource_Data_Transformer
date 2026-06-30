package com.cpb.service;

import com.cpb.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Merges candidate data from resume and CSV sources into one canonical profile.
 * Handles conflict resolution, provenance tracking, and confidence scoring.
 */
@Service
public class CandidateMergeService {

    private final NormalizationService normalizationService;
    private final ConfidenceService confidenceService;
    private final ProvenanceService provenanceService;
    private final ObjectMapper objectMapper;

    public CandidateMergeService(NormalizationService normalizationService,
                                  ConfidenceService confidenceService,
                                  ProvenanceService provenanceService,
                                  ObjectMapper objectMapper) {
        this.normalizationService = normalizationService;
        this.confidenceService = confidenceService;
        this.provenanceService = provenanceService;
        this.objectMapper = objectMapper;
    }

    /**
     * Merge resume data and CSV data into a single CandidateProfile entity.
     */
    @SuppressWarnings("unchecked")
    public CandidateProfile merge(Map<String, Object> resumeData, Map<String, Object> csvData) {
        CandidateProfile profile = new CandidateProfile();
        List<Double> allConfidences = new ArrayList<>();

        // === 1. Merge Name ===
        String resumeName = normalizationService.normalizeName((String) resumeData.get("name"));
        String csvName = normalizationService.normalizeName((String) csvData.get("name"));
        mergeName(profile, resumeName, csvName, allConfidences);

        // === 2. Merge Emails ===
        List<String> resumeEmails = normalizationService.normalizeEmails(
                getStringList(resumeData, "emails"));
        List<String> csvEmails = normalizationService.normalizeEmails(
                getStringList(csvData, "emails"));
        mergeEmails(profile, resumeEmails, csvEmails, allConfidences);

        // === 3. Merge Phones ===
        List<String> resumePhones = normalizationService.normalizePhones(
                getStringList(resumeData, "phones"));
        List<String> csvPhones = normalizationService.normalizePhones(
                getStringList(csvData, "phones"));
        mergePhones(profile, resumePhones, csvPhones, allConfidences);

        // === 4. Merge Headline ===
        String resumeHeadline = (String) resumeData.get("headline");
        String csvHeadline = (String) csvData.get("headline");
        mergeHeadline(profile, resumeHeadline, csvHeadline, allConfidences);

        // === 5. Merge Skills ===
        List<String> resumeSkills = getStringList(resumeData, "skills");
        List<String> csvSkills = getStringList(csvData, "skills");
        mergeSkills(profile, resumeSkills, csvSkills, allConfidences);

        // === 6. Education (from resume only) ===
        List<String> educationList = getStringList(resumeData, "education");
        for (String edu : educationList) {
            Education education = new Education(edu, null, null);
            profile.addEducation(education);
            profile.addProvenanceRecord(
                    provenanceService.createRecord("education", edu, "RESUME", "REGEX", 0.80));
        }

        // === 7. Experience (from resume only) ===
        List<String> experienceList = getStringList(resumeData, "experience");
        for (String exp : experienceList) {
            Experience experience = new Experience(exp);
            profile.addExperience(experience);
            profile.addProvenanceRecord(
                    provenanceService.createRecord("experience", exp, "RESUME", "PDF_TEXT_EXTRACTION", 0.80));
        }

        // === 8. Calculate overall confidence ===
        profile.setOverallConfidence(confidenceService.calculateOverallConfidence(allConfidences));

        return profile;
    }

    private void mergeName(CandidateProfile profile, String resumeName, String csvName,
                           List<Double> allConfidences) {
        if (resumeName != null && csvName != null) {
            String matchType = normalizationService.compareNames(resumeName, csvName);
            double confidence = confidenceService.nameConfidence(matchType);
            allConfidences.add(confidence);

            if ("exact".equals(matchType)) {
                profile.setFullName(resumeName);
            } else if ("partial".equals(matchType)) {
                // Prefer the longer (more complete) name
                profile.setFullName(resumeName.length() >= csvName.length() ? resumeName : csvName);
            } else {
                // Conflict: prefer resume name
                profile.setFullName(resumeName);
                profile.addConflictRecord(new ConflictRecord(
                        "name", resumeName, csvName, resumeName,
                        "Resume usually contains the candidate's preferred name format."));
            }

            profile.addProvenanceRecord(
                    provenanceService.createRecord("name", resumeName, "RESUME", "PDF_TEXT_EXTRACTION", confidence));
            profile.addProvenanceRecord(
                    provenanceService.createRecord("name", csvName, "CSV", "CSV_MAPPING", confidence));
        } else if (resumeName != null) {
            profile.setFullName(resumeName);
            double confidence = confidenceService.singleSourceConfidence("RESUME");
            allConfidences.add(confidence);
            profile.addProvenanceRecord(
                    provenanceService.createRecord("name", resumeName, "RESUME", "PDF_TEXT_EXTRACTION", confidence));
        } else if (csvName != null) {
            profile.setFullName(csvName);
            double confidence = confidenceService.singleSourceConfidence("CSV");
            allConfidences.add(confidence);
            profile.addProvenanceRecord(
                    provenanceService.createRecord("name", csvName, "CSV", "CSV_MAPPING", confidence));
        } else {
            profile.setFullName("Unknown");
            allConfidences.add(0.0);
        }
    }

    private void mergeEmails(CandidateProfile profile, List<String> resumeEmails,
                             List<String> csvEmails, List<Double> allConfidences) {
        Set<String> allEmails = new LinkedHashSet<>();
        Set<String> resumeSet = new LinkedHashSet<>(resumeEmails);
        Set<String> csvSet = new LinkedHashSet<>(csvEmails);

        allEmails.addAll(resumeEmails);
        allEmails.addAll(csvEmails);

        for (String email : allEmails) {
            boolean inResume = resumeSet.contains(email);
            boolean inCsv = csvSet.contains(email);
            double confidence = (inResume && inCsv) ?
                    confidenceService.fieldMatchConfidence() :
                    confidenceService.singleSourceConfidence(inResume ? "RESUME" : "CSV");
            allConfidences.add(confidence);

            if (inResume) {
                profile.addProvenanceRecord(
                        provenanceService.createRecord("email", email, "RESUME", "REGEX", confidence));
            }
            if (inCsv) {
                profile.addProvenanceRecord(
                        provenanceService.createRecord("email", email, "CSV", "CSV_MAPPING", confidence));
            }
        }

        try {
            profile.setEmails(objectMapper.writeValueAsString(new ArrayList<>(allEmails)));
        } catch (JsonProcessingException e) {
            profile.setEmails("[]");
        }
    }

    private void mergePhones(CandidateProfile profile, List<String> resumePhones,
                             List<String> csvPhones, List<Double> allConfidences) {
        Set<String> allPhones = new LinkedHashSet<>();
        Set<String> resumeSet = new LinkedHashSet<>(resumePhones);
        Set<String> csvSet = new LinkedHashSet<>(csvPhones);

        allPhones.addAll(resumePhones);
        allPhones.addAll(csvPhones);

        for (String phone : allPhones) {
            boolean inResume = resumeSet.contains(phone);
            boolean inCsv = csvSet.contains(phone);
            double confidence = (inResume && inCsv) ?
                    confidenceService.fieldMatchConfidence() :
                    confidenceService.singleSourceConfidence(inResume ? "RESUME" : "CSV");
            allConfidences.add(confidence);

            if (inResume) {
                profile.addProvenanceRecord(
                        provenanceService.createRecord("phone", phone, "RESUME", "REGEX", confidence));
            }
            if (inCsv) {
                profile.addProvenanceRecord(
                        provenanceService.createRecord("phone", phone, "CSV", "CSV_MAPPING", confidence));
            }
        }

        try {
            profile.setPhones(objectMapper.writeValueAsString(new ArrayList<>(allPhones)));
        } catch (JsonProcessingException e) {
            profile.setPhones("[]");
        }
    }

    private void mergeHeadline(CandidateProfile profile, String resumeHeadline,
                               String csvHeadline, List<Double> allConfidences) {
        if (resumeHeadline != null && csvHeadline != null) {
            if (resumeHeadline.trim().equalsIgnoreCase(csvHeadline.trim())) {
                profile.setHeadline(resumeHeadline.trim());
                allConfidences.add(confidenceService.fieldMatchConfidence());
                profile.addProvenanceRecord(
                        provenanceService.createRecord("headline", resumeHeadline.trim(), "RESUME", "PDF_TEXT_EXTRACTION",
                                confidenceService.fieldMatchConfidence()));
                profile.addProvenanceRecord(
                        provenanceService.createRecord("headline", csvHeadline.trim(), "CSV", "CSV_MAPPING",
                                confidenceService.fieldMatchConfidence()));
            } else {
                // Prefer resume headline
                profile.setHeadline(resumeHeadline.trim());
                double confidence = confidenceService.singleSourceConfidence("RESUME");
                allConfidences.add(confidence);

                profile.addConflictRecord(new ConflictRecord(
                        "headline", resumeHeadline.trim(), csvHeadline.trim(),
                        resumeHeadline.trim(),
                        "Resume usually contains richer self-described profile."));

                profile.addProvenanceRecord(
                        provenanceService.createRecord("headline", resumeHeadline.trim(), "RESUME", "PDF_TEXT_EXTRACTION", confidence));
                profile.addProvenanceRecord(
                        provenanceService.createRecord("headline", csvHeadline.trim(), "CSV", "CSV_MAPPING",
                                confidenceService.singleSourceConfidence("CSV")));
            }
        } else if (resumeHeadline != null) {
            profile.setHeadline(resumeHeadline.trim());
            double confidence = confidenceService.singleSourceConfidence("RESUME");
            allConfidences.add(confidence);
            profile.addProvenanceRecord(
                    provenanceService.createRecord("headline", resumeHeadline.trim(), "RESUME", "PDF_TEXT_EXTRACTION", confidence));
        } else if (csvHeadline != null) {
            profile.setHeadline(csvHeadline.trim());
            double confidence = confidenceService.singleSourceConfidence("CSV");
            allConfidences.add(confidence);
            profile.addProvenanceRecord(
                    provenanceService.createRecord("headline", csvHeadline.trim(), "CSV", "CSV_MAPPING", confidence));
        }
    }

    private void mergeSkills(CandidateProfile profile, List<String> resumeSkills,
                             List<String> csvSkills, List<Double> allConfidences) {
        // Normalize skills from both sources
        Map<String, String> normalizedResumeSkills = normalizationService.normalizeSkills(resumeSkills);
        Map<String, String> normalizedCsvSkills = normalizationService.normalizeSkills(csvSkills);

        // Combine all unique skill keys
        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(normalizedResumeSkills.keySet());
        allKeys.addAll(normalizedCsvSkills.keySet());

        for (String key : allKeys) {
            boolean inResume = normalizedResumeSkills.containsKey(key);
            boolean inCsv = normalizedCsvSkills.containsKey(key);

            String displayName = inResume ? normalizedResumeSkills.get(key) : normalizedCsvSkills.get(key);
            double confidence = confidenceService.skillConfidence(inResume, inCsv);
            allConfidences.add(confidence);

            List<String> sources = new ArrayList<>();
            if (inResume) sources.add("RESUME");
            if (inCsv) sources.add("CSV");

            try {
                String sourcesJson = objectMapper.writeValueAsString(sources);
                Skill skill = new Skill(displayName, confidence, sourcesJson);
                profile.addSkill(skill);
            } catch (JsonProcessingException e) {
                Skill skill = new Skill(displayName, confidence, "[\"UNKNOWN\"]");
                profile.addSkill(skill);
            }

            // Provenance for skills
            if (inResume) {
                profile.addProvenanceRecord(
                        provenanceService.createRecord("skill", displayName, "RESUME", "REGEX", confidence));
            }
            if (inCsv) {
                profile.addProvenanceRecord(
                        provenanceService.createRecord("skill", displayName, "CSV", "CSV_MAPPING", confidence));
            }
        }
    }

    /**
     * Helper to safely extract a list of strings from parsed data.
     */
    @SuppressWarnings("unchecked")
    private List<String> getStringList(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            return str.isEmpty() ? List.of() : List.of(str);
        }
        return List.of();
    }
}
