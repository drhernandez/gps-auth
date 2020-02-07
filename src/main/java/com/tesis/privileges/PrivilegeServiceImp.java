package com.tesis.privileges;

import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class PrivilegeServiceImp implements PrivilegeService {

    private final PrivilegeRepository repository;

    @Autowired
    public PrivilegeServiceImp(PrivilegeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Privilege getById(Long id) {
        return repository.getOne(id);
    }

    @Override
    public Privilege getByName(String name) {
        Privilege privilege = repository.getByName(name);
        if (privilege == null) {
            throw new NotFoundException(String.format("Privilege %s not found", name));
        }

        return privilege;
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
