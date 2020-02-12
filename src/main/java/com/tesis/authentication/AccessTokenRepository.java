package com.tesis.authentication;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
}
