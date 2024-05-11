package com.sahithi.logparser.parsers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestLogParserTest {
    private RequestLogParser parser;

    @BeforeEach
    void setUp() {
        parser = new RequestLogParser();
        RequestLogParser.requestMetrics.clear();
    }

    @Test
    void testParseLineWithValidData() {
        parser.parseLine("timestamp 2023-10-15 12:00:00 request_url=api/login response_time_ms=120 response_status=200");
        Map<String, Object> metrics = (Map<String, Object>) RequestLogParser.requestMetrics.get("api/login");
        assertNotNull(metrics, "Metrics for 'api/login' should exist.");
        assertEquals(120, metrics.get("response_time"), "The response time should be 120 ms.");
        assertEquals("200", metrics.get("status_code"), "The status code should be '200'.");
    }

    @Test
    void testParseLineWithInvalidData() {
        parser.parseLine("Some incorrect format");
        assertTrue(RequestLogParser.requestMetrics.isEmpty(), "Metrics should remain empty after parsing invalid data.");
    }

    @Test
    void testWriteOutput(@TempDir Path tempDir) throws Exception {
        parser.parseLine("timestamp 2023-10-15 12:00:00 request_url=api/login response_time_ms=120 response_status=200");
        File tempFile = tempDir.resolve("request.json").toFile();
        RequestLogParser.writeOutput();

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            reader.lines().forEach(content::append);
        }

        assertFalse(content.toString().isEmpty(), "The output JSON should not be empty.");
    }
}
