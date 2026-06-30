package com.cpb.controller;

import com.cpb.dto.CandidateProfileResponse;
import com.cpb.dto.ProjectionConfig;
import com.cpb.dto.CandidateProfileResponse.*;
import com.cpb.model.*;
import com.cpb.repository.CandidateProfileRepository;
import com.cpb.service.ProjectionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides read APIs for candidate profiles.
 */
@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "http://localhost:5173")
public class CandidateProfileController {

    private final CandidateProfileRepository candidateProfileRepository;
    private final ObjectMapper objectMapper;
    private final ProjectionService projectionService;

    public CandidateProfileController(CandidateProfileRepository candidateProfileRepository,
                                       ObjectMapper objectMapper,
                                       ProjectionService projectionService) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.objectMapper = objectMapper;
        this.projectionService = projectionService;
    }

    /**
     * Get all candidate profiles (summary view).
     */
    @GetMapping
    public ResponseEntity<List<CandidateProfileResponse>> getAllCandidates() {
        List<CandidateProfile> profiles = candidateProfileRepository.findAll();
        List<CandidateProfileResponse> responses = profiles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get a single candidate profile by ID (full detail view).
     */
    @GetMapping("/{id}")
    public ResponseEntity<CandidateProfileResponse> getCandidateById(@PathVariable Long id) {
        return candidateProfileRepository.findById(id)
                .map(profile -> ResponseEntity.ok(toResponse(profile)))
                .orElse(ResponseEntity.notFound().build());
    }



    /**
     * Generate a custom JSON projection using a runtime configuration.
     * This satisfies the assignment requirement to emit JSON for a custom config
     * without changing backend code for every output schema.
     */
    @PostMapping("/{id}/project")
    public ResponseEntity<Map<String, Object>> projectCandidate(
            @PathVariable Long id,
            @RequestBody ProjectionConfig config) {
        return candidateProfileRepository.findById(id)
                .map(profile -> ResponseEntity.ok(
                        projectionService.project(toResponse(profile), config)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Convert a CandidateProfile entity to a CandidateProfileResponse DTO.
     */
    public CandidateProfileResponse toResponse(CandidateProfile profile) {
        CandidateProfileResponse response = new CandidateProfileResponse();
        response.setCandidateId(profile.getId());
        response.setFullName(profile.getFullName());
        response.setHeadline(profile.getHeadline());
        response.setOverallConfidence(profile.getOverallConfidence());
        response.setCreatedAt(profile.getCreatedAt());

        // Parse JSON arrays
        response.setEmails(parseJsonList(profile.getEmails()));
        response.setPhones(parseJsonList(profile.getPhones()));

        // Map skills
        response.setSkills(profile.getSkills().stream()
                .map(s -> new SkillDto(s.getName(), s.getConfidence(), parseJsonList(s.getSources())))
                .collect(Collectors.toList()));

        // Map education
        response.setEducation(profile.getEducations().stream()
                .map(e -> new EducationDto(e.getDegree(), e.getInstitution(), e.getYear()))
                .collect(Collectors.toList()));

        // Map experience
        response.setExperience(profile.getExperiences().stream()
                .map(e -> new ExperienceDto(e.getDescription()))
                .collect(Collectors.toList()));

        // Map provenance
        response.setProvenance(profile.getProvenanceRecords().stream()
                .map(p -> new ProvenanceDto(p.getFieldName(), p.getValue(),
                        p.getSourceType(), p.getExtractionMethod(), p.getConfidence()))
                .collect(Collectors.toList()));

        // Map conflicts
        response.setConflicts(profile.getConflictRecords().stream()
                .map(c -> new ConflictDto(c.getFieldName(), c.getResumeValue(),
                        c.getCsvValue(), c.getSelectedValue(), c.getReason()))
                .collect(Collectors.toList()));

        return response;
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
