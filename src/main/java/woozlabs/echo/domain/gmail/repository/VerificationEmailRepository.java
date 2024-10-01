package woozlabs.echo.domain.gmail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.gmail.entity.VerificationEmail;
import woozlabs.echo.domain.member.entity.Account;

import java.util.Optional;

public interface VerificationEmailRepository extends JpaRepository<VerificationEmail, Long> {
    Optional<VerificationEmail> findByUuid(String uuid);
}