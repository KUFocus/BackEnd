package org.focus.logmeet.repository;

import org.focus.logmeet.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserEmail(String email);
    List<RefreshToken> findByExpirationDateBefore(LocalDateTime now);
}
