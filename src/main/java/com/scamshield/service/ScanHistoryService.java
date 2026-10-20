package com.scamshield.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.model.RiskAssessment;
import com.scamshield.model.ScanResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ScanHistoryService {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public ScanHistoryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ScanResult save(String content, RiskAssessment assessment) {
        try {
            String reasonsJson =
                    mapper.writeValueAsString(assessment.getReasons());

            jdbc.update(
                    "INSERT INTO scans(content, score, risk_level, reasons) VALUES (?, ?, ?, ?)",
                    content,
                    assessment.getScore(),
                    assessment.getRiskLevel(),
                    reasonsJson
            );

            Long id = jdbc.queryForObject(
                    "SELECT last_insert_rowid()", Long.class
            );

            return findById(id);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not save scan history.", e
            );
        }
    }

    public ScanResult findById(long id) {
        try {
            return jdbc.queryForObject(
                    "SELECT id, content, score, risk_level, reasons, scanned_at FROM scans WHERE id = ?",
                    (rs, rowNum) -> {
                        List<String> reasons = parseReasons(rs.getString("reasons"));
                        
                        return new ScanResult(
                                rs.getLong("id"),
                                rs.getString("content"),
                                rs.getInt("score"),
                                rs.getString("risk_level"),
                                reasons,
                                rs.getString("scanned_at")
                        );
                    },
                    id
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not retrieve scan.", e
            );
        }
    }

    public List<ScanResult> findAll() {
        try {
            return jdbc.query(
                    "SELECT id, content, score, risk_level, reasons, scanned_at FROM scans ORDER BY id DESC LIMIT 100",
                    (rs, rowNum) -> {
                        List<String> reasons = parseReasons(rs.getString("reasons"));

                        return new ScanResult(
                                rs.getLong("id"),
                                rs.getString("content"),
                                rs.getInt("score"),
                                rs.getString("risk_level"),
                                reasons,
                                rs.getString("scanned_at")
                        );
                    }
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not retrieve scan history.", e
            );
        }
    }

    public Map<String, Object> analytics() {
        return jdbc.queryForMap(
                "SELECT COUNT(*) AS total, " +
                "COALESCE(SUM(CASE WHEN risk_level='HIGH' THEN 1 ELSE 0 END),0) AS high, " +
                "COALESCE(SUM(CASE WHEN risk_level='MEDIUM' THEN 1 ELSE 0 END),0) AS medium, " +
                "COALESCE(SUM(CASE WHEN risk_level='LOW' THEN 1 ELSE 0 END),0) AS low " +
                "FROM scans"
        );
    }
    
    private List<String> parseReasons(String json) {
        try {
            return mapper.readValue(
                json,
                new TypeReference<List<String>>() {}
            );
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(
                "Could not parse scan reasons.", e
            );
        }
    }
}
