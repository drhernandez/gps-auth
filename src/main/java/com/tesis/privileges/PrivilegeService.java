package com.tesis.privileges;

import java.util.List;

public interface PrivilegeService {

    /**
     * Get Privilege by id
     * @param id
     * @return
     */
    Privilege getById(Long id);

    /**
     * Get Privilege by name
     * @param name
     * @return
     */
    Privilege getByName(String name);

    /**
     * Get all existing privileges
     * @return
     */
    List<Privilege> getAll();

    /**
     * Find multi privileges by name
     * @param names
     * @return
     */
    List<Privilege> getAllByNames(List<String> names);

    /**
     * Save new privilege
     * @param privilege
     */
    Privilege save(Privilege privilege);

    /**
     * Remove privilege from roles and DB by name
     * @param name
     */
    void delete(String name);
}
