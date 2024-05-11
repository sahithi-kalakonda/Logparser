package com.sahithi.logparser.parsers;

import java.util.*;

public class ApiEndpointStats {
    public List<Integer> responseTimes = new ArrayList<>();
    public Map<String, Integer> statusCodes = new HashMap<>();

    void addResponseTime(int time) {
        responseTimes.add(time);
    }

    void addStatusCode(String code) {
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
