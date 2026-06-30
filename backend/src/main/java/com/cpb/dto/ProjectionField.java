package com.cpb.dto;

public class ProjectionField {
    private String path;
    private String from;

    public ProjectionField() {}

    public ProjectionField(String path, String from) {
        this.path = path;
        this.from = from;
    }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
}
