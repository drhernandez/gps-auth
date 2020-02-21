package com.tesis.emails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesis.emails.models.EmailModel;
import com.tesis.emails.models.ErrorResponse;
import com.tesis.exceptions.InternalServerErrorException;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import kong.unirest.UnirestInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.tesis.config.RestClientConfigs.SLOW;

@Slf4j
@Component
public class SendGridClient {

    private final UnirestInstance client;
    private final String apiSecretKey;
    private final String baseUrl;
    private ObjectMapper objectMapper;

    @Autowired
    public SendGridClient(@Qualifier(SLOW) UnirestInstance client, ObjectMapper objectMapper, @Value("${email.secret-key}") String apiSecretKey, @Value("${email.sendgrid-base-url}") String baseUrl) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.apiSecretKey = apiSecretKey;
        this.baseUrl = baseUrl;
    }

    public void sendMail(EmailModel emailModel) {

        try {

            HttpResponse response = client
                    .post(String.format("%s%s", baseUrl, "/mail/send"))
                    .header("Authorization", "Bearer " + apiSecretKey)
                    .header("Content-type", "application/json")
                    .body(objectMapper.writeValueAsString(emailModel))
                    .asEmpty();

            if (!response.isSuccess()) {
                ErrorResponse errorResponse = (ErrorResponse) response.mapError(ErrorResponse.class);
                logger.error("[message: Invalid response sending mail] [error: {}]", objectMapper.writeValueAsString(errorResponse));
                throw new InternalServerErrorException("internal error");
            }

        } catch (UnirestException e) {
            logger.error("[message: Connection error sending mail] [error: {}] [stacktrace: {}]", e, e.getStackTrace());
            throw new InternalServerErrorException("internal error");
        } catch (IOException e) {
            logger.error("[message: Could not parse mail to string] [error: {}] [stacktrace: {}]", e, e.getStackTrace());
            throw new InternalServerErrorException("internal error");
        }
    }
}