package com.tesis.emails.templates;

import com.google.common.collect.Lists;
import com.tesis.emails.models.EmailModel;
import com.tesis.emails.models.MailAddress;
import com.tesis.emails.models.Personalization;
import lombok.Builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public class WelcomeEmailTemplate implements EmailTemplate {

    private final String TEMPLATE_ID = "d-5bba1686a1d54b58b15a00888dc18362";

    private String senderMail;
    private String userName;
    private List<String> receivers;

    @Override
    public EmailModel get() {

        List<MailAddress> to = receivers.stream()
                .map(receiver -> MailAddress.builder().email(receiver).build())
                .collect(Collectors.toList());

        MailAddress from = MailAddress.builder()
                .email(senderMail)
                .build();

        Map<String, Object> dynamicTemplateData = new HashMap<>();
        dynamicTemplateData.put("user_name", userName);

        return EmailModel.builder()
                .personalizations(
                        Lists.newArrayList(
                                Personalization.builder()
                                        .to(to)
                                        .dynamicTemplateData(dynamicTemplateData)
                                        .build()
                        )
                )
                .from(from)
                .templateId(TEMPLATE_ID)
                .build();
    }
}
