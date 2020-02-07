package com.tesis.roles;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @DisplayName("Role repository - getByName() entity not found")
    @Test
    public void getByName1() {
        Role role = roleRepository.getByName("CLIENT");
        assertNull(role);
    }

    @DisplayName("Role repository - getByName() ok")
    @Test
    public void getByName2() {

        Role mock = Role.builder().name("CLIENT").build();
        roleRepository.save(mock);

        Role role = roleRepository.getByName("CLIENT");
        assertNotNull(role);
        assertNotNull(role.getId());
    }

    @DisplayName("Role repository - getAllByNameIsIn() valid and invalid privileges should return the valid ones")
    @Test
    public void getAllByNameIsIn1() {

        List<Role> mocks = Lists.newArrayList(
                Role.builder().name("CLIENT").build(),
                Role.builder().name("ADMIN").build()
        );
        roleRepository.saveAll(mocks);

        List<Role> roles = roleRepository.getAllByNameIsIn(Lists.newArrayList("CLIENT", "INVALID_ROLE"));
        assertNotNull(roles);
        assertEquals(1, roles.size());
    }

    @DisplayName("Role repository - existsByName() ok")
    @Test
    public void existsByName1() {

        Role mock = Role.builder().name("CLIENT").build();
        roleRepository.save(mock);

        assertTrue(roleRepository.existsByName("CLIENT"));
        assertFalse(roleRepository.existsByName("INVALID_ROLE"));
    }
}
