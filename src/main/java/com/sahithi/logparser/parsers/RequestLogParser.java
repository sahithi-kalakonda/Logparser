
package com.sahithi.logparser.parsers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RequestLogParser implements LogParser {
    static Map<String, ApiEndpointStats> requestMetrics = new HashMap<>();

    @Override
    public void parseLine(String line) {
        if (!line.startsWith("timestamp")) {
            System.out.println("Skipping malformed line: " + line);
            return;
        }

        String[] parts = line.split(" ");
        String url = null;
        int responseTime = 0;
        String statusCode = null;

        for (String part : parts) {
            if (part.startsWith("request_url=")) {
                url = part.split("=")[1];
            } else if (part.startsWith("response_time_ms=")) {
                responseTime = Integer.parseInt(part.split("=")[1]);
            } else if (part.startsWith("response_status=")) {
                statusCode = part.split("=")[1];
            }
        }

        // Handling empty or null statusCode
        if (statusCode == null || statusCode.isEmpty()) {
            System.out.println("Warning: Line cannot be parsed, code is null or empty. Line: " + line);
            return;
        }

        if (url != null) {
            requestMetrics.putIfAbsent(url, new ApiEndpointStats());
            ApiEndpointStats stats = requestMetrics.get(url);
            stats.addResponseTime(responseTime);
            stats.addStatusCode(statusCode);
        }
    }

    public static void writeOutput() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Object> finalStats = new HashMap<>();
        requestMetrics.forEach((key, value) -> finalStats.put(key, value.getStatistics()));
        String json = gson.toJson(finalStats);

        try (FileWriter writer = new FileWriter("request.json")) {
            writer.write(json);
        } catch (IOException e) {
            System.out.println("Error writing JSON output: " + e.getMessage());
        }
    }

    private static class ApiEndpointStats {
        List<Integer> responseTimes = new ArrayList<>();
        Map<String, Integer> statusCodes = new HashMap<>();

        void addResponseTime(int time) {
            responseTimes.add(time);
        }

void addStatusCode(String code) {
    if (code == null) {
        return;
    }
    String category = code.charAt(0) + "XX";
    statusCodes.put(category, statusCodes.getOrDefault(category, 0) + 1);
}

        Map<String, Object> getStatistics() {
            Collections.sort(responseTimes);
            int size = responseTimes.size();
            Map<String, Object> stats = new HashMap<>();
            Map<String, Object> responseStats = new HashMap<>();
            if (size > 0) {
                responseStats.put("min", responseTimes.get(0));
                responseStats.put("50_percentile", responseTimes.get(size / 2));
                responseStats.put("90_percentile", responseTimes.get((int) (size * 0.90)));
                responseStats.put("95_percentile", responseTimes.get((int) (size * 0.95)));
                responseStats.put("99_percentile", responseTimes.get((int) (size * 0.99)));
                responseStats.put("max", responseTimes.get(size - 1));
            }
            stats.put("response_times", responseStats);
            stats.put("status_codes", statusCodes);
            return stats;
        }
    }
}

