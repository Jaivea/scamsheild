package com.scamshield.service;

import com.scamshield.model.RiskAssessment;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RiskAnalysisServiceTest {

    private RiskAnalysisService service;

    @Before
    public void setUp() {
        service = new RiskAnalysisService();
    }

    @Test
    public void emptyInputShouldBeRejected() {
        try {
            service.analyze(" ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(
                "Please enter a message or URL.",
                e.getMessage()
            );
        }
    }

    @Test
    public void ordinaryMessageShouldHaveLowRisk() {
        RiskAssessment result =
            service.analyze("Hello, how are you?");

        assertEquals("LOW", result.getRiskLevel());
        assertEquals(0, result.getScore());
    }

    @Test
    public void urgentLanguageShouldIncreaseRisk() {
        RiskAssessment result =
            service.analyze("URGENT! Please act now.");

        assertEquals(25, result.getScore());
        assertEquals("LOW", result.getRiskLevel());
    }

    @Test
    public void suspiciousTestLinkShouldHaveHighRisk() {
        RiskAssessment result = service.analyze(
            "URGENT! Your bank account is blocked. " +
            "Verify your KYC immediately at " +
            "http://secure-bank-kyc.example.test"
        );

        assertEquals(95, result.getScore());
        assertEquals("HIGH", result.getRiskLevel());
    }

    @Test
    public void scoreShouldNotExceedOneHundred() {
        RiskAssessment result = service.analyze(
            "URGENT! Act now. Your account is blocked. " +
            "Verify your KYC and password at " +
            "http://secure-bank-kyc.example.test"
        );

        assertTrue(result.getScore() <= 100);
        assertTrue(result.getScore() >= 0);
    }

    @Test
    public void resultShouldContainWarningReasons() {
        RiskAssessment result =
            service.analyze("Verify your KYC immediately.");

        assertFalse(result.getReasons().isEmpty());
    }
}
