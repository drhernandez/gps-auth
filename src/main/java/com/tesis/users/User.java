package com.tesis.users;

import com.tesis.constants.UserStatus;
import com.tesis.models.AuditModel;
import com.tesis.roles.Role;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "USERS")
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String dni;

    private String address;
    private String phone;

    public User merge(UserRequestBody userRequestBody) {
        this.status = userRequestBody.getStatus() != null ? UserStatus.fromName(userRequestBody.getStatus()) : this.status;
        this.password = userRequestBody.getPassword() != null ? userRequestBody.getPassword() : this.password;
        this.email = userRequestBody.getEmail() != null ? userRequestBody.getEmail() : this.email;
        this.name = userRequestBody.getName() != null ? userRequestBody.getName() : this.name;
        this.lastName = userRequestBody.getLastName() != null ? userRequestBody.getLastName() : this.lastName;
        this.dni = userRequestBody.getDni() != null ? userRequestBody.getDni() : this.dni;
        this.address = userRequestBody.getAddress() != null ? userRequestBody.getAddress() : this.address;
        this.phone = userRequestBody.getPhone() != null ? userRequestBody.getPhone() : this.phone;

        return this;
    }
}
