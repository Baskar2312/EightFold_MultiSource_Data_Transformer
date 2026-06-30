package com.cpb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "skill")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Double confidence;

    @Column(columnDefinition = "TEXT")
    private String sources; // JSON array string e.g. ["RESUME","CSV"]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_profile_id", nullable = false)
    @JsonIgnore
    private CandidateProfile candidateProfile;

    public Skill() {}

    public Skill(String name, Double confidence, String sources) {
        this.name = name;
        this.confidence = confidence;
        this.sources = sources;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getSources() { return sources; }
    public void setSources(String sources) { this.sources = sources; }

    public CandidateProfile getCandidateProfile() { return candidateProfile; }
    public void setCandidateProfile(CandidateProfile candidateProfile) { this.candidateProfile = candidateProfile; }
}
