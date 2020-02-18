package com.tesis.users;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@EnableJpaAuditing
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @DisplayName("User repository - findByEmail()")
    @Test
    public void findByEmail1() {

        User mock = User.builder()
                .name("test")
                .lastName("test")
                .email("test@test.com")
                .dni("123")
                .status(UserStatus.ACTIVE)
                .build();

        User mock2 = User.builder()
                .name("deleted")
                .lastName("test")
                .email("deleted@test.com")
                .dni("123")
                .status(UserStatus.DELETED)
                .build();

        userRepository.save(mock);
        userRepository.save(mock2);

        User found = userRepository.findByEmailAndStatusIsNot("test@test.com", UserStatus.DELETED);
        User notFound = userRepository.findByEmailAndStatusIsNot("deleted@test.com", UserStatus.DELETED);

        assertNotNull(found);
        assertEquals("test", found.getName());
        assertNull(notFound);
    }
}
