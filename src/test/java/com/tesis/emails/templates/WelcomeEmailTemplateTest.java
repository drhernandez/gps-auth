package com.tesis.emails.templates;

import com.google.common.collect.Lists;
import com.tesis.emails.models.EmailModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class WelcomeEmailTemplateTest {

    @Test
    public void generateEmailModel() {

        EmailTemplate template = WelcomeEmailTemplate.builder()
                .receivers(
                        Lists.newArrayList(
                                "receiver@test.com"
                        )
                )
                .senderMail("sender@test.com")
                .userName("un user name")
                .build();

        EmailModel model = template.get();

        assertNotNull(model);
        assertEquals("receiver@test.com", model.getPersonalizations().get(0).getTo().get(0).getEmail());
        assertEquals("sender@test.com", model.getFrom().getEmail());
        assertEquals("d-5bba1686a1d54b58b15a00888dc18362", model.getTemplateId());
        assertEquals("un user name", model.getPersonalizations().get(0).getDynamicTemplateData().get("user_name"));
    }
}
