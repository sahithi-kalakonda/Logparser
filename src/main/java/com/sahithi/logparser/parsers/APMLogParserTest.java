
package com.sahithi.logparser.parsers;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class APMLogParserTest {
    private APMLogParser parser;

    @Before
    public void setUp() {
        parser = new APMLogParser();
        APMLogParser.metrics.clear();
    }

    @Test
    public void testParseLineValid() {
        parser.parseLine("timestamp 123 metric=CPU value=75.5");
        parser.parseLine("timestamp 124 metric=CPU value=80.0");
        parser.parseLine("timestamp 125 metric=Memory value=30.2");

        assertTrue("Metrics map should contain CPU key", APMLogParser.metrics.containsKey("CPU"));
        assertTrue("Metrics map should contain Memory key", APMLogParser.metrics.containsKey("Memory"));
        assertEquals("CPU metric should have 2 entries", 2, APMLogParser.metrics.get("CPU").size());
        assertEquals("Memory metric should have 1 entry", 1, APMLogParser.metrics.get("Memory").size());
        System.out.println("testParseLineValid: Passed");
    }

    @Test
    public void testParseLineInvalid() {
        parser.parseLine("This is an invalid line");
        parser.parseLine("timestamp 126 metric value");
        assertTrue("Metrics map should be empty for invalid lines", APMLogParser.metrics.isEmpty());
        System.out.println("testParseLineInvalid: Passed");
    }

    @Test
    public void testCalculateAggregates() {
        parser.parseLine("timestamp 123 metric=CPU value=75.5");
        parser.parseLine("timestamp 124 metric=CPU value=80.0");
        parser.parseLine("timestamp 125 metric=CPU value=65.0");
        parser.parseLine("timestamp 126 metric=Memory value=30.2");
        parser.parseLine("timestamp 127 metric=Memory value=25.8");

        Map<String, Map<String, Double>> aggregates = APMLogParser.calculateAggregates();

        assertEquals("Incorrect minimum CPU value", Double.valueOf(65.0), aggregates.get("CPU").get("minimum"));
        assertEquals("Incorrect median CPU value", Double.valueOf(75.5), aggregates.get("CPU").get("median"));
        assertEquals("Incorrect average CPU value", Double.valueOf((75.5 + 80.0 + 65.0) / 3), aggregates.get("CPU").get("average"));
        assertEquals("Incorrect maximum CPU value", Double.valueOf(80.0), aggregates.get("CPU").get("maximum"));

        assertEquals("Incorrect minimum Memory value", Double.valueOf(25.8), aggregates.get("Memory").get("minimum"));
        assertEquals("Incorrect median Memory value", Double.valueOf(30.2), aggregates.get("Memory").get("median"));
        assertEquals("Incorrect average Memory value", Double.valueOf((30.2 + 25.8) / 2), aggregates.get("Memory").get("average"));
        assertEquals("Incorrect maximum Memory value", Double.valueOf(30.2), aggregates.get("Memory").get("maximum"));

        System.out.println("testCalculateAggregates: Passed");
    }
}
