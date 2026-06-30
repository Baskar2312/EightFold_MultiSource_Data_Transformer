package com.cpb.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses resume PDF files using Apache PDFBox.
 * Extracts candidate fields using regex and rule-based heuristics.
 */
@Service
public class ResumeParserService {

    // Regex patterns
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(?:\\+?\\d{1,3}[\\s\\-]?)?\\(?\\d{2,4}\\)?[\\s\\-]?\\d{3,5}[\\s\\-]?\\d{3,5}");

    private static final Pattern SKILLS_SECTION_PATTERN =
            Pattern.compile("(?i)(?:skills|technical\\s+skills|key\\s+skills|core\\s+competencies)\\s*:?\\s*(.+?)(?=\\n(?:education|experience|projects|certifications|achievements|$))",
                    Pattern.DOTALL);

    private static final Pattern EDUCATION_PATTERN =
            Pattern.compile("(?i)(?:B\\.?E\\.?|B\\.?Tech|M\\.?Tech|M\\.?S\\.?|B\\.?S\\.?|B\\.?Sc|M\\.?Sc|MBA|Ph\\.?D|Bachelor|Master|Diploma)[\\s,]+(?:in\\s+|of\\s+)?([\\w\\s]+?)(?:\\n|,|$)");

    private static final Pattern EXPERIENCE_SECTION_PATTERN =
            Pattern.compile("(?i)(?:experience|work\\s+experience|professional\\s+experience)\\s*:?\\s*(.+?)(?=\\n(?:education|skills|projects|certifications|achievements|$))",
                    Pattern.DOTALL);

    /**
     * Parse a resume PDF file and extract structured candidate data.
     */
    public Map<String, Object> parse(MultipartFile file) throws IOException {
        String text = extractText(file);
        if (text == null || text.isBlank()) {
            throw new IOException("Could not extract text from PDF. The file may be image-based or empty.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("rawText", text);
        data.put("name", extractName(text));
        data.put("emails", extractEmails(text));
        data.put("phones", extractPhones(text));
        data.put("headline", extractHeadline(text));
        data.put("skills", extractSkills(text));
        data.put("education", extractEducation(text));
        data.put("experience", extractExperience(text));

        return data;
    }

    private String extractText(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Extract name: typically the first non-empty line of a resume.
     */
    String extractName(String text) {
        String[] lines = text.split("\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            // Skip empty lines and lines that look like headers/contact info
            if (trimmed.isEmpty()) continue;
            if (trimmed.contains("@") || trimmed.matches(".*\\d{5,}.*")) continue;
            if (trimmed.toLowerCase().startsWith("resume") || trimmed.toLowerCase().startsWith("curriculum")) continue;
            // First meaningful line is likely the name
            if (trimmed.length() > 1 && trimmed.length() < 60) {
                return trimmed;
            }
        }
        return null;
    }

    List<String> extractEmails(String text) {
        List<String> emails = new ArrayList<>();
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        while (matcher.find()) {
            String email = matcher.group().toLowerCase().trim();
            if (!emails.contains(email)) {
                emails.add(email);
            }
        }
        return emails;
    }

    List<String> extractPhones(String text) {
        List<String> phones = new ArrayList<>();
        Matcher matcher = PHONE_PATTERN.matcher(text);
        while (matcher.find()) {
            String phone = matcher.group().trim();
            // Filter out numbers that are too short (likely not phone numbers)
            String digitsOnly = phone.replaceAll("[^\\d]", "");
            if (digitsOnly.length() >= 10) {
                if (!phones.contains(phone)) {
                    phones.add(phone);
                }
            }
        }
        return phones;
    }

    /**
     * Extract headline/job title: typically the second non-empty line or a line
     * containing common title keywords.
     */
    String extractHeadline(String text) {
        String[] lines = text.split("\\n");
        boolean foundName = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.contains("@") || trimmed.matches(".*\\d{5,}.*")) continue;

            if (!foundName) {
                // Skip the name (first meaningful line)
                if (trimmed.length() > 1 && trimmed.length() < 60) {
                    foundName = true;
                    continue;
                }
            } else {
                // Second meaningful line could be headline
                if (trimmed.length() > 2 && trimmed.length() < 80
                        && !trimmed.toLowerCase().startsWith("email")
                        && !trimmed.toLowerCase().startsWith("phone")
                        && !trimmed.toLowerCase().startsWith("address")
                        && !trimmed.toLowerCase().startsWith("http")) {
                    return trimmed;
                }
            }
        }
        return null;
    }

    List<String> extractSkills(String text) {
        List<String> skills = new ArrayList<>();

        // Try to find skills section
        Matcher matcher = SKILLS_SECTION_PATTERN.matcher(text);
        if (matcher.find()) {
            String skillsText = matcher.group(1);
            // Split by commas, pipes, semicolons, or bullet points
            String[] parts = skillsText.split("[,|;•·▪\\n]+");
            for (String part : parts) {
                String skill = part.trim();
                if (!skill.isEmpty() && skill.length() < 50) {
                    skills.add(skill);
                }
            }
        }

        // Fallback: look for "Skills:" on a single line
        if (skills.isEmpty()) {
            Pattern inlineSkills = Pattern.compile("(?i)skills\\s*:\\s*(.+)", Pattern.MULTILINE);
            Matcher inlineMatcher = inlineSkills.matcher(text);
            if (inlineMatcher.find()) {
                String[] parts = inlineMatcher.group(1).split("[,|;•·]+");
                for (String part : parts) {
                    String skill = part.trim();
                    if (!skill.isEmpty() && skill.length() < 50) {
                        skills.add(skill);
                    }
                }
            }
        }

        return skills;
    }

    List<String> extractEducation(String text) {
        List<String> educations = new ArrayList<>();
        Matcher matcher = EDUCATION_PATTERN.matcher(text);
        while (matcher.find()) {
            educations.add(matcher.group().trim());
        }

        // Also try to find education section
        if (educations.isEmpty()) {
            Pattern eduSection = Pattern.compile("(?i)education\\s*:?\\s*(.+?)(?=\\n(?:experience|skills|projects|$))",
                    Pattern.DOTALL);
            Matcher secMatcher = eduSection.matcher(text);
            if (secMatcher.find()) {
                String[] lines = secMatcher.group(1).split("\\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && trimmed.length() > 3) {
                        educations.add(trimmed);
                    }
                }
            }
        }

        return educations;
    }

    List<String> extractExperience(String text) {
        List<String> experiences = new ArrayList<>();

        Matcher matcher = EXPERIENCE_SECTION_PATTERN.matcher(text);
        if (matcher.find()) {
            String[] lines = matcher.group(1).split("\\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && trimmed.length() > 5) {
                    experiences.add(trimmed);
                }
            }
        }

        // Fallback: look for "Experience:" on a single line
        if (experiences.isEmpty()) {
            Pattern inlineExp = Pattern.compile("(?i)experience\\s*:\\s*(.+)", Pattern.MULTILINE);
            Matcher inlineMatcher = inlineExp.matcher(text);
            if (inlineMatcher.find()) {
                experiences.add(inlineMatcher.group(1).trim());
            }
        }

        return experiences;
    }
}
