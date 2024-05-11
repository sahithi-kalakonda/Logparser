package com.sahithi.logparser;

import com.sahithi.logparser.factory.LogParserFactory;
import com.sahithi.logparser.parsers.LogParser;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LogParserApp {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("f", "file", true, "Input log file path.");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("f")) {
                String filePath = cmd.getOptionValue("f");
                processLogFile(filePath);
            } else {
                System.out.println("Please provide a log file path using -f option.");
            }
        } catch (ParseException e) {
            System.out.println("Failed to parse command line properties" + e.getMessage());
        }
    }

    private static void processLogFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LogParser parser = LogParserFactory.getParser(line);
                if (parser != null) {
                    parser.parseLine(line);
                }
            }
            LogParserFactory.writeOutputs();
        } catch (IOException e) {
            System.out.println("Error reading the log file: " + e.getMessage());
        }
    }
}
