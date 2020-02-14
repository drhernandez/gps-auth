package com.tesis.privileges;

import com.tesis.exceptions.NotFoundException;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class PrivilegeServiceTest {

    @Mock
    private PrivilegeRepository privilegeRepository;

    @InjectMocks
    private DefaultPrivilegeService privilegeService;

    @DisplayName("Privilege service - getById() entity not found")
    @Test
    public void getById1() {

        when(privilegeRepository.getOne(1L)).thenThrow(EntityNotFoundException.class);
        assertThrows(EntityNotFoundException.class, () -> privilegeService.getById(1L));
    }

    @DisplayName("Privilege service - getById() entity found")
    @Test
    public void getById2() {

        when(privilegeRepository.getOne(1L)).thenReturn(Privilege.builder().id(1L).name("TEST").build());
        Privilege privilege = privilegeService.getById(1L);
        assertEquals("TEST", privilege.getName());
    }

    @DisplayName("Privilege service - getByName() entity not found")
    @Test
    public void getByName1() {

        when(privilegeRepository.getByName("TEST")).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> privilegeService.getByName("TEST"));
    }

    @DisplayName("Privilege service - getByName() entity found")
    @Test
    public void getByName2() {

        when(privilegeRepository.getByName("TEST")).thenReturn(Privilege.builder().id(1L).name("TEST").build());
        Privilege privilege = privilegeService.getByName("TEST");
        assertEquals(1, privilege.getId());
        assertEquals("TEST", privilege.getName());
    }

    @DisplayName("Privilege service - getAllByName() empty list")
    @Test
    public void getAllByName1() {

        when(privilegeRepository.getAllByNameIsIn(Lists.newArrayList("TEST", "TEST2"))).thenReturn(Lists.emptyList());
        List<Privilege> privileges = privilegeService.getAllByNames(Lists.newArrayList("TEST", "TEST2"));
        assertNotNull(privileges);
        assertEquals(0, privileges.size());
    }

    @DisplayName("Privilege service - getAllByName() ok")
    @Test
    public void getAllByName2() {

        when(privilegeRepository.getAllByNameIsIn(Lists.newArrayList("TEST", "TEST2"))).thenReturn(Lists.newArrayList(
                Privilege.builder().id(1L).name("TEST").build(),
                Privilege.builder().id(2L).name("TEST2").build()
        ));
        List<Privilege> privileges = privilegeService.getAllByNames(Lists.newArrayList("TEST", "TEST2"));
        assertEquals(2, privileges.size());
    }

    @DisplayName("Privilege service - save() ok")
    @Test
    public void save1() {

        Privilege mock = Privilege.builder().id(1L).name("TEST").build();
        when(privilegeRepository.save(any(Privilege.class))).thenReturn(mock);
        Privilege privilege = privilegeService.save(Privilege.builder().name("TEST").build());
        assertNotNull(privilege);
        assertEquals("TEST", privilege.getName());
        assertEquals(1L, privilege.getId());
    }

    @DisplayName("Privilege service - delete() ok")
    @Test
    public void delete1() {

        doNothing().when(privilegeRepository).deleteByName("TEST");
        privilegeService.delete("TEST");
    }
}
