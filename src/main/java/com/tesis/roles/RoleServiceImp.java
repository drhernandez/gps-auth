package com.tesis.roles;

import com.google.common.collect.Sets;
import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.NotFoundException;
import com.tesis.privileges.Privilege;
import com.tesis.privileges.PrivilegeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleServiceImp implements RoleService {

    private final RoleRepository roleRepository;
    private final PrivilegeService privilegeService;

    @Autowired
    public RoleServiceImp(RoleRepository roleRepository, PrivilegeService privilegeService) {
        this.roleRepository = roleRepository;
        this.privilegeService = privilegeService;
    }

    @Override
    public Role getByName(String name) {
        Role role = roleRepository.getByName(name);
        if (role == null) {
            throw new NotFoundException(String.format("Role %s not found", name));
        }

        return role;
    }

    @Override
    public List<Role> getAllByName(List<String> names) {
        return roleRepository.getAllByNameIsIn(names);
    }

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Override
    public Role createRole(RolePostRequest newRole) {

        if (roleRepository.existsByName(newRole.getName())) {
            throw new BadRequestException(String.format("Role %s already exist", newRole.getName()));
        }

        List<Privilege> privileges = privilegeService.getAllByNames(newRole.getPrivileges());
        if (privileges.size() != newRole.getPrivileges().size()) {

            newRole.getPrivileges().removeAll(
                    privileges
                            .stream()
                            .map(Privilege::getName)
                            .collect(Collectors.toList())
            );
            throw new BadRequestException(String.format("Could not create new role %s with invalid privileges %s", newRole.getName(), Arrays.toString(newRole.getPrivileges().toArray())));
        }

        return roleRepository.save(
                Role.builder()
                        .name(newRole.getName())
                        .privileges(Sets.newHashSet(privileges))
                        .build()
        );
    }

    @Override
    public Role updatePrivileges(String roleName, List<String> privilegeNames) {

        Role role = getByName(roleName);
        List<Privilege> privileges = privilegeService.getAllByNames(privilegeNames);
        if (privileges.size() != privilegeNames.size()) {

            privilegeNames.removeAll(
                    privileges
                            .stream()
                            .map(Privilege::getName)
                            .collect(Collectors.toList())
            );
            throw new BadRequestException(String.format("Could not update role %s with invalid privileges %s", roleName, Arrays.toString(privilegeNames.toArray())));
        }

        role.setPrivileges(Sets.newHashSet(privileges));
        return roleRepository.save(role);
    }

    @Override
    public void deleteRole(String roleName) {

        Role role = getByName(roleName);
        roleRepository.delete(role);
    }
}
