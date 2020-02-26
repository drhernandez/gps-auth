package com.tesis.emails.models;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Personalization {

    private List<MailAddress> to;
    private List<MailAddress> cc;
    private List<MailAddress> bcc;
    private String subject;
    private Map<String, Object> dynamicTemplateData;
}
