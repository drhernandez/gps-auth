package com.tesis.recovery;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecoveryRepository extends JpaRepository<RecoveryToken, Long> {
}
