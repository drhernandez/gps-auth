package com.tesis.privileges;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/privileges")
public class PrivilegeController {

    private final PrivilegeService privilegeService;

    @Autowired
    public PrivilegeController(PrivilegeService privilegeService) {
        this.privilegeService = privilegeService;
    }

    @GetMapping("/{name}")
    public ResponseEntity<Privilege> getByName(@Valid @PathVariable String name) {
        return ResponseEntity.ok(privilegeService.getByName(name));
    }

    @GetMapping
    public ResponseEntity<List<String>> getAll() {
        return ResponseEntity.ok(
                privilegeService
                        .getAll()
                        .stream()
                        .map(privilege -> privilege.toString())
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    public ResponseEntity create(@RequestBody Privilege privilege) {
        return ResponseEntity.ok(privilegeService.save(privilege));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity delete(@PathVariable String name) {
        privilegeService.delete(name);
        return ResponseEntity.ok().build();
    }
}
