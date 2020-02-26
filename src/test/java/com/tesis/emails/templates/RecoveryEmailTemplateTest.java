package com.tesis.emails.templates;

import com.google.common.collect.Lists;
import com.tesis.emails.models.EmailModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
public class RecoveryEmailTemplateTest {

    @Test
    public void generateEmailModel() {

        EmailTemplate template = RecoveryEmailTemplate.builder()
                .receivers(
                        Lists.newArrayList(
                                "receiver@test.com"
                        )
                )
                .senderMail("sender@test.com")
                .recoveryLink("este es un recovery link")
                .build();

        EmailModel model = template.get();

        assertNotNull(model);
        assertEquals("receiver@test.com", model.getPersonalizations().get(0).getTo().get(0).getEmail());
        assertEquals("sender@test.com", model.getFrom().getEmail());
        assertEquals("d-7ddf984beed04648a85a3afa3b8128b8", model.getTemplateId());
        assertEquals("este es un recovery link", model.getPersonalizations().get(0).getDynamicTemplateData().get("recovery_link"));
    }
}
