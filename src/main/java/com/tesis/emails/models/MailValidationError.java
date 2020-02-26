package com.tesis.emails.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailValidationError {

    private String message;
    private String field;
}
