package com.tesis.privileges;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

    Privilege getByName(String name);

    List<Privilege> getAllByNameIsIn(List<String> names);

    void deleteByName(String name);
}
