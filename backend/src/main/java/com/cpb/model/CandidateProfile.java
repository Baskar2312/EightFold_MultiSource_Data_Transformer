package com.cpb.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidate_profile")
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String headline;

    @Column(columnDefinition = "TEXT")
    private String emails; // JSON array string e.g. ["a@b.com","c@d.com"]

    @Column(columnDefinition = "TEXT")
    private String phones; // JSON array string e.g. ["+919876543210"]

    private Double overallConfidence;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "candidateProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "candidateProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "candidateProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "candidateProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProvenanceRecord> provenanceRecords = new ArrayList<>();

    @OneToMany(mappedBy = "candidateProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ConflictRecord> conflictRecords = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getEmails() { return emails; }
    public void setEmails(String emails) { this.emails = emails; }

    public String getPhones() { return phones; }
    public void setPhones(String phones) { this.phones = phones; }

    public Double getOverallConfidence() { return overallConfidence; }
    public void setOverallConfidence(Double overallConfidence) { this.overallConfidence = overallConfidence; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) { this.skills = skills; }

    public List<Education> getEducations() { return educations; }
    public void setEducations(List<Education> educations) { this.educations = educations; }

    public List<Experience> getExperiences() { return experiences; }
    public void setExperiences(List<Experience> experiences) { this.experiences = experiences; }

    public List<ProvenanceRecord> getProvenanceRecords() { return provenanceRecords; }
    public void setProvenanceRecords(List<ProvenanceRecord> provenanceRecords) { this.provenanceRecords = provenanceRecords; }

    public List<ConflictRecord> getConflictRecords() { return conflictRecords; }
    public void setConflictRecords(List<ConflictRecord> conflictRecords) { this.conflictRecords = conflictRecords; }

    // --- Helper methods ---

    public void addSkill(Skill skill) {
        skills.add(skill);
        skill.setCandidateProfile(this);
    }

    public void addEducation(Education education) {
        educations.add(education);
        education.setCandidateProfile(this);
    }

    public void addExperience(Experience experience) {
        experiences.add(experience);
        experience.setCandidateProfile(this);
    }

    public void addProvenanceRecord(ProvenanceRecord record) {
        provenanceRecords.add(record);
        record.setCandidateProfile(this);
    }

    public void addConflictRecord(ConflictRecord record) {
        conflictRecords.add(record);
        record.setCandidateProfile(this);
    }
}
