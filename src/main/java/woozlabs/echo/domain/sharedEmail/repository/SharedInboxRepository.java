package woozlabs.echo.domain.sharedEmail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmail;

import java.util.Optional;
import java.util.UUID;

public interface SharedInboxRepository extends JpaRepository<SharedEmail, UUID> {

    Optional<SharedEmail> findByDataId(String dataId);
}
