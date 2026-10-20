package com.scamshield.model;

import java.util.List;

public class ScanResult {
    private final long id;
    private final String content;
    private final int score;
    private final String riskLevel;
    private final List<String> reasons;
    private final String scannedAt;

    public ScanResult(long id, String content, int score,
                      String riskLevel, List<String> reasons,
                      String scannedAt) {
        this.id = id;
        this.content = content;
        this.score = score;
        this.riskLevel = riskLevel;
        this.reasons = reasons;
        this.scannedAt = scannedAt;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public int getScore() {
        return score;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public String getScannedAt() {
        return scannedAt;
    }
}
