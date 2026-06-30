package com.cpb.service;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Calculates confidence scores for merged candidate fields.
 */
@Service
public class ConfidenceService {

    // Confidence constants
    public static final double BOTH_SOURCES_MATCH = 0.95;
    public static final double RESUME_ONLY = 0.80;
    public static final double CSV_ONLY = 0.75;
    public static final double PARTIAL_MATCH = 0.80;
    public static final double CONFLICT = 0.60;

    /**
     * Skill confidence based on which sources contain it.
     */
    public double skillConfidence(boolean inResume, boolean inCsv) {
        if (inResume && inCsv) return BOTH_SOURCES_MATCH;
        if (inResume) return RESUME_ONLY;
        return CSV_ONLY;
    }

    /**
     * Field confidence when the same value appears in both sources.
     */
    public double fieldMatchConfidence() {
        return BOTH_SOURCES_MATCH;
    }

    /**
     * Field confidence when a value appears in only one source.
     */
    public double singleSourceConfidence(String source) {
        return "RESUME".equals(source) ? RESUME_ONLY : CSV_ONLY;
    }

    /**
     * Name match confidence.
     */
    public double nameConfidence(String matchType) {
        return switch (matchType) {
            case "exact" -> BOTH_SOURCES_MATCH;
            case "partial" -> PARTIAL_MATCH;
            default -> CONFLICT;
        };
    }

    /**
     * Calculate overall confidence as weighted average of individual confidences.
     */
    public double calculateOverallConfidence(List<Double> confidences) {
        if (confidences == null || confidences.isEmpty()) return 0.0;
        double sum = 0;
        for (double c : confidences) {
            sum += c;
        }
        double avg = sum / confidences.size();
        // Round to 2 decimal places
        return Math.round(avg * 100.0) / 100.0;
    }
}
