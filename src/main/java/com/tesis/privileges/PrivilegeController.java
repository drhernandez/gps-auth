package com.tesis.privileges;

import com.tesis.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        return ResponseEntity.ok(
                privilegeService.getByName(name)
                        .orElseThrow(() -> new NotFoundException(String.format("Privilege %s not found", name)))
        );
    }

    @GetMapping
    public ResponseEntity<List<String>> getAll() {
        return ResponseEntity.ok(
                privilegeService
                        .getAll()
                        .stream()
                        .map(Privilege::toString)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    public ResponseEntity<Privilege> create(@RequestBody Privilege privilege) {
        return ResponseEntity.status(HttpStatus.CREATED).body(privilegeService.save(privilege));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity delete(@PathVariable String name) {
        privilegeService.delete(name);
        return ResponseEntity.ok().build();
    }
}
