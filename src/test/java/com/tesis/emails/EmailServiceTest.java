package com.tesis.emails;

import com.google.common.collect.Lists;
import com.tesis.exceptions.InternalServerErrorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(SpringExtension.class)
public class EmailServiceTest {

    @Mock
    private SendGridClient mailClient;
    @InjectMocks
    private DefaultEmailService emailService;

    @DisplayName("Email service - sendRecoveryPasswordEmail() client error")
    @Test
    public void recoveryEmail1() {

        doThrow(InternalServerErrorException.class).when(mailClient).sendMail(any());
        assertThrows(InternalServerErrorException.class, () -> emailService.sendRecoveryPasswordEmail(Lists.newArrayList(), "token"));
    }

    @DisplayName("Email service - sendWelcomePasswordEmail() client error")
    @Test
    public void sendWelcomePasswordEmail1() {

        doThrow(InternalServerErrorException.class).when(mailClient).sendMail(any());
        assertThrows(InternalServerErrorException.class, () -> emailService.sendWelcomePasswordEmail(Lists.newArrayList(), "Pedro", "token"));
    }

}
