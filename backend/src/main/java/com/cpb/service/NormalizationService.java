package com.cpb.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Normalizes candidate data fields to canonical formats.
 */
@Service
public class NormalizationService {

    // Skill alias map: common variations -> canonical name
    private static final Map<String, String> SKILL_ALIASES = new LinkedHashMap<>();

    static {
        SKILL_ALIASES.put("reactjs", "React");
        SKILL_ALIASES.put("react.js", "React");
        SKILL_ALIASES.put("react js", "React");
        SKILL_ALIASES.put("springboot", "Spring Boot");
        SKILL_ALIASES.put("spring-boot", "Spring Boot");
        SKILL_ALIASES.put("js", "JavaScript");
        SKILL_ALIASES.put("javascript", "JavaScript");
        SKILL_ALIASES.put("typescript", "TypeScript");
        SKILL_ALIASES.put("ts", "TypeScript");
        SKILL_ALIASES.put("nodejs", "Node.js");
        SKILL_ALIASES.put("node.js", "Node.js");
        SKILL_ALIASES.put("node js", "Node.js");
        SKILL_ALIASES.put("postgres", "PostgreSQL");
        SKILL_ALIASES.put("postgresql", "PostgreSQL");
        SKILL_ALIASES.put("mongo", "MongoDB");
        SKILL_ALIASES.put("mongodb", "MongoDB");
        SKILL_ALIASES.put("mysql", "MySQL");
        SKILL_ALIASES.put("aws", "AWS");
        SKILL_ALIASES.put("amazon web services", "AWS");
        SKILL_ALIASES.put("gcp", "GCP");
        SKILL_ALIASES.put("google cloud", "GCP");
        SKILL_ALIASES.put("k8s", "Kubernetes");
        SKILL_ALIASES.put("kubernetes", "Kubernetes");
        SKILL_ALIASES.put("docker", "Docker");
        SKILL_ALIASES.put("python", "Python");
        SKILL_ALIASES.put("java", "Java");
        SKILL_ALIASES.put("html", "HTML");
        SKILL_ALIASES.put("html5", "HTML");
        SKILL_ALIASES.put("css", "CSS");
        SKILL_ALIASES.put("css3", "CSS");
        SKILL_ALIASES.put("angular", "Angular");
        SKILL_ALIASES.put("angularjs", "Angular");
        SKILL_ALIASES.put("vue", "Vue.js");
        SKILL_ALIASES.put("vuejs", "Vue.js");
        SKILL_ALIASES.put("vue.js", "Vue.js");
        SKILL_ALIASES.put("spring boot", "Spring Boot");
        SKILL_ALIASES.put("react", "React");
    }

    /**
     * Normalize an email address: lowercase, trim spaces.
     */
    public String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    /**
     * Normalize a phone number:
     * - Remove spaces, hyphens, brackets, dots
     * - If Indian 10-digit number, convert to +91 format
     */
    public String normalizePhone(String phone) {
        if (phone == null) return null;

        // Remove all non-digit characters except leading +
        String cleaned = phone.trim();
        boolean hasPlus = cleaned.startsWith("+");
        String digitsOnly = cleaned.replaceAll("[^\\d]", "");

        if (digitsOnly.isEmpty()) return null;

        // Indian number handling
        if (digitsOnly.length() == 10) {
            // Assume Indian number
            return "+91" + digitsOnly;
        } else if (digitsOnly.length() == 12 && digitsOnly.startsWith("91")) {
            return "+" + digitsOnly;
        } else if (digitsOnly.length() == 11 && digitsOnly.startsWith("0")) {
            // Remove leading 0 and add +91
            return "+91" + digitsOnly.substring(1);
        } else if (hasPlus) {
            return "+" + digitsOnly;
        }

        return "+" + digitsOnly;
    }

    /**
     * Normalize a skill name using the alias map.
     * Returns the canonical display name.
     */
    public String normalizeSkill(String skill) {
        if (skill == null) return null;
        String trimmed = skill.trim();
        String key = trimmed.toLowerCase();

        // Check alias map
        if (SKILL_ALIASES.containsKey(key)) {
            return SKILL_ALIASES.get(key);
        }

        // Return original with proper trimming
        return trimmed;
    }

    /**
     * Get the normalized key for a skill (for comparison).
     */
    public String getSkillKey(String skill) {
        if (skill == null) return null;
        String normalized = normalizeSkill(skill);
        return normalized.toLowerCase();
    }

    /**
     * Normalize a name: trim extra spaces, proper casing is preserved.
     */
    public String normalizeName(String name) {
        if (name == null) return null;
        // Replace multiple spaces with single space
        return name.trim().replaceAll("\\s+", " ");
    }

    /**
     * Compare two names case-insensitively.
     * Returns: "exact", "partial", or "conflict"
     */
    public String compareNames(String name1, String name2) {
        if (name1 == null || name2 == null) return "conflict";

        String n1 = normalizeName(name1).toLowerCase();
        String n2 = normalizeName(name2).toLowerCase();

        if (n1.equals(n2)) {
            return "exact";
        }

        // Check if one contains the other (partial/initial match)
        if (n1.contains(n2) || n2.contains(n1)) {
            return "partial";
        }

        // Check initial-based matching: "B S" vs "Baskar S"
        String[] parts1 = n1.split("\\s+");
        String[] parts2 = n2.split("\\s+");

        if (parts1.length > 0 && parts2.length > 0) {
            // Check if last names match and first name is an initial
            if (parts1.length >= 2 && parts2.length >= 2) {
                String last1 = parts1[parts1.length - 1];
                String last2 = parts2[parts2.length - 1];
                if (last1.equals(last2)) {
                    String first1 = parts1[0];
                    String first2 = parts2[0];
                    if (first1.length() == 1 && first2.startsWith(first1) ||
                            first2.length() == 1 && first1.startsWith(first2)) {
                        return "partial";
                    }
                }
            }
        }

        return "conflict";
    }

    /**
     * Normalize a list of emails.
     */
    public List<String> normalizeEmails(List<String> emails) {
        if (emails == null) return List.of();
        Set<String> unique = new LinkedHashSet<>();
        for (String email : emails) {
            String normalized = normalizeEmail(email);
            if (normalized != null && !normalized.isEmpty()) {
                unique.add(normalized);
            }
        }
        return new ArrayList<>(unique);
    }

    /**
     * Normalize a list of phones.
     */
    public List<String> normalizePhones(List<String> phones) {
        if (phones == null) return List.of();
        Set<String> unique = new LinkedHashSet<>();
        for (String phone : phones) {
            String normalized = normalizePhone(phone);
            if (normalized != null && !normalized.isEmpty()) {
                unique.add(normalized);
            }
        }
        return new ArrayList<>(unique);
    }

    /**
     * Normalize and deduplicate a list of skills.
     * Returns a map of normalizedKey -> displayName.
     */
    public Map<String, String> normalizeSkills(List<String> skills) {
        if (skills == null) return Map.of();
        Map<String, String> unique = new LinkedHashMap<>();
        for (String skill : skills) {
            String display = normalizeSkill(skill);
            String key = display.toLowerCase();
            if (!unique.containsKey(key)) {
                unique.put(key, display);
            }
        }
        return unique;
    }
}
