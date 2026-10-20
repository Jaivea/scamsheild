package com.scamshield.service;

import junit.framework.TestCase;
import com.scamshield.model.RiskAssessment;

public class WarningSignDetectionTest extends TestCase {

    private RiskAnalysisService service;

    protected void setUp() {
        service = new RiskAnalysisService();
    }

    public void testUrgentLanguageWarning() {
        RiskAssessment result = service.analyze("Please act now");

        assertTrue(result.getReasons().contains(
            "Uses urgent language."
        ));
        assertEquals(25, result.getScore());
    }

    public void testSensitiveInformationWarning() {
        RiskAssessment result =
            service.analyze("Please verify your password");

        assertTrue(result.getReasons().contains(
            "Mentions sensitive information or account verification."
        ));
        assertEquals(25, result.getScore());
    }

    public void testWebLinkWarning() {
        RiskAssessment result =
            service.analyze("Visit https://example.com");

        assertTrue(result.getReasons().contains(
            "Contains a web link."
        ));
    }

    public void testHttpLinkWarning() {
        RiskAssessment result =
            service.analyze("Visit http://example.com");

        assertTrue(result.getReasons().contains(
            "Link uses HTTP rather than HTTPS."
        ));
    }

    public void testReservedTestDomainWarning() {
        RiskAssessment result =
            service.analyze("Visit https://example.test");

        assertTrue(result.getReasons().contains(
            "Link uses a reserved test domain."
        ));
    }

    public void testScamStyleClaimWarning() {
        // Matches the exact phrase checked by the current implementation.
        RiskAssessment result =
            service.analyze("Your account blocked");

        assertTrue(result.getReasons().contains(
            "Contains a common scam-style claim or threat."
        ));
    }

    public void testOrdinaryMessageHasNoConfiguredWarnings() {
        RiskAssessment result =
            service.analyze("Hello, have a nice day");

        assertEquals(0, result.getScore());
        assertEquals("LOW", result.getRiskLevel());
        assertTrue(result.getReasons().get(0).contains(
            "No configured warning signs detected."
        ));
    }
}