package com.tesis.emails.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailAddress {

    private String name;
    private String email;
}
