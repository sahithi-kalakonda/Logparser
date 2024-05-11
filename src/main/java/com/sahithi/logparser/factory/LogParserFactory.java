package com.sahithi.logparser.factory;

import com.sahithi.logparser.parsers.*;

public class LogParserFactory {
    public static LogParser getParser(String line) {
        if (line.contains("metric")) {
            return new APMLogParser();
        } else if (line.contains("level")) {
            return new ApplicationLogParser();
        } else if (line.contains("request_method")) {
            return new RequestLogParser();
        }
        return null;
    }

    public static void writeOutputs() {
        APMLogParser.writeOutput();
        ApplicationLogParser.writeOutput();
        RequestLogParser.writeOutput();
    }
}
