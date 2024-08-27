package woozlabs.echo.domain.gmail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.gmail.entity.VerificationEmail;

public interface VerificationEmailRepository extends JpaRepository<VerificationEmail, Long> {
}
