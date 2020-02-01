package com.tesis.roles;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role getByName(String name);
    List<Role> getAllByNameIsIn(List<String> names);
    boolean existsByName(String name);
}
