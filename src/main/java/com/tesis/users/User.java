package com.tesis.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tesis.roles.Role;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.tesis.users.UserStatus.DELETED;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "USERS")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @LastModifiedDate
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
            this.setDeletedAt(LocalDateTime.now(ZoneId.systemDefault()));
        }
    }

    public String getName() {
        return name;
    }

}
