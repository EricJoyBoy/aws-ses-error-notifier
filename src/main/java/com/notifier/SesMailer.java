package com.notifier;

import com.notifier.exception.NotificationException;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;


import java.util.Objects;

public final class SesMailer {
    private final SesClient sesClient;
    private static final String CHARSET = "UTF-8";


    SesMailer(SesClient sesClient) {
        this.sesClient = Objects.requireNonNull(sesClient);
    }

    public void sendNotification(ConfigurationValidator config, String htmlContent) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(config.getMailSource())
                    .destination(d -> d.toAddresses(config.getMailDestinations()))
                    .message(Message.builder()
                            .subject(buildContent(config.getMailSubject()))
                            .body(b -> b.html(buildContent(htmlContent)))
                            .build())
                    .build();

            sesClient.sendEmail(request);
        } catch (SesException e) {
            throw new NotificationException("Failed to send email: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    private Content buildContent(String data) {
        return Content.builder()
                .data(data)
                .charset(CHARSET)
                .build();
    }
}