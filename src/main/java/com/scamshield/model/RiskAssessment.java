package com.scamshield.model;

import java.util.List;

public class RiskAssessment {
    private final int score;
    private final String riskLevel;
    private final List<String> reasons;

    public RiskAssessment(int score, String riskLevel,
                          List<String> reasons) {
        this.score = score;
        this.riskLevel = riskLevel;
        this.reasons = reasons;
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
}
