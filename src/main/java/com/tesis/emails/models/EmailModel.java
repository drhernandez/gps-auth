package com.tesis.emails.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailModel {

    private List<Personalization> personalizations;
    private MailAddress from;
    private MailAddress replyTo;
    private String subject;
    private List<MailContent> content;
    private String templateId;
}