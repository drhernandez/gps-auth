package com.tesis.privileges;

import com.tesis.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DefaultPrivilegeService implements PrivilegeService {

    private final PrivilegeRepository repository;

    @Autowired
    public DefaultPrivilegeService(PrivilegeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Privilege> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Privilege> getByName(String name) {
        return Optional.ofNullable(repository.getByName(name));
    }

    @Override
    public List<Privilege> getAll() {
        return repository.findAll();
    }

    @Override
    public List<Privilege> getAllByNames(List<String> names) {

        if (names == null) {
            return new ArrayList<>();
        }
        return repository.getAllByNameIsIn(names);
    }

    @Override
    public Privilege save(Privilege privilege) {
        return repository.save(privilege);
    }

    @Override @Transactional
    public void delete(String name) {
        repository.deleteByName(name);
    }


}
