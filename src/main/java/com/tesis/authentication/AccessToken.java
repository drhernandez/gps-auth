package com.tesis.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Entity
@Table(name = "ACCESS_TOKENS")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AccessToken implements Serializable {

    @JsonIgnore
    @Id
    Long userId;

    @Column(name = "token", nullable = false, updatable = false, unique = true, length = 3000)
    @NotBlank
    String token;
}
