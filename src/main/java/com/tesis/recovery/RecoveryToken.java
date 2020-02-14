package com.tesis.recovery;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "RECOVERY_TOKENS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecoveryToken implements Serializable {

    @JsonIgnore
    @Id
    private Long userId;

    @Column(length = 1000, nullable = false, unique = true)
    private String token;
}
