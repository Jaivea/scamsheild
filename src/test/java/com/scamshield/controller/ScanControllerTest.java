
package com.scamshield.controller;

import junit.framework.TestCase;

import com.scamshield.model.RiskAssessment;
import com.scamshield.model.ScanRequest;
import com.scamshield.model.ScanResult;
import com.scamshield.service.RiskAnalysisService;
import com.scamshield.service.ScanHistoryService;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

public class ScanControllerTest extends TestCase {

    private ScanController controller;
    private RiskAnalysisService riskService;

    protected void setUp() {
        riskService = new RiskAnalysisService();

        // A test history service that does not access the database.
        ScanHistoryService fakeHistoryService =
            new ScanHistoryService((JdbcTemplate) null) {

                public ScanResult save(
                        String content,
                        RiskAssessment assessment) {

                    return new ScanResult(
                        1L,
                        content,
                        assessment.getScore(),
                        assessment.getRiskLevel(),
                        assessment.getReasons(),
                        "2026-10-20 10:00:00"
                    );
                }
            };

        controller = new ScanController(
            riskService,
            fakeHistoryService
        );
    }

    public void testEmptyInputRejected() {
        ScanRequest request = new ScanRequest();
        request.setContent("   ");

        try {
            controller.scan(request);
            fail("Expected ResponseStatusException");
        } catch (ResponseStatusException exception) {
            assertEquals(400, exception.getStatusCode().value());
        }
    }

    public void testNullRequestRejected() {
        try {
            controller.scan(null);
            fail("Expected ResponseStatusException");
        } catch (ResponseStatusException exception) {
            assertEquals(400, exception.getStatusCode().value());
        }
    }

    public void testInputOver5000CharactersRejected() {
        ScanRequest request = new ScanRequest();
        StringBuilder content = new StringBuilder();

        for (int i = 0; i < 5001; i++) {
            content.append('a');
        }

        request.setContent(content.toString());

        try {
            controller.scan(request);
            fail("Expected ResponseStatusException");
        } catch (ResponseStatusException exception) {
            assertEquals(400, exception.getStatusCode().value());
        }
    }

    public void testValidRequestReturnsScanResult() {
        ScanRequest request = new ScanRequest();
        request.setContent("Act now");

        ScanResult result = controller.scan(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Act now", result.getContent());
        assertEquals(25, result.getScore());
        assertEquals("LOW", result.getRiskLevel());
        assertTrue(result.getReasons().contains(
            "Uses urgent language."
        ));
    }
}
