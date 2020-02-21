package com.tesis.emails.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailContent {

    private String type;
    private String value;
}
