package com.sahithi.logparser.parsers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationLogParserTest {
    private ApplicationLogParser parser;

    @BeforeEach
    void setUp() {
        parser = new ApplicationLogParser();
        ApplicationLogParser.logCounts.clear();
    }

    @Test
    void testParseLineWithCorrectFormat() {
        parser.parseLine("timestamp 2023-10-15 12:00:00 level=INFO Some message");
        assertEquals(1, ApplicationLogParser.logCounts.getOrDefault("INFO", 0));
    }

    @Test
    void testParseLineWithIncorrectFormat() {
        parser.parseLine("Some random text");
        assertTrue(ApplicationLogParser.logCounts.isEmpty());
    }
    @Test
    void testWriteOutput(@TempDir Path tempDir) throws Exception {
        parser.parseLine("timestamp 2023-10-15 12:00:00 level=INFO Some message");
        parser.parseLine("timestamp 2023-10-15 12:00:01 level=ERROR Another message");

        File tempFile = tempDir.resolve("application.json").toFile();
        try (FileWriter writer = new FileWriter(tempFile)) {
            ApplicationLogParser.writeOutput();
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            reader.lines().forEach(content::append);
        }

        String expectedJson = "{\n  \"INFO\": 1,\n  \"ERROR\": 1\n}";
        assertEquals(expectedJson, content.toString());
    }
}
