package woozlabs.echo.domain.sharedEmail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmail;

public interface SharedInboxRepository extends JpaRepository<SharedEmail, Long> {
}
