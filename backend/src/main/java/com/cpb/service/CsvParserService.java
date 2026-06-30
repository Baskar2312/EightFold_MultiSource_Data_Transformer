package com.cpb.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Parses CSV files containing structured candidate data.
 * Maps CSV columns to canonical candidate fields.
 */
@Service
public class CsvParserService {

    /**
     * Parse a CSV file and extract the first candidate row.
     * Expected headers: name, email, phone, title, skills
     */
    public Map<String, Object> parse(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("CSV file is empty.");
        }

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {

            List<CSVRecord> records = parser.getRecords();
            if (records.isEmpty()) {
                throw new IOException("CSV file contains no data rows.");
            }

            // Process first record (single candidate upload)
            CSVRecord record = records.get(0);
            return mapRecord(record, parser.getHeaderNames());
        }
    }

    private Map<String, Object> mapRecord(CSVRecord record, List<String> headers) {
        Map<String, Object> data = new LinkedHashMap<>();

        // Map known columns (case-insensitive)
        for (String header : headers) {
            String normalizedHeader = header.trim().toLowerCase();
            String value = record.get(header).trim();

            switch (normalizedHeader) {
                case "name":
                case "full_name":
                case "fullname":
                case "candidate_name":
                    data.put("name", value);
                    break;

                case "email":
                case "email_address":
                case "emailaddress":
                    data.put("emails", value.isEmpty() ? List.of() : List.of(value));
                    break;

                case "phone":
                case "phone_number":
                case "phonenumber":
                case "mobile":
                case "contact":
                    data.put("phones", value.isEmpty() ? List.of() : List.of(value));
                    break;

                case "title":
                case "job_title":
                case "jobtitle":
                case "headline":
                case "designation":
                case "role":
                    data.put("headline", value);
                    break;

                case "skills":
                case "skill":
                case "technologies":
                case "tech_stack":
                    if (!value.isEmpty()) {
                        List<String> skills = new ArrayList<>();
                        // Skills may be comma-separated, possibly within quotes
                        String[] parts = value.split(",");
                        for (String part : parts) {
                            String skill = part.trim();
                            if (!skill.isEmpty()) {
                                skills.add(skill);
                            }
                        }
                        data.put("skills", skills);
                    } else {
                        data.put("skills", List.of());
                    }
                    break;

                default:
                    // Store unmapped fields as-is
                    if (!value.isEmpty()) {
                        data.put(normalizedHeader, value);
                    }
                    break;
            }
        }

        return data;
    }
}
