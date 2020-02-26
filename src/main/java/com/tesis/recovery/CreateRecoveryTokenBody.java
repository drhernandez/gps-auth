package com.tesis.recovery;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecoveryTokenBody {

    private String email;
}
