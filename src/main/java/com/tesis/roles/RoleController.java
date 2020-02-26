package com.tesis.roles;

import com.tesis.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/{name}")
    public ResponseEntity<Role> getByName(@Valid @PathVariable String name) {
        return ResponseEntity.ok(
                roleService.getByName(name)
                        .orElseThrow(() -> new NotFoundException(String.format("Role %s not found", name)))
        );
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAll() {
        return ResponseEntity.ok(roleService.getAll());
    }

    @PostMapping
    public ResponseEntity<Role> create(@RequestBody RolePostRequest rolePostRequest) {
        return ResponseEntity.ok(roleService.createRole(rolePostRequest));
    }

    @PutMapping("/{name}/privileges")
    public ResponseEntity<Role> changePrivileges(@Valid @PathVariable String name, @Valid @RequestBody List<String> privileges) {
        return ResponseEntity.ok(roleService.updatePrivileges(name, privileges));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity deleteRole(@Valid @PathVariable String name) {
        roleService.deleteRole(name);
        return ResponseEntity.ok().build();
    }
}
