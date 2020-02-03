package com.tesis.roles;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role getByName(String name);
    boolean existsByName(String name);
}
