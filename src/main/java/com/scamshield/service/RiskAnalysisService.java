package com.scamshield.service;

import com.scamshield.model.RiskAssessment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RiskAnalysisService {

    private static final Pattern URL_PATTERN =
        Pattern.compile("(?i)\\bhttps?://[^\\s<>\\\"']+");

    public RiskAssessment analyze(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Please enter a message or URL.");
        }

        String text = input.trim();
        String lower = text.toLowerCase(Locale.ROOT);

        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (containsAny(lower, "urgent", "immediately",
                "act now", "today")) {
            score += 25;
            reasons.add("Uses urgent language.");
        }

        if (containsAny(lower, "password", "otp", "cvv",
                "pin", "kyc", "bank details")) {
            score += 25;
            reasons.add(
                "Mentions sensitive information or account verification.");
        }

        Matcher matcher = URL_PATTERN.matcher(text);

        if (matcher.find()) {
            score += 20;
            reasons.add("Contains a web link.");
        }

        if (containsAny(lower, "account blocked",
                "account suspended", "prize", "claim reward")) {
            score += 15;
            reasons.add("Contains a common scam-style claim or threat.");
        }

        matcher.reset();

        while (matcher.find()) {
            String url = matcher.group();

            if (url.toLowerCase(Locale.ROOT).startsWith("http://")) {
                score += 10;
                reasons.add("Link uses HTTP rather than HTTPS.");
            }

            if (url.toLowerCase(Locale.ROOT).contains(".test")) {
                score += 15;
                reasons.add("Link uses a reserved test domain.");
            }
        }

        score = Math.min(score, 100);

        String riskLevel;
        if (score >= 60) {
            riskLevel = "HIGH";
        } else if (score >= 30) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        if (reasons.isEmpty()) {
            reasons.add(
                "No configured warning signs detected. This does not prove the message is safe.");
        }

        return new RiskAssessment(score, riskLevel, reasons);
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
