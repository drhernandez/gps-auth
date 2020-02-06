package com.tesis.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Entity
@Table(name = "ACCESS_TOKENS")
@Getter
@Setter
@Builder
public class AccessToken implements Serializable {

    @Id
    Long userId;

    @Column(name = "token", nullable = false, updatable = false, length = 1000)
    @NotBlank
    String token;
}