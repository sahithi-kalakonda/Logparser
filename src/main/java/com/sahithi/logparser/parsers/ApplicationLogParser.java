package com.sahithi.logparser.parsers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApplicationLogParser implements LogParser {
    static Map<String, Integer> logCounts = new HashMap<>();

    @Override
    public void parseLine(String line) {
        if (!line.startsWith("timestamp")) return;

        String[] parts = line.split(" ");
        String severity = null;

        for (String part : parts) {
            if (part.startsWith("level=")) {
                severity = part.split("=")[1];
                logCounts.put(severity, logCounts.getOrDefault(severity, 0) + 1);
            }
        }
    }

    public static void writeOutput() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(logCounts);

        try (FileWriter writer = new FileWriter("application.json")) {
            writer.write(json);
        } catch (IOException e) {
            System.out.println("Error writing JSON output: " + e.getMessage());
        }
    }
}
