package com.scamshield.service;

import junit.framework.TestCase;

import com.scamshield.model.RiskAssessment;
import com.scamshield.model.ScanResult;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;

public class ScanHistoryServiceTest extends TestCase {

    private JdbcTemplate jdbc;
    private ScanHistoryService historyService;

    protected void setUp() {
        DriverManagerDataSource dataSource =
            new DriverManagerDataSource();

        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite:scamshield-junit-test.db");

        jdbc = new JdbcTemplate(dataSource);

        jdbc.execute(
            "CREATE TABLE IF NOT EXISTS scans (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "content TEXT NOT NULL, " +
            "score INTEGER NOT NULL, " +
            "risk_level TEXT NOT NULL, " +
            "reasons TEXT NOT NULL, " +
            "scanned_at TEXT NOT NULL DEFAULT (datetime('now'))" +
            ")"
        );

        jdbc.update("DELETE FROM scans");

        historyService = new ScanHistoryService(jdbc);
    }

    public void testScanCanBeSavedAndRetrieved() {
        RiskAssessment assessment = new RiskAssessment(
            25, "LOW",
            java.util.Collections.singletonList(
                "Uses urgent language."
            )
        );

        ScanResult saved =
            historyService.save("Act now", assessment);

        assertNotNull(saved);
        assertTrue(saved.getId() > 0);
        assertEquals("Act now", saved.getContent());
        assertEquals(25, saved.getScore());
        assertEquals("LOW", saved.getRiskLevel());
    }

    public void testHistoryReturnsSavedScans() {
        historyService.save(
            "First message",
            new RiskAssessment(
                25, "LOW",
                java.util.Collections.singletonList("Test reason")
            )
        );

        historyService.save(
            "Second message",
            new RiskAssessment(
                70, "HIGH",
                java.util.Collections.singletonList("Test reason")
            )
        );

        List<ScanResult> history = historyService.findAll();

        assertEquals(2, history.size());
        assertEquals("Second message", history.get(0).getContent());
        assertEquals("First message", history.get(1).getContent());
    }

    public void testAnalyticsCountsMatchSavedScans() {
        historyService.save(
            "Low message",
            new RiskAssessment(
                10, "LOW",
                java.util.Collections.singletonList("Test reason")
            )
        );

        historyService.save(
            "Medium message",
            new RiskAssessment(
                40, "MEDIUM",
                java.util.Collections.singletonList("Test reason")
            )
        );

        historyService.save(
            "High message",
            new RiskAssessment(
                70, "HIGH",
                java.util.Collections.singletonList("Test reason")
            )
        );

        Map<String, Object> analytics =
            historyService.analytics();

        assertEquals(3, ((Number) analytics.get("total")).intValue());
        assertEquals(1, ((Number) analytics.get("high")).intValue());
        assertEquals(1, ((Number) analytics.get("medium")).intValue());
        assertEquals(1, ((Number) analytics.get("low")).intValue());
    }

    public void testEmptyHistoryHasZeroCounts() {
        Map<String, Object> analytics =
            historyService.analytics();

        assertEquals(0, ((Number) analytics.get("total")).intValue());
        assertEquals(0, ((Number) analytics.get("high")).intValue());
        assertEquals(0, ((Number) analytics.get("medium")).intValue());
        assertEquals(0, ((Number) analytics.get("low")).intValue());
    }
}