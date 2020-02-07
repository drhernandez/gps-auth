package com.tesis.privileges;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PrivilegeRepositoryTest {

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @DisplayName("Privilege repository - getByName() entity not found")
    @Test
    public void getByName1() {
        Privilege privilege = privilegeRepository.getByName("TEST");
        assertNull(privilege);
    }

    @DisplayName("Privilege repository - getByName() ok")
    @Test
    public void getByName2() {

        Privilege mock = Privilege.builder()
                .name("TEST")
                .build();
        privilegeRepository.save(mock);

        Privilege privilege = privilegeRepository.getByName("TEST");

        assertNotNull(privilege);
        assertEquals("TEST", privilege.getName());
    }

    @DisplayName("Privilege repository - getAllByNameIsIn() valid and invalid privileges should return the valid ones")
    @Test
    public void getAllByNameIsIn1() {

        List<Privilege> mocks = Lists.newArrayList(
                Privilege.builder().name("PRIV1").build(),
                Privilege.builder().name("PRIV2").build(),
                Privilege.builder().name("PRIV3").build()
        );

        privilegeRepository.saveAll(mocks);

        List<Privilege> privileges = privilegeRepository.getAllByNameIsIn(Lists.newArrayList("PRIV1", "PRIV2", "INVALID_PRIV"));

        assertNotNull(privileges);
        assertEquals(2, privileges.size());
    }

    @DisplayName("Privilege repository - getAllByNameIsIn() priv not found should not throw exception")
    @Test
    public void deleteByName1() {
        privilegeRepository.deleteByName("TEST");
    }

    @DisplayName("Privilege repository - getAllByNameIsIn() ok")
    @Test
    public void deleteByName2() {

        privilegeRepository.save(
                Privilege.builder().name("PRIV1").build()
        );
        privilegeRepository.deleteByName("PRIV1");

        Privilege privilege = privilegeRepository.getByName("PRIV1");
        assertNull(privilege);
    }
}
