package com.notifier;


import software.amazon.awssdk.services.ses.SesClient;

import java.util.*;


public class LambdaSesNotifier {

    public static Map<String, Object> handleRequest(Map<String, Object> event) {
        try {
            ConfigurationValidator config = new ConfigurationValidator();
            SesMailer mailer = new SesMailer(SesClient.create());
            List<Map<String, Object>> records = extractRecords(event);

            int processed = 0;
            for (Map<String, Object> record : records) {
                try {
                    String content = EmailComposer.createEmailContent(record);
                    mailer.sendNotification(config, content);
                    processed++;
                } catch (Exception e) {
                    logError("Error processing record: " + record.get("messageId"), e);
                    throw e; // Remove to continue processing
                }
            }
            return successResponse(processed);
        } catch (Exception e) {
            logError("Processing failed", e);
            return errorResponse(e);
        }
    }

    private static List<Map<String, Object>> extractRecords(Map<String, Object> event) {
        Object rawRecords = event.getOrDefault("Records", Collections.emptyList());
        if (rawRecords instanceof List) {
            return (List<Map<String, Object>>) rawRecords;
        }
        return Collections.emptyList();
    }

    private static void logError(String message, Throwable e) {
        System.err.printf("[ERROR] %s: %s%n", message, e.getMessage());
    }

    private static Map<String, Object> successResponse(int processed) {
        return Map.of(
                "status", "success",
                "processed", processed
        );
    }

    private static Map<String, Object> errorResponse(Throwable e) {
        return Map.of(
                "status", "error",
                "message", e.getMessage()
        );
    }
}
