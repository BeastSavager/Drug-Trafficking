package com.example.analyzer;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analyses")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String platform;

    @ElementCollection
    @CollectionTable(name = "analysis_keywords", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "keyword")
    private List<String> detectedKeywords = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "analysis_types", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "type")
    private List<String> detectedDrugTypes = new ArrayList<>();

    private Integer score = 0;

    // Active | Under Review | Suspended
    private String status = "Active";

    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Analysis() {}

    // --- getters & setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public List<String> getDetectedKeywords() { return detectedKeywords; }
    public void setDetectedKeywords(List<String> detectedKeywords) { this.detectedKeywords = detectedKeywords; }

    public List<String> getDetectedDrugTypes() { return detectedDrugTypes; }
    public void setDetectedDrugTypes(List<String> detectedDrugTypes) { this.detectedDrugTypes = detectedDrugTypes; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
