package com.tesis.models;

import com.tesis.constants.UserStatus;
import com.tesis.roles.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "USERS")
@Getter
@Setter
@Builder
public class User extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(length = 1000)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USERS_ROLES",
            joinColumns = { @JoinColumn(name = "user_id") },
            inverseJoinColumns = { @JoinColumn(name = "role_id") })
    private Set<Role> roles;

    private String email;
    private String name;
    private String lastName;
    private String dni;
    private String address;
    private String phone;
}
