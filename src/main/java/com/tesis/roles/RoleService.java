package com.tesis.roles;

import javassist.NotFoundException;

import java.util.List;

public interface RoleService {

    /**
     * Get Role by id
     * @param id
     * @return
     */
    Role getById(Long id);

    /**
     * Get Role by name
     * @param name
     * @return
     */
    Role getByName(String name);

    /**
     * Get all roles
     * @return
     */
    List<Role> getAll();

    /**
     * Create new Role
     * @param newRole
     * @return
     */
    Role createRole(RolePostRequest newRole);

    /**
     * Update role privileges
     * @param roleName
     * @param privilegeNames
     * @return
     * @throws NotFoundException
     */
    Role updatePrivileges(String roleName, List<String> privilegeNames);

    /**
     * Delete Role by name
     * @param roleName
     */
    void deleteRole(String roleName);
}
