//package com.sahithi.logparser.parsers;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class APMLogParser implements LogParser {
//    static Map<String, List<Double>> metrics = new HashMap<>();
//
//
//    @Override
//    public void parseLine(String line) {
//        if (!line.startsWith("timestamp")) return;
//
//        String[] parts = line.split(" ");
//        String metricKey = null;
//        double value = 0;
//
//        for (String part : parts) {
//            if (part.startsWith("metric=")) {
//                metricKey = part.split("=")[1];
//            } else if (part.startsWith("value=")) {
//                value = Double.parseDouble(part.split("=")[1]);
//            }
//        }
//
//        if (metricKey != null) {
//            metrics.putIfAbsent(metricKey, new ArrayList<>());
//            metrics.get(metricKey).add(value);
//        }
//    }
//    public static void writeOutput() {
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        String json = gson.toJson(calculateAggregates());
//
//        try (FileWriter writer = new FileWriter("apm.json")) {
//            writer.write(json);
//        } catch (IOException e) {
//            System.out.println("Error writing JSON output: " + e.getMessage());
//        }
//    }
//    public static Map<String, Map<String, Double>> calculateAggregates() {
//        Map<String, Map<String, Double>> aggregates = new HashMap<>();
//        for (Map.Entry<String, List<Double>> entry : metrics.entrySet()) {
//            Map<String, Double> summary = new HashMap<>();
//            List<Double> values = entry.getValue();
//            values.sort(Double::compare);
//
//            summary.put("minimum", values.stream().min(Double::compare).orElse(0.0));
//
//            double median = 0.0;
//            if (!values.isEmpty()) {
//                int middle = values.size() / 2;
//                if (values.size() % 2 == 1) { // Odd number of elements
//                    median = values.get(middle);
//                } else {
//                    median = (values.get(middle - 1) + values.get(middle)) / 2.0;
//                }
//            }
//            summary.put("median", median);
//            summary.put("average", values.stream().mapToDouble(d -> d).average().orElse(0.0));
//            summary.put("maximum", values.stream().max(Double::compare).orElse(0.0));
//            aggregates.put(entry.getKey(), summary);
//        }
//        return aggregates;
//    }
//
//}

package com.sahithi.logparser.parsers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APMLogParser implements LogParser {
    static Map<String, List<Double>> metrics = new HashMap<>();

    @Override
    public void parseLine(String line) {
        if (!line.startsWith("timestamp")) return;

        String[] parts = line.split(" ");


        if (parts.length < 3) {
            System.out.println("Skipping corrupt line: " + line);
            return;
        }

        String metricKey = null;
        Double value = null;

        for (String part : parts) {
            if (part.startsWith("metric=")) {
                metricKey = part.split("=")[1];
            } else if (part.startsWith("value=")) {
                String valueStr = part.split("=")[1];
                try {
                    value = Double.parseDouble(valueStr);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping line due to invalid value: " + line);
                    return; // Skip this line
                }
            }
        }


        if (metricKey != null && value != null) {
            metrics.putIfAbsent(metricKey, new ArrayList<>());
            metrics.get(metricKey).add(value);
        } else {
            System.out.println("Skipping line due to missing metric key or value: " + line);
        }
    }

    public static void writeOutput() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(calculateAggregates());

        try (FileWriter writer = new FileWriter("apm.json")) {
            writer.write(json);
        } catch (IOException e) {
            System.out.println("Error writing JSON output: " + e.getMessage());
        }
    }

    public static Map<String, Map<String, Double>> calculateAggregates() {
        Map<String, Map<String, Double>> aggregates = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : metrics.entrySet()) {
            Map<String, Double> summary = new HashMap<>();
            List<Double> values = entry.getValue();
            values.sort(Double::compare);

            summary.put("minimum", values.stream().min(Double::compare).orElse(0.0));

            double median = 0.0;
            if (!values.isEmpty()) {
                int middle = values.size() / 2;
                if (values.size() % 2 == 1) {
                    median = values.get(middle);
                } else {
                    median = (values.get(middle - 1) + values.get(middle)) / 2.0;
                }
            }
            summary.put("median", median);
            summary.put("average", values.stream().mapToDouble(d -> d).average().orElse(0.0));
            summary.put("maximum", values.stream().max(Double::compare).orElse(0.0));
            aggregates.put(entry.getKey(), summary);
        }
        return aggregates;
    }
}
