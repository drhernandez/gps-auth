package com.tesis.privileges;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivilegeServiceImp implements PrivilegeService {

    private final PrivilegeRepository repository;

    @Override
    public Privilege getById(Long id) {
        return repository.getOne(id);
    }

    @Override
    public Privilege getByName(String name) {
        return repository.getByName(name);
    }

    @Override
    public List<Privilege> getAll() {
        return repository.findAll();
    }

    @Override
    public List<Privilege> getAllByNames(List<String> names) {

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
