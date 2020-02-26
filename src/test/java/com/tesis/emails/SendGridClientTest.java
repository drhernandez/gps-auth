package com.tesis.emails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesis.emails.models.EmailModel;
import com.tesis.emails.models.ErrorResponse;
import com.tesis.exceptions.InternalServerErrorException;
import kong.unirest.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class SendGridClientTest {

    @Mock
    private UnirestInstance instance;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private SendGridClient client;



    @DisplayName("SendGrid client - sendMail() connection timeout")
    @Test
    public void sendMail1() throws JsonProcessingException {

        HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
        RequestBodyEntity requestBodyEntity = mock(RequestBodyEntity.class);

        when(instance.post(anyString())).thenReturn(httpRequestWithBody);
        when(httpRequestWithBody.header(anyString(), anyString())).thenReturn(httpRequestWithBody);
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(httpRequestWithBody.body(anyString())).thenReturn(requestBodyEntity);
        when(requestBodyEntity.asEmpty()).thenThrow(UnirestException.class);

        assertThrows(InternalServerErrorException.class, () -> client.sendMail(EmailModel.builder().build()));
    }

    @DisplayName("SendGrid client - sendMail() json unmarshall error")
    @Test
    public void sendMail2() throws JsonProcessingException {

        HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);

        when(instance.post(anyString())).thenReturn(httpRequestWithBody);
        when(httpRequestWithBody.header(anyString(), anyString())).thenReturn(httpRequestWithBody);
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        assertThrows(InternalServerErrorException.class, () -> client.sendMail(EmailModel.builder().build()));
    }

    @DisplayName("SendGrid client - sendMail() invalid response")
    @Test
    public void sendMail3() throws JsonProcessingException {

        HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
        RequestBodyEntity requestBodyEntity = mock(RequestBodyEntity.class);
        HttpResponse httpResponse = mock(HttpResponse.class);

        when(instance.post(anyString())).thenReturn(httpRequestWithBody);
        when(httpRequestWithBody.header(anyString(), anyString())).thenReturn(httpRequestWithBody);
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(httpRequestWithBody.body(anyString())).thenReturn(requestBodyEntity);
        when(requestBodyEntity.asEmpty()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(403);
        when(httpResponse.mapError(ErrorResponse.class)).thenReturn(ErrorResponse.builder().build());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> client.sendMail(EmailModel.builder().build()));
        assertNotNull(exception);
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @DisplayName("SendGrid client - sendMail() ok")
    @Test
    public void sendMail4() throws JsonProcessingException {

        HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
        RequestBodyEntity requestBodyEntity = mock(RequestBodyEntity.class);
        HttpResponse httpResponse = mock(HttpResponse.class);

        when(instance.post(anyString())).thenReturn(httpRequestWithBody);
        when(httpRequestWithBody.header(anyString(), anyString())).thenReturn(httpRequestWithBody);
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(httpRequestWithBody.body(anyString())).thenReturn(requestBodyEntity);
        when(requestBodyEntity.asEmpty()).thenReturn(httpResponse);
        when(httpResponse.isSuccess()).thenReturn(true);

        assertDoesNotThrow(() -> client.sendMail(EmailModel.builder().build()));
    }
}
