package com.cpb.controller;

import com.cpb.dto.CandidateProfileResponse;
import com.cpb.dto.UploadResponse;
import com.cpb.model.*;
import com.cpb.repository.CandidateProfileRepository;
import com.cpb.service.CandidateMergeService;
import com.cpb.service.CsvParserService;
import com.cpb.service.ResumeParserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Handles candidate file uploads (resume PDF + CSV).
 * Orchestrates parsing, normalization, merging, persistence, and upsert behavior.
 */
@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "http://localhost:5173")
public class CandidateUploadController {

    private final ResumeParserService resumeParserService;
    private final CsvParserService csvParserService;
    private final CandidateMergeService candidateMergeService;
    private final CandidateProfileRepository candidateProfileRepository;
    private final CandidateProfileController profileController;
    private final ObjectMapper objectMapper;

    public CandidateUploadController(ResumeParserService resumeParserService,
                                     CsvParserService csvParserService,
                                     CandidateMergeService candidateMergeService,
                                     CandidateProfileRepository candidateProfileRepository,
                                     CandidateProfileController profileController,
                                     ObjectMapper objectMapper) {
        this.resumeParserService = resumeParserService;
        this.csvParserService = csvParserService;
        this.candidateMergeService = candidateMergeService;
        this.candidateProfileRepository = candidateProfileRepository;
        this.profileController = profileController;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadCandidateData(
            @RequestParam("resumeFile") MultipartFile resumeFile,
            @RequestParam("csvFile") MultipartFile csvFile) {

        // --- Validation ---
        if (resumeFile == null || resumeFile.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(UploadResponse.error("Resume PDF file is required."));
        }
        if (csvFile == null || csvFile.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(UploadResponse.error("CSV file is required."));
        }

        String resumeFilename = resumeFile.getOriginalFilename();
        if (resumeFilename == null || !resumeFilename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest()
                    .body(UploadResponse.error("Resume file must be a PDF."));
        }

        String csvFilename = csvFile.getOriginalFilename();
        if (csvFilename == null || !csvFilename.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest()
                    .body(UploadResponse.error("Data file must be a CSV."));
        }

        try {
            // --- Parse Resume PDF ---
            Map<String, Object> resumeData = resumeParserService.parse(resumeFile);

            // --- Parse CSV ---
            Map<String, Object> csvData = csvParserService.parse(csvFile);

            // --- Merge Data into a fresh canonical profile ---
            CandidateProfile incomingProfile = candidateMergeService.merge(resumeData, csvData);

            // --- Upsert instead of blindly creating a duplicate record ---
            boolean existedBefore = findExistingCandidate(incomingProfile).isPresent();
            CandidateProfile saved = saveOrUpdateExistingCandidate(incomingProfile);

            // --- Build Response ---
            CandidateProfileResponse response = profileController.toResponse(saved);

            String message = existedBefore
                    ? "Existing candidate profile updated successfully."
                    : "Candidate profile created successfully.";

            return ResponseEntity.ok(
                    UploadResponse.success(
                            message,
                            saved.getId(),
                            response));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(UploadResponse.error("Failed to process files: " + e.getMessage()));
        }
    }

    /**
     * Upsert policy:
     * 1. Match by overlapping email.
     * 2. If email does not match, match by overlapping phone.
     * 3. If phone does not match, match by full name.
     *
     * If a match is found, update that existing row and replace its child records
     * using orphanRemoval=true from CandidateProfile relationships.
     */
    private CandidateProfile saveOrUpdateExistingCandidate(CandidateProfile incomingProfile) {
        Optional<CandidateProfile> existingOptional = findExistingCandidate(incomingProfile);

        if (existingOptional.isEmpty()) {
            return candidateProfileRepository.save(incomingProfile);
        }

        CandidateProfile existing = existingOptional.get();
        copyIncomingProfileIntoExistingProfile(incomingProfile, existing);
        return candidateProfileRepository.save(existing);
    }

    private Optional<CandidateProfile> findExistingCandidate(CandidateProfile incomingProfile) {
        List<CandidateProfile> existingProfiles = candidateProfileRepository.findAll();

        Set<String> incomingEmails = parseJsonArray(incomingProfile.getEmails());
        if (!incomingEmails.isEmpty()) {
            Optional<CandidateProfile> byEmail = existingProfiles.stream()
                    .filter(existing -> hasOverlap(parseJsonArray(existing.getEmails()), incomingEmails))
                    .findFirst();
            if (byEmail.isPresent()) return byEmail;
        }

        Set<String> incomingPhones = parseJsonArray(incomingProfile.getPhones());
        if (!incomingPhones.isEmpty()) {
            Optional<CandidateProfile> byPhone = existingProfiles.stream()
                    .filter(existing -> hasOverlap(parseJsonArray(existing.getPhones()), incomingPhones))
                    .findFirst();
            if (byPhone.isPresent()) return byPhone;
        }

        if (incomingProfile.getFullName() != null && !incomingProfile.getFullName().isBlank()) {
            return candidateProfileRepository.findByFullNameIgnoreCase(incomingProfile.getFullName().trim());
        }

        return Optional.empty();
    }

    private Set<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) return Set.of();
        try {
            List<String> values = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            Set<String> normalized = new HashSet<>();
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    normalized.add(value.trim().toLowerCase());
                }
            }
            return normalized;
        } catch (JsonProcessingException e) {
            return Set.of();
        }
    }

    private boolean hasOverlap(Set<String> existingValues, Set<String> incomingValues) {
        if (existingValues.isEmpty() || incomingValues.isEmpty()) return false;
        for (String value : incomingValues) {
            if (existingValues.contains(value)) return true;
        }
        return false;
    }

    private void copyIncomingProfileIntoExistingProfile(CandidateProfile incoming, CandidateProfile existing) {
        existing.setFullName(incoming.getFullName());
        existing.setHeadline(incoming.getHeadline());
        existing.setEmails(incoming.getEmails());
        existing.setPhones(incoming.getPhones());
        existing.setOverallConfidence(incoming.getOverallConfidence());

        existing.getSkills().clear();
        for (Skill skill : incoming.getSkills()) {
            existing.addSkill(new Skill(skill.getName(), skill.getConfidence(), skill.getSources()));
        }

        existing.getEducations().clear();
        for (Education education : incoming.getEducations()) {
            existing.addEducation(new Education(education.getDegree(), education.getInstitution(), education.getYear()));
        }

        existing.getExperiences().clear();
        for (Experience experience : incoming.getExperiences()) {
            existing.addExperience(new Experience(experience.getDescription()));
        }

        existing.getProvenanceRecords().clear();
        for (ProvenanceRecord record : incoming.getProvenanceRecords()) {
            existing.addProvenanceRecord(new ProvenanceRecord(
                    record.getFieldName(),
                    record.getValue(),
                    record.getSourceType(),
                    record.getExtractionMethod(),
                    record.getConfidence()
            ));
        }

        existing.getConflictRecords().clear();
        for (ConflictRecord conflict : incoming.getConflictRecords()) {
            existing.addConflictRecord(new ConflictRecord(
                    conflict.getFieldName(),
                    conflict.getResumeValue(),
                    conflict.getCsvValue(),
                    conflict.getSelectedValue(),
                    conflict.getReason()
            ));
        }
    }
}
