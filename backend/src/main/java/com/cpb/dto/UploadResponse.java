package com.cpb.dto;

public class UploadResponse {

    private boolean success;
    private String message;
    private Long candidateId;
    private CandidateProfileResponse profile;

    public UploadResponse() {}

    public UploadResponse(boolean success, String message, Long candidateId, CandidateProfileResponse profile) {
        this.success = success;
        this.message = message;
        this.candidateId = candidateId;
        this.profile = profile;
    }

    public static UploadResponse error(String message) {
        return new UploadResponse(false, message, null, null);
    }

    public static UploadResponse success(String message, Long candidateId, CandidateProfileResponse profile) {
        return new UploadResponse(true, message, candidateId, profile);
    }

    // --- Getters and Setters ---

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public CandidateProfileResponse getProfile() { return profile; }
    public void setProfile(CandidateProfileResponse profile) { this.profile = profile; }
}
