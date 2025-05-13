package com.notifier;

import com.notifier.exception.ConfigurationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class ConfigurationValidator {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private final String mailSource;
    private final String mailSubject;
    private final List<String> mailDestinations;

    public ConfigurationValidator() {
        this.mailSource = validateAndGetSource();
        this.mailSubject = validateAndGetSubject();
        this.mailDestinations = validateAndGetDestinations();
    }

    private String validateAndGetSource() {
        return getRequiredEnv("MAIL_SOURCE");
    }

    private String validateAndGetSubject() {
        return getRequiredEnv("MAIL_SUBJECT");
    }

    private List<String> validateAndGetDestinations() {
        String rawDestinations = getRequiredEnv("MAIL_DESTINATIONS");
        List<String> destinations = parseDestinations(rawDestinations);
        validateEmailList(destinations);
        return Collections.unmodifiableList(destinations);
    }

    private String getRequiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Missing required environment variable: " + name);
        }
        return value.trim();
    }

    private List<String> parseDestinations(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private void validateEmailList(List<String> emails) {
        if (emails.isEmpty()) {
            throw new ConfigurationException("MAIL_DESTINATIONS must contain at least one valid email address");
        }

        emails.forEach(email -> {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new ConfigurationException("Invalid email format: " + email);
            }
        });
    }

    // Getters
    public String getMailSource() {
        return mailSource;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public List<String> getMailDestinations() {
        return mailDestinations;
    }

    // Custom Exception

}