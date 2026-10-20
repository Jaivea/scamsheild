package com.scamshield.service;

import junit.framework.TestCase;
import com.scamshield.model.RiskAssessment;

public class RiskAnalysisServiceTest extends TestCase {

    private RiskAnalysisService service;

    @Override
    protected void setUp() {
        service = new RiskAnalysisService();
    }

    public void testEmptyInputRejected() {
        try {
            service.analyze("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertEquals(
                "Please enter a message or URL.",
                exception.getMessage()
            );
        }
    }

    public void testOrdinaryTextHasLowRisk() {
        RiskAssessment result =
            service.analyze("Hello, how are you?");

        assertEquals(0, result.getScore());
        assertEquals("LOW", result.getRiskLevel());
    }

    public void testUrgentLanguageDetected() {
        RiskAssessment result = service.analyze("Act now");

        assertEquals(25, result.getScore());
        assertEquals("LOW", result.getRiskLevel());
        assertTrue(
            result.getReasons().contains("Uses urgent language.")
        );
    }

    public void testSuspiciousTestUrl() {
        RiskAssessment result =
            service.analyze("http://secure-bank-kyc.example.test");

        assertEquals(70, result.getScore());
        assertEquals("HIGH", result.getRiskLevel());
    }

    public void testHighRiskClassification() {
        RiskAssessment result = service.analyze(
            "Urgent! Your account is blocked. " +
            "Verify your KYC at " +
            "http://secure-bank-kyc.example.test"
        );

        assertEquals(95, result.getScore());
        assertEquals("HIGH", result.getRiskLevel());
    }

    public void testScoreNeverExceeds100() {
        RiskAssessment result = service.analyze(
            "Urgent, act now! Verify your password, OTP, " +
            "CVV, PIN and KYC. Your account is blocked. " +
            "Claim reward at http://secure-bank-kyc.example.test"
        );

        assertTrue(result.getScore() >= 0);
        assertTrue(result.getScore() <= 100);
    }
}
