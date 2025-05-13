package com.notifier;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ses.SesClient;

import java.util.*;

public class LambdaSesNotifier implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger =  LoggerFactory.getLogger(LambdaSesNotifier.class);
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
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
                    logger.error("Error processing record: " + record.get("messageId"), e);
                }
            }
            return successResponse(processed);
        } catch (Exception e) {
            logger.error("Processing failed", e);
            return errorResponse(e);
        }
    }

    private static List<Map<String, Object>> extractRecords(Map<String, Object> event) {
        Object rawRecords = event.get("Records");
        if (!(rawRecords instanceof List<?> rawList)) {
            return Collections.emptyList();
        }

        return rawList.stream()
                .filter(record -> record instanceof Map<?, ?>)
                .map(record -> {
                    Map<?, ?> rawMap = (Map<?, ?>) record;
                    Map<String, Object> normalizedRecord = new HashMap<>();

                    String messageId = safeCastToString(rawMap.get("messageId"));
                    String body = safeCastToString(rawMap.get("body"));
                    Map<String, Object> parsedBody = parseBodyOrFallback(body);

                    normalizedRecord.put("messageId", messageId);
                    normalizedRecord.put("body", parsedBody);
                    normalizedRecord.put("messageAttributes", rawMap.get("messageAttributes"));
                    normalizedRecord.put("attributes", rawMap.get("attributes"));
                    normalizedRecord.put("eventSource", rawMap.get("eventSource"));
                    normalizedRecord.put("awsRegion", rawMap.get("awsRegion"));

                    return normalizedRecord;
                })
                .toList();
    }

    private static String safeCastToString(Object obj) {
        return obj instanceof String str ? str : null;
    }

    private static Map<String, Object> parseBodyOrFallback(String body) {
        if (body == null) return Map.of();
        try {
            return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.warn("Failed to parse body as JSON: {}", e.getMessage());
            return Map.of("rawBody", body);
        }
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
