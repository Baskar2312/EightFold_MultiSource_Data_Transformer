package com.cpb.dto;

import java.util.ArrayList;
import java.util.List;

public class ProjectionConfig {
    private List<ProjectionField> fields = new ArrayList<>();
    private Boolean includeConfidence = false;
    private String onMissing = "null";

    public ProjectionConfig() {}

    public List<ProjectionField> getFields() { return fields; }
    public void setFields(List<ProjectionField> fields) { this.fields = fields; }

    public Boolean getIncludeConfidence() { return includeConfidence; }
    public void setIncludeConfidence(Boolean includeConfidence) { this.includeConfidence = includeConfidence; }

    public String getOnMissing() { return onMissing; }
    public void setOnMissing(String onMissing) { this.onMissing = onMissing; }
}
