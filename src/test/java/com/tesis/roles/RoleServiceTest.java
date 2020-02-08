package com.tesis.roles;

import com.google.common.collect.Sets;
import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.NotFoundException;
import com.tesis.privileges.Privilege;
import com.tesis.privileges.PrivilegeService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PrivilegeService privilegeService;
    @InjectMocks
    private RoleServiceImp roleService;

    @DisplayName("Role service - getByName() entity not found")
    @Test
    public void getByName1() {
        when(roleRepository.getByName("TEST")).thenReturn(null);
        assertThrows(NotFoundException.class, () -> roleService.getByName("TEST"));
    }

    @DisplayName("Role service - getByName() entity found")
    @Test
    public void getByName2() {
        Role mock = Role.builder().id(1L).name("CLIENT").build();
        when(roleRepository.getByName("CLIENT")).thenReturn(mock);

        Role role = roleService.getByName("CLIENT");
        assertNotNull(role);
        assertEquals("CLIENT", role.getName());
    }

    @DisplayName("Role service - getAllByName() partial match")
    @Test
    public void getAllByName1() {

        Role mock1 = Role.builder().id(1L).name("CLIENT").build();
        Role mock2 = Role.builder().id(2L).name("ADMIN").build();

        when(roleRepository.getAllByNameIsIn(Lists.newArrayList("ADMIN", "CLIENT", "TEST"))).thenReturn(Lists.newArrayList(mock1, mock2));

        List<Role> roles = roleService.getAllByName(Lists.newArrayList("ADMIN", "CLIENT", "TEST"));
        assertNotNull(roles);
        assertEquals(2, roles.size());
    }

    @DisplayName("Role service - getAllByName() full match")
    @Test
    public void getAllByName2() {

        Role mock1 = Role.builder().id(1L).name("CLIENT").build();
        Role mock2 = Role.builder().id(2L).name("ADMIN").build();
        Role mock3 = Role.builder().id(2L).name("TEST").build();

        when(roleRepository.getAllByNameIsIn(Lists.newArrayList("ADMIN", "CLIENT", "TEST"))).thenReturn(Lists.newArrayList(mock1, mock2, mock3));

        List<Role> roles = roleService.getAllByName(Lists.newArrayList("ADMIN", "CLIENT", "TEST"));
        assertNotNull(roles);
        assertEquals(3, roles.size());
    }

    @DisplayName("Role service - getAll() full match")
    @Test
    public void getAll1() {

        Role mock1 = Role.builder().id(1L).name("CLIENT").build();
        Role mock2 = Role.builder().id(2L).name("ADMIN").build();
        Role mock3 = Role.builder().id(2L).name("TEST").build();

        when(roleRepository.findAll()).thenReturn(Lists.newArrayList(mock1, mock2, mock3));

        List<Role> roles = roleService.getAll();
        assertNotNull(roles);
        assertEquals(3, roles.size());
    }

    @DisplayName("Role service - createRole() role already exist")
    @Test
    public void createRole1() {

        when(roleRepository.existsByName("CLIENT")).thenReturn(true);

        RolePostRequest request = RolePostRequest.builder().name("CLIENT").build();
        BadRequestException e = assertThrows(BadRequestException.class, () -> roleService.createRole(request));
        assertEquals("Role CLIENT already exist", e.getReason());
    }

    @DisplayName("Role service - createRole() no privileges")
    @Test
    public void createRole2() {

        when(roleRepository.getByName("CLIENT")).thenReturn(null);

        RolePostRequest request = RolePostRequest.builder().name("CLIENT").build();
        BadRequestException e = assertThrows(BadRequestException.class, () -> roleService.createRole(request));
        assertEquals("Could not create new role CLIENT with no privileges", e.getReason());
    }

    @DisplayName("Role service - createRole() invalid privileges")
    @Test
    public void createRole3() {

        List<Privilege> privilegesMock = Lists.newArrayList(
                Privilege.builder().id(1L).name("GET_CLIENT").build()
        );

        when(roleRepository.getByName("CLIENT")).thenReturn(null);
        when(privilegeService.getAllByNames(Lists.newArrayList("GET_CLIENT", "TEST"))).thenReturn(privilegesMock);

        RolePostRequest request = RolePostRequest.builder().name("CLIENT").privileges(Lists.newArrayList("GET_CLIENT", "TEST")).build();
        BadRequestException e = assertThrows(BadRequestException.class, () -> roleService.createRole(request));
        assertEquals("Could not create new role CLIENT with invalid privileges [TEST]", e.getReason());
    }

    @DisplayName("Role service - createRole() ok")
    @Test
    public void createRole4() {

        List<Privilege> privilegesMock = Lists.newArrayList(
                Privilege.builder().id(1L).name("GET_CLIENT").build()
        );

        Role expectedResult = Role.builder()
                .name("CLIENT")
                .privileges(
                        Sets.newHashSet(
                                Privilege.builder().id(1L).name("GET_CLIENT").build()
                        )
                ).build();

        when(roleRepository.existsByName("CLIENT")).thenReturn(false);
        when(privilegeService.getAllByNames(Lists.newArrayList("GET_CLIENT"))).thenReturn(privilegesMock);
        when(roleRepository.save(expectedResult)).thenReturn(expectedResult);

        RolePostRequest request = RolePostRequest.builder().name("CLIENT").privileges(Lists.newArrayList("GET_CLIENT")).build();
        Role role = roleService.createRole(request);
        assertNotNull(role);
        assertEquals("CLIENT", role.getName());
        assertEquals(1, role.getPrivileges().size());
    }

    @DisplayName("Role service - updatePrivileges() role does not exist")
    @Test
    public void updatePrivileges1() {

        when(roleRepository.getByName("CLIENT")).thenReturn(null);

        NotFoundException e = assertThrows(NotFoundException.class, () -> roleService.updatePrivileges("CLIENT", Lists.newArrayList("CREATE_CLIENT")));
        assertEquals("Role CLIENT not found", e.getReason());
    }

    @DisplayName("Role service - updatePrivileges() no privileges")
    @Test
    public void updatePrivileges2() {

        Role mock1 = Role.builder()
                .id(1L)
                .name("CLIENT")
                .privileges(
                        Sets.newHashSet(
                                Privilege.builder().id(1L).name("GET_CLIENT").build()
                        )
                ).build();

        when(roleRepository.getByName("CLIENT")).thenReturn(mock1);

        BadRequestException e = assertThrows(BadRequestException.class, () -> roleService.updatePrivileges("CLIENT", Lists.newArrayList()));
        assertEquals("Could not update role CLIENT with no privileges", e.getReason());
    }

    @DisplayName("Role service - updatePrivileges() invalid privileges")
    @Test
    public void updatePrivileges3() {

        List<Privilege> privilegesMock = Lists.newArrayList(
                Privilege.builder().id(1L).name("GET_CLIENT").build()
        );
        Role mock1 = Role.builder()
                .id(1L)
                .name("CLIENT")
                .privileges(
                        Sets.newHashSet(
                                Privilege.builder().id(1L).name("GET_CLIENT").build()
                        )
                ).build();

        when(roleRepository.getByName("CLIENT")).thenReturn(mock1);
        when(privilegeService.getAllByNames(Lists.newArrayList("GET_CLIENT", "TEST"))).thenReturn(privilegesMock);

        BadRequestException e = assertThrows(BadRequestException.class, () -> roleService.updatePrivileges("CLIENT", Lists.newArrayList("GET_CLIENT", "TEST")));
        assertEquals("Could not update role CLIENT with invalid privileges [TEST]", e.getReason());
    }

    @DisplayName("Role service - updatePrivileges() ok")
    @Test
    public void updatePrivileges4() {

        List<Privilege> privilegesMock = Lists.newArrayList(
                Privilege.builder().id(1L).name("GET_CLIENT").build(),
                Privilege.builder().id(2L).name("CREATE_CLIENT").build()
        );
        Role mock1 = Role.builder()
                .id(1L)
                .name("CLIENT")
                .privileges(
                        Sets.newHashSet(
                                Privilege.builder().id(1L).name("GET_CLIENT").build()
                        )
                ).build();

        Role expectedResult = Role.builder()
                .id(1L)
                .name("CLIENT")
                .privileges(
                        Sets.newHashSet(
                                Privilege.builder().id(1L).name("GET_CLIENT").build(),
                                Privilege.builder().id(2L).name("CREATE_CLIENT").build()
                        )
                ).build();

        when(roleRepository.getByName("CLIENT")).thenReturn(mock1);
        when(privilegeService.getAllByNames(Lists.newArrayList("GET_CLIENT", "CREATE_CLIENT"))).thenReturn(privilegesMock);
        when(roleRepository.save(expectedResult)).thenReturn(expectedResult);

        Role role = roleService.updatePrivileges("CLIENT", Lists.newArrayList("GET_CLIENT", "CREATE_CLIENT"));
        assertNotNull(role);
        assertEquals("CLIENT", role.getName());
        assertEquals(2, role.getPrivileges().size());
    }

    @DisplayName("Role service - deleteRole() entity not found")
    @Test
    public void deleteRole1() {
        when(roleRepository.getByName("TEST")).thenReturn(null);
        assertThrows(NotFoundException.class, () -> roleService.deleteRole("TEST"));
    }

    @DisplayName("Role service - getByName() ok")
    @Test
    public void deleteRole2() {
        Role mock = Role.builder().id(1L).name("CLIENT").build();
        when(roleRepository.getByName("CLIENT")).thenReturn(mock);

        roleService.deleteRole("CLIENT");
        assertTrue(true); //it should not fail or throw exception
    }
}
