package com.tesis.users;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByIdAndStatusIsNot(Long id, UserStatus status);
    User findByEmailAndStatusIsNot(String email, UserStatus status);
}
