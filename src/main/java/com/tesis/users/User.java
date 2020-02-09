package com.tesis.users;

import com.tesis.models.AuditModel;
import com.tesis.roles.Role;
import lombok.*;

import javax.persistence.*;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.tesis.users.UserStatus.DELETED;

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

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

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

    public void setStatus(UserStatus status) {
        this.status = status;

        if (DELETED.equals(status)) {
            this.setDeletedAt(LocalDateTime.now(Clock.systemUTC()));
        }
    }
}
